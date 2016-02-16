package de.mpg.mpiinf.csb.kpmcytoplugin;

import de.mpg.mpiinf.csb.kpmcytoplugin.gui.charts.ChartsPanel;
import de.mpg.mpiinf.csb.kpmcytoplugin.interfaces.*;
import de.mpg.mpiinf.csb.kpmcytoplugin.util.LValueMapper;
import dk.sdu.kpm.KPMSettings;
import dk.sdu.kpm.graph.GeneEdge;
import dk.sdu.kpm.statistics.Pair;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

import java.util.*;

public class CyGlobals {

	private CyGlobals() {
	}

    public static final String COMMON_L_PANEL_NAME = "COMMON_L_PANEL";

    public static volatile CyNetwork LAST_ADDED_NETWORK;

    // Interaction type for cytoscape network
    public static String INTERACTION_TYPE = "pp";

    // Main CyNetwork id
    public static String MAIN_CYNETWORK_ID = "ID";


    // The current working cytoscape graph
    public static volatile CyNetwork WORKING_GRAPH;


    // If there is a result tab already
    public static boolean HAS_RESULTS_TAB = false;

    public static final String CYNODE_KEY_COL_NAME = "name";
    
    // Maps the current networks title's to their id's (cytoscape)
    public static final Map<String, Long> TITLE_TO_NETWORK_ID_MAP = new HashMap<String, Long>();
    
	// Interface for communication and registering services with the CyActivator
    public static volatile IActivator ACTIVATOR;
    
    public static final List<ISearchEnabledListener> SearchEnabledListeners = new ArrayList<ISearchEnabledListener>();
    
    public static volatile KPMSettings KPM = new KPMSettings();

    public volatile static boolean SAME_L_VALUES = false;

    public static volatile IRobustnessSettings RobustnessSettings;

    public static volatile RunTypeEnum RunType = RunTypeEnum.Normal;

    public static volatile IGraphAttributesHandler GraphAttributeHandler;
    
    public static volatile IResultsPanelHandler ResultsPanelHandler;

    public enum RunTypeEnum {
        Validation, Robustness, Normal
    }

    public static volatile ChartsPanel ChartPanel;

    private static HashMap<String, CyRow> EdgeRowMap;

    public static HashMap<String, Boolean> L_InPercentageMap = new HashMap<String, Boolean>();

    public static final LValueMapper L_OriginalValueMapper = new LValueMapper();

    public static HashMap<String, Integer> L_DatasetFileSizeMap = new HashMap<String, Integer>();

    public static HashMap<String, Integer> L_DatasetColumnSizeMap = new HashMap<String, Integer>();


    public static synchronized void ClearEdgeRowMap(){
        EdgeRowMap = null;
    }

    public static synchronized HashMap<String, CyRow> getEdgeRowMap(){
        if(EdgeRowMap != null){
            return EdgeRowMap;
        }

        HashMap<String, CyRow> edgeRowMap = new HashMap<String, CyRow>();

        CyTable edgeTable = CyGlobals.WORKING_GRAPH.getDefaultEdgeTable();
        Collection<GeneEdge> edges = CyGlobals.KPM.MAIN_GRAPH.getEdges();
        for (GeneEdge edge : edges) {
            String edgeId = edge.getEdgeId();

            if(edgeRowMap.containsKey(edgeId)){
                continue;
            }

            String edgeName = CyGlobals.KPM.EDGE_ID_MAP.get(edgeId);
            Collection<CyRow> matches = edgeTable.getMatchingRows(CyNetwork.NAME, edgeName);
            if (matches.size() == 1) {
                CyRow edgeRow = matches.iterator().next();
                edgeRowMap.put(edgeId, edgeRow);
            }
        }

        EdgeRowMap = edgeRowMap;
        return edgeRowMap;
    }
}
