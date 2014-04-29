package soundcoundlogo

import scala.io.Source

object Runner {
  def main(args: Array[String]) = {
    println("Starting Soundcloud Logo Approximator...")

    val pattern = List(0, 0, 0, 0, 0, 3, 3, 9, 9, 9, 6, 0, 0, 0, 0, 0, 0, 3, 4, 9, 5, 9, 9, 9, 9, 4, 0, 0, 0, 1, 2, 7, 5, 9, 5, 9, 9, 9, 9, 7, 1, 0, 3, 9, 5, 9, 5, 9, 5, 9, 9, 9, 9, 9, 9, 3, 6, 9, 5, 9, 5, 9, 5, 9, 9, 9, 9, 9, 9, 6, 1, 8, 5, 9, 5, 9, 5, 9, 9, 9, 9, 9, 8, 1).map(_.toString.charAt(0))

    val chunkSize = pattern.length
    Source.stdin.take(2) // skip 3. part of 3.141592
    var window: Array[Char] = Source.stdin.take(pattern.length).toArray
    var minimum = 10 * pattern.length

    var iterations = 0
    for(chunk <- Source.stdin.grouped(chunkSize); character <- chunk) {
      iterations += 1
      if(iterations % (1024 * 1024) == 0) println("Read " + iterations + " digits")
      window = window.drop(1)
      window = window :+ character
      val score = window.zip(pattern).foldLeft(0)((mem, pair) => mem + Math.abs(pair._1 - pair._2))
      if (minimum > score) {
        minimum = score
        println("found new score: " + score + ", " + window.mkString)
      }
    }

    println
    println("Exiting...")
  }
}
