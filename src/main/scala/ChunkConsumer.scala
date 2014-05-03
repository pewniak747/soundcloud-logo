package soundcloudlogo

import com.twitter.util.RingBuffer
import akka.actor._

class ChunkConsumer(val pattern: Array[(Int, Int)], val weights: Array[Int]) extends Actor {

  val breakpoints: Array[Int] = pattern.sliding(2).zipWithIndex.map {
    case (pattern, index) if pattern(0) != pattern(1) => index
    case _ => -1
  }.filter(_ >= 0).toArray

  val maxDelta = breakpoints.map { (b) => weights(b) }.sum + weights.last

  var maximum = 0

  override def preStart = {
    context.parent ! ChunkRequest
  }

  def receive = {
    case ChunkResponse(chunk, newMaximum) => {
      maximum = newMaximum
      work(chunk)
      sender ! ChunkRequest
    }
  }

  def work(chunk: Seq[Int]) = {
    val in = chunk.toIterator
    var window = new RingBuffer[Int](pattern.length)
    window ++= in.take(pattern.length).toArray

    var scores = new RingBuffer[Int](pattern.length)
    scores ++= scoresOfSequence(window)

    var iterations = 0
    var skip = 0

    for(pixel <- in) {
      iterations += 1

      // calculate changed pixel scores
      breakpoints.foreach { (breakpoint) =>
        scores(breakpoint + 1) = scoreOfPixel(window(breakpoint + 1), pattern(breakpoint), weights(breakpoint))
      }

      // append new pixel
      scores += scoreOfPixel(pixel, pattern.last, weights.last)
      window += pixel

      if (skip == 0) {
        val score = scores.sum
        if (maximum <= score) {
          sender ! SequenceResult(window.toList, score, iterations)
        } else if(score + maxDelta < maximum) {
          skip = (maximum - score) / maxDelta
        }
      }
      else {
        skip -= 1
      }
    }

  }

  private

  def scoreOfPixel(pixel: Int, range: (Int, Int), weight: Int) = range match {
    case (lowerBound, upperBound) => if(lowerBound <= pixel && pixel <= upperBound) weight else 0
  }

  def scoresOfSequence(pixels: Seq[Int]) = (pixels, pattern, weights).zipped.map {
    case (pixel, range, weight) => scoreOfPixel(pixel, range, weight)
  }.toArray

}
