package at.ofai.gate.scala

import gate._
import gate.util._
import gate.creole.ontology._

class ScalaProcessingResource {
  var doc: Document = null
  var corpus: Corpus = null
  var inputAS: AnnotationSet = null
  var outputAS: AnnotationSet = null
  var inputASName: String = null
  var outputASName: String = null
  var controller: Controller = null
  var parms: FeatureMap = null
  var ontology: Ontology = null
  def setInit(c: Corpus, i: String, o: String, p: FeatureMap, 
      onto: Ontology): Unit = {
    corpus = c
    inputASName = i
    outputASName = o
    parms = p
    ontology = onto
  }
  def setDoc(d: Document): Unit = { doc = d }
  def execute4Interpreter: Unit = {
    if(doc != null) {
      inputAS = doc.getAnnotations(inputASName)
      outputAS = doc.getAnnotations(outputASName)
    }
    execute
  }
  def execute: Unit = {
    throw new GateRuntimeException("please overwrite execute()")
  }
  def controllerStarted(c: Controller): Unit = {
  }
  def controllerFinished(c: Controller): Unit = {
  }
  def controllerAborted(c: Controller, t: Throwable): Unit = {
  }
  def init: Unit = { }
  def reInit: Unit = { init }
  def cleanup: Unit = {  }
  
  def controllerExecutionStarted4Interpreter(c: Controller): Unit = { 
    controller = c 
    controllerStarted(c)
  }
  def controllerExecutionFinished4Interpreter(c: Controller): Unit = {
    controllerFinished(c)
    controller = null 
  }
  def controllerExecutionAborted4Interpreter(c: Controller, t: Throwable): Unit = {
    controllerAborted(c,t)
    controller = null
  }
}