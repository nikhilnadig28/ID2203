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
package se.kth.id2203.kvstore

import java.util.UUID

import se.kth.id2203.kvstore.OpCode.{Ok, OpCode}
import se.kth.id2203.networking.NetAddress
import se.sics.kompics.KompicsEvent


trait Operation extends KompicsEvent {
  def id: UUID;
  def key: String;
}

@SerialVersionUID(0xfacc6612da2139eaL)
case class Op(key: String, id: UUID = UUID.randomUUID()) extends Operation with Serializable {
  def response(status: OpCode.OpCode): OpResponse = OpResponse(id, status);
}

object OpCode {

  sealed trait OpCode;

  case object Ok extends OpCode;

  case object NotFound extends OpCode;

  case object NotImplemented extends OpCode;
}

trait OperationResponse extends KompicsEvent {
  def id: UUID;
  def status: OpCode.OpCode;
}

trait Invoke extends Operation {
  val value: String
}

trait Response extends Operation {
  val status: OpCode
}


@SerialVersionUID(0x0227a2aea45e5e75L)
case class OpResponse(id: UUID, status: OpCode.OpCode) extends OperationResponse with Serializable;

case class Connect(id: UUID) extends KompicsEvent

case class Ack(id: UUID, clusterSize: Int) extends KompicsEvent


case class GetInvoke(id: UUID, key: String , value: String) extends Invoke

case class GetResponse(id: UUID, key: Option[String], status: OpCode = Ok) extends Response

case class PutInvoke(id: UUID, key: String, value: String) extends Invoke

case class PutResponse(id: UUID, key: Option[String], status: OpCode = Ok) extends Response

case class CASOperation(id: UUID, key: String, refValue: String) extends Operation
