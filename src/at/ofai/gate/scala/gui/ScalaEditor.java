/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ofai.gate.scala.gui;

import gate.Resource;
import gate.creole.AbstractVisualResource;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.GuiType;
import gate.event.ProgressListener;
import gate.gui.MainFrame;
import gate.util.Files;
import gate.util.GateRuntimeException;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


/**
 *
 * @author johann
 */
@CreoleResource(
  name = "Scala Editor", 
  comment = "Editor for Scala objects and scripts", 
  guiType = GuiType.LARGE, 
  mainViewer = true, 
  resourceDisplayed = "at.ofai.gate.scala.ScalaCodeDriven")
public class ScalaEditor extends AbstractVisualResource implements
                                                          ProgressListener,
                                                          DocumentListener {
  
}
