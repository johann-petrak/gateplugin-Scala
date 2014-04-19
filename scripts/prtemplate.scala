import at.ofai.gate.scala._
import gate._

class TestScript extends ScalaProcessingResource {
  var fm: FeatureMap = Factory.newFeatureMap
  override def execute: Unit = {
    println("processing!!!! ")
  }
  override def init = {
    System.out.println("Running init!!!!!!!")
  }
}

new TestScript()
