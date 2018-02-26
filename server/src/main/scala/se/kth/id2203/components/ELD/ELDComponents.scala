package se.kth.id2203.components

import se.sics.kompics.sl.Port
import se.kth.id2203.networking.NetAddress
import se.sics.kompics.KompicsEvent

object ELDComponents {

  case class Trust(process: NetAddress) extends KompicsEvent

  class EventualLeaderDetector extends Port {
    indication[Trust]
  }
}
