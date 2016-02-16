package de.mpg.mpiinf.csb.kpmcytoplugin.kpmhandlers;

import de.mpg.mpiinf.csb.kpmcytoplugin.CyGlobals;
import de.mpg.mpiinf.csb.kpmcytoplugin.gui.charts.ChartsPanel;
import dk.sdu.kpm.charts.IChart;
import dk.sdu.kpm.logging.KpmLogger;
import dk.sdu.kpm.results.IKPMResultSet;
import dk.sdu.kpm.utils.DialogUtils;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * Created by Martin on 17-10-2014.
 */
class KPMResultSetHandler extends AbstractTask {
    private volatile IKPMResultSet results;

    public KPMResultSetHandler(IKPMResultSet resultSet){
        this.results = resultSet;
    }

    @Override
    public void run(TaskMonitor taskMonitor) {
        if(results.getKpmSettings() == null){
            DialogUtils.showNonModalDialog("No setttings found.", "KPM warning", DialogUtils.MessageTypes.Warn);
            return;
        }

        taskMonitor.setTitle("Extracting results");
        taskMonitor.setStatusMessage("Charts");

        try {
            if (CyGlobals.ChartPanel != null) {
                if(!CyGlobals.ChartPanel.isVisible()){
                    CyGlobals.ChartPanel = new ChartsPanel();
                    CyGlobals.ACTIVATOR.registerAndFocusCytoPanel(CyGlobals.ChartPanel);
                }

                CyGlobals.ChartPanel.clearCharts();
            }else{
                CyGlobals.ChartPanel = new ChartsPanel();
                CyGlobals.ACTIVATOR.registerAndFocusCytoPanel(CyGlobals.ChartPanel);
            }


        CyGlobals.ChartPanel.clearCharts();
        if(results.getCharts() != null && results.getCharts().size() > 0) {
            ArrayList<IChart> charts = new ArrayList<IChart>(results.getCharts().values());
            CyGlobals.ChartPanel.addCharts(charts);
        }else{
            CyGlobals.ChartPanel.setNoChartsTab();
        }
        }catch(Exception e){
            System.out.println("error 1: \n" + e);
            KpmLogger.log(Level.SEVERE, e);
        }

        taskMonitor.setProgress(0.3);
        taskMonitor.setStatusMessage("Graph");
        try {
            CyGlobals.GraphAttributeHandler.update(results.getKpmSettings());
        }catch(Exception e){
            System.out.println("error 2: \n" + e);
            KpmLogger.log(Level.SEVERE, e);
        }
        taskMonitor.setProgress(0.7);
        taskMonitor.setStatusMessage("Results panel");

        HashMap<Integer, String> l_map = new HashMap<Integer, String>();
        try {
        if(results.getKpmSettings() != null && results.getKpmSettings().INDEX_L_MAP != null){
            l_map =results.getKpmSettings().INDEX_L_MAP;
        }

        boolean useINES = false;
        if(CyGlobals.KPM != null){
            useINES = CyGlobals.KPM.USE_INES;
        }

        if(CyGlobals.ResultsPanelHandler != null) {
//            int test = results.getKpmSettings().STATS_MAP.values().iterator().next().getResults().get(0).getFitness();
//            System.out.println("RESULTS LARGEST FROM STATS MAP AT KPMResultsSetHandler: " + test);
//            System.out.println("CALCULATE_ONLY_SAME_L_VALUES AT KPMResultsSetsHandler= " + results.getKpmSettings().CALCULATE_ONLY_SAME_L_VALUES);

            CyGlobals.ResultsPanelHandler.addBatchResultsPanel(results, l_map, useINES);
        }else{
            DialogUtils.showNonModalDialog("No Results panel handler", "", DialogUtils.MessageTypes.Warn);
        }
        }catch(Exception e){
            System.out.println("error 3: \n" + e);
            KpmLogger.log(Level.SEVERE, e);
        }
        taskMonitor.setProgress(1);
    }
}
