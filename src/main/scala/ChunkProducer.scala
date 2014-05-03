package soundcloudlogo

import akka.actor._

case class Chunk(val data: Seq[Int], val offset: Int)

case class ChunkResponse(val chunk: Chunk, val maximum: Int)

case class SequenceResult(val sequence: Seq[Int], val score: Int, val offset: Int)

case object ChunkRequest

class ChunkProducer(val rawSource: Iterator[Char]) extends Actor {

  val white = (0, 2)
  val gray  = (3, 9)
  val black = (6, 9)
  val pattern = Array(white, white, white, white, white, gray,  gray,  black, black, black, black, white, white, white,
                      white, white, white, gray,  gray,  gray,  gray,  black, black, black, black, white, white, white,
                      white, white, gray,  gray,  gray,  gray,  gray,  black, black, black, black, black, white, white,
                      gray,  gray,  gray,  gray,  gray,  gray,  gray,  black, black, black, black, black, black, black,
                      gray,  gray,  gray,  gray,  gray,  gray,  gray,  black, black, black, black, black, black, black,
                      white, gray,  gray,  gray,  gray,  gray,  gray,  black, black, black, black, black, black, white)

  val weights = pattern.map((r) => if(r == white) 5 else 1)

  val chunkSize = 1024 * pattern.size

  var source = rawSource.grouped(chunkSize).map { (chunk) => chunk.map(charToInt(_)) }

  var workers = Array.fill(4) { context.actorOf(Props(new ChunkConsumer(pattern, weights))) }.toSet

  var chunksCount = 0

  var results = Array[Int]()

  var maximum = weights.sum * 2 / 3

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
