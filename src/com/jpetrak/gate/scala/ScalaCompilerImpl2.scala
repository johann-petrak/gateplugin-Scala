package com.jpetrak.gate.scala

import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.IMain
import gate.util.GateClassLoader
import scala.reflect.internal.util.BatchSourceFile;
import scala.tools.nsc.Settings;
import scala.tools.nsc.reporters.ConsoleReporter;
import scala.tools.reflect.ReflectGlobal;

// compiler implementation based on Mark's approach to use the ReflectGlobal
// class
// 
// TODO:
// = find the best time to abandon the old gate classloader
// = find the best time to remove the class files from disk
// = make sure there is no race condition when creating the temporary dir
class ScalaCompilerImpl2 extends ScalaCompiler {
  
  var compiler: IMain = null 
  
  def init() = {
    this.synchronized {
      println("Creating settings instance");
      // the ReflectGlobal compiler cannot seem to be re-used, so we do nothing
      // here and we create a new compiler instance each time we compile
    }
  }

  def compile(name: String, source: String, classloader: GateClassLoader): ScalaScript = {
    this.synchronized {
      var classDir = java.nio.file.Files.createTempDirectory("gate-scala").toFile()
      println("Trying to compile the source: "+source)
      val settings = new Settings();
      settings.outdir.tryToSet(List(classDir.getAbsolutePath()))
      //settings.debug.tryToSet(List("true"))
      //settings.verbose.tryToSet(List("true"))
      val rg = new ReflectGlobal(settings, new ConsoleReporter(settings), classloader)
      val run = new rg.Run()
      val sf = new BatchSourceFile(name,source)
      run.compileSources(List(sf))
      println("Trying to load class: "+name)
      classloader.addURL(classDir.toURI.toURL)
      val c =  classloader.loadClass(name)
      println("Loaded the class: "+c)      
      val ret: ScalaScript = c.newInstance().asInstanceOf[ScalaScript]
      ret
    }
  }
  
}
