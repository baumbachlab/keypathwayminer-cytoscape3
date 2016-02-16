package de.mpg.mpiinf.csb.kpmcytoplugin.kpmhandlers;

import de.mpg.mpiinf.csb.kpmcytoplugin.CyProvider;
import dk.sdu.kpm.utils.DialogUtils;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

import de.mpg.mpiinf.csb.kpmcytoplugin.CyGlobals;
import de.mpg.mpiinf.csb.kpmcytoplugin.kpmhandlers.mappers.KPMCytoscapeTaskMonitorMapper;
import dk.sdu.kpm.perturbation.IPerturbation;
import dk.sdu.kpm.results.IKPMResultSet;
import dk.sdu.kpm.results.IKPMRunListener;
import dk.sdu.kpm.runners.BatchRunWithPerturbationParameters;
import dk.sdu.kpm.runners.BatchRunWithPerturbationRunner;


/**
 * Class for running perturbation robustness runs of KPM.
 *
 * @author Martin
 *
 */
public class BatchRunWithPerturbationHandler extends AbstractTask implements IKPMRunListener {

    private final IPerturbation permuter;
    private final int minPercentage;
    private final int stepPercentage;
    private final int maxPercentage;
    private final int graphsPerStep;
    private final boolean isINEs;
    private final String runId;
    private volatile TaskMonitor taskMonitor;

    private volatile BatchRunWithPerturbationRunner runner;

    public BatchRunWithPerturbationHandler(
            IPerturbation permuter,
            int minPercentage,
            int stepPercentage,
            int maxPercentage,
            int graphsPerStep,
            boolean isINEs,
            String runId) {
        this.permuter = permuter;
        this.minPercentage = minPercentage;
        this.stepPercentage = stepPercentage;
        this.maxPercentage = maxPercentage;
        this.graphsPerStep = graphsPerStep;
        this.isINEs = isINEs;
        this.runId = runId;
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        this.taskMonitor = taskMonitor;

        if (this.minPercentage < 0
                || this.maxPercentage < 0
                || this.minPercentage > this.maxPercentage
                || this.stepPercentage < 0) {
            taskMonitor.setProgress(1);
            taskMonitor.setStatusMessage("Error in input");
            taskMonitor.setTitle("KPM run failed");
            DialogUtils.showNonModalDialog("Wrong perturbation parameters.\nInvalid percentages.", "Could not start run", DialogUtils.MessageTypes.Error);
            return;
        }

        if (this.graphsPerStep < 1) {
            taskMonitor.setProgress(1);
            taskMonitor.setStatusMessage("Error in input");
            taskMonitor.setTitle("KPM run failed");
            DialogUtils.showNonModalDialog("Wrong perturbation parameters.\nGraphs per step must be greater than 0.", "Could not start run", DialogUtils.MessageTypes.Error);
        }

//        System.out.println(CyGlobals.KPM);
        KPMCytoscapeTaskMonitorMapper mappedListener = new KPMCytoscapeTaskMonitorMapper(taskMonitor);
        BatchRunWithPerturbationParameters bprp = new BatchRunWithPerturbationParameters(
                this.permuter,
                this.minPercentage,
                this.stepPercentage,
                this.maxPercentage,
                this.graphsPerStep,
                this.isINEs,
                this.runId,
                mappedListener,
                this,
                true); // this will include the standard graphs

        CyGlobals.KPM.INCLUDE_CHARTS = true;
        runner = new BatchRunWithPerturbationRunner(bprp, CyGlobals.KPM);
        runner.run();
    }

    @Override
    public synchronized void cancel() {
        System.out.println("Canceling robustness run");
        this.cancelled = true;
        this.runner.cancel();
    }

    @Override
    public void runFinished(IKPMResultSet results) {
//        System.out.println("RESULTS LARGEST BATCHRUNPERTUBATIONHANDLER: " + results.getResults().get(0).getAllComputedNodeSets().iterator().next().size());
//        int test = results.getKpmSettings().STATS_MAP.values().iterator().next().getResults().get(0).getFitness();
//        System.out.println("RESULTS LARGEST FROM STATS MAP AT BATCHRUNPERTUBATIONHANDLER: " + test);
//        CyGlobals.KPM = results.getKpmSettings();
 
        KPMResultSetHandler handler = new KPMResultSetHandler(results);
        CyProvider.dialogTaskManager.execute(new TaskIterator(handler));
    }

    @Override
    public void runCancelled(String reason, String runID) {
        System.out.println("Run was cancelled. Reason: " + reason);
        if (this.taskMonitor != null) {
            this.taskMonitor.setStatusMessage("Run was cancelled for reason: \n" + reason);
        }
    }
}
