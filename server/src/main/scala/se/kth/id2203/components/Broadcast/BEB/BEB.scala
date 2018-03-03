package se.kth.id2203.components.Broadcast.BEB


import se.kth.id2203.{PL_Deliver, PL_Send, PerfectLink}
import se.kth.id2203.components.NetworkComponents._
import se.kth.id2203.networking.NetAddress
import se.sics.kompics.sl._

class BEB(init: Init[BEB]) extends ComponentDefinition {

  val pLink = requires[PerfectLink]

  val beb = provides[BestEffortBroadcast]

  val (self, topology) = init match {
    case Init(s: NetAddress, t: Set[NetAddress]@unchecked) => (s, t)
  }

  beb uponEvent {
    case x: BEB_Broadcast => handle {
      for (q <- topology) {
        /* WRITE YOUR CODE HERE  */
        trigger(PL_Send(q, x) -> pLink)
      }
    }
  }

  pLink uponEvent {
    case PL_Deliver(src, BEB_Broadcast(payload)) => handle {
      /* WRITE YOUR CODE HERE  */
      trigger(BEB_Deliver(src, payload) -> beb)
    }
  }
}
