package at.ofai.gate.scala

import gate._
import gate.creole._
import gate.creole.ontology.Ontology
import gate.creole.metadata._
import gate.util._
import java.net.URL
import java.io._
import scala.tools.nsc.interpreter._
import scala.tools.nsc.io.PlainFile
import scala.tools.nsc.GenericRunnerSettings
import scala.tools.nsc.util.BatchSourceFile


@CreoleResource(
  name = "Scala Script PR",
  comment = "Use a Scala script as a processing resource")
class ScalaScriptPR  
  extends AbstractLanguageAnalyser with ControllerAwarePR with ScalaCodeDriven
{
  @CreoleParameter(
      comment="The URL of the scala script to run")
  def setScriptURL(parm: URL) { scriptURL = parm }
  def getScriptURL(): URL = scriptURL
  var scriptURL: URL = null
  
  @RunTime
  @Optional
  @CreoleParameter(
      defaultValue = "",
      comment="The name of the annotation set to read from")
  def setInputAS(parm: String) { inputAS = parm }
  def getInputAS(): String = inputAS
  var inputAS: String = null
  
  @RunTime
  @Optional
  @CreoleParameter(
      defaultValue = "",
      comment="The name of the annotation set to write to")
  def setOutputAS(parm: String) { outputAS = parm }
  def getOutputAS(): String = outputAS
  var outputAS: String = null
  
  @RunTime
  @Optional
  @CreoleParameter(
      defaultValue = "",
      comment="The parameters to pass on to the script")
  def setParms(parm: gate.FeatureMap) { parms = parm }
  def getParms(): gate.FeatureMap = parms
  var parms: gate.FeatureMap = gate.Factory.newFeatureMap()
  
  @RunTime
  @Optional
  @CreoleParameter(
      comment="An ontology to use in the script")
  def setOntology(parm: Ontology) { ontology = parm }
  def getOntology(): Ontology = ontology
  var ontology: Ontology = null
  
  var imain: IMain = null
  var scriptText: String = ""
  
  @throws(classOf[ResourceInstantiationException])
  override def init: Resource = {
    if(getScriptURL() == null) {
      throw new ResourceInstantiationException("No ScriptURL given!");
    }
    scriptText = Files.getString(Files.fileFromURL(getScriptURL()).toString())
    val settings = new GenericRunnerSettings(str => ())
    // this will add the System classpath to the compiler classpath
    // settings.usejavacp.value = true
    // append all jars in the lib directory of the plugin to 
    // the settings classpath
    
    var pluginDirName = 
      Utils.getPluginDir(this.getClass.getName()).getCanonicalPath()
    
    System.setProperty("scala.home",pluginDirName)
        
        
    //println("The scala.home property is: "+System.getProperty("scala.home"))
    
    println("The bootclasspath is "+settings.bootclasspath)
         
    Utils.getJarFileNames4Plugin(this.getClass.getName()).foreach { x =>
      System.out.println("Appending to settings.classpath: "+x)
      settings.classpath.append(x)
    }
    imain = new IMain(settings)
    println("Compiler classpath is: "+imain.compilerClasspath)
    imain.setContextClassLoader
    imain.quietBind(NamedParam("corpus","gate.Corpus",corpus))
    imain.quietBind(NamedParam("inputAS","java.lang.String",inputAS))
    imain.quietBind(NamedParam("outputAS","java.lang.String",outputAS))
    imain.quietBind(NamedParam("parms","gate.FeatureMap",parms))
    imain.quietBind(NamedParam("ontology","gate.creole.ontology.Ontology",ontology))
    this
  }
  
  override def reInit: Unit = {
    scriptText = Files.getString(Files.fileFromURL(getScriptURL()).toString())
    imain.reset
    imain.setContextClassLoader
    imain.quietBind(NamedParam("corpus","gate.Corpus",corpus))
    imain.quietBind(NamedParam("inputAS","java.lang.String",inputAS))
    imain.quietBind(NamedParam("outputAS","java.lang.String",outputAS))
    imain.quietBind(NamedParam("parms","gate.FeatureMap",parms))
    imain.quietBind(NamedParam("ontology","gate.creole.ontology.Ontology",ontology))
  }
  
  override def execute = {    
    imain.quietBind(NamedParam("doc","gate.Document",document))
    var res = imain.interpret(scriptText)
    if(res == Results.Error) {
      throw new GateRuntimeException(
          "Error executing script for document "+document.getName)
    }
  }
  
  override def controllerExecutionStarted(c: Controller): Unit = {
    imain.quietBind(NamedParam("controller","gate.Controller",c))
  }
  override def controllerExecutionFinished(c: Controller): Unit = {
  }
  override def controllerExecutionAborted(c: Controller, t: Throwable): Unit = {
  }
}
