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

import se.kth.PerfectLink
import se.kth.id2203.bootstrapping._
import se.kth.id2203.broadcast.PerfectP2PLink
import se.kth.id2203.components.Broadcast.{BEB}
import se.kth.id2203.components.GLE.GossipLeaderElection
import se.kth.id2203.components.NetworkComponents.{BallotLeaderElection, BestEffortBroadcast}
import se.kth.id2203.components.SeqCons.PaxosComponents.SequenceConsensus
import se.kth.id2203.components.SeqCons.SequencePaxos
import se.kth.id2203.kvstore.KVStore
import se.kth.id2203.networking.NetAddress
import se.kth.id2203.overlay._
import se.sics.kompics.sl._
import se.sics.kompics.Init
import se.sics.kompics.network.Network
import se.sics.kompics.timer.Timer;

class ParentComponent extends ComponentDefinition {

  //******* Ports ******
  val net = requires[Network];
  val timer = requires[Timer];
  //******* Children ******
  val overlay = create(classOf[VSOverlayManager], Init.NONE);
  val kv = create(classOf[KVStore], Init.NONE);
  val plink = create(classOf[PerfectP2PLink], Init.NONE); //Create an instance of the perfect link
  val beb = create(classOf[BEB], Init.NONE);
  val seqpax = create(classOf[SequencePaxos], Init.NONE)
  val gle = create(classOf[GossipLeaderElection], Init.NONE)
  val boot = cfg.readValue[NetAddress]("id2203.project.bootstrap-address") match {
    case Some(_) => create(classOf[BootstrapClient], Init.NONE); // start in client mode
    case None    => create(classOf[BootstrapServer], Init.NONE); // start in server mode
  }

  {
//
//    //Doing connections between ports
//
//    //Since plink exists, connect network to plink
//    connect[Network] (net -> plink);
//
//    //Connecting Plink to : overlay, bootstrap, kv, beb
//
//    connect(PerfectLink) (plink -> overlay);
//    connect(PerfectLink) (plink -> kv);
//    connect(PerfectLink) (plink -> boot);
//    connect(PerfectLink)  (plink -> beb);
//
//    //Connecting Bootstrap to : overlay, beb
//
//    connect(Bootstrapping) (boot -> beb);
//    connect(Bootstrapping)(boot -> overlay);
//
//
//    //Connecting beb to overlay, kv
//
//    connect(BestEffortBroadcast) (beb -> overlay);
//    connect(BestEffortBroadcast) (beb -> kv);
//
//    //connecting overlay to: kv
//
//    connect(Routing)(overlay -> kv);
//
//
//    //Connecting Timer to : bootstrap
//
//    connect[Timer](timer -> boot);
//
//
//    // Connecting leader to beb
//
//
////To sequencial paxos
//    connect(PerfectLink)(plink -> seqpax);
//    connect(BestEffortBroadcast)(beb -> seqpax);
//    connect(Bootstrapping)(boot -> seqpax);
//    connect(BallotLeaderElection)(gle -> seqpax);
//    //Connecting epfd to ..
//
//
//    //To leadre ellection
//
//
//    connect(Bootstrapping)(boot -> gle);
//    connect(PerfectLink)(plink -> gle);
//    connect[Timer](timer -> gle);
//
//
//    //Tokv
//
//
//    connect(Bootstrapping)(boot -> kv);
//    connect[SequenceConsensus](seqpax -> kv);
//
//    connect[BestEffortBroadcast](beb -> overlay);
//    connect[BallotLeaderElection](gle -> overlay);


    //PerfectLink
    connect[Network](net -> plink);
    //BestEffortBroadcast
    connect(PerfectLink)(plink -> beb);
    connect(Bootstrapping)(boot -> beb);
    //Bootstrap
    connect[Timer](timer -> boot);
    connect(PerfectLink)(plink -> boot);
    // Overlay
    connect(Bootstrapping)(boot -> overlay);
    connect(PerfectLink)(plink -> overlay);
    connect(BestEffortBroadcast)(beb -> overlay);
    connect(BallotLeaderElection)(gle -> overlay);
    // LeaderElection
    connect(Bootstrapping)(boot -> gle);
    connect(PerfectLink)(plink -> gle);
    connect[Timer](timer -> gle);
    // KV
  //  connect(Bootstrapping)(boot -> kv);
    connect(Routing)(overlay -> kv);
    connect(PerfectLink)(plink -> kv);
    //connect[BestEffortBroadcast](beb -> kv);
    connect(SequenceConsensus)(seqpax -> kv);
    // Pax
    connect(PerfectLink)(plink -> seqpax);
    connect(BestEffortBroadcast)(beb -> seqpax);
    connect(Bootstrapping)(boot -> seqpax);
    connect(BallotLeaderElection)(gle -> seqpax);
    connect[Timer](timer -> seqpax);

  }
}
