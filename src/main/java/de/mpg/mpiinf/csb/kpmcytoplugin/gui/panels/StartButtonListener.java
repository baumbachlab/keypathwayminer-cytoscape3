package de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels;

import de.mpg.mpiinf.csb.kpmcytoplugin.CyGlobals;
import de.mpg.mpiinf.csb.kpmcytoplugin.CyProvider;
import de.mpg.mpiinf.csb.kpmcytoplugin.kpmhandlers.BatchRunHandler;
import de.mpg.mpiinf.csb.kpmcytoplugin.kpmhandlers.BatchRunWithPerturbationHandler;
import de.mpg.mpiinf.csb.kpmcytoplugin.kpmhandlers.KPMParameterConfigurationHandler;
import dk.sdu.kpm.KPMSettings;
import dk.sdu.kpm.utils.DialogUtils;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskIterator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Listens to invocations of the start button, which is labeled 'Search
 * Pathways' in the GUI and which is situated in KPM's 'Run' tab. Reads the
 * number of gene exceptions, number of node exceptions, the chosen algorithm
 * and ACO advanced parameter settings from the GUI and writes them to the
 * {@link KPMSettings} class. Then runs the algorithm and displays the 'Results'
 * tab.
 */
public class StartButtonListener implements ActionListener {

    /**
     * The {@link KPMMainPanel} containing the 'Search Pathways' button this
     * instance is listening to.
     */
    private final KPMMainPanel kpmmp;

    private CyGlobals.RunTypeEnum runType;

    /**
     * Creates a listener for the 'Search Pathways' button which is part of the
     * 'Run' tab of the given {@link KPMMainPanel}.
     *
     */
    public StartButtonListener(CyGlobals.RunTypeEnum runType, KPMMainPanel kpmmp) {
        this.kpmmp = kpmmp;
        this.runType = runType;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        CyGlobals.RunType = this.runType;

        KPMParameterConfigurationHandler config = new KPMParameterConfigurationHandler(kpmmp);

        if (!config.runSanityChecks()) {
            return;
        }

        KPMParameterTab pp = kpmmp.getKPMTabbedPane().getParameterPanel();

        checkConvertToPercentages();

        if (this.runType == CyGlobals.RunTypeEnum.Robustness && CyGlobals.RobustnessSettings.getRunType() == CyGlobals.RunTypeEnum.Validation) {
            List<String> validationList = CyGlobals.RobustnessSettings.getValidationList();

            if (validationList.size() > 0) {
                CyGlobals.KPM.VALIDATION_GOLDSTANDARD_NODES = validationList;
            }
        }

        if (CyGlobals.RunType == CyGlobals.RunTypeEnum.Robustness || CyGlobals.RunType == CyGlobals.RunTypeEnum.Validation) {
            runWithPerturbation(pp.isINEs());
            return;
        }

        BatchRunHandler br = new BatchRunHandler(pp.isINEs());
        CyProvider.dialogTaskManager.execute(new TaskIterator(br));

    }

    private void checkConvertToPercentages() {
        if (!CyGlobals.KPM.CALCULATE_ONLY_SAME_L_VALUES) {
            return;
        }

        System.out.println("..........");

        for (String lid : CyGlobals.KPM.CASE_EXCEPTIONS_MAP.keySet()) {
            int percentage = CyGlobals.KPM.CASE_EXCEPTIONS_MAP.get(lid);
            CyGlobals.KPM.MIN_PER = percentage;
            CyGlobals.KPM.MAX_PER = percentage;
            CyGlobals.KPM.INC_PER = 1;
            System.out.println("lid: " + lid + " (not range), MIN_PER = " + CyGlobals.KPM.MIN_PER);
            System.out.println("lid: " + lid + " (not range), INC_PER = " + CyGlobals.KPM.INC_PER);
            System.out.println("lid: " + lid + " (not range), MAX_PER = " + CyGlobals.KPM.MAX_PER);

            if (CyGlobals.KPM.IS_BATCH_RUN == false && CyGlobals.L_InPercentageMap.containsKey(lid) && CyGlobals.L_InPercentageMap.get(lid)) {
                if (!CyGlobals.L_DatasetFileSizeMap.containsKey(lid)) {
                    continue;
                }

                int setSize = CyGlobals.L_DatasetColumnSizeMap.get(lid);
                int percentageVal = (int) Math.ceil(((double) setSize / (double) 100) * percentage);
                CyGlobals.KPM.CASE_EXCEPTIONS_MAP.put(lid, percentageVal);
            }
        }

        if (CyGlobals.KPM.IS_BATCH_RUN || !CyGlobals.KPM.CALCULATE_ONLY_SAME_L_VALUES) {
            for (String lid : CyGlobals.KPM.MIN_L.keySet()) {
                CyGlobals.KPM.MIN_PER = CyGlobals.KPM.MIN_L.get(lid);
                CyGlobals.KPM.CASE_EXCEPTIONS_MAP.put(lid, CyGlobals.KPM.MIN_L.get(lid));
                System.out.println("lid: " + lid + ", MIN_PER = " + CyGlobals.KPM.MIN_PER);
            }
            for (String lid : CyGlobals.KPM.INC_L.keySet()) {
                CyGlobals.KPM.INC_PER = CyGlobals.KPM.INC_L.get(lid);
                System.out.println("lid: " + lid + ", INC_PER = " + CyGlobals.KPM.INC_PER);
            }
            for (String lid : CyGlobals.KPM.MAX_L.keySet()) {
                CyGlobals.KPM.MAX_PER = CyGlobals.KPM.MAX_L.get(lid);
                System.out.println("lid: " + lid + ", MAX_PER = " + CyGlobals.KPM.MAX_PER);
            }
        }
        System.out.println("..........");
    }

    private void runWithPerturbation(boolean isINEs) {
        int val = CyGlobals.RobustnessSettings.getPerturbationValue();
        int step = CyGlobals.RobustnessSettings.getStepPerturbationValue();
        int max = CyGlobals.RobustnessSettings.getMaxPerturbationValue();

        String common = "The settings are not valid.\n";
        if (val > max) {
            DialogUtils.showNonModalDialog(common + "The mininum value must < or = to the maximum.", "Robustness settings", DialogUtils.MessageTypes.Warn);
            return;
        }

        if (step > max - val) {
            DialogUtils.showNonModalDialog(common + "The step value must be < or = to maximum minus minimum.", "Robustness settings", DialogUtils.MessageTypes.Warn);
            return;
        }

        if (CyGlobals.RobustnessSettings.getGraphsPerStep() < 1) {
            DialogUtils.showNonModalDialog(common + "Graphs per step must be at least 1.", "Robustness settings", DialogUtils.MessageTypes.Warn);
            return;
        }

        String runId = CyGlobals.WORKING_GRAPH.getRow(CyGlobals.WORKING_GRAPH).get(CyNetwork.NAME, String.class);
        BatchRunWithPerturbationHandler handler = new BatchRunWithPerturbationHandler(
                CyGlobals.RobustnessSettings.getPerturbationTechnique(),
                CyGlobals.RobustnessSettings.getPerturbationValue(),
                CyGlobals.RobustnessSettings.getStepPerturbationValue(),
                CyGlobals.RobustnessSettings.getMaxPerturbationValue(),
                CyGlobals.RobustnessSettings.getGraphsPerStep(),
                isINEs,
                runId);
        CyProvider.dialogTaskManager.execute(new TaskIterator(handler));
    }
}
