package se.kth.id2203.components.SeqCons

import java.util.UUID

import se.kth.id2203.kvstore.Op
import se.kth.id2203.networking.NetAddress
import se.sics.kompics.KompicsEvent
import se.sics.kompics.sl._

object PaxosComponents {

  object SequenceConsensus extends Port {
    request[SC_Propose];
    indication[SC_Decide];
  }

  case class SC_Propose(value: RSM) extends KompicsEvent;
  case class SC_Decide(value: RSM) extends KompicsEvent;

  trait RSM_Command
  {
    def id: UUID
    def src: NetAddress
    def op: Op
  }

  @SerialVersionUID(0xfacc6612da2139eaL)
  case class RSM(id: UUID = UUID.randomUUID(), src: NetAddress, op: Op) extends RSM_Command with Serializable {
  }


  case class Prepare(nL: Long, ld: Int, na: Long) extends KompicsEvent;
  case class Promise(nL: Long, na: Long, suffix: List[RSM], ld: Int) extends KompicsEvent;
  case class AcceptSync(nL: Long, suffix: List[RSM], ld: Int) extends KompicsEvent;
  case class Accept(nL: Long, c: RSM) extends KompicsEvent;
  case class Accepted(nL: Long, m: Int) extends KompicsEvent;
  case class Decide(ld: Int, nL: Long) extends KompicsEvent;

  object State extends Enumeration {
    type State = Value;
    val PREPARE, ACCEPT, UNKNOWN = Value;
  }

  object Role extends Enumeration {
    type Role = Value;
    val LEADER, FOLLOWER = Value;
  }

  def suffix(s: List[RSM], l: Int): List[RSM] = {
    s.drop(l)
  }

  def prefix(s: List[RSM], l: Int): List[RSM] = {
    s.take(l)
  }

}
