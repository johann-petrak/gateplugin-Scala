package com.jpetrak.gate.scala

import java.io.File
import gate.Gate
import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.IMain
import javax.script.CompiledScript
import javax.script.ScriptContext


class ScalaCompilerImpl1 extends ScalaCompiler {
  
  var compiler: IMain = null 
  
  def init() = {
    this.synchronized {
      println("Creating settings instance");
      val settings = new Settings();
      println("Have settings: "+settings)
      println("The original classpath: "+settings.classpath)
      println("The bootclasspath: "+settings.bootclasspath)
      println("The javabootclasspath: "+settings.javabootclasspath)
    
      settings.usejavacp.value = true
      var pluginDirName = 
        getPluginDir(this.getClass.getName()).getCanonicalPath()
      System.setProperty("scala.home",pluginDirName)
    
      // now set both the bootclasspath and the javaclasspath to include
      // all the URLs we found
      var knownJars = Set[String]()
      // the best classloader to use so far seems to be 
      // <somepluginclass>.getClass().getClassLoader()
      // we do not use any more:
      //   java.lang.Thread.currentThread.getContextClassLoader
      //   gate.Gate.getClassLoader
         
      val pluginClUrls: List[String] = getJarUrls4ClassLoader(classOf[ScalaScriptPR].getClassLoader)
      pluginClUrls.foreach { url =>
        if(!knownJars.contains(url)) {
          println("appending from Plugin classloader to settings.classpath: "+url)
          settings.classpath.append(url) 
          settings.bootclasspath.append(url)
          knownJars += url
        } else {
          println("Plugin classloader - ignoring already known "+url)
        }
      }
      
      // NOTE: accessing the Gate.class classloader may not be necessary if we
      // instead always also process all the parent classloaders, then the 
      // previous code should take care of this automatically
      val gateClUrls: List[String] = getJarUrls4ClassLoader(gate.Gate.getClassLoader)
      gateClUrls.foreach { url =>
        if(!knownJars.contains(url)) {
          println("appending from Gate.class classloader to settings.classpath: "+url)
          settings.classpath.append(url) 
          settings.bootclasspath.append(url)
          knownJars += url
        } else {
          println("Gate.class - ignoring already known "+url)
        }
      }
      val pluginJars: List[String] = getJarFileNames4pluginDir(pluginDirName)
      pluginJars.foreach { url =>
        if(!knownJars.contains(url)) {
          println("appending from plugin jars to settings.classpath: "+url)
          settings.classpath.append(url) 
          settings.bootclasspath.append(url)
          knownJars += url
        } else {
          println("plugin dir - ignoring already known "+url)
        }
      }
    
      
      println("Settings initialized, all settings: "+settings)
      println("Settings.classpath is: "+settings.classpath)
      println("Settings.bootclasspath is: "+settings.bootclasspath)
      
      println("Creating actual compiler instance")
      compiler = new IMain(settings);
      println("Compiler classpath is: "+compiler.compilerClasspath)

    }    

  }

  def compile(source: String): ScalaScript = {
    this.synchronized {
      println("Trying to compile the source: "+source)
      var ret: ScalaScript = null
      var obj = compiler.compile(source)
      println("Compilation gave me: "+obj)
      if(obj.isInstanceOf[CompiledScript]) {
        println("YES, is instance of compiled script")
        val cs = obj.asInstanceOf[CompiledScript]
        val e = cs.eval(null.asInstanceOf[ScriptContext])
        println("Got evaluation result: "+e+" of type "+e.getClass())
        if(e.isInstanceOf[ScalaScript]) {
          println("YES is a ScalaScript")
          ret = e.asInstanceOf[ScalaScript]
        } else {
          println("NO, not a scalaprocessingresource")
        }
      } else {
        println("NO is not instance, but "+ret.getClass())
      }
      ret
    }
  }
  
  /// UTILITY classes
  def getPluginDir(pluginClassName: String): java.io.File = {
    val creoleFileURL = 
      Gate.getCreoleRegister().get("com.jpetrak.gate.scala.ScalaScriptPR").getXmlFileUrl()
    gate.util.Files.fileFromURL(creoleFileURL).getParentFile();    
  }
  def getJarUrls4ClassLoader(cl: ClassLoader): List[String] = {
    val theUrls: List[String] = 
      cl match {
        case cl: java.net.URLClassLoader => 
          cl.getURLs.toList.
          map { _.toString }.
          filter { _.startsWith("file:") }.
          filter { _.endsWith(".jar") }.
          map { new java.net.URL(_) }.
          map { gate.util.Files.fileFromURL(_).getCanonicalPath } 
        case _ => List[String]()
      }
    theUrls
  }

  def getJarFileNames4pluginDir(dirname: String): List[String] = {
    val dir = new File(dirname)
    var list = dir.listFiles.toList.filter(
      f => f.toString.toLowerCase.endsWith(".jar")).map( _.getCanonicalPath )
    val libDir = new java.io.File(dir,"lib")
    if(libDir.exists) {
      list ++= libDir.listFiles.toList.filter( 
        f => f.toString.toLowerCase.endsWith(".jar")).map( _.getCanonicalPath )      
    }
    list
  }
  
  
  
  
}
