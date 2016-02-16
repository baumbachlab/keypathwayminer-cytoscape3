package de.mpg.mpiinf.csb.kpmcytoplugin.kpmhandlers;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

import de.mpg.mpiinf.csb.kpmcytoplugin.CyGlobals;
import de.mpg.mpiinf.csb.kpmcytoplugin.interfaces.IGraphAttributesHandler;
import de.mpg.mpiinf.csb.kpmcytoplugin.util.CytoscapeFieldNames;
import de.mpg.mpiinf.csb.kpmcytoplugin.util.KPMUtilities;
import dk.sdu.kpm.KPMSettings;
import dk.sdu.kpm.RunStats;
import dk.sdu.kpm.graph.GeneEdge;

public class GraphAttributesHandler implements IGraphAttributesHandler {

    private int totalPathways;

    @Override
    public void update(KPMSettings settings) {
        totalPathways = 0;
        if (settings == null) {
            return;
        }
        if (settings.STATS_MAP != null && !settings.STATS_MAP.isEmpty()) {
            for (RunStats rs: settings.STATS_MAP.values()) {
                totalPathways += rs.getNumPathways();
            }
        } else if (settings.STATS_MAP_PER != null && !settings.STATS_MAP_PER.isEmpty()) {
            for (RunStats rs: settings.STATS_MAP_PER.values()) {
                totalPathways += rs.getNumPathways();
            }
        }
        CyTable nodeTable = CyGlobals.WORKING_GRAPH.getDefaultNodeTable();

        CyTable edgeTable = CyGlobals.WORKING_GRAPH.getDefaultEdgeTable();

        List<CyNode> cyNodes = KPMUtilities.getCyNodes(CyGlobals.WORKING_GRAPH, settings.MAIN_GRAPH.getNodeIdSet());

        if (nodeTable.getColumn(CytoscapeFieldNames.NODE_TOTAL_HITS_PROPERTY_NAME)
                == null) {
            nodeTable.createColumn(CytoscapeFieldNames.NODE_TOTAL_HITS_PROPERTY_NAME,
                    Integer.class, false);
        }

        if (nodeTable.getColumn(CytoscapeFieldNames.NODE_TOTAL_HITS_NORMALIZED_PROPERTY_NAME)
                == null) {
            nodeTable.createColumn(CytoscapeFieldNames.NODE_TOTAL_HITS_NORMALIZED_PROPERTY_NAME,
                    Double.class, false);
        }

        if (edgeTable.getColumn(CytoscapeFieldNames.EDGE_TOTAL_HITS_PROPERTY_NAME)
                == null) {
            edgeTable.createColumn(CytoscapeFieldNames.EDGE_TOTAL_HITS_PROPERTY_NAME,
                    Integer.class, false);
        }

        if (edgeTable.getColumn(CytoscapeFieldNames.EDGE_TOTAL_HITS_NORMALIZED_PROPERTY_NAME)
                == null) {
            edgeTable.createColumn(CytoscapeFieldNames.EDGE_TOTAL_HITS_NORMALIZED_PROPERTY_NAME,
                    Double.class, false);
        }

        if (edgeTable.getColumn(CytoscapeFieldNames.EDGE_WIDTH_BY_TOTAL_HITS_PROPERTY_NAME)
                == null) {
            edgeTable.createColumn(CytoscapeFieldNames.EDGE_WIDTH_BY_TOTAL_HITS_PROPERTY_NAME,
                    Double.class, false);
        }

        for (CyNode cyNode : cyNodes) {
            CyRow nodeRow = nodeTable.getRow(cyNode.getSUID());
            String nodeId = CyGlobals.WORKING_GRAPH.getRow(cyNode).get(CyNetwork.NAME, String.class);
            int nodeTotalHits = 0;
            if (settings.TOTAL_NODE_HITS != null && settings.TOTAL_NODE_HITS.containsKey(nodeId)) {
                nodeTotalHits = settings.TOTAL_NODE_HITS.get(nodeId);
            }

            nodeRow.set(CytoscapeFieldNames.NODE_TOTAL_HITS_PROPERTY_NAME,
                    nodeTotalHits);
            double totalHitsP = 0.0;
            if (totalPathways > 0) {
                totalHitsP = (double) nodeTotalHits / (double) totalPathways;
            }
            nodeRow.set(CytoscapeFieldNames.NODE_TOTAL_HITS_NORMALIZED_PROPERTY_NAME, totalHitsP);
        }

        HashMap<String, CyRow> edgeRowMap = CyGlobals.getEdgeRowMap();

        if (settings.MAIN_GRAPH == null) {
            return;
        }

        for (GeneEdge edge : settings.MAIN_GRAPH.getEdges()) {
            String edgeId = edge.getEdgeId();
            if (edgeRowMap.containsKey(edgeId)) {
                CyRow edgeRow = edgeRowMap.get(edgeId);

                int edgeHits = 0;
                if (settings.TOTAL_EDGE_HITS != null && settings.TOTAL_EDGE_HITS.containsKey(edgeId)) {
                    edgeHits = settings.TOTAL_EDGE_HITS.get(edgeId);
                }

                edgeRow.set(CytoscapeFieldNames.EDGE_TOTAL_HITS_PROPERTY_NAME, edgeHits);
                double totalHitsNorm = 0.0;
                if (totalPathways > 0) {
                    totalHitsNorm = (double) settings.TOTAL_EDGE_HITS.get(edgeId) / (double) totalPathways;
                }
                edgeRow.set(CytoscapeFieldNames.EDGE_TOTAL_HITS_NORMALIZED_PROPERTY_NAME,
                        totalHitsNorm);
                double width = totalHitsNorm * 10.0;
                if (settings.TOTAL_EDGE_HITS != null && settings.TOTAL_EDGE_HITS.containsKey(edgeId) && settings.TOTAL_EDGE_HITS.get(edgeId) == 0) {
                    width = 1.0;
                }
                edgeRow.set(CytoscapeFieldNames.EDGE_WIDTH_BY_TOTAL_HITS_PROPERTY_NAME, width);
            }
        }
    }

}
