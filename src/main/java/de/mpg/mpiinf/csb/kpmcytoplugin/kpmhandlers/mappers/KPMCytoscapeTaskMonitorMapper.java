package de.mpg.mpiinf.csb.kpmcytoplugin.kpmhandlers.mappers;


import org.cytoscape.work.TaskMonitor;

import dk.sdu.kpm.taskmonitors.IKPMTaskMonitor;

/**
 * Created by: Martin
 * Date: 19-02-14
 */
public class KPMCytoscapeTaskMonitorMapper implements IKPMTaskMonitor{
    private final TaskMonitor taskMonitor;

    public KPMCytoscapeTaskMonitorMapper(TaskMonitor taskMonitorParam){
        this.taskMonitor = taskMonitorParam;
    }

    @Override
    public void setTitle(String title) {
        this.taskMonitor.setTitle(title);
    }

    @Override
    public void setProgress(double progress) {
        this.taskMonitor.setProgress(progress);
    }

    @Override
    public void setStatusMessage(String statusMessage) {
        this.taskMonitor.setStatusMessage(statusMessage);
    }
}
