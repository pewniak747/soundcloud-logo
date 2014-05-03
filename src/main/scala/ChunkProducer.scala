package soundcloudlogo

import akka.actor._
import scala.collection.SortedSet

case class Chunk(val data: Seq[Int], val offset: Int)

case class ChunkResponse(val chunk: Chunk, val maximum: Int)

case class Sequence(val data: Seq[Int], val score: Int, val offset: Int)

case object ChunkRequest

class ChunkProducer(val rawSource: Iterator[Char]) extends Actor {

  val patternSize = 84

  val chunkSize = 1024 * patternSize

  var source = rawSource.grouped(chunkSize).map { (chunk) => chunk.map(charToInt(_)) }

  var workers = Array.fill(4) { context.actorOf(Props[ChunkConsumer]) }.toSet

  var chunksCount = 0

  implicit val resultsOrdering = Ordering[Int].on[Sequence](-_.score)
  var results = Array[Sequence]()

  var maxResults = 10

  var maximum = 100

  var lastChunk: Option[Chunk] = None

  def receive = {
    case ChunkRequest => if (source.hasNext) nextChunk else noChunks

    case sequence@Sequence(data, score, offset) => {
      results = (results :+ sequence).sorted.take(maxResults)
      maximum = results.last.score
      println("Score: " + score + "; Sequence: " + data.mkString + "; Offset: " + offset)
    }
  }

  private

  def nextChunk = {
    val data = source.next
    val chunk = lastChunk match {
      case Some(lastChunk) =>
        Chunk(lastChunk.data.reverse.take(patternSize).reverse ++ data, lastChunk.offset + chunkSize - patternSize)
      case None => Chunk(data, 0)
    }
    lastChunk = Some(chunk)
    sender ! ChunkResponse(chunk, maximum)
    chunksCount += 1
    println("Sent " + chunksCount + " chunks")
  }

  def noChunks = {
    workers = workers - sender
    if (workers.isEmpty) {
      println
      results.toList.zipWithIndex.map { case (result, index) =>
        println("Result " + (index + 1) + ": Sequence: " + result.data.mkString + " Offset: " + result.offset + " Score: " + result.score)
      }
      exit()
    }
  }

  def charToInt(a: Char): Int = a.toInt - 48

}
