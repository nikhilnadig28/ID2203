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
package se.kth.id2203.bootstrapping;

import java.util.UUID

import se.kth.id2203.networking.PerfectLinkComponents.{PL_Deliver, PL_Send, PerfectLink}
import se.kth.id2203.networking._
import se.sics.kompics.sl._
import se.sics.kompics.Start
import se.sics.kompics.timer._;

object BootstrapClient {
  sealed trait State;
  case object Waiting extends State;
  case object Started extends State;
}

class BootstrapClient extends ComponentDefinition {
  import BootstrapClient._;

  //******* Ports ******
  val bootstrap = provides(Bootstrapping);
  val timer = requires[Timer];
  val pLink = requires[PerfectLink];
  //******* Fields ******
  val self = cfg.getValue[NetAddress]("id2203.project.address");
  val server = cfg.getValue[NetAddress]("id2203.project.bootstrap-address");

  private var state: State = Waiting;

  private var timeoutId: Option[UUID] = None;

  //******* Handlers ******
  ctrl uponEvent {
    case _: Start => handle {
      log.debug("Starting bootstrap client on {}", self);
      val timeout: Long = cfg.getValue[Long]("id2203.project.keepAlivePeriod");
      val spt = new SchedulePeriodicTimeout(timeout, timeout);
      spt.setTimeoutEvent(BSTimeout(spt));
      trigger (spt -> timer);
      timeoutId = Some(spt.getTimeoutEvent().getTimeoutId());
    }
  }

  timer uponEvent {
    case BSTimeout(_) => handle {
      state match {
        case Waiting => {
          trigger(PL_Send(server, CheckIn) -> pLink);
        }
        case Started => {
          trigger(PL_Send(server, Ready) -> pLink);
          suicide();
        }
      }
    }
  }

  pLink uponEvent {
    case PL_Deliver(header, Boot(assignment)) => handle {
      state match {
        case Waiting => {
          log.info("{} Booting up.", self);
          trigger(Booted(assignment) -> bootstrap);
          trigger(BLELookUp(assignment) -> bootstrap)
          trigger(SeqPaxLookUp(assignment) -> bootstrap)
          trigger(BEBLookUp(assignment) -> bootstrap)

          timeoutId match {
            case Some(tid) => trigger(new CancelPeriodicTimeout(tid) -> timer);
            case None      => // nothing to cancel
          }
          trigger(PL_Send(server, Ready) -> pLink);
          state = Started;
        }
        case _ => // ignore
      }
    }
  }

  override def tearDown(): Unit = {
    timeoutId match {
      case Some(tid) => trigger(new CancelPeriodicTimeout(tid) -> timer);
      case None      => // nothing to cancel
    }
  }
}
