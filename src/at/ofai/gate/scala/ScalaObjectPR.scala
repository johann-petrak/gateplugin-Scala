package at.ofai.gate.scala

import scala.reflect.BeanProperty
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

// TODO: add a right-click menu entry for generating a script scaffold file
// TODO: see if we can make it so that fsc is used? 
// TODO: add an option to cache the compiled script -- this could be done 
//   by creating a file script.scala.cache.jar. If we do init or reinit and
//   the cache is newer than the script, just add the jar to the classpath and
//   import the object in the interpreter instad of compiling it.

// TODO: In addition to the classpath we already pass on to the compiler,
// try to add whatever has been added since GATE was started..
// There may be to approaches to do this:
// - figure out which plugins are loaded, then get the URLs of all the JARs
//   Ideally this would be provided by a method on DirectoryInfo (but we have
//   to change GATE for that) or we just add all the jars in the root and the
//   ./lib dirtree
// - get the URLs of the JARs that are on the current GATE classloader 
//   (or the threads context class loader?)
//    lazy val urls = java.lang.Thread.currentThread.getContextClassLoader match {
//      case cl: java.net.URLClassLoader => cl.getURLs.toList
//      case _ => error("classloader is not a URLClassLoader")
//    }
//    lazy val classpath = urls map {_.toString}
//    // todo: add to instead or replace classpath.value!
//    settings.classpath.value = classpath.distinct.mkString(java.io.File.pathSeparator)

 


@CreoleResource(
  name = "Scala Object PR",
  comment = "Use a Scala object as a processing resource and run the object's execute function for each document")
class ScalaObjectPR  
  extends AbstractLanguageAnalyser with ControllerAwarePR with ScalaCodeDriven
{
  @CreoleParameter(
      comment="The URL of the scala file to run")
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
  
  @throws(classOf[ResourceInstantiationException])
  override def init: Resource = {
    if(getScriptURL() == null) {
      throw new ResourceInstantiationException("No ScriptURL given!");
    }
    val script = 
      new BatchSourceFile(
          new PlainFile(
              Files.fileFromURL(getScriptURL()).toString()))
    val settings = new GenericRunnerSettings(str => ())
    // this will add the System classpath to the compiler classpath
    settings.usejavacp.value = true
    // append all jars in the lib directory of the plugin to 
    // the settings classpath
    
    var pluginDirName = 
      Utils.getPluginDir(this.getClass.getName()).getCanonicalPath()
    
    System.setProperty("scala.home",pluginDirName)
        
        
    //println("The scala.home property is: "+System.getProperty("scala.home"))
    
    //println("The bootclasspath is "+settings.bootclasspath)
         
    Utils.getJarFileNames4Plugin(this.getClass.getName()).foreach { x =>
      settings.classpath.append(x)
    }
    
    // get the classpath from the Thread context class loader
    lazy val contextClUrls: List[String] = 
      java.lang.Thread.currentThread.getContextClassLoader match {
      case cl: java.net.URLClassLoader => cl.getURLs.toList.map { _.toString }
      case _ => List[String]()
    }
    println("contect CL URLS: "+contextClUrls)
    
    // get the classpath from the GATE classloader
    lazy val gateClUrls: List[String] = 
      gate.Gate.getClassLoader match {
      case cl: java.net.URLClassLoader => cl.getURLs.toList.map { _.toString }
      case _ => List[String]()
    }
    println("GATE CL URLS: "+gateClUrls)
    
    imain = new IMain(settings)
    //println("Compiler classpath is: "+imain.compilerClasspath)
    val ok = imain.compileSources(script)
    if(!ok) {
      throw new ResourceInstantiationException("Compile error!")
    }
    imain.setContextClassLoader
    imain.quietBind(NamedParam("corpus","gate.Corpus",corpus))
    imain.quietBind(NamedParam("inputAS","java.lang.String",inputAS))
    imain.quietBind(NamedParam("outputAS","java.lang.String",outputAS))
    imain.quietBind(NamedParam("parms","gate.FeatureMap",parms))
    imain.quietBind(NamedParam("ontology","gate.creole.ontology.Ontology",ontology))
    imain.interpret("script.setInit(corpus,inputAS,outputAS,parms,ontology)")
    var res = imain.interpret("script.init")
    if(res == Results.Error) {
      throw new GateRuntimeException(
          "Error executing script during reinitialisation")
    }
    this
  }
  
  override def reInit: Unit = {
    val script = 
      new BatchSourceFile(
          new PlainFile(
              Files.fileFromURL(getScriptURL()).toString()))
    imain.reset
    val ok = imain.compileSources(script)
    if(!ok) {
      throw new ResourceInstantiationException("Recompile error!")
    }
    imain.setContextClassLoader
    imain.quietBind(NamedParam("corpus","gate.Corpus",corpus))
    imain.quietBind(NamedParam("inputAS","java.lang.String",inputAS))
    imain.quietBind(NamedParam("outputAS","java.lang.String",outputAS))
    imain.quietBind(NamedParam("parms","gate.FeatureMap",parms))
    imain.quietBind(NamedParam("ontology","gate.creole.ontology.Ontology",ontology))
    imain.interpret("script.setInit(corpus,inputAS,outputAS,parms,ontology)")
    var res = imain.interpret("script.reInit")
    if(res == Results.Error) {
      throw new GateRuntimeException(
          "Error executing script during reinitialisation")
    }
  }
  
  override def execute = {    
    imain.quietBind(NamedParam("doc","gate.Document",document))
    imain.interpret("script.setDoc(doc)")
    var res = imain.interpret("script.execute")
    if(res == Results.Error) {
      throw new GateRuntimeException(
          "Error executing script.execute for document "+document.getName)
    }
  }
  
  override def controllerExecutionStarted(c: Controller): Unit = {
    imain.quietBind(NamedParam("contr","gate.Controller",c))
    imain.interpret("script.controllerExecutionStarted4Interpreter(contr)")
  }
  override def controllerExecutionFinished(c: Controller): Unit = {
    imain.quietBind(NamedParam("contr","gate.Controller",c))
    imain.interpret("script.controllerExecutionFinished4Interpreter(contr)")    
  }
  override def controllerExecutionAborted(c: Controller, t: Throwable): Unit = {
    imain.quietBind(NamedParam("contr","gate.Controller",c))
    imain.quietBind(NamedParam("thr","java.lang.Throwable",t))
    imain.interpret("script.controllerExecutionAborted4Interpreter(contr,thr)")
  }
}
