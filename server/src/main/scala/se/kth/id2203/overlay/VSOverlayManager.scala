/*
 * The MIT License
 *
 * Copyright 2017 Lars Kroll <lkroll@kth.se>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package se.kth.id2203.overlay;

import se.kth._
import se.kth.id2203.bootstrapping._
import se.kth.id2203.components.NetworkComponents.{BallotLeaderElection, BestEffortBroadcast}
import se.kth.id2203.networking._
import se.sics.kompics.sl._
import se.sics.kompics.network.Network
import se.sics.kompics.timer.Timer

import scala.util.Random;

/**
 * The V(ery)S(imple)OverlayManager.
 * <p>
 * Keeps all nodes in a single partition in one replication group.
 * <p>
 * Note: This implementation does not fulfill the project task. You have to
 * support multiple partitions!
 * <p>
 * @author Lars Kroll <lkroll@kth.se>
 */

class VSOverlayManager extends ComponentDefinition {

  //******* Ports ******
  val route = provides(Routing);
  val boot = requires(Bootstrapping);
  val net = requires[Network];
  val timer = requires[Timer];

  //val epfd = requires[EventuallyPerfectFailureDetector]
  val beb = requires(BestEffortBroadcast)
  val gle = requires(BallotLeaderElection);
  val pLink = requires(PerfectLink)
  var suspect : Set[NetAddress] = Set()
  //******* Fields ******
  val self = cfg.getValue[NetAddress]("id2203.project.address");
  private var lut: Option[LookupTable] = None;
//  var range = (0,0)
  //******* Handlers ******
  boot uponEvent {
    case GetInitialAssignments(nodes) => handle {
      println("Generating LookupTable...");
      val lut = LookupTable.generate(nodes); //Is the nodes here the replication factor?
      println(s"Generated assignments:\n$lut")
      trigger (new InitialAssignments(lut) -> boot);
    }
    case BootKV(assignment: LookupTable) => handle {
      println("Got NodeAssignment, overlay ready.");
      lut = Some(assignment);
    }
  }

  //  epfd uponEvent {
  //    case Suspect(add) => handle {
  //      suspect += add // Add the node when you suspect it to fail
  //    }
  //    case Restore(add) => handle {
  //      suspect -= add // Remove it when it restores
  //    }
  //  }

  pLink uponEvent {

    case PL_Deliver(header, RouteMsg(key, msg)) => handle {
      val nodes = lut.get.lookup(key);
      assert(!nodes.isEmpty);
      val i = Random.nextInt(nodes.size);
      val target = nodes.drop(i).head;
      for (node <- nodes) {
        println(s"Forwarding message for key $key to $node");
        trigger(PL_Send2(header, node, msg) -> pLink);
      }
    }
    case PL_Deliver(header, msg: Connect) => handle {
      lut match {
        case Some(l) => {
          println(s"Accepting connection request from ${header}");
          val size = l.getNodes().size;
          trigger (PL_Send2(self, header, msg.ack(size)) -> pLink);
        }
        case None => println(s"Rejecting connection request from ${header}, as system is not ready, yet.");
      }
    }
  }

  route uponEvent {
    case RouteMsg(key, msg) => handle {
      val nodes = lut.get.lookup(key);
      assert(!nodes.isEmpty);
      //      val i = Random.nextInt(nodes.size);
      //      val target = nodes.drop(i).head;
      println(s"routing message for $key to all nodes")
      for (node <- nodes) {

        trigger(PL_Send2(self, node, msg) -> pLink);
      }
    }
  }
}


//class VSOverlayManager extends ComponentDefinition {
//
//  //******* Ports ******
//  val route = provides(Routing);
//  val boot = requires(Bootstrapping);
//  //val net = requires[Network];
//  val timer = requires[Timer];
//  val pl = requires(PerfectLink);
//  val beb = requires(BestEffortBroadcast);
//  val ble = requires(BallotLeaderElection);
//  //******* Fields ******
//  val self = cfg.getValue[NetAddress]("id2203.project.address");
//  private var lut: Option[LookupTable] = None;
//  //******* Handlers ******
//  boot uponEvent {
//    case GetInitialAssignments(nodes) => handle {
//      log.info("Generating LookupTable...");
//      val lut = LookupTable.generate(nodes);
//      logger.debug("Generated assignments:\n$lut");
//      trigger (new InitialAssignments(lut) -> boot);
//    }
//    case Booted(assignment: LookupTable) => handle {
//      log.info("Got NodeAssignment, overlay ready.");
//      lut = Some(assignment);
//    }
//  }
//
//  pl uponEvent {
//    case PL_Deliver(header, RouteMsg(key, msg)) => handle {
//      val nodes = lut.get.lookup(key);
//      assert(!nodes.isEmpty);
//      val i = Random.nextInt(nodes.size);
//      val target = nodes.drop(i).head;
//      log.info(s"Forwarding message for key $key to $target");
//      trigger(PL_Original_Sender(header, target, msg) -> pl); //Difference with pl send is that it is not the current component that sends
//    }
//    case PL_Deliver(header, msg: Connect) => handle {
//      lut match {
//        case Some(l) => {
//          log.debug("Accepting connection request from ${header.src}");
//          val size = l.getNodes().size;
//          trigger (PL_Original_Sender(self, header, msg.ack(size)) -> pl);
//        }
//        case None => log.info("Rejecting connection request from ${header.src}, as system is not ready, yet.");
//      }
//    }
//  }
//
//
//
//  //This will change for leader election/perfect failure detector
//
//  route uponEvent {
//    case RouteMsg(key, msg) => handle {
//      val nodes = lut.get.lookup(key);
//      assert(!nodes.isEmpty);
//      val i = Random.nextInt(nodes.size);
//      val target = nodes.drop(i).head;
//      log.info(s"Routing message for key $key to $target");
//      trigger (PL_Original_Sender(self, target, msg) -> pl);
//    }
//  }
//}
