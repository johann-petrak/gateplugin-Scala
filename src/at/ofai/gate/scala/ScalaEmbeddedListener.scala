/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.ofai.gate.scala

// A trait for clients that want to listen to ScalaEmbedded events: compile,
// interpret, bind etc.
// 
trait ScalaEmbeddedListener {
  def compiled(source: java.io.File, result: ScalaEmbeddedResult): Unit = { }
  def interpreted(content: String, source: AnyRef, result: ScalaEmbeddedResult) = { }
  def bound(name: String, typename: String, value: Any, result: ScalaEmbeddedResult)
}
