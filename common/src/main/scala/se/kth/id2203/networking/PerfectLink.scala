package se.kth.id2203

import se.kth.id2203.networking.PerfectLinkComponents._
import se.kth.id2203.networking.{NetAddress, NetMessage}

import se.sics.kompics.network.Network
import se.sics.kompics.sl.{ComponentDefinition, handle}

class PerfectP2PLink extends ComponentDefinition {

  val pLink = provides[PerfectLink]
  val network = requires[Network]

  val self = cfg.getValue[NetAddress]("id2203.project.address");



  pLink uponEvent {
    case PL_Send(dest, payload) => handle {
      trigger(NetMessage(self, dest, payload) -> network)
    }

    case PL_Send2(src, dest, payload) => handle {
      trigger(NetMessage(src, dest, payload) -> network)
    }
  }

  network uponEvent {
    case NetMessage(src, payload) => handle {
      trigger(PL_Deliver(src.src, payload) -> pLink)
    }
  }
}
