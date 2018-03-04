package se.kth.id2203.components.Broadcast.BEB


import se.kth.id2203.bootstrapping.{BEBLookUp, Bootstrapping}
import se.kth.id2203.components.NetworkComponents._
import se.kth.id2203.networking.NetAddress
import se.kth.id2203.networking.PerfectLinkComponents.{PL_Deliver, PL_Send, PerfectLink}
import se.kth.id2203.overlay.LookupTable
import se.sics.kompics.sl._

class BEB extends ComponentDefinition {

  val pLink = requires[PerfectLink]

  val boot = requires(Bootstrapping)

  val beb = provides[BestEffortBroadcast]

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
//      for( p <- table.partitions.keySet; if table.partitions.contains(p))
//      {
//         topology ++= table.partitions(p)
//      }
    }
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
