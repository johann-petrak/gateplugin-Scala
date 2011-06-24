package my.pkg.name 

import scala.reflect.BeanProperty

import gate._
import gate.creole._
import gate.creole.metadata._
import gate.util._


@CreoleResource(name = "ScalaPlugin",
        comment = "Add a descriptive comment about this resource")
class HelloWorld  
  extends AbstractProcessingResource with ProcessingResource
{
  @RunTime
  @Optional
  @CreoleParameter(
    comment="Some parameter",
    defaultValue="default"
  )
  def setParm(v: String): Unit = {
    parm = v
  }
  def getParm(): String = parm

  var parm = "Something"
  
  override def execute() {
    println("Hello World!")
    println("the parameter is "+getParm())
  }
}
