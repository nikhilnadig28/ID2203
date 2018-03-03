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
import se.kth.id2203.components.NetworkComponents.CRB_Broadcast
import se.kth.id2203.components.NetworkComponents._
import se.kth.id2203.kvstore.OpCode.{Failure, NotFound, Ok}
import se.kth.id2203.networking.{NetAddress, NetMessage}
import se.sics.kompics.sl._

import scala.collection.mutable


class KVStore extends ComponentDefinition {

  //******* Ports ******
  val pLink = requires[PerfectLink]
  //  val route = requires[Routing]
  val beb = requires[BestEffortBroadcast]
  val boot = requires(Bootstrapping)


  //******* Fields ******
  val self = cfg.getValue[NetAddress]("id2203.project.address");

  val data: mutable.HashMap[String, String] = mutable.HashMap()

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
    data(operation.key) = operation.value
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
        data(key) = newVal
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
}
