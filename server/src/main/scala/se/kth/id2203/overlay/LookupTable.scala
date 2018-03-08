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

import com.larskroll.common.collections._
import se.kth.id2203.bootstrapping.NodeAssignment
import se.kth.id2203.networking.NetAddress
import scala.collection.mutable.ListBuffer

///TODO BAHUT IMPORTANT Beautify karo

@SerialVersionUID(0x57bdfad1eceeeaaeL)
class LookupTable extends NodeAssignment with Serializable {


  val partitions = TreeSetMultiMap.empty[Int, NetAddress]
  val replicationFactor : Int = 3 ///Each replication will have 5 nodes

  def lookup(key: String): Iterable[NetAddress] = {
    val keyInt = key.toInt
    var finalVal = 0
    for (key <- partitions.keySet)
    {
      println(s"The current loop is ${key} and the key is ${keyInt}")
      if (keyInt > key && keyInt <= (key+1000))
      {
        println("key pool found")
        finalVal = key
      }
    }

    println(s"The key pool is ${finalVal}  ")
    println(s" The size of the returned partitions is ${partitions(finalVal).size} ")
    return partitions(finalVal)
  }


  def getNodes(): Set[NetAddress] = partitions.foldLeft(Set.empty[NetAddress]) {
    case (acc, kv) => acc ++ kv._2
  }



  override def toString(): String = {
    val sb = new StringBuilder()
    sb.append("LookupTable(\n")
    sb.append(partitions.mkString(","))
    sb.append(")")
    return sb.toString()
  }

}

object LookupTable {
  def generate(nodes: Set[NetAddress]): LookupTable = {

    val lut = new LookupTable()
    var l : Int = 0
    var tempStore : ListBuffer[NetAddress] = new ListBuffer[NetAddress]
    tempStore.clear()
    var i : Int = 0
    var range  = 1000 * nodes.size
    for (node <- nodes)
    {
      if (i < lut.replicationFactor)
      {
        tempStore.append(node)
        i += 1
      }
      if (i == lut.replicationFactor)
      {
        lut.partitions ++= ( (l)    -> tempStore)
        println("I am inside")
        tempStore.clear()
        i = 0
        l += 2000
      }
      println(s"The temp store size is ${tempStore.size}")
      println(s"The partitions are of number ${lut.partitions.size}")
      //key range for each partition
    }
    println(s"The partitions are of number ${lut.partitions.size}")
    lut
  }
}