package se.kth.id2203.components


import se.sics.kompics.KompicsEvent
import se.sics.kompics.sl._
import se.kth.id2203.networking.{NetAddress, NetMessage}

object NetworkComponents {


  case class BEB_Deliver(src: NetAddress, payload: KompicsEvent) extends KompicsEvent

  case class BEB_Broadcast(payload: KompicsEvent) extends KompicsEvent

  class BestEffortBroadcast extends Port {
    indication[BEB_Deliver]
    request[BEB_Broadcast]
  }

  case class RB_Deliver(src: NetAddress, payload: KompicsEvent) extends KompicsEvent

  case class RB_Broadcast(payload: KompicsEvent) extends KompicsEvent

  case class DataMessage(timestamp: VectorClock, payload: KompicsEvent) extends KompicsEvent


  class ReliableBroadcast extends Port {
    indication[RB_Deliver]
    request[RB_Broadcast]
  }
  case class OriginatedData(src: NetAddress, payload: KompicsEvent) extends KompicsEvent;

  case class CRB_Deliver(src: NetAddress, payload: KompicsEvent) extends KompicsEvent

  case class CRB_Broadcast(payload: KompicsEvent) extends KompicsEvent

  class CausalOrderReliableBroadcast extends Port {
    indication[CRB_Deliver]
    request[CRB_Broadcast]
  }

  case class VectorClock(var vc: Map[NetAddress, Int]) {

    def inc(addr: NetAddress) = {
      vc = vc + ((addr, vc.get(addr).get + 1))
    }

    def set(addr: NetAddress, value: Int) = {
      vc = vc + ((addr, value))
    }

    def <=(that: VectorClock): Boolean = vc.foldLeft[Boolean](true)((leq, entry) => leq & (entry._2 <= that.vc.getOrElse(entry._1, entry._2)))

  }
  object VectorClock {

    def empty(topology: scala.Seq[NetAddress]): VectorClock = {
      VectorClock(topology.foldLeft[Map[NetAddress, Int]](Map[NetAddress, Int]())((mp, addr) => mp + ((addr, 0))))
    }

    def apply(that: VectorClock): VectorClock = {
      VectorClock(that.vc)
    }

  }
}