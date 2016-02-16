package de.mpg.mpiinf.csb.kpmcytoplugin.interfaces;

import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;

/**
 * Interface for communication and registering services with the CyActivator
 * An instance of this activator can be found in the CyGlobals class.
 * @author Martin
 *
 */
public interface IActivator {
	
	/**
	 * Registers the panel component, without and action
	 * @param comp
	 */
	///public void registerCytoPanelComponent(CytoPanelComponent comp);
	
	/**
	 * Registers the panel component, with an action. Used mainly for registering a component, 
	 * and an action for showing the component
	 * @param comp
	 */
	public void registerAndFocusCytoPanel(CytoPanelComponent comp);
	
	/**
	 * Returns the CytoPanel with the given name.
	 * @param panelName
	 * @return
	 */
	public CytoPanel getCytoPanel(CytoPanelName panelName);
}
