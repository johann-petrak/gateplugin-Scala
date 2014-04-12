package at.ofai.gate.scala;

import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import gate.Gate;
import gate.GateConstants;
import gate.Resource;
import gate.creole.AbstractResource;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.AutoInstance;
import gate.creole.metadata.CreoleResource;
import gate.gui.ActionsPublisher;
import gate.gui.MainFrame;
import gate.persist.PersistenceException;
import gate.util.GateException;
import gate.util.GateRuntimeException;
import gate.util.persistence.PersistenceManager;

@CreoleResource(
    name = "Scala Support for GATE", 
    isPrivate = true, 
    tool = true,
    autoinstances = @AutoInstance)
public class ScalaTool extends AbstractResource implements ActionsPublisher {
  
  public Resource init() throws ResourceInstantiationException {
    //try {
    //  PersistenceManager.registerPersistentEquivalent(ScalaObjectController.class,
    //      ScalaObjectControllerPersistence.class);
    //}
    //catch(PersistenceException e) {
    //  throw new ResourceInstantiationException(e);
    //}    
    return this;
  }
  
  private List<Action> actions;
  
  public List<Action> getActions() {
    if(actions == null) {
      actions = new ArrayList<Action>();
      // Action 11
      actions.add(
          new AbstractAction("Scala/Java Plugin Generator", MainFrame.getIcon("/scala-icon")) 
          {
            {
              putValue(SHORT_DESCRIPTION, 
                  "Generate a plugin directory for a mixed Scala/Java plugin");
              putValue(GateConstants.MENU_PATH_KEY, 
                  new String[] {"Scala"});            
            }
        private static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent evt) {
          System.out.println("Should now start the Plugin Generator");
        }
      });
      actions.add(
          new AbstractAction("Scala Interpreter", MainFrame.getIcon("/scala-icon")) 
          {
            {
              putValue(SHORT_DESCRIPTION, "Run Scala interactively");
              putValue(GateConstants.MENU_PATH_KEY, 
                  new String[] {"Scala"});            
            }
        private static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent evt) {
          System.out.println("Should now start the interactive Interpreter");
        }
      });
      actions.add(
          new AbstractAction("Scala Editor/Runner", MainFrame.getIcon("/scala-icon")) 
          {
            {
              putValue(SHORT_DESCRIPTION, "Edit a Scala script or object and run it");
              putValue(GateConstants.MENU_PATH_KEY, 
                  new String[] {"Scala"});            
            }
        private static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent evt) {
          System.out.println("Should now start the editor/runner");
        }
      });
    }
    return actions;
  }
  
  

}
