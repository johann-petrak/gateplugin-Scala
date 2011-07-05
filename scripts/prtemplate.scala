import at.ofai.gate.scala._
import gate._
import gate.creole.ontology._

object script extends ScalaProcessingResource {
  var fm: FeatureMap = Factory.newFeatureMap
  var onto: gate.creole.ontology.Ontology = null
  override def execute: Unit = {
    println("processing "+doc.getName)
  }
  override def init = {
    System.out.println("Running init!!!!!!!")
    onto = Factory.createResource(
      "gate.creole.ontology.impl.sesame.OWLIMOntology",parms,fm,"onto1").asInstanceOf[gate.creole.ontology.Ontology]
    System.out.println("Ontology created")
  }
}
