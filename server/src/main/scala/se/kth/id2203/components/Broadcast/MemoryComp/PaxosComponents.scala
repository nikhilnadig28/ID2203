package se.kth.id2203.components.Broadcast

import se.kth.id2203.components.Broadcast.MemoryComp.{C_Decide, C_Propose}
import se.sics.kompics.KompicsEvent
import se.sics.kompics.sl.Port

package object MemoryComp {

  case class Prepare(proposalBallot: (Int, Int)) extends KompicsEvent;
  case class Promise(promiseBallot: (Int, Int), acceptedBallot: (Int, Int), acceptedValue: Option[Any]) extends KompicsEvent;
  case class Accept(acceptBallot: (Int, Int), proposedValue: Any) extends KompicsEvent;
  case class Accepted(acceptedBallot: (Int, Int)) extends KompicsEvent;
  case class Nack(ballot: (Int, Int)) extends KompicsEvent;
  case class Decided(decidedValue: Any) extends KompicsEvent;

  case class C_Decide(value: Any) extends KompicsEvent;
  case class C_Propose(value: Any) extends KompicsEvent;
}

class Consensus extends Port{
  request[C_Propose];
  indication[C_Decide];
}

