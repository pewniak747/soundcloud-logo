package soundcoundlogo

import scala.io.Source
import scala.collection.mutable.ArrayBuffer

object Runner {
  def main(args: Array[String]) = {
    println("Starting Soundcloud Logo Approximator...")

    def charToInt(a: Char): Int = a.toInt - 48

    val pattern = List(0, 0, 0, 0, 0, 3, 3, 9, 9, 9, 6, 0, 0, 0,
                       0, 0, 0, 3, 4, 9, 5, 9, 9, 9, 9, 4, 0, 0,
                       0, 1, 2, 7, 5, 9, 5, 9, 9, 9, 9, 7, 1, 0,
                       3, 9, 5, 9, 5, 9, 5, 9, 9, 9, 9, 9, 9, 3,
                       6, 9, 5, 9, 5, 9, 5, 9, 9, 9, 9, 9, 9, 6,
                       1, 8, 5, 9, 5, 9, 5, 9, 9, 9, 9, 9, 8, 1)

    val white = (0, 2)
    val gray  = (3, 9)
    val black = (6, 9)
    val ranges = List(white, white, white, white, white, gray,  gray,  black, black, black, black, white, white, white,
                      white, white, white, gray,  gray,  gray,  gray,  black, black, black, black, white, white, white,
                      white, white, gray,  gray,  gray,  gray,  gray,  black, black, black, black, black, white, white,
                      gray,  gray,  gray,  gray,  gray,  gray,  gray,  black, black, black, black, black, black, black,
                      gray,  gray,  gray,  gray,  gray,  gray,  gray,  black, black, black, black, black, black, black,
                      white, gray,  gray,  gray,  gray,  gray,  gray,  black, black, black, black, black, black, white)


    val chunkSize = pattern.length
    Source.stdin.take(2) // skip 3. part of 3.141592
    var window = ArrayBuffer[Int]()
    window ++= Source.stdin.take(pattern.length).map(charToInt(_))
    var maximum = 0
    var results = Array[Int]()

    var iterations = 0
    for(chunk <- Source.stdin.grouped(chunkSize); character <- chunk) {
      iterations += 1
      if(iterations % (1024 * 1024) == 0) println("Read " + iterations + " digits")

      val digit = charToInt(character)
      window += digit
      window = window.drop(1)

      val score = ranges.zip(window).filter({
        case ((lowerBound, upperBound), pixel) => lowerBound <= pixel && pixel <= upperBound
      }).length

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
