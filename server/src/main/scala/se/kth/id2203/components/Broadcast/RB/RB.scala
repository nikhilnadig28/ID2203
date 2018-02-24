package se.kth.id2203.components.Broadcast.RB

import se.kth.id2203.components.NetworkComponents._
import se.sics.kompics.KompicsEvent
import se.kth.id2203.networking.NetAddress
import se.sics.kompics.sl._

class RB extends ComponentDefinition{

  //EagerReliableBroadcast Subscriptions
  val beb = requires[BestEffortBroadcast];
  val rb = provides[ReliableBroadcast];

  //EagerReliableBroadcast Component State and Initialization
  val self = init match {
    case Init(s: NetAddress) => s
  };
  var delivered = collection.mutable.Set[KompicsEvent]();

  //EagerReliableBroadcast Event Handlers
  rb uponEvent {
    case RB_Broadcast(payload) => handle {

      /* WRITE YOUR CODE HERE  */
      trigger(BEB_Broadcast(new OriginatedData(self, payload)) -> beb)

    }
  }

  beb uponEvent {
    case BEB_Deliver(_, data@OriginatedData(origin, payload)) => handle {

      /* WRITE YOUR CODE HERE  */
      if (!delivered.contains(payload)) {
        delivered = delivered + payload
        trigger(RB_Deliver(origin, payload) -> rb)
        trigger(BEB_Broadcast(data) -> beb)
      }
    }
  }
}
