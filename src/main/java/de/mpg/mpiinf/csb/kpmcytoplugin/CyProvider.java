package de.mpg.mpiinf.csb.kpmcytoplugin;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.swing.DialogTaskManager;

/**
 * Central class for fetching Cytoscape-related factories and managers.
 * Created by Martin on 27-10-2014.
 */
public class CyProvider {
    private CyProvider(){

    }

    public static CyNetworkManager networkManager;

    public static CyRootNetworkManager rootNetworkManager;

    public static CyNetworkViewManager networkViewManager;

    public static CyNetworkViewFactory networkViewFactory;

    public static CyApplicationManager appManager;

    public static CyLayoutAlgorithmManager layoutManager;

    public static VisualMappingManager vmmServiceRef;

    public static VisualStyleFactory visualStyleFactoryServiceRef;

    public static VisualMappingFunctionFactory vmfFactoryC;

    public static VisualMappingFunctionFactory vmfFactoryD;

    public static VisualMappingFunctionFactory vmfFactoryP;

    public static TaskManager taskManager;

    public static DialogTaskManager dialogTaskManager;

    public static FileUtil fileUtil;
}
