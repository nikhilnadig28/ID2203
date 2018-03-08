
package se.kth.id2203.components.Broadcast

import se.kth.id2203.bootstrapping.{BEBLookUp, Bootstrapping}
import se.kth.id2203.components.NetworkComponents.{BEB_Broadcast, BEB_Deliver, BestEffortBroadcast}
import se.kth.id2203.networking.NetAddress
import se.kth.id2203.overlay.LookupTable
import se.kth.{PL_Deliver, PL_Send, PerfectLink}
import se.sics.kompics.sl._
import se.sics.kompics.{ComponentDefinition => _, Port => _}

import scala.collection.immutable.Set

class BEB extends ComponentDefinition {

  val pLink = requires(PerfectLink)
  val boot = requires(Bootstrapping)
  val beb = provides(BestEffortBroadcast)
  val self = cfg.getValue[NetAddress]("id2203.project.address");
  var group = Set.empty[NetAddress];
  var topology =Set.empty[NetAddress];

  boot uponEvent {

    case BEBLookUp(table: LookupTable) => handle {

      val lut = table;
      for (range<-lut.partitions.keySet){
        if (lut.partitions(range).contains(self)){
          group ++= lut.partitions(range);
        }
        topology ++= lut.partitions(range);
      }
    }
  }

  beb uponEvent {
    case x: BEB_Broadcast => handle {
      for (q <- topology) {
        trigger(PL_Send(q, x) -> pLink)
      }
    }
  }

  pLink uponEvent {
    case PL_Deliver(src, BEB_Broadcast(payload)) => handle {
      trigger(BEB_Deliver(src, payload) -> beb)
    }
  }
}