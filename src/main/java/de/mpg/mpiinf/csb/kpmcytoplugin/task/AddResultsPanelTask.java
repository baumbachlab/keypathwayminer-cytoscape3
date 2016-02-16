package de.mpg.mpiinf.csb.kpmcytoplugin.task;

import de.mpg.mpiinf.csb.kpmcytoplugin.CyGlobals;
import de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels.KPMResultsTab;
import de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels.KPMTabbedPane;
import de.mpg.mpiinf.csb.kpmcytoplugin.util.CytoscapePanelNames;
import dk.sdu.kpm.results.IKPMResultSet;
import dk.sdu.kpm.utils.DialogUtils;
import org.cytoscape.model.CyRow;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Martin on 04-12-2014.
 */
public class AddResultsPanelTask extends AbstractTask {
    /**
     * Holds the initially created KPM tabs "Data", "Help", "Links", "Pos/Neg
     * List", "Run", etc. tabs which are created after starting KPM. After
     * running KPM, "Results" tabs are added.
     */
    private KPMTabbedPane kpmTabbedPane;

    private Map<Integer, String> indexLMap;
    private boolean isInes;
    private IKPMResultSet results;
    
    public AddResultsPanelTask(KPMTabbedPane kpmTabbedPane, IKPMResultSet results, Map<Integer, String> indexLMap, boolean isInes){
        this.kpmTabbedPane = kpmTabbedPane;
        this.indexLMap = indexLMap;
        this.isInes = isInes;
        this.results = results;
        
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        taskMonitor.setTitle("Processing results-panel");
        taskMonitor.setStatusMessage("Checking if panel already exists.");
        if (CyGlobals.HAS_RESULTS_TAB) {
            int index = kpmTabbedPane.indexOfTab(CytoscapePanelNames.RESULTSNAME);

            if(index >= 0) {
                kpmTabbedPane.removeTabAt(index);
            }
        }
        taskMonitor.setProgress(0.1);
        taskMonitor.setStatusMessage("Checking if any results were found.");
        if (CyGlobals.KPM == null) {

            taskMonitor.setStatusMessage("No results found.");
            taskMonitor.setProgress(1);

            DialogUtils.showNonModalDialog("No pathways were found with the current set"
                            + " of parameter values. Please try again with different values.",
                    "No pathways found",  DialogUtils.MessageTypes.Warn);
        } else if (CyGlobals.KPM.CALCULATE_ONLY_SAME_L_VALUES && (CyGlobals.KPM.STATS_MAP_PER == null || CyGlobals.KPM.STATS_MAP_PER.isEmpty())) {
            taskMonitor.setStatusMessage("No results found.");
            DialogUtils.showNonModalDialog("No pathways were found with the current set"
                            + " of parameter values. Please try again with different values.",
                    "No pathways found",  DialogUtils.MessageTypes.Warn);
        } else if (CyGlobals.KPM.CALCULATE_ONLY_SAME_L_VALUES == false && (CyGlobals.KPM.STATS_MAP == null || CyGlobals.KPM.STATS_MAP.isEmpty())) {
            taskMonitor.setStatusMessage("No results found.");
            DialogUtils.showNonModalDialog("No pathways were found with the current set"
                            + " of parameter values. Please try again with different values.",
                    "No pathways found",  DialogUtils.MessageTypes.Warn);
        } else {
            HashMap<String, CyRow> edgeRowMap = CyGlobals.getEdgeRowMap();

            taskMonitor.setProgress(0.8);
            try {
                taskMonitor.setStatusMessage("Building results-panel.");
                KPMResultsTab expp = new KPMResultsTab(kpmTabbedPane, results, indexLMap, isInes, edgeRowMap);

                taskMonitor.setStatusMessage("Adding creating results-panel.");
                kpmTabbedPane.addTab(CytoscapePanelNames.RESULTSNAME, expp);
                int index = kpmTabbedPane.indexOfTab(CytoscapePanelNames.RESULTSNAME);
                kpmTabbedPane.setSelectedIndex(index);
                taskMonitor.setProgress(0.9);
                kpmTabbedPane.revalidate();
            }catch(Exception e){
                System.out.println("An error occurred.");
                e.printStackTrace();
            }

            CyGlobals.HAS_RESULTS_TAB = true;
            taskMonitor.setProgress(1);
        }
    }
}
