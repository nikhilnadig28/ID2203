package se.kth.id2203.broadcast



import se.kth._
import se.kth.id2203.networking._
import se.sics.kompics.network._
import se.sics.kompics.sl._

class PerfectP2PLink extends ComponentDefinition {

  val pl = provides(PerfectLink);
  val network = requires[Network];
  val self = cfg.getValue[NetAddress]("id2203.project.address");


  pl uponEvent {
    case PL_Send(dest:NetAddress, payload) => handle {
      trigger(NetMessage(self, dest, payload) -> network);
    }

    case PL_Send2(src: NetAddress, dst: NetAddress, payload) => handle {
      trigger(NetMessage(src, dst, payload) -> network);
    }
  }

  network uponEvent {
    case NetMessage(header, payload) => handle {
      trigger(PL_Deliver(header.src, payload) -> pl);
    }
  }
}