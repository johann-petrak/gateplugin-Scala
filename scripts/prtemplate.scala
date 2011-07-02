import at.ofai.gate.scala._

object script extends ScalaProcessingResource {
  override def execute: Unit = {
    println("Hello World 3")
    println("Doc is "+doc)
  }
}
