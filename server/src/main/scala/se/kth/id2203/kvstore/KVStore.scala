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
package se.kth.id2203.kvstore;


import se.kth.id2203.bootstrapping.Bootstrapping
import se.kth.id2203.components.GLEComponents.BallotLeaderElection
import se.kth.id2203.components.NetworkComponents._
import se.kth.id2203.components.SeqCons.PaxosComponents.{RSM, SC_Decide, SequenceConsensus}
import se.kth.id2203.kvstore.OpCode.{Failure, NotFound, Ok}
import se.kth.id2203.networking.PerfectLinkComponents.{PL_Deliver, PL_Send, PerfectLink}
import se.kth.id2203.networking._
import se.kth.id2203.overlay.Routing
import se.sics.kompics.sl._

import scala.collection.mutable


class KVStore extends ComponentDefinition {

  //******* Ports ******
  val pLink = requires[PerfectLink]
    val route = requires(Routing)
  val beb = requires[BestEffortBroadcast]
  val boot = requires(Bootstrapping)

  val seqCons = requires[SequenceConsensus]
  val ble = requires[BallotLeaderElection]
  //******* Fields ******
  val self = cfg.getValue[NetAddress]("id2203.project.address");

  var data: mutable.HashMap[String, String] = mutable.HashMap()

  //******* Handlers ******
  //  route uponEvent {
  //   todo
  //  }

  pLink uponEvent {

    case PL_Deliver(leader, operation : Op) if operation.requestType == "get"  => handle {
    val value = data.get(operation.key)
      trigger(PL_Send(leader, operation.response(Ok, value.toString())) -> pLink)


    /* Send the NetAddress and Operation, but how?
  - Have another case class to send the NetAddress and the kind of operation?
  - Write each separately? Would that even work? Doubtful.

  - Any idea at this point would be GREAT!
 */
  // Get operation and trigger the response to the network
  }

    case PL_Deliver(leader, operation : Op) if operation.requestType == "put"  => handle {

    // PUT operation.
    data(operation.key) = operation.value.fold("")(_.toString())
      trigger(PL_Send(leader, operation.response(Ok, "Value Inserted")) -> pLink)
  }

    case PL_Deliver(leader, operation : Op) if operation.requestType == "cas"  => handle {

    // CAS
    val key = operation.key
    val oldVal = operation.refVal
    val newVal = operation.newVal
    if(data.contains(operation.key))
    {
    //Compare oldvalue with val in storage
      //if same, update
      //else, throw error!
      if(data(key).equals(oldVal))
      {
        data(key) = newVal.fold("")(_.toString())
        //Trigger success
        trigger(PL_Send(leader, operation.response(Ok, newVal.toString())) -> pLink)
      }
      else
        {
          //Trigger CAS Failure
          trigger(PL_Send(leader, operation.response(Failure, "CAS old Value not equal to input")) -> pLink)

        }
  }
      else
      {
        //Trigger Value not found
        trigger(PL_Send(leader, operation.response(NotFound, "key : value not found")) -> pLink)
      }
  }
  }

  seqCons uponEvent {

    case SC_Decide(RSM(id, src, op)) => handle {

      if(op.requestType == "GET")
        {
          if(data.exists(_._1 == op.key.toInt))
            {
              val value = data.get(op.key)
              trigger(PL_Send(src, op.response(OpCode.Ok, "GET : "+value.toString)) -> pLink);
            }
          else
            {
              val value = "GET : No value for key: "+op.key
              trigger((PL_Send(src, op.response(OpCode.NotFound, value.toString)))-> pLink)
            }
        }
      else if(op.requestType == "PUT")
        {
          data(op.key) = op.value.fold("")(_.toString())
          trigger(PL_Send(src, op.response(OpCode.Ok, "PUT : Success "+op.key+":"+op.value)) -> pLink);
        }
      else if(op.requestType == "CAS")
        {
          val refVal = op.refVal
          val newVal = op.newVal
          if(data(op.key) == refVal)
          {
            data(op.key) = newVal.fold("")(_.toString())
            trigger(PL_Send(src, op.response(OpCode.Ok,"CAS : Success "+op.key+": oldVal-"+op.refVal+" newVal-"+op.newVal)) -> pLink)
          }
          else
          {
            trigger(PL_Send(src, op.response(OpCode.Failure,"CAS : Failure "+op.key+": oldVal-"+op.refVal+" newVal-"+op.newVal)) -> pLink)
          }
        }
      else
      {
        trigger(PL_Send(src, op.response(OpCode.NotImplemented,"Error : "+op.requestType+" NOT A VALID OPERATION")) -> pLink);
      }
    }
  }
}
