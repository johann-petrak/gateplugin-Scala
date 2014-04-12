/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.ofai.gate.scala

import scala.collection.mutable.HashSet
import scala.tools.nsc._
import scala.tools.nsc.interpreter._
import scala.tools.nsc.io._
import scala.tools.nsc.util._
import gate._
import gate.util._

// A trait that provides methods for easily invoking the Scala interpreter 
// and compiler from clients and for registering listening to embedded
// events.
// 
// to use: 
// 
// var scala = new Scala()
// scala.addToClassPath(someURLClassloader)
// scala.addToClassPath(someotherURLClassloader)
// scala.addToClassPath(someJarFileNameString)
// scala.setHome(homePathString)
// scala.setClassLoader(someClassLoader)
// scala.init -- actually initializes the compiler/interpreter for use
// ret = scala.bind(name,type,val)
// ret = scala.compile(file)
// ret = scala.interpret(string)

// This code has been inspired from a helpful message by Norbert Tausch on
// the scala-user mailinglist

trait ScalaEmbedded {
  
  val listeners = new HashSet[ScalaEmbeddedListener]()
  def registerScalaEmbeddedListener(listener: ScalaEmbeddedListener): Unit = {
    listeners += listener
  }
  def removeScalaEmbeddedListener(listener: ScalaEmbeddedListener): Unit = {
    listeners -= listener
  }
  
  class Scala(classpath: String, classloader: ClassLoader) {
    val settings = new GenericRunnerSettings(str => println("From the settings: "+str))
    val ourOutputStream = new java.io.ByteArrayOutputStream()
    
    settings.classpath.value = classpath
    settings.embeddedDefaults(classloader)
    var interpreter: IMain = null
    try {
      interpreter = new IMain(settings, new java.io.PrintWriter(ourOutputStream))
    } catch {
      case ex => {
        val msg = ourOutputStream.toString
        throw new GateRuntimeException("Problem initializing the Scala compiler: "+msg,ex)
      }
    }
    
    // the relevant methods of IMain which we want to wrap
    
    def bind(name: String, typeName: String, value: Any): ScalaEmbeddedResult = {
      val result = new ScalaEmbeddedResult()
      ourOutputStream.reset
      val ret = interpreter.bind(name,typeName,value)
      if(ret != Results.Success) {
        result.isError = true
      }
      result.message = ourOutputStream.toString
      ourOutputStream.reset
      result
    }
    def interpret(what: String): ScalaEmbeddedResult = {
      val result = new ScalaEmbeddedResult()
      ourOutputStream.reset
      val ret = interpreter.interpret(what)
      if(ret != Results.Success) {
        result.isError = true
      }
      result.message = ourOutputStream.toString
      ourOutputStream.reset
      result
    }
    def compile(file: java.io.File): ScalaEmbeddedResult = {
      val what = new BatchSourceFile(new PlainFile(file))
      val result = new ScalaEmbeddedResult()
      ourOutputStream.reset
      val isOk = interpreter.compileSources(what)
      result.isError = isOk
      result.message = ourOutputStream.toString
      ourOutputStream.reset
      result
    }
  }
  
  def fireCompiledEvent(what: java.io.File, result: ScalaEmbeddedResult) = {
    listeners.foreach { _.compiled(what, result)}
  }
  def fireInterpretedEvent(what: String, source: AnyRef, result: ScalaEmbeddedResult) = {
    listeners.foreach { _.interpreted(what, source, result)}
  }
  def fireBoundEvent(name: String, typename: String, value: Any, result: ScalaEmbeddedResult) = {
    listeners.foreach { _.bound(name, typename, value, result)}
  }
  
}
