package soundcloudlogo

import scala.io.Source
import com.twitter.util.RingBuffer
import akka.actor._

object Runner {
  def main(args: Array[String]) = {
    println("Starting Soundcloud Logo Approximator...")

    val actorSystem = ActorSystem()

    val in = Source.stdin
    in.drop(2) // drop 3.
    val producer = actorSystem.actorOf(Props(new ChunkProducer(in)))

    println
    println("Exiting...")
  }
}
