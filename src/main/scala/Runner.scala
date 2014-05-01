package soundcoundlogo

import scala.io.Source

object Runner {
  def main(args: Array[String]) = {
    println("Starting Soundcloud Logo Approximator...")

    def levelOfChar(a: Char) = level(a.toInt - 48)

    def level(a: Int): Int = if(a <= 2) return 0
      else if (a <= 6) return 1
      else return 2

    val pattern = List(0, 0, 0, 0, 0, 3, 3, 9, 9, 9, 6, 0, 0, 0, 0, 0, 0, 3, 4, 9, 5, 9, 9, 9, 9, 4, 0, 0, 0, 1, 2, 7, 5, 9, 5, 9, 9, 9, 9, 7, 1, 0, 3, 9, 5, 9, 5, 9, 5, 9, 9, 9, 9, 9, 9, 3, 6, 9, 5, 9, 5, 9, 5, 9, 9, 9, 9, 9, 9, 6, 1, 8, 5, 9, 5, 9, 5, 9, 9, 9, 9, 9, 8, 1).map(_.toString.charAt(0))
    val weights = pattern.map((c) => if(levelOfChar(c) == 0) 5 else 1)

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
      val score = (window, pattern, weights).zipped.foldLeft(0)((mem, triple: (Char, Char, Int)) =>
        triple match {
          case (windowChar, patternChar, weight) => mem + weight * Math.abs(levelOfChar(windowChar) - levelOfChar(patternChar))
        }
      )
      if (minimum >= score) {
        minimum = score
        println("Score: " + score + "; Sequence: " + window.mkString + "; Offset: " + iterations)
      }
    }

    println
    println("Exiting...")
  }
}
