package se.kth.id2203.components.SeqCons

import se.kth.id2203.{PL_Deliver, PL_Send, PerfectLink}
import se.kth.id2203.components.GLEComponents.{BLE_Leader, BallotLeaderElection}
import se.kth.id2203.networking.NetAddress
import se.sics.kompics.sl._
import se.kth.id2203.components.SeqCons.PaxosComponents.Role._
import se.kth.id2203.components.SeqCons.PaxosComponents.State._
import se.kth.id2203.components.SeqCons.PaxosComponents._
import scala.collection.mutable

class SequencePaxos(init: Init[SequencePaxos]) extends ComponentDefinition {



  val sc = provides[SequenceConsensus];
  val ble = requires[BallotLeaderElection];
  val pl = requires[PerfectLink];

  val (self, pi, others) = init match {
    case Init(addr: NetAddress, pi: Set[NetAddress] @unchecked) => (addr, pi, pi - addr)
  }
  val majority = (pi.size / 2) + 1;

  var state = (FOLLOWER, UNKNOWN);
  var nL = 0l;
  var nProm = 0l;
  var leader: Option[NetAddress] = None;
  var na = 0l;
  var va = List.empty[RSM_Command];
  var ld = 0;
  // leader state
  var propCmds = List.empty[RSM_Command];
  val las = mutable.Map.empty[NetAddress, Int];
  val lds = mutable.Map.empty[NetAddress, Int];
  var lc = 0;
  val acks = mutable.Map.empty[NetAddress, (Long, List[RSM_Command])];


  ble uponEvent {
    case BLE_Leader(l, n) => handle {
      /* INSERT YOUR CODE HERE */
      if (n > nL) {
        leader = Some(l);
        nL = n;
        if(self == l && nL > nProm) {
          state = (LEADER, PREPARE);
          propCmds = List.empty[RSM_Command];
          las.clear();
          for( p<-pi)
          {
            las += (p -> 0)
          }
          lds.clear();
          acks.clear();
          lc = 0;
          for(p<- pi)
          {
            if(p != self)
            {
              trigger(PL_Send(p, Prepare(nL, ld, na)) -> pl);
            }
          }
          acks += (l -> (na, suffix(va,ld)));
          lds += (self -> ld);
          nProm = nL;
        }
        else {
          state = (FOLLOWER,state._2)
        }
      }
    }
  }

  pl uponEvent {
    case PL_Deliver(p, Prepare(np, ldp, n)) => handle {
      /* INSERT YOUR CODE HERE */
      if(nProm < np){
        nProm = np;
        state = (FOLLOWER,PREPARE);
        var sfx = List.empty[RSM_Command];
        if(na >= n){
          sfx = suffix(va,ldp);
        }
        else
        {
          sfx = List.empty[RSM_Command];
        }
        trigger(PL_Send(p, Promise(np, na, sfx, ld)) -> pl);
      }

    }
    case PL_Deliver(a, Promise(n, na, sfxa, lda)) => handle {
      if ((n == nL) && (state == (LEADER, PREPARE))) {
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
              trigger(PL_Send(p, AcceptSync(nL,sfxp,lds(p))) -> pl);
            }
          }
        }
      } else if ((n == nL) && (state == (LEADER, ACCEPT))) {
        lds(a) = lda;
        val sfx = suffix(va, lds(a));
        trigger(PL_Send(a, AcceptSync(nL,sfx,lds(a))) -> pl);
        if(lc != 0){
          //if(ld > las(a))// println(s"WARNING $a $ld > ${las(a)}");
          trigger(PL_Send(a, Decide(ld,nL)) -> pl);
        }
      }
    }
    case PL_Deliver(p, AcceptSync(nL, sfx, ldp)) => handle {
      if ((state == (FOLLOWER, PREPARE)) && (nProm == nL)) {
        na = nL;
        va = prefix(va,ldp) ++ sfx;
        trigger(PL_Send(p, Accepted(nL, va.size)) -> pl);
        state = (FOLLOWER,ACCEPT);
        // println(s"$self at $state ${va.size}");
      }

    }
    case PL_Deliver(p, Accept(nL, c)) => handle {
      if ((state == (FOLLOWER, ACCEPT)) && (nProm == nL)) {
        va = va ++ List(c);
        trigger(PL_Send(p, Accepted(nL,va.size)) -> pl);
      }
    }
    case PL_Deliver(_, Decide(l, nL)) => handle {
      /* INSERT YOUR CODE HERE */
      if(nProm == nL){
        //val vasize = va.size;
        //  println(s"$ld <  $lcL ($vasize)");
        while(ld < l){
          // val decidemsg = SC_Decide(va(ld));
          // println(s"$self $decidemsg");
          trigger(SC_Decide(va(ld)) -> sc);
          ld += 1;
        }
      }
    }
    case PL_Deliver(a, Accepted(n, m)) => handle {
      if ((state == (LEADER, ACCEPT)) && (n == nL)) {
        las(a) = m;
        val count = pi count (x => {las(x) >= m});
        if((lc < m)&&(count >= majority)){
          lc = m;
          for( p <- pi)
          {
            if(lds.contains(p))
            {
              trigger(PL_Send(p, Decide(lc, nL)) -> pl);
            }
          }
        }
      }
    }
  }

  sc uponEvent {
    case SC_Propose(c) => handle {
      if (state == (LEADER, PREPARE)) {
        propCmds = propCmds ++ List(c);
      }
      else if (state == (LEADER, ACCEPT)) {
        va = va ++ List(c);
        las(self) += 1;
        for(p <- pi) {
          if((lds.contains(p))&&(p != self)){
            trigger(PL_Send(p, Accept(nL,c)) -> pl);
          }
        }
      }
    }

  }
}