/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mpg.mpiinf.csb.kpmcytoplugin.kpmhandlers;

import de.mpg.mpiinf.csb.kpmcytoplugin.CyGlobals;
import de.mpg.mpiinf.csb.kpmcytoplugin.CyProvider;
import de.mpg.mpiinf.csb.kpmcytoplugin.kpmhandlers.mappers.KPMCytoscapeTaskMonitorMapper;
import de.mpg.mpiinf.csb.kpmcytoplugin.util.KPMUtilities;
import dk.sdu.kpm.results.IKPMResultSet;
import dk.sdu.kpm.results.IKPMRunListener;
import dk.sdu.kpm.runners.BatchRunner;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;


/**
 * Mapper to the BatchRunner class of KPM runs
 * @author nalcaraz / Martin Dissing-Hansen
 */
    public class BatchRunHandler extends AbstractTask implements IKPMRunListener{

        private final boolean isInes;
        private volatile BatchRunner batcher;
        private volatile TaskMonitor taskMonitor;

        public BatchRunHandler(boolean isInes) {
            this.isInes = isInes;
            int totalPathways = 0;
        }

        @Override
        public void run(TaskMonitor tm) throws Exception {
            this.taskMonitor = tm;
            String runId = CyGlobals.WORKING_GRAPH.getRow(CyGlobals.WORKING_GRAPH).get(CyNetwork.NAME, String.class);
            KPMCytoscapeTaskMonitorMapper mappedListener = new KPMCytoscapeTaskMonitorMapper(tm);
            CyGlobals.KPM.USE_INES = this.isInes;
            CyGlobals.KPM.INCLUDE_CHARTS = true;
            if(KPMUtilities.allSameForVaryingL()){
                CyGlobals.KPM.INCLUDE_CHARTS = false;
            }

//            System.out.println(CyGlobals.KPM);

            this.batcher = new BatchRunner(runId, mappedListener, this, CyGlobals.KPM);
            this.batcher.run();
        }

        @Override
        public synchronized void cancel(){
            System.out.println("Canceling run");
            this.cancelled = true;
            this.batcher.cancel();
        }

        @Override
        public void runFinished(IKPMResultSet results) {
            KPMResultSetHandler handler = new KPMResultSetHandler(results);
            CyProvider.dialogTaskManager.execute(new TaskIterator(handler));
        }

        @Override
        public void runCancelled(String reason, String runID) {
            System.out.println("Run was cancelled. Reason: " + reason);
            if(this.taskMonitor != null) {
                this.taskMonitor.setStatusMessage("Run was cancelled for reason: \n" + reason);
            }
        }


    }
