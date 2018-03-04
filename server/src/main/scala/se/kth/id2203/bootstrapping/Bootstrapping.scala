package se.kth.id2203.bootstrapping

import se.sics.kompics.KompicsEvent
import se.sics.kompics.sl._;
import se.kth.id2203.networking.NetAddress;

object Bootstrapping extends Port {
  indication[GetInitialAssignments];
  indication[Booted];
  indication[BLELookUp];
  indication[SeqPaxLookUp];
  indication[BEBLookUp];
  request[InitialAssignments];
}

case class GetInitialAssignments(nodes: Set[NetAddress]) extends KompicsEvent;
case class Booted(assignment: NodeAssignment) extends KompicsEvent;
case class InitialAssignments(assignment: NodeAssignment) extends KompicsEvent;

//TODO
//Try making all of these in to one bootloading event

case class BLELookUp(assignment: NodeAssignment) extends KompicsEvent;

case class SeqPaxLookUp(assignment: NodeAssignment) extends KompicsEvent;

case class BEBLookUp(assignment: NodeAssignment) extends KompicsEvent;
