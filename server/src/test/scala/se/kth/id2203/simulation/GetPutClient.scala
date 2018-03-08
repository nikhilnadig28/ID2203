//package se.kth.id2203.simulation
//
//import java.util.UUID
//import se.kth.id2203.kvstore._
//import se.kth.id2203.networking._
//import se.kth.id2203.overlay.RouteMsg
//import se.sics.kompics.sl._
//import se.sics.kompics.Start
//import se.sics.kompics.network.Network
//import se.sics.kompics.timer.Timer
//import se.sics.kompics.sl.simulator.SimulationResult
//import collection.mutable
//
//
//class GetPutClient extends ComponentDefinition {
//
//  //******* Ports ******
//  val net = requires[Network];
//  val timer = requires[Timer];
//  //******* Fields ******
//  val self = cfg.getValue[NetAddress]("id2203.project.address");
//  val server = cfg.getValue[NetAddress]("id2203.project.bootstrap-address");
//  private val pending = mutable.Map.empty[UUID, String];
//
//  //******* Handlers ******
//  ctrl uponEvent {
//    case _: Start => handle {
//      val messages = SimulationResult[Int]("messages");
//      for (i <- 0 to 5) {
//        val keyIndex: Int = 1000 + i
//        val valIndex: Int = 1000 + i
//        val operationOne = new Op(keyIndex.toString(),UUID.randomUUID(),"PUT", Some(valIndex.toString()));
//        val operationTwo =new Op(keyIndex.toString(),UUID.randomUUID(),"GET");
//
//        val routeMsg = RouteMsg(operationOne.key, operationOne);
//        trigger(NetMessage(self, server, routeMsg) -> net);
//
//
//        val secondRouteMsg = RouteMsg(operationTwo.key, operationTwo);
//        trigger(NetMessage(self, server, secondRouteMsg) -> net);
//        pending += (operationTwo.id -> operationTwo.key);
//        logger.info("Sending {}", operationTwo);
//        SimulationResult += (operationTwo.key -> "Sent");
//      }
//    }
//  }
//
//  net uponEvent {
//    case NetMessage(header, or @ OpResponse(id, status, answer )) => handle {
//      logger.debug(s"Got OpResponse: $or");
//      pending.remove(id) match {
//        case Some(key) => SimulationResult += (key -> answer.toString());
//        case None      => logger.warn("ID $id was not pending! Ignoring response.");
//      }
//    }
//  }
//
//}
package se.kth.id2203.simulation;

import java.util.UUID;
import se.kth.id2203.kvstore._;
import se.kth.id2203.networking._;
import se.kth.id2203.overlay.RouteMsg;
import se.sics.kompics.sl._
import se.sics.kompics.Start;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.sl.simulator.SimulationResult;
import collection.mutable;

class PutnGetScenario extends ComponentDefinition {

  //******* Ports ******
  val net = requires[Network];
  val timer = requires[Timer];
  //******* Fields ******
  val self = cfg.getValue[NetAddress]("id2203.project.address");
  val server = cfg.getValue[NetAddress]("id2203.project.bootstrap-address");
  private val pending = mutable.Map.empty[UUID, String];
  //******* Handlers ******
  ctrl uponEvent {
    case _: Start => handle {
      val messages = SimulationResult[Int]("messages");
      for (i <- 0 to 10) {
        val keyIndex: Int = 100 + i
        val valIndex: Int = 1000 + i
        val op = new Op(keyIndex.toString(),UUID.randomUUID(),"PUT", Some(valIndex.toString()));
        val op2 =new Op(keyIndex.toString(),UUID.randomUUID(),"GET");

        val rm = RouteMsg(op.key, op);
        trigger(NetMessage(self, server, rm) -> net);


        val rm2 = RouteMsg(op2.key, op2);
        trigger(NetMessage(self, server, rm2) -> net);
        pending += (op2.id -> op2.key);
        logger.info("Sending {}", op2);
        SimulationResult += (op2.key -> "Sent");
      }
    }
  }

  net uponEvent {
    case NetMessage(header, or @ OpResponse(id, status, answer )) => handle {
      logger.debug(s"Got OpResponse: $or");
      pending.remove(id) match {
        case Some(key) => SimulationResult += (key -> answer.toString());
        case None      => logger.warn("ID $id was not pending! Ignoring response.");
      }
    }
  }
}