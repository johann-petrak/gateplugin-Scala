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
import scala.tools.nsc.Settings
import scala.tools.nsc.util.BatchSourceFile
import javax.script.CompiledScript
import javax.script.ScriptContext

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

// TODO: factor out the code that will be common to all classes that use
// scala scripting/compilation
// This includes the possibility to listen for the following events:
// - script has been reloaded (url,content): for the editor to refresh content
// - script has been successfully compiled
// - script had error on compile (error output)
// - error on interpret (command,error output)
// TODO: refactor so that loading and compiling is a separate function that
// gets called from init, but can alos get called from elsewhere (e.g. the 
// gui editor) -> into parent class/trait!


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
    val script = scala.io.Source.fromFile(Files.fileFromURL(getScriptURL())).mkString
    val settings = new Settings()
    // this will add the System classpath to the compiler classpath
    settings.usejavacp.value = true
    // append all jars in the lib directory of the plugin to 
    // the settings classpath
    
    println("Got brand new settings: "+settings)
    println("Settings are: "+settings)
    println("Settings.classpath is: "+settings.classpath)
    println("Settings.bootclasspath is: "+settings.bootclasspath)
    
    
    var pluginDirName = 
      Utils.getPluginDir(this.getClass.getName()).getCanonicalPath()
    
    System.setProperty("scala.home",pluginDirName)
        
        
    //println("The scala.home property is: "+System.getProperty("scala.home"))
    println("\nThe original classpath: "+settings.classpath)
    
    println("\nThe bootclasspath: "+settings.bootclasspath)
    println("\nThe javabootclasspath: "+settings.javabootclasspath)
         
    Utils.getJarFileNames4Plugin(this.getClass.getName()).foreach { x =>
      println("Appending from jar file names to settings.classpath: "+x)
      settings.classpath.append(x)
      settings.bootclasspath.append(x)
    }
    
    // get the classpath from the Thread context class loader
    val contextClUrls: List[String] = 
      Utils.getJarUrls4ClassLoader(java.lang.Thread.currentThread.getContextClassLoader)
    //println("\ncontext CL URLS: "+contextClUrls)
    
    contextClUrls.foreach { url => 
      println("appending from context classloader to settings.classpath: "+url)
      settings.classpath.append(url) 
      settings.bootclasspath.append(url) 
    }
    
    // get the classpath from the GATE classloader
    val gateClUrls: List[String] = 
      Utils.getJarUrls4ClassLoader(gate.Gate.getClassLoader)
    //println("\nGATE CL URLS: "+gateClUrls)

    gateClUrls.foreach { url =>
      println("appending from cgetClUrls to settings.classpath: "+url)
      settings.classpath.append(url) 
      settings.bootclasspath.append(url) 
    }
    
    // TODO: is this needed at all? what exactly does embeddedDefaults do?
    //settings.embeddedDefaults(gate.Gate.getClassLoader)
    
    
    println("All settings: "+settings)
    println("Settings.classpath is: "+settings.classpath)
    println("Settings.bootclasspath is: "+settings.bootclasspath)
    
    imain = new IMain(settings)
    println("The class we made is: "+imain.getClass())
    println("Compiler classpath is: "+imain.compilerClasspath)
    println("Settings.classpath is: "+settings.classpath)
    //val ret = imain.compileString(script)
    val ret = imain.compile(script)
    println("Compilation gave me: "+ret)
    if(ret.isInstanceOf[CompiledScript]) {
      println("YES, is instance of compiled script")
      val cs = ret.asInstanceOf[CompiledScript]
      val e = cs.eval(null.asInstanceOf[ScriptContext])
      println("Got evaluation result: "+e+" of type "+e.getClass())
      if(e.isInstanceOf[ScalaProcessingResource]) {
        println("YES is a ScalaProcessingResource")
        val spr = e.asInstanceOf[ScalaProcessingResource]
        spr.execute
      } else {
        println("NO, not a scalaprocessingresource")
      }
    } else {
        println("NO is not instance, but "+ret.getClass())
    }
      
    // now try to find the class we just compiled: 
    
    //imain.setContextClassLoader
    //imain.quietBind(NamedParam("corpus","gate.Corpus",corpus))
    //imain.quietBind(NamedParam("inputAS","java.lang.String",inputAS))
    //imain.quietBind(NamedParam("outputAS","java.lang.String",outputAS))
    //imain.quietBind(NamedParam("parms","gate.FeatureMap",parms))
    //imain.quietBind(NamedParam("ontology","gate.creole.ontology.Ontology",ontology))
    //imain.interpret("script.setInit(corpus,inputAS,outputAS,parms,ontology)")
    //var res = imain.interpret("script.init")
    //if(res == Results.Error) {
    //  throw new GateRuntimeException(
    //      "Error executing script during reinitialisation")
    //}
    this
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
