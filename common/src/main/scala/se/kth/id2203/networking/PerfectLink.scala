package se.kth.id2203

import se.kth.id2203.networking.{NetAddress, NetMessage}
import se.sics.kompics.KompicsEvent
import se.sics.kompics.network.Network
import se.sics.kompics.sl.{ComponentDefinition, Init, Port, handle}

package object PerfectLink {

}

object PerfectP2PLink {

  val PLMessage = NetMessage
}


class PerfectP2PLink(init: Init[PerfectP2PLink]) extends ComponentDefinition {

  import PerfectP2PLink._

  val pLink = provides[PerfectLink]
  val network = requires[Network]

  val self: NetAddress = init match {
    case Init(self: NetAddress) => self
  }

  pLink uponEvent {
    case PL_Send(dest, payload) => handle {
      trigger(PLMessage(self, dest, payload) -> network)
    }
  }

  network uponEvent {
    case PLMessage(src, payload) => handle {
      trigger(PL_Deliver(src.src, payload) -> pLink)
    }
  }
}
case class PL_Deliver(src: NetAddress, payload: KompicsEvent) extends KompicsEvent

case class PL_Send(dest: NetAddress, payload: KompicsEvent) extends KompicsEvent

class PerfectLink extends Port {
  indication[PL_Deliver]
  request[PL_Send]
}