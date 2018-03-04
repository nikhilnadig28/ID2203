package se.kth.id2203.networking

import se.sics.kompics.KompicsEvent
import se.sics.kompics.sl.Port

object PerfectLinkComponents {

  case class PL_Deliver(src: NetAddress, payload: KompicsEvent) extends KompicsEvent

  case class PL_Send(dest: NetAddress, payload: KompicsEvent) extends KompicsEvent

  case class PL_Send2(src: NetAddress, dest: NetAddress, payload: KompicsEvent) extends KompicsEvent

  class PerfectLink extends Port {
    indication[PL_Deliver]
    request[PL_Send]
    request[PL_Send2]
  }

}
