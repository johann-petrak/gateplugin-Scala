import at.ofai.gate.scala._

object script extends ScalaProcessingResource {
  override def execute: Unit = {
    println("processing "+doc.getName)
  }
}