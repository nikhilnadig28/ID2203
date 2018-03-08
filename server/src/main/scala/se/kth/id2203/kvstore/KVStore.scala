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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package se.kth.id2203.kvstore;

import java.util.UUID

import se.kth.{PL_Deliver, PL_Send, PL_Send2, PerfectLink}
import se.kth.id2203.bootstrapping.{BootKV, Bootstrapping}
import se.kth.id2203.components.NetworkComponents.{BallotLeaderElection, BestEffortBroadcast}
import se.kth.id2203.components.SeqCons.PaxosComponents.{RSM, SC_Decide, SC_Propose, SequenceConsensus}
import se.kth.id2203.networking._
import se.kth.id2203.overlay.{LookupTable, Routing}
import se.sics.kompics.sl._

import scala.collection.mutable;
class KVStore extends ComponentDefinition {

  //******* Ports ******
  val pLink = requires(PerfectLink)
  val route = requires(Routing)
  val beb = requires(BestEffortBroadcast)
  val boot = requires(Bootstrapping)

  val seqCons = requires(SequenceConsensus)
  val ble = requires(BallotLeaderElection)
  //******* Fields ******
  val self = cfg.getValue[NetAddress]("id2203.project.address");

  var data: mutable.HashMap[Int, String] = mutable.HashMap()

  //******* Handlers ******
  //  route uponEvent {
  //   todo
  //  }

  pLink uponEvent {


    case PL_Deliver(leader, operation: Op) => handle {

      println("KV REACHED - SENDING SC_PROPOSE")
      trigger(SC_Propose(RSM(UUID.randomUUID(),leader, operation)) -> seqCons)

    }
  }

      /* Send the NetAddress and Operation, but how?
    - Have another case class to send the NetAddress and the kind of operation?
    - Write each separately? Would that even work? Doubtful.

    - Any idea at this point would be GREAT!
   */
      // Get operation and trigger the response to the network
//    }

//    case PL_Deliver(leader, operation : Op) if operation.requestType == "put"  => handle {
//      println("KV - PUT")
//
//      // PUT operation.
//      data(operation.key) = operation.value.fold("")(_.toString())
//      trigger(PL_Send(leader, operation.response(Ok, "Value Inserted")) -> pLink)
//    }
//
//    case PL_Deliver(leader, operation : Op) if operation.requestType == "cas"  => handle {
//      println("KV - CAS")
//
//      // CAS
//      val key = operation.key
//      val oldVal = operation.refVal
//      val newVal = operation.newVal
//      if(data.contains(operation.key))
//      {
//        //Compare oldvalue with val in storage
//        //if same, update
//        //else, throw error!
//        if(data(key).equals(oldVal))
//        {
//          data(key) = newVal.fold("")(_.toString())
//          //Trigger success
//          trigger(PL_Send(leader, operation.response(Ok, newVal.toString())) -> pLink)
//        }
//        else
//        {
//          //Trigger CAS Failure
//          trigger(PL_Send(leader, operation.response(Failure, "CAS old Value not equal to input")) -> pLink)
//
//        }
//      }
//      else
//      {
//        //Trigger Value not found
//        trigger(PL_Send(leader, operation.response(NotFound, "key : value not found")) -> pLink)
//      }
//    }
//  }

  seqCons uponEvent {

    case SC_Decide(RSM(id, header, op)) => handle {

      println("KV -> SC_DECIDE")
      val key:Int = (op.key).toInt;

      if(op.requestType == "GET")
      {
        println("KV -> SC_DECIDE -> GET")

        if(data.contains(key))
        {
          val value :Option[String] = data.get(key)
          //trigger(PL_Send2(self, header, op.response(OpCode.Ok, "GET : "+value.toString())) -> pLink);//For testing
          trigger(PL_Send2(self, header, op.response(OpCode.Ok, value.toString())) -> pLink);
        }
        else
        {
          val value = "GET : No value for key: "+op.key
          trigger((PL_Send2(self, header, op.response(OpCode.NotFound, value.toString())))-> pLink)
        }
      }
      else if(op.requestType == "PUT")
      {
        println("KV -> SC_DECIDE -> PUT")
        if(!data.contains(key))
        {
//          data(key) = op.value.toString
          data ++= Map(key -> op.value.fold("")(_.toString))
          trigger(PL_Send2(self, header, op.response(OpCode.Ok, "PUT : Success "+op.key+":"+op.value)) -> pLink);
        }
        else
        {
          val value = "PUT : Value already exists for key: "+op.key
          trigger((PL_Send2(self, header, op.response(OpCode.Failure, value.toString)))-> pLink)
        }

        //        data(op.key) = op.value.fold("")(_.toString())
//        trigger(PL_Send2(self, header, op.response(OpCode.Ok, "PUT : Success "+op.key+":"+op.value)) -> pLink);
      }
      else if(op.requestType == "CAS")
      {
        println("KV -> SC_DECIDE -> CAS")
        val refVal = op.value
        val newVal = op.newVal
        if(data(key) == op.value.fold("")(_.toString))
        {
          data(key) = op.newVal.fold("")(_.toString)
//          trigger(PL_Send2(self, header, op.response(OpCode.Ok,"CAS : Success "+op.key+": oldVal-"+refVal+" newVal-"+newVal)) -> pLink)//For testing
          trigger(PL_Send2(self, header, op.response(OpCode.Ok,"Success")) -> pLink)
        }
        else
        {
          val value = data.get(key)
          println("Expected value : "+value)
          trigger(PL_Send2(self,header, op.response(OpCode.Failure,"CAS : Failure "+op.key+": oldVal-"+refVal+" newVal-"+newVal+" Expected Val-"+value)) -> pLink)
        }
//
      }
      else
      {
        trigger(PL_Send2(self, header, op.response(OpCode.NotImplemented,"Error : "+op.requestType+" NOT A VALID OPERATION")) -> pLink);
      }
    }
  }
}
//
//class KVService extends ComponentDefinition {
//
//  //******* Ports ******
//  val net = requires[Network];
//  val route = requires(Routing);
//  val pl = requires(PerfectLink);
//  val SequencePax = requires(SequenceConsensus)
//  val beb = requires(BestEffortBroadcast);
//  val ble = requires(BallotLeaderElection);
//  val boot = requires(Bootstrapping);
//  //******* Fields ******
//  val self = cfg.getValue[NetAddress]("id2203.project.address");
//
//  val kvstore : mutable.HashMap[Int, String] = mutable.HashMap();
//
//  //******* Handlers ******
//  boot uponEvent {
//    case BootedKV(assignment: LookupTable) => handle {
//      log.info("Got NodeAssignment, preload KV store.");
//      val lut = assignment;
//      for (range<-lut.partitions.keySet){
//        if (lut.partitions(range).contains(self))
//        {
//          log.info("SELF {}",self)
//          log.info("TOP RANGE {}! ", range._1+1000);
//          log.info("BOT RANGE {}! ", range._2);
//
//          kvstore+=(range._1+1000->(range._1+1000).toString);
//          kvstore+=(range._2->(range._2).toString);
//        }
//      }
//    }
//  }
//
//
//  pl uponEvent {
//    case PL_Deliver(header, op: Op) => handle {
//
//      trigger(SC_Propose(RSM_Wrapper(header, op)) -> SequencePax)
//
//    }
//  }
//
//
//
//  SequencePax uponEvent {
//    case SC_Decide(RSM_Wrapper(header : NetAddress, op : Op, _)) => handle {
//
//      log.info("Got operation {}! Now implement me please :)", op);
//      val key:Int = (op.key).toInt;
//
//      if (op.request_type == "get")
//      {
//
//        log.info("Got operation in GET {}! Now implement me please :)", op);
//
//        if (kvstore.contains(key))
//        {
//          val answer = kvstore.get(key)
//          trigger(PL_Original_Sender(self, header, op.response(OpCode.Ok, answer.toString())) -> pl);
//        }
//        else {
//          trigger(PL_Original_Sender(self, header, op.response(OpCode.NotFound, "Operation Not Completed")) -> pl);
//        }
//
//      }
//      else if (op.request_type == "put")
//      {
//        log.info("Got operation in PUT {}! Now implement me please :)", op);
//
//        if (kvstore.contains(key))
//          {
//            trigger(PL_Original_Sender(self, header, op.response(OpCode.NotFound, "Operation Not Completed, value " +
//              "already exists at key")) -> pl)
//          }
//        else
//        {
//          kvstore ++= Map(key -> op.newval.fold("")(_.toString))
//          trigger(PL_Original_Sender(self, header, op.response(OpCode.Ok, "Operation Completed")) -> pl);
//        }
//
//      }
//
//      else if (op.request_type == "cas")
//      {
//
//        log.info("Got operation in CAS {}! Now implement me please :)", op);
//
//
//        if (kvstore(key) == op.oldval.fold("")(_.toString))
//        {
//          kvstore(key) = op.newval.fold("")(_.toString)
//          trigger(PL_Original_Sender(self, header, op.response(OpCode.Ok, "Operation Completed")) -> pl);
//        }
//        else
//        {
//          trigger(PL_Original_Sender(self, header, op.response(OpCode.NotFound, "Operation Not Completed Don't try to fool me mannz")) -> pl);
//        }
//
//      }
//
//
//    }
//  }
//
//
//
//}
