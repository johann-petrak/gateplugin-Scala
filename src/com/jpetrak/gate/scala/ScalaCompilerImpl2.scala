/*
 * Copyright (c) 2010- Austrian Research Institute for Artificial Intelligence (OFAI). 
 * Copyright (C) 2014-2016 The University of Sheffield.
 *
 * This file is part of gateplugin-ModularPipelines
 * (see https://github.com/johann-petrak/gateplugin-ModularPipelines)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software. If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpetrak.gate.scala

import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.IMain
import gate.util.GateClassLoader
import scala.reflect.internal.util.BatchSourceFile;
import scala.tools.nsc.Settings;
import scala.tools.nsc.reporters.ConsoleReporter;
import scala.tools.reflect.ReflectGlobal;
import org.apache.commons.io.FileUtils


// 
// TODO:
// = find the best time to abandon the old gate classloader
// = find the best time to remove the class files from disk
// = make sure there is no race condition when creating the temporary dir
/**
 * Compiler implementation based on the standard ReflectGlobal class.
 * 
 * This uses the ReflectGlobal class to compile the script. 
 * The advantages of this approach are:
 * <ul>
 * <li>The compiler can be made to use a classloader instead of all the JAR
 * files
 * <li>We can use our own classloader to load the class
 * </ul>
 * 
 * The disadvantages are:
 * <ul>
 * <li>Not much to find about this approach anywhere on the internet, we do not
 * know if it has a future
 * <li>do not know if that compiler has other limitations
 * </ul>
 */
class ScalaCompilerImpl2 extends ScalaCompiler {
  
  var compiler: IMain = null 
  
  def init() = {
    this.synchronized {
      println("Creating Scala compiler instance ReflectGlobal")
      //println("Creating settings instance");
      // the ReflectGlobal compiler cannot seem to be re-used, so we do nothing
      // here and we create a new compiler instance each time we compile
    }
  }

  def getClassEpilog() = "}"
  
  def compile(name: String, source: String, classloader: GateClassLoader): ScalaScript = {
    this.synchronized {
      var classDir = java.nio.file.Files.createTempDirectory("gate-scala").toFile()
      //println("Trying to compile the source: "+source)
      val settings = new Settings();
      settings.outdir.tryToSet(List(classDir.getAbsolutePath()))
      //settings.debug.tryToSet(List("true"))
      //settings.verbose.tryToSet(List("true"))
      val rg = new ReflectGlobal(settings, new ConsoleReporter(settings), classloader)
      val run = new rg.Run()
      val sf = new BatchSourceFile(name,source)
      run.compileSources(List(sf))
      //println("Trying to load class: "+name)
      classloader.addURL(classDir.toURI.toURL)
      val c =  classloader.loadClass(name)
      // TODO: not sure if we can actually do this here already, but why not?
      FileUtils.deleteDirectory(classDir);
      //println("Loaded the class: "+c)      
      val ret: ScalaScript = c.newInstance().asInstanceOf[ScalaScript]
      ret
    }
  }
  
}
