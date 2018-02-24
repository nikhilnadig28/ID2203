package se.kth.id2203.components


import se.sics.kompics.KompicsEvent
import se.sics.kompics.sl._
import se.kth.id2203.networking.NetAddress

object NetworkComponents {

  case class PL_Deliver(src: NetAddress, payload: KompicsEvent) extends KompicsEvent

  case class PL_Send(dest: NetAddress, payload: KompicsEvent) extends KompicsEvent

  class PerfectLink extends Port {
    indication[PL_Deliver]
    request[PL_Send]
  }

  case class BEB_Deliver(src: NetAddress, payload: KompicsEvent) extends KompicsEvent

  case class BEB_Broadcast(payload: KompicsEvent) extends KompicsEvent

  class BestEffortBroadcast extends Port {
    indication[BEB_Deliver]
    request[BEB_Broadcast]
  }

  case class RB_Deliver(src: NetAddress, payload: KompicsEvent) extends KompicsEvent

  case class RB_Broadcast(payload: KompicsEvent) extends KompicsEvent

  class ReliableBroadcast extends Port {
    indication[RB_Deliver]
    request[RB_Broadcast]
  }
  case class OriginatedData(src: NetAddress, payload: KompicsEvent) extends KompicsEvent;

  case class CRB_Deliver(src: NetAddress, payload: KompicsEvent) extends KompicsEvent

  case class CRB_Broadcast(payload: KompicsEvent) extends KompicsEvent

  case class DataMessage(timestamp: VectorClock, payload: KompicsEvent) extends KompicsEvent;

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

//  case class AR_Read_Request() extends KompicsEvent
//
//  case class AR_Read_Response(value: Option[Any]) extends KompicsEvent
//
//  case class AR_Write_Request(value: Any) extends KompicsEvent
//
//  case class AR_Write_Response() extends KompicsEvent
//
//  class AtomicRegister extends Port {
//    request[AR_Read_Request]
//    request[AR_Write_Request]
//    indication[AR_Read_Response]
//    indication[AR_Write_Response]
//  }
//
//  case class C_Decide(value: Any) extends KompicsEvent
//
//  case class C_Propose(value: Any) extends KompicsEvent
//
//  class Consensus extends Port {
//    request[C_Propose]
//    indication[C_Decide]
//  }
//
//  case class AC_Propose(value: Any) extends KompicsEvent
//
//  case class AC_Decide(value: Any) extends KompicsEvent
//
//  case object AC_Abort extends KompicsEvent
//
//  class AbortableConsensus extends Port {
//    request[AC_Propose]
//    indication[AC_Decide]
//    indication(AC_Abort)
//  }
}
