/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mpg.mpiinf.csb.kpmcytoplugin.task;

import java.util.Collection;
import java.util.List;

import de.mpg.mpiinf.csb.kpmcytoplugin.CyProvider;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

/**
 *
 * @author nalcaraz
 */
public class KPMCreateSelectedPathwayTask extends AbstractKPMCreatePathwayTask {
    
    private final String newNetworkTitle;
    private final CyNetwork parentNetwork;
    private final VisualStyle vs;

    public KPMCreateSelectedPathwayTask(final String newNetworkTitle,
            final CyNetwork parentNetwork,
            final VisualStyle vs) {
        super();
        this.newNetworkTitle = newNetworkTitle;
        this.parentNetwork = parentNetwork;
        this.vs = vs;
    }

    @Override
    public void run(TaskMonitor tm) throws Exception {
        if (parentNetwork == null) {
            throw new NullPointerException("Source network is null.");
        }
        tm.setProgress(0.0);

        final Collection<CyNetworkView> views = CyProvider.networkViewManager.getNetworkViews(parentNetwork);
        CyNetworkView sourceView = null;
        if (views.size() != 0) {
            sourceView = views.iterator().next();
        }

        tm.setProgress(0.1);

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
//            if (groupMgr.isGroup(node, parentNetwork)) {
//                CyGroup group = groupMgr.getGroup(node, parentNetwork);
//                
//               GroupUtils.addGroupToNetwork(group, parentNetwork, newNet);
//            }
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

        CyProvider.networkManager.addNetwork(newNet);
        tm.setProgress(0.6);

        // create the view in a separate task
        CyNetworkView newView  = CyProvider.networkViewFactory.createNetworkView(newNet);
        CyProvider.networkViewManager.addNetworkView(newView);
        vs.apply(newView);
        CyLayoutAlgorithm layout = CyProvider.layoutManager.getLayout("force-directed");
        TaskIterator layoutTaskIterator = layout.createTaskIterator(
                newView, layout.createLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS, 
                null);
        insertTasksAfterCurrentTask(layoutTaskIterator);
        tm.setProgress(1.0);
    }
}
