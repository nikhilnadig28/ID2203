package se.kth.id2203.components


//import se.kth.id2203.components.NetworkComponents.{PL_Deliver, PL_Send, PerfectLink}
import se.sics.kompics.KompicsEvent
import se.sics.kompics.sl._
import se.kth.id2203.networking.{NetAddress, NetMessage}
import se.sics.kompics.network.Network

object NetworkComponents {

//  case class PL_Deliver(src: NetAddress, payload: KompicsEvent) extends KompicsEvent
//
//  case class PL_Send(dest: NetAddress, payload: KompicsEvent) extends KompicsEvent
//
//  class PerfectLink extends Port {
//    indication[PL_Deliver]
//    request[PL_Send]
//  }

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

//object Primitives {
//
//  object PerfectP2PLink {
//
//    val PLMessage = NetMessage
//  }
//
//
//  class PerfectP2PLink(init: Init[PerfectP2PLink]) extends ComponentDefinition {
//
//    import PerfectP2PLink._
//
//    val pLink = provides[PerfectLink]
//    val network = requires[Network]
//
//    val self: NetAddress = init match {
//      case Init(self: NetAddress) => self
//    }
//
//    pLink uponEvent {
//      case PL_Send(dest, payload) => handle {
//        trigger(PLMessage(self, dest, payload) -> network)
//      }
//    }
//
//    network uponEvent {
//      case PLMessage(src, payload) => handle {
//        trigger(PL_Deliver(src.src, payload) -> pLink)
//      }
//    }
//  }
//}
