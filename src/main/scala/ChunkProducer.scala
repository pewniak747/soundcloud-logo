package soundcloudlogo

import akka.actor._

case class Chunk(val data: Seq[Int], val offset: Int)

case class ChunkResponse(val chunk: Chunk, val maximum: Int)

case class SequenceResult(val sequence: Seq[Int], val score: Int, val offset: Int)

case object ChunkRequest

class ChunkProducer(val rawSource: Iterator[Char]) extends Actor {

  val chunkSize = 1024 * 84

  var source = rawSource.grouped(chunkSize).map { (chunk) => chunk.map(charToInt(_)) }

  var workers = Array.fill(4) { context.actorOf(Props[ChunkConsumer]) }.toSet

  var chunksCount = 0

  var results = Array[Int]()

  var maximum = 100

  def receive = {
    case ChunkRequest => {
      if (source.hasNext) {
        val data = source.next
        val chunk = Chunk(data, chunksCount * chunkSize)
        sender ! ChunkResponse(chunk, maximum)
        chunksCount += 1
        println("Sent " + chunksCount + " chunks")
      } else {
        workers = workers - sender
        if (workers.isEmpty) exit()
      }
    }

    case SequenceResult(sequence, score, offset) => {
      results = (results :+ score).sorted.reverse.take(10)
      maximum = results.last
      println("Score: " + score + "; Sequence: " + sequence.mkString + "; Offset: " + offset)
    }
  }

  private

  def charToInt(a: Char): Int = a.toInt - 48

}
