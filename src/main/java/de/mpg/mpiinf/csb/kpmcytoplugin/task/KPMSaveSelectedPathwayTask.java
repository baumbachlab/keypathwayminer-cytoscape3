/*
  * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mpg.mpiinf.csb.kpmcytoplugin.task;


import de.mpg.mpiinf.csb.kpmcytoplugin.CyGlobals;
import de.mpg.mpiinf.csb.kpmcytoplugin.CyProvider;
import de.mpg.mpiinf.csb.kpmcytoplugin.util.SifWriter;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.work.TaskMonitor;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 *
 * @author nalcaraz
 */
public class KPMSaveSelectedPathwayTask extends AbstractKPMCreatePathwayTask {

    private final File file;
    private final String newNetworkTitle;
    private final CyNetwork parentNetwork;


    public KPMSaveSelectedPathwayTask(final File file,
            final String newNetworkTitle,
            final CyNetwork parentNetwork) {
        super();
        this.file = file;
        this.newNetworkTitle = newNetworkTitle;
        this.parentNetwork = parentNetwork;
    }

    @Override
    public void run(TaskMonitor tm) throws Exception {
        if (parentNetwork == null) {
            throw new NullPointerException("Source network is null.");
        }
        tm.setProgress(0.0);


        // Get the selected nodes, but only create network if nodes are actually
        // selected.
        final List<CyNode> selectedNodes = CyTableUtil.getNodesInState(parentNetwork, CyNetwork.SELECTED, true);
        tm.setProgress(0.2);

        if (selectedNodes.size() <= 0) {
            throw new IllegalArgumentException("No nodes are selected.");
        }

        // create subnetwork and add selected nodes and appropriate edges
        final CySubNetwork newNet = CyProvider.rootNetworkManager.getRootNetwork(parentNetwork).addSubNetwork();

        //We need to cpy the columns to local tables, since copying them to default table will duplicate the virtual columns.
        addColumns(parentNetwork.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS), newNet.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS));
        addColumns(parentNetwork.getTable(CyEdge.class, CyNetwork.LOCAL_ATTRS), newNet.getTable(CyEdge.class, CyNetwork.LOCAL_ATTRS));
        addColumns(parentNetwork.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS), newNet.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS));

        tm.setProgress(0.3);

        for (final CyNode node : selectedNodes) {
            newNet.addNode(node);
            cloneRow(parentNetwork.getRow(node), newNet.getRow(node));
            //Set rows and edges to not selected state to avoid conflicts with table browser
            newNet.getRow(node).set(CyNetwork.SELECTED, false);
        }

        tm.setProgress(0.4);
        for (final CyEdge edge : getEdges(parentNetwork, selectedNodes)) {
            newNet.addEdge(edge);
            cloneRow(parentNetwork.getRow(edge), newNet.getRow(edge));
            //Set rows and edges to not selected state to avoid conflicts with table browser
            newNet.getRow(edge).set(CyNetwork.SELECTED, false);
        }
        tm.setProgress(0.5);


        newNet.getRow(newNet).set(CyNetwork.NAME, newNetworkTitle);
        String fileName = file.getAbsolutePath() + CyGlobals.KPM.fileSep +
                newNetworkTitle + ".sif";
        

        tm.setProgress(0.6);
        SifWriter sifWriter = 
                new SifWriter(new FileOutputStream(new File(fileName)), newNet);
        insertTasksAfterCurrentTask(sifWriter);
        tm.setProgress(1.0);
    }
    
}
