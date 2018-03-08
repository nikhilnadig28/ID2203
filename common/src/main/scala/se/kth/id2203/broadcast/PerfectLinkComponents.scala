package se.kth

import se.kth.id2203.networking.NetAddress
import se.sics.kompics.KompicsEvent
import se.sics.kompics.sl.Port


  case class PL_Deliver(src: NetAddress, payload: KompicsEvent) extends KompicsEvent //changed to type netaddress to be conpatible with bootstrap and vsOverlay
  case class PL_Send(dst: NetAddress, payload: KompicsEvent) extends KompicsEvent
  case class PL_Send2(src: NetAddress, dst:NetAddress,  payload : KompicsEvent) extends KompicsEvent

  object PerfectLink extends Port {
    indication[PL_Deliver]
    request[PL_Send]
    request[PL_Send2]
  }






