package se.kth.id2203.bootstrapping

import se.sics.kompics.KompicsEvent
import se.sics.kompics.sl._
import se.kth.id2203.networking.NetAddress
import se.kth.id2203.overlay.LookupTable
import se.sics.kompics.network.Address


object Bootstrapping extends Port {
  indication[GetInitialAssignments]
  indication[BootKV]
  indication[BLELookUp]
  indication[BEBLookUp]
  indication[Booted]
  indication[SeqPaxLookUp]

  request[InitialAssignments]
}

case class GetInitialAssignments(nodes: Set[NetAddress]) extends KompicsEvent //Changed type to be compatible with exercice
case class Booted(assignment: NodeAssignment) extends KompicsEvent
case class InitialAssignments(assignment: NodeAssignment) extends KompicsEvent

case class BLELookUp(assignment: NodeAssignment) extends KompicsEvent;
case class SeqPaxLookUp(assignment: NodeAssignment) extends KompicsEvent;
case class BEBLookUp(assignment: NodeAssignment) extends KompicsEvent;
case class BootKV(assignment: NodeAssignment) extends KompicsEvent;


