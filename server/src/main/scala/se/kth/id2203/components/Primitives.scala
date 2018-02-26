
import se.sics.kompics.network._
import se.sics.kompics.sl._
import se.kth.id2203.components.NetworkComponents._
import se.kth.id2203.networking._
import se.sics.kompics.KompicsEvent

object Primitives {

  object PerfectP2PLink {

    val PerfectLinkMessage = NetMessage
  }

  case class DataMessage(timestamp: VectorClock, payload: KompicsEvent) extends KompicsEvent

  class PerfectP2PLink(init: Init[PerfectP2PLink]) extends ComponentDefinition {

    import PerfectP2PLink._

    val pLink = provides[PerfectLink]
    val network = requires[Network]

    val self: NetAddress = init match {
      case Init(self: NetAddress) => self
    }

    pLink uponEvent {
      case PL_Send(dest, payload) => handle {
        trigger(PerfectLinkMessage(self, dest, payload) -> network)
      }
    }

    network uponEvent {
      case PerfectLinkMessage(src, payload) => handle {
        trigger(PL_Deliver(src.src, payload) -> pLink)
      }
    }
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