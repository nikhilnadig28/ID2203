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
package se.kth.id2203;

import se.kth.id2203.components.Primitives.PerfectP2PLink
import se.kth.id2203.bootstrapping._
import se.kth.id2203.components.Broadcast.BEB.BEB
import se.kth.id2203.components.ELD.ELD
import se.kth.id2203.components.ELDComponents.EventualLeaderDetector
import se.kth.id2203.components.EPFD.EPFD
import se.kth.id2203.components.EPFD.EPFDComponents.EventuallyPerfectFailureDetector
import se.kth.id2203.components.NetworkComponents.BestEffortBroadcast
import se.kth.id2203.kvstore.KVStore
import se.kth.id2203.networking.NetAddress
import se.kth.id2203.overlay._
import se.sics.kompics.sl._
import se.sics.kompics.{Component, Init}
import se.sics.kompics.network.Network
import se.sics.kompics.timer.Timer;

class ParentComponent extends ComponentDefinition {
//TODO
  //******* Ports ******
  val net = requires[Network];
  val timer = requires[Timer];
  //******* Children ******
  val epfd = create(classOf[EPFD], Init.NONE)
  val eld = create(classOf[ELD], Init.NONE)
  val pLink = create(classOf[PerfectP2PLink], Init.NONE)
  val beb = create(classOf[BEB], Init.NONE)


  val overlay = create(classOf[VSOverlayManager], Init.NONE);
  val kv = create(classOf[KVStore], Init.NONE);


  val boot = cfg.readValue[String]("id2203.project.bootstrap-address") match {
    case Some(_) => create(classOf[BootstrapClient], Init.NONE); // start in client mode
    case Some("server")    => create(classOf[BootstrapServer], Init.NONE); // start in server mode
  }

  if (boot.equals("server")) {

    // KV Store
    connect[Network](net -> kv)
    connect(Routing)(overlay -> kv)
    connect[BestEffortBroadcast](beb -> kv)

    // Leader elector
    connect(Routing)(overlay -> eld)
    connect[EventuallyPerfectFailureDetector](epfd -> eld)

    //BEB
    connect[Timer](timer -> beb)
    connect[Network](net -> beb)
    connect[EventuallyPerfectFailureDetector](epfd -> beb)
    connect[EventualLeaderDetector](eld -> beb)


  }

  // Bootstrap
  connect[Network](net -> boot)
  connect[Timer](timer -> boot)

  // Overlay
  connect(Bootstrapping)(boot -> overlay)
  connect[Network](net -> overlay)
  connect[EventuallyPerfectFailureDetector](epfd -> overlay)


  // Failure detector
  connect(Routing)(overlay -> epfd)
  connect[Network](net -> epfd)
  connect[Timer](timer -> epfd)

}
