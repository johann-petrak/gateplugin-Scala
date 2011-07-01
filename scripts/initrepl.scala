import gate._
import java.io._

def startGate(configPrefix: String = ""): Unit = {
  if(configPrefix != "") {
    val curdir = new File(".").getCanonicalPath() 
    val sessionFileName = 
      new File(".",configPrefix + ".session").getCanonicalPath()
    val configFileName = 
      new File(".",configPrefix + ".xml").getCanonicalPath()
    System.setProperty("gate.user.session",sessionFileName)
    System.setProperty("gate.user.config",configFileName)
    System.setProperty("gate.user.filechooser",curdir)
    println("Session File:   "+sessionFileName)
    println("Config File:    "+configFileName)
    println("Filechooser:    "+curdir)
  }
  Gate.init()
}
println("startGate([configPrefix]) defined")

def startGUI(): Unit = {
  gate.gui.MainFrame.getInstance.setVisible(true)
}
println("startGUI defined")


// More involved from Ian: not sure what the purpose is:
def startGUI2(): Unit = {
  object R1 extends Runnable {
    def run(): Unit = {
      gate.Main.applyUserPreferences();
      gate.gui.MainFrame.getInstance().setVisible(true);
    }
  }
  javax.swing.SwingUtilities.invokeAndWait(R1)
}
println("startGUI2 defined")

def getResources(cn: String = "gate.Resource"): List[Resource] = {
  var l = List[Resource]()
  val i = Gate.getCreoleRegister().getAllInstances(cn).iterator;
  while(i.hasNext()) {
    l = i.next.asInstanceOf[Resource] :: l
  }
  l 
}
println("getResources([resourceclass]) defined")

def getResource(name: String, cn: String = "gate.Resource"): Resource = {
  var l = getResources(cn).filter { _.getName == name }
  if(l.isEmpty) {
    return null
  } else {
    l.foreach { (x: Resource) => System.out.print( x.getName +" ") }
    System.out.println()
    return l.head
  }
}
println("getResource(name,[resourceclass]) defined")


def getDocument(name: String): Document = {
  return getResource(name, "gate.Document").asInstanceOf[Document]
}
println("getDocument(name) defined")

def getCorpus(name: String): Corpus = {
  return getResource(name, "gate.Corpus").asInstanceOf[Corpus]
}
println("getCorpus(name) defined")

def getController(name: String): Controller = {
  return getResource(name, "gate.Controller").asInstanceOf[Controller]
}
println("getController(name) defined")

def getDataStore(name: String): DataStore = {
  return getResource(name, "gate.DataStore").asInstanceOf[gate.DataStore]
}
println("getDataStore(name) defined")

def resourceNames(cn: String = "gate.Resource"): List[String] = 
  getResources(cn).map { _.getName }
println("resourceNames([resourceclass]) defined")

def documentNames(): List[String] =
  getResources("gate.Document").map { _.getName }
println("documentNames() defined")

def corpusNames(): List[String] =
  getResources("gate.Corpus").map { _.getName }
println("corpusNames() defined")

def newDocument(name: String, content: String = ""): Document = {
  // TODO: set initial content!
  val parms = Factory.newFeatureMap
  val feats = Factory.newFeatureMap
  return Factory.
    createResource("gate.corpora.DocumentImpl",parms,feats,name).
      asInstanceOf[Document]
}
println("newDocument(name, [content]) defined")

def loadDocument(location: String): Document = {
  var u: java.net.URL = null
  if(location.startsWith("http://") || location.startsWith("file:") ) {
    u = new java.net.URL(location)
  } else {
    u = new File(location).getCanonicalFile().toURI.toURL
  }
  return Factory.newDocument(u)
}
println("loadDocument(locationOrURL) defined")

def registerCurDir(): Unit = {
  Gate.getCreoleRegister().registerDirectories(
    new File(".").getCanonicalFile().toURI().toURL() )
}
println("registerCurDir() defined")

def registerGatePlugin(name: String): Unit = {
  Gate.getCreoleRegister().registerDirectories(
    new File(Gate.getPluginsHome(), name).getCanonicalFile().toURI().toURL() )
}
println("registerGatePlugin(pluginname) defined")

def testCurDir(): Unit = {
  startGate()
  registerCurDir() 
}
println("testCurDir() defined") 
