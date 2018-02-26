package se.kth.id2203.components.ELD

import se.kth.id2203.components.ELDComponents.EventualLeaderDetector
import se.kth.id2203.components.EPFD.EPFDComponents.EventuallyPerfectFailureDetector
import se.sics.kompics.sl._
import se.kth.id2203.overlay.VSOverlayManager

class ELD(init: Init[ELD]) extends ComponentDefinition {

  //TODO
  val eld = provides[EventualLeaderDetector]
  val epfd = requires[EventuallyPerfectFailureDetector]
  val routing = requires[Routing]


}
