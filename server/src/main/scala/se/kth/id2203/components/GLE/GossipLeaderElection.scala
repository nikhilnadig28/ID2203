package se.kth.id2203.components.GLE

import se.kth.id2203.components.GLEComponents._
import se.kth.id2203.{PL_Deliver, PL_Send, PerfectLink}
import se.kth.id2203.networking.NetAddress
import se.sics.kompics.network._
import se.sics.kompics.sl._
import se.sics.kompics.timer.{ScheduleTimeout, Timeout, Timer}
import se.sics.kompics.{KompicsEvent, Start}

import scala.collection.mutable;



class GossipLeaderElection(init: Init[GossipLeaderElection]) extends ComponentDefinition {

  val ble = provides[BallotLeaderElection];
  val pl = requires[PerfectLink];
  val timer = requires[Timer];

  val self = init match {
    case Init(s: NetAddress) => s
  }
  val topology = cfg.getValue[List[NetAddress]]("ble.simulation.topology");
  val delta = cfg.getValue[Long]("ble.simulation.delay");
  val majority = (topology.size / 2) + 1;

  private var period = cfg.getValue[Long]("ble.simulation.delay");
  private var ballots = mutable.Map.empty[NetAddress, Long];

  private var round = 0l;
  private var ballot = ballotFromNAddress(0, self);

  private var leader: Option[(Long, Address)] = None;
  private var highestBallot: Long = ballot;

  private var topProcess : NetAddress = self
  private var topBallot : Long = 0l
  private var top = (self -> 0l)

  private def startTimer(delay: Long): Unit = {
    val scheduledTimeout = new ScheduleTimeout(period);
    scheduledTimeout.setTimeoutEvent(CheckTimeout(scheduledTimeout));
    trigger(scheduledTimeout -> timer);
  }

  private def findtop()
  {
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

  ctrl uponEvent {
    case _: Start => handle {
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