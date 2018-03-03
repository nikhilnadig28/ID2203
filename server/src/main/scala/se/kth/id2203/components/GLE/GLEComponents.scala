package se.kth.id2203.components

import se.kth.id2203.networking.NetAddress
import se.sics.kompics.KompicsEvent
import se.sics.kompics.timer.{ScheduleTimeout, Timeout}
import se.sics.kompics.sl._

package object GLEComponents {

  class BallotLeaderElection extends Port {
    indication[BLE_Leader]
  }

  case class BLE_Leader(leader: NetAddress, ballot: Long) extends KompicsEvent;

  case class CheckTimeout(timeout: ScheduleTimeout) extends Timeout(timeout);

  case class HeartbeatReq(round: Long, highestBallot: Long) extends KompicsEvent;

  case class HeartbeatResp(round: Long, ballot: Long) extends KompicsEvent;

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


}


