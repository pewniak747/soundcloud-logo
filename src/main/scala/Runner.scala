package soundcoundlogo

import scala.io.Source
import scala.collection.mutable.ArrayBuffer

object Runner {
  def main(args: Array[String]) = {
    println("Starting Soundcloud Logo Approximator...")

    def charToInt(a: Char): Int = a.toInt - 48

    def level(a: Int): Int = if(a <= 2) return 0
      else if (a <= 6) return 1
      else return 2

    val pattern = List(0, 0, 0, 0, 0, 3, 3, 9, 9, 9, 6, 0, 0, 0,
                       0, 0, 0, 3, 4, 9, 5, 9, 9, 9, 9, 4, 0, 0,
                       0, 1, 2, 7, 5, 9, 5, 9, 9, 9, 9, 7, 1, 0,
                       3, 9, 5, 9, 5, 9, 5, 9, 9, 9, 9, 9, 9, 3,
                       6, 9, 5, 9, 5, 9, 5, 9, 9, 9, 9, 9, 9, 6,
                       1, 8, 5, 9, 5, 9, 5, 9, 9, 9, 9, 9, 8, 1)
    val weights = pattern.map((d) => if(level(d) == 0) 5 else 1)

    val chunkSize = pattern.length
    Source.stdin.take(2) // skip 3. part of 3.141592
    var window = ArrayBuffer[Int]()
    window ++= Source.stdin.take(pattern.length).map(charToInt(_))
    var minimum = 10 * pattern.length
    var results = Array[Int]()

    var iterations = 0
    for(chunk <- Source.stdin.grouped(chunkSize); character <- chunk) {
      iterations += 1
      if(iterations % (1024 * 1024) == 0) println("Read " + iterations + " digits")

      val digit = charToInt(character)
      window += digit
      window = window.drop(1)

      val score = (window, pattern, weights).zipped.foldLeft(0)((mem, triple) =>
        triple match {
          case (digit1, digit2, weight) => mem + weight * Math.abs(level(digit1) - level(digit2))
        }
      )
      if (minimum >= score) {
        results = (results :+ score).sorted.take(10)
        minimum = results.last
        println("Score: " + score + "; Sequence: " + window.mkString + "; Offset: " + iterations)
      }
    }

    println
    println("Exiting...")
  }
}
