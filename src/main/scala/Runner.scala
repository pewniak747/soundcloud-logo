package soundcoundlogo

import scala.io.Source
import com.twitter.util.RingBuffer

object Runner {
  def main(args: Array[String]) = {
    println("Starting Soundcloud Logo Approximator...")

    def charToInt(a: Char): Int = a.toInt - 48

    val white = (0, 2)
    val gray  = (3, 9)
    val black = (6, 9)
    val pattern = Array(0, 0, 0, 0, 0, 3, 3, 9, 9, 9, 6, 0, 0, 0,
                        0, 0, 0, 3, 4, 9, 5, 9, 9, 9, 9, 4, 0, 0,
                        0, 1, 2, 7, 5, 9, 5, 9, 9, 9, 9, 7, 1, 0,
                        3, 9, 5, 9, 5, 9, 5, 9, 9, 9, 9, 9, 9, 3,
                        6, 9, 5, 9, 5, 9, 5, 9, 9, 9, 9, 9, 9, 6,
                        1, 8, 5, 9, 5, 9, 5, 9, 9, 9, 9, 9, 8, 1)

    val ranges = Array(white, white, white, white, white, gray,  gray,  black, black, black, black, white, white, white,
                       white, white, white, gray,  gray,  gray,  gray,  black, black, black, black, white, white, white,
                       white, white, gray,  gray,  gray,  gray,  gray,  black, black, black, black, black, white, white,
                       gray,  gray,  gray,  gray,  gray,  gray,  gray,  black, black, black, black, black, black, black,
                       gray,  gray,  gray,  gray,  gray,  gray,  gray,  black, black, black, black, black, black, black,
                       white, gray,  gray,  gray,  gray,  gray,  gray,  black, black, black, black, black, black, white)

    val weights = ranges.map((r) => if(r == white) 5 else 1)

    val breakpoints: Array[Int] = ranges.sliding(2).zipWithIndex.map {
      case (ranges, index) if ranges(0) != ranges(1) => index
      case _ => -1
    }.filter(_ >= 0).toArray

    def scoreOfPixel(pixel: Int, range: (Int, Int), weight: Int) = range match {
      case (lowerBound, upperBound) => if(lowerBound <= pixel && pixel <= upperBound) weight else 0
    }

    def scoresOfSequence(pixels: Seq[Int]) = (pixels, ranges, weights).zipped.map {
      case (pixel, range, weight) => scoreOfPixel(pixel, range, weight)
    }.toArray

    if(args.size == 1) {
      val input = args(0).map(charToInt(_))
      val score = scoresOfSequence(input).sum
      println("Score: " + score)
      System.exit(0)
    }

    val chunkSize = pattern.length
    val stdin = Source.stdin
    stdin.drop(2) // skip 3. part of 3.141592

    var window = new RingBuffer[Int](pattern.length)
    window ++= stdin.take(pattern.length).map(charToInt(_)).toArray

    var scores = new RingBuffer[Int](pattern.length)
    scores ++= scoresOfSequence(window)
    var currentScore = scores.sum

    var maximum = 0
    var results = Array[Int]()

    //println("Breakpoints: " + breakpoints.mkString(" "))
    //println("Pre digits:  " + window.mkString(" "))
    //println("Pre scores:  " + scores.mkString(" "))

    var iterations = 0
    for(chunk <- stdin.grouped(chunkSize); character <- chunk) {
      iterations += 1
      if(iterations % (1024 * 1024) == 0) println("Read " + iterations + " digits")

      val digit = charToInt(character)

      // calculate changed pixel scores
      breakpoints.foreach { (breakpoint) =>
        val newScore = scoreOfPixel(window(breakpoint + 1), ranges(breakpoint), weights(breakpoint))
        val oldScore = scores(breakpoint + 1)
        scores(breakpoint + 1) = newScore
        currentScore += (newScore - oldScore)
      }

      // append new pixel
      currentScore -= scores.head
      scores += scoreOfPixel(digit, ranges.last, weights.last)
      window += digit
      currentScore += scores.last

      //println("Post scores: " + scores.mkString(" "))
      //println("Post soseq:  " + scoresOfSequence(window).mkString(" "))
      //println("Post digits: " + window.mkString(" "))

      val score = currentScore
      //val scoreOfSequence = scoresOfSequence(window).sum
      //println("Score: " + score + "; Score of seq: " + scoreOfSequence)
      //if(score != scoreOfSequence) System.exit(1)

      if (maximum <= score) {
        results = (results :+ score).sorted.reverse.take(10)
        maximum = results.last
        println("Result set: " + results.mkString(", "))
        println("Score: " + score + "; Sequence: " + window.mkString + "; Offset: " + iterations)
      }
    }

    println
    println("Exiting...")
  }
}
