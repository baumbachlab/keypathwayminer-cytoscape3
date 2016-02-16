/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mpg.mpiinf.csb.kpmcytoplugin.gui.actions;

import de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels.KPMMainPanel;
import java.awt.event.ActionEvent;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;

/**
 *
 * @author nalcaraz
 */
public class AddKPMMainPanelAction extends AbstractCyAction {

    private final CytoPanel cytoPanelWest;
    private final KPMMainPanel kpmmp;

    public AddKPMMainPanelAction(CySwingApplication desktopApp, KPMMainPanel kpmmp) {
        super("KeyPathwayMiner");
        setPreferredMenu("Apps");

        CySwingApplication desktopApp1 = desktopApp;

        this.cytoPanelWest = desktopApp1.getCytoPanel(CytoPanelName.WEST);
        this.kpmmp = kpmmp;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // If the state of the cytoPanelWest is HIDE, show it
        if (cytoPanelWest.getState() == CytoPanelState.HIDE) {
            cytoPanelWest.setState(CytoPanelState.DOCK);
        }

        // Select my panel
        int index = cytoPanelWest.indexOfComponent(kpmmp);
        if (index == -1) {
            return;
        }
        cytoPanelWest.setSelectedIndex(index);
    }
}
