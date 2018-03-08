package se.kth.id2203.components.SeqCons

import se.kth.{PL_Deliver, PL_Send, PerfectLink}
import se.kth.id2203.bootstrapping.{BLELookUp, Bootstrapping}
import se.kth.id2203.components.NetworkComponents.{BLE_Leader, BallotLeaderElection, BestEffortBroadcast}
import se.kth.id2203.components.SeqCons.PaxosComponents.Role._
import se.kth.id2203.components.SeqCons.PaxosComponents.State._
import se.kth.id2203.components.SeqCons.PaxosComponents._
import se.kth.id2203.networking.NetAddress
import se.kth.id2203.overlay.{LookupTable, Routing}
import se.sics.kompics.sl._
import se.sics.kompics.timer.Timer

import scala.collection.mutable

class SequencePaxos extends ComponentDefinition {

  val sc = provides(SequenceConsensus);
  val ble = requires(BallotLeaderElection);
  val pLink = requires(PerfectLink);
  val boot = requires(Bootstrapping)
  val beb = requires(BestEffortBroadcast)
  val timer = requires[Timer]
  val route = requires(Routing)

 val self = cfg.getValue[NetAddress]("id2203.project.address")
  var pi : Set[NetAddress] @unchecked = Set.empty

  var majority = (pi.size / 2) + 1;

  var state = (FOLLOWER, UNKNOWN);
  var nL = 0l;
  var nProm = 0l;
  var leader: Option[NetAddress] = None;
  var na = 0l;
  var va = List.empty[RSM];
  var ld = 0;
  // leader state
  var propCmds = List.empty[RSM];
  val las = mutable.Map.empty[NetAddress, Int];
  var lds = mutable.Map.empty[NetAddress, Int];
  var lc = 0;
  var acks = mutable.Map.empty[NetAddress, (Long, List[RSM])];


  boot uponEvent {

    case BLELookUp(table: LookupTable) => handle {

      for( p <- table.partitions.keySet)
      {
        if(table.partitions(p).contains(self)) {
          pi ++= table.partitions(p)
        }
      }
      majority = (pi.size / 2) + 1

    }
  }

  ble uponEvent {
    case BLE_Leader(l, n) => handle {
      /* INSERT YOUR CODE HERE */
      println("Entered Sequential PAXOS")
      if (n > nL) {
        println("Entered PAXOS -> n > nL")
        leader = Some(l);
        nL = n;
        if(self == l && nL > nProm) {
          println("Entered PAXOS -> n > nL -> self == l && nL > nProm")

          state = (LEADER, PREPARE);
          propCmds = List.empty[RSM];
          las.clear();
          lds = mutable.Map.empty[NetAddress, Int]
          acks = mutable.Map.empty[NetAddress, (Long, List[RSM])]
          for( p<-pi)
          {
            las += (p -> 0)
          }
          lc = 0;
          for(p<- pi)
          {
            if(p != self)
            {
              println("PAXOS PL_SEND")
              trigger(PL_Send(p, Prepare(nL, ld, na)) -> pLink);
            }
          }
          acks += (l -> (na, suffix(va,ld)));
          lds += (self -> ld);
          nProm = nL;
        }
        else {
          println("PAXOS else")
          state = (FOLLOWER,state._2)
        }
      }
    }
  }

  pLink uponEvent {
    case PL_Deliver(p, Prepare(np, ldp, n)) => handle {
      println("PAXOS -> PL - Prepare Phase -> PL_DELIVER")

      if(nProm < np){
        nProm = np;
        println("PAXOS -> PL - Prepare Phase -> PL_DELIVER -> nProm < nP")

        state = (FOLLOWER,PREPARE);
        var sfx = List.empty[RSM];
        if(na >= n){
          println("PAXOS -> PL - Prepare Phase-> PL_DELIVER -> nProm < nP -> na >= n")

          sfx = suffix(va,ldp);
        }
        else
        {
          println("PAXOS -> PL - Prepare Phase-> PL_DELIVER -> nProm < nP -> empty the sfx List")
          sfx = List.empty[RSM];
        }
        trigger(PL_Send(p, Promise(np, na, sfx, ld)) -> pLink);
      }

    }
    case PL_Deliver(a, Promise(n, na, sfxa, lda)) => handle {
      println("PAXOS -> PL - Promise Phase -> PL_DELIVER")
      if ((n == nL) && (state == (LEADER, PREPARE))) {
        println("PAXOS -> PL - Prepare Phase -> PL_DELIVER -> IF LOOP")

        var b = (na, sfxa)
        acks += (a -> b);
        lds += (a -> lda);
        val P : Set[NetAddress] = pi.filter(acks.contains(_))
        if(P.size == majority){
          val (k,sfx) = acks.values.maxBy(x => {(x._1,x._2.size)});
          va = prefix(va, ld) ++ sfx ++ propCmds;
          las(self) = va.size;
          propCmds = List.empty;
          state = (LEADER, ACCEPT);
          for(p <- pi)
          {
            if((lds.contains(p)) && (p != self)){
              val sfxp = suffix(va, lds(p));
              trigger(PL_Send(p, AcceptSync(nL,sfxp,lds(p))) -> pLink);
            }
          }
        }
      } else if ((n == nL) && (state == (LEADER, ACCEPT))) {
        println("PAXOS -> PL - Prepare Phase -> PL_DELIVER -> ELSE IF")

        lds(a) = lda;
        val sfx = suffix(va, lds(a));
        trigger(PL_Send(a, AcceptSync(nL,sfx,lds(a))) -> pLink);
        if(lc != 0){
          //if(ld > las(a))// println(s"WARNING $a $ld > ${las(a)}");
          trigger(PL_Send(a, Decide(ld,nL)) -> pLink);
        }
      }
    }
    case PL_Deliver(p, AcceptSync(nL, sfx, ldp)) => handle {
      println("PAXOS -> PL - ACCEPTSYNC Phase -> PL_DELIVER")

      if ((state == (FOLLOWER, PREPARE)) && (nProm == nL)) {
        println("PAXOS -> PL - ACCEPTSYNC Phase -> PL_DELIVER -> IF")

        na = nL;
        va = prefix(va,ldp) ++ sfx;
        trigger(PL_Send(p, Accepted(nL, va.size)) -> pLink);
        state = (FOLLOWER,ACCEPT);
      }

    }
    case PL_Deliver(p, Accept(nL, c)) => handle {
      println("PAXOS -> PL - ACCEPT Phase -> PL_DELIVER")

      if ((state == (FOLLOWER, ACCEPT)) && (nProm == nL)) {
        println("PAXOS -> PL - ACCEPT Phase -> PL_DELIVER -> IF")

        va = va ++ List(c);
        trigger(PL_Send(p, Accepted(nL,va.size)) -> pLink);
      }
    }
    case PL_Deliver(_, Decide(l, nL)) => handle {
      println("PAXOS -> PL - DECIDE Phase -> PL_DELIVER")

      if(nProm == nL){
        println("PAXOS -> PL - DECIDE Phase -> PL_DELIVER -> IF")

        while(ld < l){
          trigger(SC_Decide(va(ld)) -> sc);
          ld += 1;
        }
      }
    }
    case PL_Deliver(a, Accepted(n, m)) => handle {
      println("PAXOS -> PL - ACCEPTED Phase -> PL_DELIVER")
      if ((state == (LEADER, ACCEPT)) && (n == nL)) {
        println("PAXOS -> PL - ACCEPTED Phase -> PL_DELIVER -> IF")

        las(a) = m;
        val count = pi count (x => {las(x) >= m});
        if((lc < m)&&(count >= majority)){
          println("PAXOS -> PL - ACCEPTED Phase -> PL_DELIVER -> IF -> lc < m and count >= majority")

          lc = m;
          for( p <- pi)
          {
            if(lds.contains(p))
            {
              println("PL_SEND to all in pi")
              trigger(PL_Send(p, Decide(lc, nL)) -> pLink);
            }
          }
        }
      }
    }
  }

  sc uponEvent {
    case SC_Propose(c) => handle {
      println("BLE -> SC_PROPOSE")

      if (state == (LEADER, PREPARE)) {
        println("BLE -> SC_PROPOSE -> IF STATE IS LEADER + PREPARE")

        propCmds = propCmds ++ List(c);
      }
      else if (state == (LEADER, ACCEPT)) {
        println("BLE -> SC_PROPOSE -> IF STATE IS LEADER + ACCEPT")

        va = va ++ List(c);
        las(self) += 1;
        for(p <- pi) {
          if((lds.contains(p))&&(p != self)){
            trigger(PL_Send(p, Accept(nL,c)) -> pLink);
          }
        }
      }
    }
  }
}