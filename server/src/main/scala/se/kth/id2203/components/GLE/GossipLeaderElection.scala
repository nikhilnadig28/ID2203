package se.kth.id2203.components.GLE

import se.kth.id2203._
import se.kth.id2203.bootstrapping.{BLELookUp, Bootstrapping}
import se.kth.id2203.components.NetworkComponents._
import se.kth.id2203.networking.NetAddress
import se.kth.id2203.overlay.LookupTable
import se.kth.{PL_Deliver, PL_Send, PerfectLink}
import se.sics.kompics.sl._
import se.sics.kompics.timer.{ScheduleTimeout, Timer}

import scala.collection.mutable
//TODO remodelling
class GossipLeaderElection extends ComponentDefinition {
  private val ballotOne = 0x0100000000l;

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

  val ble = provides(BallotLeaderElection);
  val pl = requires(PerfectLink);
  val timer = requires[Timer];
  val boot = requires(Bootstrapping)

  val self = cfg.getValue[NetAddress]("id2203.project.address");
  var topology :List[NetAddress] = List.empty
  // CURRENT PARTITION FOR LOOKUP TABLE
  var delta :Long = 200l;
  var majority = (topology.size / 2) + 1;

  private var period :Long = 200l;
  private var ballots = mutable.Map.empty[NetAddress, Long];
  private var round = 0l;
  private var ballot = ballotFromNAddress(0, self);
  private var leader: Option[(Long, NetAddress)] = None;
  private var highestBallot: Long = ballot;
  private var topProcess = self
  private var topBallot : Long = 0l
  private var top = (self -> 0l)

  private def startTimer(delay: Long): Unit = {
    val scheduledTimeout = new ScheduleTimeout(period);
    scheduledTimeout.setTimeoutEvent(CheckTimeout(scheduledTimeout));
    trigger(scheduledTimeout -> timer);
  }

  private def lookUpTop()
  {
    topProcess = self
    topBallot = 0l
    top = (self -> 0l)
    var check = ballots + (self -> ballot)
    top = check.maxBy(_._2)
    topProcess = top._1
    topBallot = top._2

  }

  private def checkLeader() {

    lookUpTop()
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
    println("BLE -> checkLeader : "+leader)


  }

  boot uponEvent {
    case BLELookUp(lut: LookupTable) => handle {

      println("BLE Lookup")

      //Going through all the partitions and finding the one that contains the current node, which will represent
      //the table topology (the set of node for the an instance of a leader election)


      //Better way of doing (uncomment when you are sure that the project work): :)


      //topology ++= (lut.partitions.keySet).filter(x => lut.partitions(x).contains(self))

      for (partition <- lut.partitions.keySet)
      {
          if (lut.partitions(partition).contains(self))
        {
            topology ++= lut.partitions(partition)
        }
      }

      majority = (topology.size / 2) + 1
      round = 0
      ballots = mutable.Map.empty[NetAddress, Long]
      ballot = ballotFromNAddress(0, self)
      leader = None
      highestBallot = ballot
      period = delta
      startTimer(period)
    }
  }

  timer uponEvent {
    case CheckTimeout(_) => handle {
      /* INSERT YOUR CODE HERE */

      println("BLE -> Timer")
      if (ballots.size +1 >=  majority  ) //we have three nodes per partition
      {
        println("BLE -> Timer -> ballotSize > Majority")
        checkLeader()
      }
      ballots = mutable.Map.empty[NetAddress, Long]
      round = round + 1
      for (p <- topology)
      {
        if (p != self)
        {
          trigger(PL_Send(p, HeartbeatReq(round, highestBallot)) -> pl)
        }
      }
      startTimer(period)


    }
  }

  pl uponEvent {
    case PL_Deliver(src, HeartbeatReq(r, hb)) => handle {
      /* INSERT YOUR CODE HERE */

      if (hb > highestBallot)
      {
        highestBallot = hb
      }
      trigger(PL_Send(src, HeartbeatResp(r, ballot)) -> pl)
      println("BLE -> PL Deliver to : "+src)

    }
    case PL_Deliver(src, HeartbeatResp(r, b)) => handle {
      /* INSERT YOUR CODE HERE */


      if (r == round)
      {
        ballots += (src -> b)
      }
      else
      {
        period = period + delta
      }
    }
  }
}
