/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mpg.mpiinf.csb.kpmcytoplugin.event;

import de.mpg.mpiinf.csb.kpmcytoplugin.CyGlobals;
import de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels.KPMParameterTab;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;

/**
 *
 * @author nalcaraz
 */
public class KPMEventListener implements NetworkAddedListener, NetworkAboutToBeDestroyedListener,
        ACOVersionSelectedListener {

    private final KPMParameterTab parameterTab;

    public KPMEventListener(KPMParameterTab parameterTab) {
        this.parameterTab = parameterTab;
    }

    @Override
    public void handleEvent(NetworkAddedEvent nae) {
        parameterTab.setGraphSelectionModel(false, nae.getNetwork().getSUID());
        CyGlobals.LAST_ADDED_NETWORK = nae.getNetwork();
    }

    @Override
    public void handleEvent(NetworkAboutToBeDestroyedEvent natbde) {
        parameterTab.setGraphSelectionModel(true, natbde.getNetwork().getSUID());
    }

    @Override
    public void handleEvent(ACOVersionSelectedEvent e) {
        parameterTab.checkEnableProcessorSlider(e.getIsIterationBased());
    }
}
