package at.ofai.gate.scala

import gate._

object Utils {
  // this returns a list of strings, where each string is the 
  // name of a jar file. At the moment this function first 
  // finds the root directory of the plugin, then returns the 
  // full path names of all files with extension "jar" that are 
  // found in the root directory and the lib subdirectory 
  // TODO: recurse inside the lib directory?
  def getJarFileNames4Plugin(pluginClassName: String): List[String] = {
    val pluginDir = getPluginDir(pluginClassName)
    var list = pluginDir.listFiles.toList.filter(
        f => f.toString.toLowerCase.endsWith(".jar")).map( _.getCanonicalPath )
    val libDir = new java.io.File(pluginDir,"lib")
    if(libDir.exists) {
      list ++= libDir.listFiles.toList.filter( 
        f => f.toString.toLowerCase.endsWith(".jar")).map( _.getCanonicalPath )      
    }
    list
  }
  
  def getPluginDir(pluginClassName: String): java.io.File = {
    val creoleFileURL = 
      Gate.getCreoleRegister().get(pluginClassName).getXmlFileUrl()
    gate.util.Files.fileFromURL(creoleFileURL).getParentFile();    
  }
  
  def getJarUrls4ClassLoader(cl: ClassLoader): List[String] = {
    lazy val theUrls: List[String] = 
      java.lang.Thread.currentThread.getContextClassLoader match {
      case cl: java.net.URLClassLoader => cl.getURLs.toList.map { _.toString }
      case _ => List[String]()
    }
    theUrls
  }
    
}