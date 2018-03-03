//
//import se.sics.kompics.network._
//import se.sics.kompics.sl._
//import se.kth.id2203.components.NetworkComponents._
//import se.kth.id2203.networking._
//
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