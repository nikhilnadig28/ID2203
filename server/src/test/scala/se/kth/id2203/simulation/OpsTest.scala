/////*
//// * The MIT License
//// *
//// * Copyright 2017 Lars Kroll <lkroll@kth.se>.
//// *
//// * Permission is hereby granted, free of charge, to any person obtaining a copy
//// * of this software and associated documentation files (the "Software"), to deal
//// * in the Software without restriction, including without limitation the rights
//// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//// * copies of the Software, and to permit persons to whom the Software is
//// * furnished to do so, subject to the following conditions:
//// *
//// * The above copyright notice and this permission notice shall be included in
//// * all copies or substantial portions of the Software.
//// *
//// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//// * THE SOFTWARE.a
//// */
//
//
//package se.kth.id2203.simulation
//
//import org.scalatest._
//import se.kth.id2203.ParentComponent;
//import se.kth.id2203.networking._;
//import se.sics.kompics.network.Address
//import java.net.{ InetAddress, UnknownHostException };
//import se.sics.kompics.sl._;
//import se.sics.kompics.sl.simulator._;
//import se.sics.kompics.simulator.{ SimulationScenario => JSimulationScenario }
//import se.sics.kompics.simulator.run.LauncherComp
//import se.sics.kompics.simulator.result.SimulationResultSingleton;
//import scala.concurrent.duration._
//
//class OpsTest extends FlatSpec with Matchers {
//
//  private val nMessages = 10;
//
//  "GET & PUT Operations" should "be implemented & needs to be Linearizable" in { // well of course eventually they should be implemented^^
//    val seed = 123l;
//    JSimulationScenario.setSeed(seed);
//    val simpleBootScenario = SimpleScenario.scenarioGETandPUT(5);
//    val res = SimulationResultSingleton.getInstance();
//    SimulationResult += ("messages" -> nMessages);
//    simpleBootScenario.simulate(classOf[LauncherComp]);
//    for (i <- 0 to 5) {
//      val keyIndex: Int = 1000 + i
//      val valIndex: Int = 1000 + i
//      SimulationResult.get[String](keyIndex.toString()) should be("Some(\"Some(" + valIndex.toString() + ")\")");
//    }
//  }
//
//  "CAS Operation" should "be implemented & Linearizable" in { // well of course eventually they should be implemented^^
//    val seed = 123l;
//    JSimulationScenario.setSeed(seed);
//    val simpleBootScenario = SimpleScenario.scenarioCAS(5);
//    val res = SimulationResultSingleton.getInstance();
//    SimulationResult += ("messages" -> nMessages);
//    simpleBootScenario.simulate(classOf[LauncherComp]);
//    for (i <- 0 to 5) {
//      val keyIndex: Int = 1000 + i
//
//      SimulationResult.get[String](keyIndex.toString()) should be("Some(\"Success\")");
//      //println(SimulationResult.get[String](keyIndex.toString()))
//    }
//  }
//}
//
//object SimpleScenario {
//
//  import Distributions._
//  // needed for the distributions, but needs to be initialised after setting the seed
//  implicit val random = JSimulationScenario.getRandom();
//
//  private def intToServerAddress(i: Int): Address = {
//    try {
//      NetAddress(InetAddress.getByName("192.193.0." + i), 45678);
//    } catch {
//      case ex: UnknownHostException => throw new RuntimeException(ex);
//    }
//  }
//  private def intToClientAddress(i: Int): Address = {
//    try {
//      NetAddress(InetAddress.getByName("192.193.1." + i), 45678);
//    } catch {
//      case ex: UnknownHostException => throw new RuntimeException(ex);
//    }
//  }
//
//  private def isBootstrap(self: Int): Boolean = self == 1;
//
//  val startServerOp = Op { (self: Integer) =>
//
//    val selfAddr = intToServerAddress(self)
//    val conf = if (isBootstrap(self)) {
//      // don't put this at the bootstrap server, or it will act as a bootstrap client
//      Map("id2203.project.address" -> selfAddr)
//    } else {
//      Map(
//        "id2203.project.address" -> selfAddr,
//        "id2203.project.bootstrap-address" -> intToServerAddress(1))
//    };
//    StartNode(selfAddr, Init.none[ParentComponent], conf);
//  };
//
////  val startServerWithReplication5 = Op { (self: Integer) =>
////
////    val selfAddr = intToServerAddress(self)
////    val conf = if (isBootstrap(self)) {
////      Map("id2203.project.address" -> selfAddr, "id2203.project.bootThreshold" -> 10,
////        "id2203.project.replicationFactor" -> 5)
////
////    } else {
////      Map(
////        "id2203.project.address" -> selfAddr,
////        "id2203.project.bootstrap-address" -> intToServerAddress(1),
////        "id2203.project.bootThreshold" -> 10,
////        "id2203.project.replicationFactor" -> 5)
////
////    };
////    StartNode(selfAddr, Init.none[ParentComponent], conf);
////  };
////
////  val startServerWithReplication3 = Op { (self: Integer) =>
////
////    val selfAddr = intToServerAddress(self)
////    val conf = if (isBootstrap(self)) {
////      Map("id2203.project.address" -> selfAddr, "id2203.project.bootThreshold" -> 9,
////        "id2203.project.replicationFactor" -> 3)
////
////    } else {
////      Map(
////        "id2203.project.address" -> selfAddr,
////        "id2203.project.bootstrap-address" -> intToServerAddress(1),
////        "id2203.project.bootThreshold" -> 9,
////        "id2203.project.replicationFactor" -> 3)
////
////    };
////    StartNode(selfAddr, Init.none[ParentComponent], conf);
////  };
//
//  val startPutnGetScenario = Op { (self: Integer) =>
//    val selfAddr = intToClientAddress(self)
//    val conf = Map(
//      "id2203.project.address" -> selfAddr,
//      "id2203.project.bootstrap-address" -> intToServerAddress(1));
//    StartNode(selfAddr, Init.none[GetPutClient], conf);
//  };
//
//  val startCasScenario = Op { (self: Integer) =>
//    val selfAddr = intToClientAddress(self)
//    val conf = Map(
//      "id2203.project.address" -> selfAddr,
//      "id2203.project.bootstrap-address" -> intToServerAddress(1));
//    StartNode(selfAddr, Init.none[CASClient], conf);
//  };
//
//  def scenarioGETandPUT(servers: Int): JSimulationScenario = {
//
//    val startCluster = raise(servers, startServerOp, 1.toN).arrival(constant(1.second));
//    val startClients = raise(1, startPutnGetScenario, 1.toN).arrival(constant(1.second));
//
//    startCluster andThen
//      10.seconds afterTermination startClients andThen
//      100.seconds afterTermination Terminate
//  }
//
//  def scenarioCAS(servers: Int): JSimulationScenario = {
//
//    val startCluster = raise(servers, startServerOp, 1.toN).arrival(constant(1.second));
//    val startClients = raise(1, startCasScenario, 1.toN).arrival(constant(1.second));
//
//    startCluster andThen
//      10.seconds afterTermination startClients andThen
//      100.seconds afterTermination Terminate
//  }
//}
//
//
//
//
////package se.kth.id2203.simulation
////
////import org.scalatest._
////import se.kth.id2203.ParentComponent;
////import se.kth.id2203.networking._;
////import se.sics.kompics.network.Address
////import java.net.{ InetAddress, UnknownHostException };
////import se.sics.kompics.sl._;
////import se.sics.kompics.sl.simulator._;
////import se.sics.kompics.simulator.{ SimulationScenario => JSimulationScenario }
////import se.sics.kompics.simulator.run.LauncherComp
////import se.sics.kompics.simulator.result.SimulationResultSingleton;
////import scala.concurrent.duration._
////
////class OpsTest extends FlatSpec with Matchers {
////
////  private val nMessages = 10;
////
////
////  "Put & Get Operations" should "be implemented & Linearizable" in { // well of course eventually they should be implemented^^
////    val seed = 123l;
////    JSimulationScenario.setSeed(seed);
////    val simpleBootScenario = SimpleScenario.scenarioPutnGet(10);
////    val res = SimulationResultSingleton.getInstance();
////    SimulationResult += ("messages" -> nMessages);
////    simpleBootScenario.simulate(classOf[LauncherComp]);
////    for (i <- 0 to 10) {
////      val keyIndex: Int = 100 + i
////      val valIndex: Int = 1000 + i
////      SimulationResult.get[String](keyIndex.toString()) should be(Some("Some(" + valIndex.toString() + ")"));
////    }
////
////
////
////  "Classloader" should "be something" in {
////      val cname = classOf[SimulationResultSingleton].getCanonicalName();
////      var cl = classOf[SimulationResultSingleton].getClassLoader;
////      var i = 0;
////      while (cl != null) {
////        val res = try {
////          val c = cl.loadClass(cname);
////          true
////        } catch {
////          case t: Throwable => false
////        }
////        println(s"$i -> ${cl.getClass.getName} has class? $res");
////        cl = cl.getParent();
////        i -= 1;
////      }
////    }
////
//////  "Simple Operations" should "not be implemented" in { // well of course eventually they should be implemented^^
//////    val seed = 123l;
//////    JSimulationScenario.setSeed(seed);
//////    val simpleBootScenario = SimpleScenario.scenario(3);
//////    val res = SimulationResultSingleton.getInstance();
//////    SimulationResult += ("messages" -> nMessages);
//////    simpleBootScenario.simulate(classOf[LauncherComp]);
//////    for (i <- 0 to nMessages) {
//////      SimulationResult.get[String](s"test$i") should be (Some("NotImplemented"));
//////      // of course the correct response should be Success not NotImplemented, but like this the test passes
//////    }
//////  }
////
////}
////
////object SimpleScenario {
////
////  import Distributions._
////  // needed for the distributions, but needs to be initialised after setting the seed
////  implicit val random = JSimulationScenario.getRandom();
////
////  private def intToServerAddress(i: Int): Address = {
////    try {
////      NetAddress(InetAddress.getByName("192.193.0." + i), 45678);
////    } catch {
////      case ex: UnknownHostException => throw new RuntimeException(ex);
////    }
////  }
////  private def intToClientAddress(i: Int): Address = {
////    try {
////      NetAddress(InetAddress.getByName("192.193.1." + i), 45678);
////    } catch {
////      case ex: UnknownHostException => throw new RuntimeException(ex);
////    }
////  }
////
////  private def isBootstrap(self: Int): Boolean = self == 1;
////
////  val startServerOp = Op { (self: Integer) =>
////
////    val selfAddr = intToServerAddress(self)
////    val conf = if (isBootstrap(self)) {
////      // don't put this at the bootstrap server, or it will act as a bootstrap client
////      Map("id2203.project.address" -> selfAddr)
////    } else {
////      Map(
////        "id2203.project.address" -> selfAddr,
////        "id2203.project.bootstrap-address" -> intToServerAddress(1))
////    };
////    StartNode(selfAddr, Init.none[ParentComponent], conf);
////  };
////
//////  val startClientOp = Op { (self: Integer) =>
//////    val selfAddr = intToClientAddress(self)
//////    val conf = Map(
//////      "id2203.project.address" -> selfAddr,
//////      "id2203.project.bootstrap-address" -> intToServerAddress(1));
//////    StartNode(selfAddr, Init.none[ScenarioClient], conf);
//////  };
////
////
////val startPutnGetScenario = Op { (self: Integer) =>
////  val selfAddr = intToClientAddress(self)
////  val conf = Map(
////    "id2203.project.address" -> selfAddr,
////    "id2203.project.bootstrap-address" -> intToServerAddress(1));
////  StartNode(selfAddr, Init.none[PutnGetScenario], conf);
////};
////
////  def scenarioPutnGet(servers: Int): JSimulationScenario = {
////
////    val startCluster = raise(servers, startServerOp, 1.toN).arrival(constant(1.second));
////    val startClients = raise(1, startPutnGetScenario, 1.toN).arrival(constant(1.second));
////
////    startCluster andThen
////      10.seconds afterTermination startClients andThen
////      100.seconds afterTermination Terminate
////  }
////
////
//////  def scenario(servers: Int): JSimulationScenario = {
//////
//////    val startCluster = raise(servers, startServerOp, 1.toN).arrival(constant(1.second));
//////    val startClients = raise(1, startClientOp, 1.toN).arrival(constant(1.second));
//////
//////    startCluster andThen
//////      10.seconds afterTermination startClients andThen
//////      100.seconds afterTermination Terminate
//////  }
////
////}
package se.kth.id2203.simulation

import org.scalatest._
import se.kth.id2203.ParentComponent;
import se.kth.id2203.networking._;
import se.sics.kompics.network.Address
import java.net.{ InetAddress, UnknownHostException };
import se.sics.kompics.sl._;
import se.sics.kompics.sl.simulator._;
import se.sics.kompics.simulator.{ SimulationScenario => JSimulationScenario }
import se.sics.kompics.simulator.run.LauncherComp
import se.sics.kompics.simulator.result.SimulationResultSingleton;
import scala.concurrent.duration._

class OpsTest extends FlatSpec with Matchers {

  private val nMessages = 10;

  "Put & Get Operations" should "be implemented & Linearizable" in { // well of course eventually they should be implemented^^
    val seed = 123l;
    JSimulationScenario.setSeed(seed);
    val simpleBootScenario = SimpleScenario.scenarioPutnGet(10);
    val res = SimulationResultSingleton.getInstance();
    SimulationResult += ("messages" -> nMessages);
    simpleBootScenario.simulate(classOf[LauncherComp]);
    for (i <- 0 to 10) {
      val keyIndex: Int = 100 + i
      val valIndex: Int = 1000 + i
      SimulationResult.get[String](keyIndex.toString()) should be(Some("Some(" + valIndex.toString() + ")"));
    }
  }

  "CAS Operation" should "be implemented & Linearizable" in { // well of course eventually they should be implemented^^
    val seed = 123l;
    JSimulationScenario.setSeed(seed);
    val simpleBootScenario = SimpleScenario.scenarioCas(10);
    val res = SimulationResultSingleton.getInstance();
    SimulationResult += ("messages" -> nMessages);
    simpleBootScenario.simulate(classOf[LauncherComp]);
    for (i <- 0 to 10) {
      val keyIndex: Int = 100 + i
      //val valIndex: Int = 1000 + i
      SimulationResult.get[String](keyIndex.toString()) should be(Some("Success"));
      //println(SimulationResult.get[String](keyIndex.toString()))
    }
  }

//  "Replication Factor" should "Work with different Threshold and Partition" in { // well of course eventually they should be implemented^^
//    val seed = 123l;
//    JSimulationScenario.setSeed(seed);
//    val simpleBootScenario = SimpleScenario.scenarioReplication(10);
//    val res = SimulationResultSingleton.getInstance();
//    SimulationResult += ("messages" -> nMessages);
//    simpleBootScenario.simulate(classOf[LauncherComp]);
//    for (i <- 0 to 10) {
//      val keyIndex: Int = 100 + i
//      val valIndex: Int = 1000 + i
//      SimulationResult.get[String](keyIndex.toString()) should be(Some("Some(" + valIndex.toString() + ")"));
//    }
//
//  }
//  "Leader Election and EPFD" should "Work" in { // well of course eventually they should be implemented^^
//    val seed = 123l;
//    JSimulationScenario.setSeed(seed);
//    val simpleBootScenario = SimpleScenario.SencarioGossipLeaderElection(9);
//    val res = SimulationResultSingleton.getInstance();
//    SimulationResult += ("messages" -> nMessages);
//    simpleBootScenario.simulate(classOf[LauncherComp]);
//    println(SimulationResult.get[String]("ElectedLeader111"))
//    println(SimulationResult.get[String]("ElectedLeader222"))
//    SimulationResult.get[String]("ElectedLeader") should not be SimulationResult.get[String]("ElectedLeader2")
//
//  }
}

object SimpleScenario {

  import Distributions._
  // needed for the distributions, but needs to be initialised after setting the seed
  implicit val random = JSimulationScenario.getRandom();

  private def intToServerAddress(i: Int): Address = {
    try {
      NetAddress(InetAddress.getByName("192.193.0." + i), 45678);
    } catch {
      case ex: UnknownHostException => throw new RuntimeException(ex);
    }
  }
  private def intToClientAddress(i: Int): Address = {
    try {
      NetAddress(InetAddress.getByName("192.193.1." + i), 45678);
    } catch {
      case ex: UnknownHostException => throw new RuntimeException(ex);
    }
  }

  private def isBootstrap(self: Int): Boolean = self == 1;

  val startServerOp = Op { (self: Integer) =>

    val selfAddr = intToServerAddress(self)
    val conf = if (isBootstrap(self)) {
      // don't put this at the bootstrap server, or it will act as a bootstrap client
      Map("id2203.project.address" -> selfAddr)
    } else {
      Map(
        "id2203.project.address" -> selfAddr,
        "id2203.project.bootstrap-address" -> intToServerAddress(1))
    };
    StartNode(selfAddr, Init.none[ParentComponent], conf);
  };

  val startServerWithReplication5 = Op { (self: Integer) =>

    val selfAddr = intToServerAddress(self)
    val conf = if (isBootstrap(self)) {
      Map("id2203.project.address" -> selfAddr, "id2203.project.bootThreshold" -> 10,
        "id2203.project.replicationFactor" -> 5)

    } else {
      Map(
        "id2203.project.address" -> selfAddr,
        "id2203.project.bootstrap-address" -> intToServerAddress(1),
        "id2203.project.bootThreshold" -> 10,
        "id2203.project.replicationFactor" -> 5)

    };
    StartNode(selfAddr, Init.none[ParentComponent], conf);
  };

  val startServerWithReplication3 = Op { (self: Integer) =>

    val selfAddr = intToServerAddress(self)
    val conf = if (isBootstrap(self)) {
      Map("id2203.project.address" -> selfAddr, "id2203.project.bootThreshold" -> 9,
        "id2203.project.replicationFactor" -> 3)

    } else {
      Map(
        "id2203.project.address" -> selfAddr,
        "id2203.project.bootstrap-address" -> intToServerAddress(1),
        "id2203.project.bootThreshold" -> 9,
        "id2203.project.replicationFactor" -> 3)

    };
    StartNode(selfAddr, Init.none[ParentComponent], conf);
  };

  val startPutnGetScenario = Op { (self: Integer) =>
    val selfAddr = intToClientAddress(self)
    val conf = Map(
      "id2203.project.address" -> selfAddr,
      "id2203.project.bootstrap-address" -> intToServerAddress(1));
    StartNode(selfAddr, Init.none[PutnGetScenario], conf);
  };

  val startCasScenario = Op { (self: Integer) =>
    val selfAddr = intToClientAddress(self)
    val conf = Map(
      "id2203.project.address" -> selfAddr,
      "id2203.project.bootstrap-address" -> intToServerAddress(1));
    StartNode(selfAddr, Init.none[CasScenario], conf);
  };

//  val startWhoIsLeader = Op { (self: Integer) =>
//    val selfAddr = intToClientAddress(self)
//    val conf = Map(
//      "id2203.project.address" -> selfAddr,
//      "id2203.project.bootstrap-address" -> intToServerAddress(1) );
//    //StartNode(selfAddr, Init.none[CasScenario], conf);
//    StartNode(selfAddr, Init.none[LeaderElectionScenario], conf);
//  };
//
//  val startWhoIsLeader2 = Op { (self: Integer) =>
//    val selfAddr = intToClientAddress(self)
//    val conf = Map(
//      "id2203.project.address" -> selfAddr,
//      "id2203.project.bootstrap-address" -> intToServerAddress(1));
//    //StartNode(selfAddr, Init.none[CasScenario], conf);
//    StartNode(selfAddr, Init.none[LeaderElectionScenario2], conf);
//  };

  val killLeader = Op { (self: Integer) =>
    KillNode(SimulationResult.get[NetAddress]("ElectedLeader").get)
  };


  def scenarioPutnGet(servers: Int): JSimulationScenario = {

    val startCluster = raise(servers, startServerOp, 1.toN).arrival(constant(1.second));
    val startClients = raise(1, startPutnGetScenario, 1.toN).arrival(constant(1.second));

    startCluster andThen
      10.seconds afterTermination startClients andThen
      100.seconds afterTermination Terminate
  }

  def scenarioCas(servers: Int): JSimulationScenario = {

    val startCluster = raise(servers, startServerOp, 1.toN).arrival(constant(1.second));
    val startClients = raise(1, startCasScenario, 1.toN).arrival(constant(1.second));

    startCluster andThen
      10.seconds afterTermination startClients andThen
      100.seconds afterTermination Terminate
  }

  def scenarioReplication(servers: Int): JSimulationScenario = {
    if(servers == 10) {

      val startCluster = raise(servers, startServerWithReplication5, 1.toN).arrival(constant(1.second));
      val startClients = raise(1, startPutnGetScenario, 1.toN).arrival(constant(1.second));

      startCluster andThen
        10.seconds afterTermination startClients andThen
        100.seconds afterTermination Terminate
    }
    else  {

      val startCluster = raise(servers, startServerWithReplication3, 1.toN).arrival(constant(1.second));
      val startClients = raise(1, startPutnGetScenario, 1.toN).arrival(constant(1.second));

      startCluster andThen
        10.seconds afterTermination startClients andThen
        100.seconds afterTermination Terminate
    }
  }

  def SencarioMutlipleClients3(servers: Int) : JSimulationScenario = {
    val startCluster = raise(servers, startServerOp, 1.toN).arrival(constant(1.second));
    val startClients = raise(3, startCasScenario, 1.toN).arrival(constant(1.second));

    startCluster andThen
      10.seconds afterTermination startClients andThen
      100.seconds afterTermination Terminate
  }

//  def SencarioGossipLeaderElection (servers: Int) : JSimulationScenario = {
//    val startCluster = raise(servers, startServerOp, 1.toN).arrival(constant(1.second));
//    val startClient = raise(1, startWhoIsLeader, 1.toN).arrival(constant(2.second));
//    val killNode = raise(1, killLeader, 1.toN).arrival(constant(2.second));
//    val startClientElection = raise(1, startWhoIsLeader2, 2.toN).arrival(constant(2.second));
//
//
//    startCluster andThen
//      10.seconds afterTermination startClient andThen
//      10.seconds afterTermination killNode andThen
//      10.seconds afterTermination startClientElection andThen
//      10.seconds afterTermination Terminate
//  }

}