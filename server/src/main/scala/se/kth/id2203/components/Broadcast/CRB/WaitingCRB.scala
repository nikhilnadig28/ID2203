package se.kth.id2203.components.Broadcast.CRB



import se.kth.id2203.components.NetworkComponents._
import se.kth.id2203.networking.NetAddress
import se.sics.kompics.sl._

import scala.collection.mutable.ListBuffer

class WaitingCRB(init: Init[WaitingCRB]) extends ComponentDefinition {

  //subscriptions
  val rb = requires[ReliableBroadcast];
  val crb = provides[CausalOrderReliableBroadcast];

  //configuration
  val (self, vec) = init match {
    case Init(s: NetAddress, t: Set[NetAddress]@unchecked) => (s, VectorClock.empty(t.toSeq))
  };

  //state
  var pending: ListBuffer[(NetAddress, DataMessage)] = ListBuffer();
  var lsn = 0;


  //handlers
  crb uponEvent {
    case x@CRB_Broadcast(payload) => handle {

      /* WRITE YOUR CODE HERE  */
      var v = VectorClock.apply(vec)
      v.set(self, lsn);
      lsn += 1
      trigger(RB_Broadcast(DataMessage(v, payload)) -> rb)

    }
  }

  rb uponEvent {
    case x@RB_Deliver(src: NetAddress, msg: DataMessage) => handle {

      /* WRITE YOUR CODE HERE  */
      pending +=((src, msg))
      while(pending.exists(p => p._2.timestamp <= vec))
      {
        for(p <- pending)
        {
          if(p._2.timestamp <= vec)
          {
            val(source, message) = p
            pending -= p
            vec.inc(source)
            trigger(CRB_Deliver(source, message.payload) -> crb)
          }
        }
      }
    }
  }
}
