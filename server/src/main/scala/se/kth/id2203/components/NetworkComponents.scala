package se.kth.id2203.components

import se.kth.id2203.networking.NetAddress
import se.sics.kompics.KompicsEvent
import se.sics.kompics.sl._
import se.sics.kompics.timer.{ScheduleTimeout, Timeout}

object NetworkComponents {


  case class BEB_Deliver(src: NetAddress, payload: KompicsEvent) extends KompicsEvent

  case class BEB_Broadcast(payload: KompicsEvent) extends KompicsEvent

  object BestEffortBroadcast extends Port {
    indication[BEB_Deliver]
    request[BEB_Broadcast]
  }


  object BallotLeaderElection extends Port {
    indication[BLE_Leader]
  }

  case class BLE_Leader(leader: NetAddress, ballot: Long) extends KompicsEvent;

  case class CheckTimeout(timeout: ScheduleTimeout) extends Timeout(timeout);

  case class HeartbeatReq(round: Long, highestBallot: Long) extends KompicsEvent;

  case class HeartbeatResp(round: Long, ballot: Long) extends KompicsEvent;


//  case class VectorClock(var vc: Map[NetAddress, Int]) {
//
//    def inc(addr: NetAddress) = {
//      vc = vc + ((addr, vc.get(addr).get + 1))
//    }
//
//    def set(addr: NetAddress, value: Int) = {
//      vc = vc + ((addr, value))
//    }
//
//    def <=(that: VectorClock): Boolean = vc.foldLeft[Boolean](true)((leq, entry) => leq & (entry._2 <= that.vc.getOrElse(entry._1, entry._2)))
//
//  }
//  object VectorClock {
//
//    def empty(topology: scala.Seq[NetAddress]): VectorClock = {
//      VectorClock(topology.foldLeft[Map[NetAddress, Int]](Map[NetAddress, Int]())((mp, addr) => mp + ((addr, 0))))
//    }
//
//    def apply(that: VectorClock): VectorClock = {
//      VectorClock(that.vc)
//    }
//
//  }
}