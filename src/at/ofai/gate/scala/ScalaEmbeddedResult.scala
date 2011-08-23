/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.ofai.gate.scala

// A class encapsulating the result of a compilation, interpretation or binding.
// It has a flag to indicate success or failure and a string field containing
// any error message.
class ScalaEmbeddedResult {
  var isError = false
  var message = ""
}
