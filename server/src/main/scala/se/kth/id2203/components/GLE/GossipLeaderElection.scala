package se.kth.id2203.components.GLE

import se.kth.id2203.bootstrapping.{BLELookUp, Bootstrapping}
import se.kth.id2203.components.GLEComponents._
import se.kth.id2203.networking.NetAddress
import se.kth.id2203.networking.PerfectLinkComponents.{PL_Deliver, PL_Send, PerfectLink}
import se.kth.id2203.overlay.{LookupTable, Routing}
import se.sics.kompics.network._
import se.sics.kompics.sl._
import se.sics.kompics.timer.{ScheduleTimeout, Timer}


import scala.collection.mutable;



class GossipLeaderElection extends ComponentDefinition {

  val ble = provides[BallotLeaderElection];
  val pl = requires[PerfectLink];
  val timer = requires[Timer];

  val boot = requires(Bootstrapping)

  val route = requires(Routing)

  val self = cfg.getValue[NetAddress]("id2203.project.address");

  var topology : List[NetAddress] = List.empty
  var delta : Long = 0l
  var majority = (topology.size / 2) + 1

  private var period : Long = 0l
  private var ballots = mutable.Map.empty[NetAddress, Long]

  private var round = 0l
  private var ballot = ballotFromNAddress(0, self)

  private var leader: Option[(Long, Address)] = None
  private var highestBallot: Long = ballot

  private var topProcess : NetAddress = self
  private var topBallot : Long = 0l
  private var top = (self -> 0l)
  private val ballotOne = 0x0100000000l;

  private def startTimer(delay: Long): Unit = {
    val scheduledTimeout = new ScheduleTimeout(period)
    scheduledTimeout.setTimeoutEvent(CheckTimeout(scheduledTimeout))
    trigger(scheduledTimeout -> timer)
  }

  def ballotFromNAddress(n: Int, adr: NetAddress): Long = {
    val nBytes = com.google.common.primitives.Ints.toByteArray(n);
    val addrBytes = com.google.common.primitives.Ints.toByteArray(adr.hashCode());
    val bytes = nBytes ++ addrBytes;
    val r = com.google.common.primitives.Longs.fromByteArray(bytes);
    assert(r > 0); // should not produce negative numbers!
    r
  }

  def incrementBallotBy(ballot: Long, inc: Int): Long = {
    ballot + inc.toLong * ballotOne
  }

  def incrementBallot(ballot: Long): Long = {
    ballot + ballotOne
  }
  private def findtop()
  {
    //TODO CHANGES SCHEME
    topProcess = self
    topBallot = 0l
    top = (self -> 0l)
    var ballottocheck = ballots + (self -> ballot)
    top = ballottocheck.maxBy(_._2)
    topProcess = top._1
    topBallot = top._2

  }

  private def checkLeader() {
    /* INSERT YOUR CODE HERE */

    findtop()
    if (topBallot < highestBallot)
    {
      while (ballot <= highestBallot)
      {
        ballot = incrementBallotBy(ballot,1)
      }
      leader = None
    }
    else if (Option(topBallot -> topProcess) != leader)
    {
      highestBallot = topBallot
      leader = Option(topBallot -> topProcess)
      trigger(BLE_Leader(topProcess, topBallot) -> ble);

    }


  }

  boot uponEvent {
    case BLELookUp(table: LookupTable) => handle {

      for( p <- table.partitions.keySet; if table.partitions.contains(p))
        {
          topology ++= table.partitions(p)
        }
      startTimer(period);
    }
  }

  timer uponEvent {
    case CheckTimeout(_) => handle {
      /* INSERT YOUR CODE HERE */
      if(ballots.size + 1 > majority)
        checkLeader()
      ballots = mutable.Map.empty[NetAddress, Long];
      round += 1
      for( p <- topology)
      {
        if(p != self )
        {
          trigger(PL_Send(p,HeartbeatReq(round, highestBallot)) -> pl)
        }
      }
      startTimer(period)
    }
  }

  pl uponEvent {
    case PL_Deliver(src, HeartbeatReq(r, hb)) => handle {
      /* INSERT YOUR CODE HERE */
      if( hb > highestBallot)
      {
        highestBallot = hb
      }
      trigger(PL_Send(src,HeartbeatResp(r, ballot)) -> pl)
    }
    case PL_Deliver(src, HeartbeatResp(r, b)) => handle {
      /* INSERT YOUR CODE HERE */
      if(r == round)
      {
        ballots += (src -> b)
      }
      else
        period += delta
    }
  }
}