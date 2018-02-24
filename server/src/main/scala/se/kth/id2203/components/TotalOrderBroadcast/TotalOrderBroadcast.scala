package se.kth.id2203.components.TotalOrderBroadcast

import se.sics.kompics.network.Network
import se.sics.kompics.sl.ComponentDefinition
import se.sics.kompics.timer.Timer
import se.kth.id2203.components.EventuallyPerfectFailureDetector.EPFDComponents.EventuallyPerfectFailureDetector;
class TotalOrderBroadcast extends ComponentDefinition {

  val tob = provides[TotalOrderBroadcast]

  val perfectLink = requires[Network]
  val epfd = requires[EventuallyPerfectFailureDetector]
  val timer = requires[Timer]
  //val asc = requires[AbortableConsensus]

}
