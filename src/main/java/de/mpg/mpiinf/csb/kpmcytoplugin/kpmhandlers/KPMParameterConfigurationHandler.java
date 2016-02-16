package de.mpg.mpiinf.csb.kpmcytoplugin.kpmhandlers;

import de.mpg.mpiinf.csb.kpmcytoplugin.CyGlobals;
import de.mpg.mpiinf.csb.kpmcytoplugin.CyProvider;
import de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels.*;
import de.mpg.mpiinf.csb.kpmcytoplugin.util.CytoscapeFieldNames;
import dk.sdu.kpm.Combine;
import dk.sdu.kpm.graph.GeneNode;
import dk.sdu.kpm.gui.clause.Clause;
import dk.sdu.kpm.gui.clause.ClauseFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

import javax.swing.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Logic that initially was part of StartButtonListener, but moved since the
 * configuration method was needed multiple places.
 *
 * @author Martin
 *
 */
public class KPMParameterConfigurationHandler {

    private final KPMMainPanel kpmmp;

    public KPMParameterConfigurationHandler(KPMMainPanel kpmmp) {
        this.kpmmp = kpmmp;
    }

    public boolean runSanityChecks() {
        if (CyProvider.networkManager.getNetworkSet().size() < 1) {
            JOptionPane.showMessageDialog(null,
                    "Please import a network into Cytoscape.",
                    "No network imported", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (CyGlobals.HAS_RESULTS_TAB) {
            Object[] options = {"Yes", "Cancel"};
            int chose = JOptionPane
                    .showOptionDialog(
                            null,
                            "All results from the previous run will be lost if not saved. Continue? ",
                            "Warning", JOptionPane.DEFAULT_OPTION,
                            JOptionPane.WARNING_MESSAGE, null, options,
                            options[0]);

            if (chose == 1) {
                return false;
            }
        }
        CyGlobals.KPM.STARTING_TIME = System.nanoTime();

        // Initially, check if a data file has been loaded.
        KPMDataTab dp = kpmmp.getKPMTabbedPane().getDataPanel();
        int filesLoaded = dp.getNoFilesLoaded();
        // If no files have been loaded, print an error and do northing.
        if (filesLoaded == 0) {
            JOptionPane.showMessageDialog(null,
                    "Please load at least one data set.", "No data set loaded",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        KPMParameterTab pp = kpmmp.getKPMTabbedPane().getParameterPanel();
        CyGlobals.KPM.IS_BATCH_RUN = pp.isBatchRunMode();

//       CyGlobals.KPM.VARYING_L_ID = new ArrayList<String>(2);
//        CyGlobals.KPM.VARYING_L_ID_IN_PERCENTAGE = new HashMap<String, Boolean>(2);
        for (String lid : CyGlobals.L_InPercentageMap.keySet()) {
            for (String varyingL : CyGlobals.KPM.VARYING_L_ID) {
                if (varyingL.equals(CyGlobals.COMMON_L_PANEL_NAME) && !lid.equals(CyGlobals.COMMON_L_PANEL_NAME)) {
                    //                  CyGlobals.KPM.VARYING_L_ID.add(lid);
                    CyGlobals.KPM.VARYING_L_ID_IN_PERCENTAGE.put(lid, Boolean.TRUE);
                }
            }
            if (CyGlobals.KPM.IS_BATCH_RUN == false && (!CyGlobals.L_InPercentageMap.containsKey(lid) || !CyGlobals.L_InPercentageMap.get(lid))) {
                CyGlobals.KPM.CALCULATE_ONLY_SAME_L_VALUES = false;
                break;
            }
        }

        // Read L parameters for the data sets from sub-panel.
        KLPanel klp = pp.getKLPanel();

        // Perform sanity check of K and L values while loading them.
        // Save a warning message for each value which is changed.
        int maxNumberOfGeneExceptions = CyGlobals.WORKING_GRAPH.getNodeCount();
        StringBuffer warningMessage = new StringBuffer();
        Map<String, Integer> maxNumberofCaseExceptionsMap = klp
                .getMaxNumberOfCaseExceptionsMap();

        if (CyGlobals.KPM.IS_BATCH_RUN) {
            int factor;
            if (pp.isINEs()) {
                factor = 1;
                // Set K interval and step size.
                if (klp.isKBatchRun()) {
                    int maxKTemp = klp.getMaxGeneExceptions();
                    if (maxKTemp > maxNumberOfGeneExceptions) {
                        CyGlobals.KPM.MAX_K = maxNumberOfGeneExceptions;
                        warningMessage.append("Max K was set to ").append(maxNumberOfGeneExceptions);
                        warningMessage.append(CyGlobals.KPM.lineSep);
                    } else {
                        CyGlobals.KPM.MAX_K = maxKTemp;
                    }

                    int minKTemp = klp.getMinGeneExceptions();
                    if (minKTemp >= CyGlobals.KPM.MAX_K) {
                        CyGlobals.KPM.MIN_K = CyGlobals.KPM.MAX_K - 1;
                        warningMessage.append("Min K was set to ").append(CyGlobals.KPM.MAX_K - 1);
                        warningMessage.append(CyGlobals.KPM.lineSep);
                    } else {
                        CyGlobals.KPM.MIN_K = minKTemp;
                    }

                    int maxStepSize = CyGlobals.KPM.MAX_K - CyGlobals.KPM.MIN_K;
                    int stepSizeTemp = klp.getStepSizeGeneExceptions();
                    if (stepSizeTemp > maxStepSize) {
                        CyGlobals.KPM.INC_K = maxStepSize;
                        warningMessage.append("Step K was set to ").append(maxStepSize);
                        warningMessage.append(CyGlobals.KPM.lineSep);
                    } else {
                        CyGlobals.KPM.INC_K = stepSizeTemp;
                    }
                } else {
                    CyGlobals.KPM.INC_K = 0;
                    int kTemp = klp.getMaxGeneExceptions();
                    if (kTemp > maxNumberOfGeneExceptions) {
                        CyGlobals.KPM.MAX_K = CyGlobals.KPM.MIN_K = maxNumberOfGeneExceptions;
                        warningMessage.append("K was set to ").append(maxNumberOfGeneExceptions);
                        warningMessage.append(CyGlobals.KPM.lineSep);
                    } else {
                        CyGlobals.KPM.MAX_K = CyGlobals.KPM.MIN_K = kTemp;
                    }
                }
            } else {
                factor = maxNumberOfGeneExceptions;
                CyGlobals.KPM.MIN_K = CyGlobals.KPM.MAX_K = CyGlobals.KPM.INC_K = 0;
                CyGlobals.KPM.GENE_EXCEPTIONS = 0;
            }
            Set<String> batchRunDataSets = klp.getLBatchRunDataSetIDs();
            sanityCheckAndSetMaxCaseExceptionsMap(
                    klp.getMaxCaseExceptionsMap(),
                    maxNumberofCaseExceptionsMap, batchRunDataSets, factor,
                    warningMessage);
            sanityCheckAndSetMinCaseExceptionsMap(
                    klp.getMinCaseExceptionsMap(), batchRunDataSets,
                    warningMessage);
            sanityCheckAndSetStepSizeCaseExceptionsMap(
                    klp.getStepSizeCaseExceptionsMap(), batchRunDataSets,
                    warningMessage);
        } else {
            int factor;
            if (pp.isINEs()) {
                factor = 1;
                int kTemp = klp.getGeneExceptions();
                if (kTemp > maxNumberOfGeneExceptions) {
                    CyGlobals.KPM.GENE_EXCEPTIONS = maxNumberOfGeneExceptions;
                    warningMessage.append("K was set to ").append(maxNumberOfGeneExceptions);
                    warningMessage.append(CyGlobals.KPM.lineSep);
                } else {
                    CyGlobals.KPM.GENE_EXCEPTIONS = kTemp;
                }
            } else {
                factor = maxNumberOfGeneExceptions;
                CyGlobals.KPM.GENE_EXCEPTIONS = 0;
            }
            sanityCheckAndSetCaseExceptionsMap(klp.getCaseExceptionsMap(),
                    maxNumberofCaseExceptionsMap, factor, warningMessage);
        }
        if (warningMessage.length() > 0) {
            JOptionPane.showMessageDialog(null,
                    warningMessage, "Invalid K/L parameters",
                    JOptionPane.WARNING_MESSAGE);
        }

        CyGlobals.KPM.ALGO = pp.getAlgorithm();

        CyGlobals.KPM.NUMBER_OF_PROCESSORS = pp.getNoProcessors();

        CyGlobals.KPM.NUM_SOLUTIONS = pp.getNumberOfPathways();

        AdvancedParameterPanel app = pp.getAdvancedParameterPanel();
        CyGlobals.KPM.ALPHA = app.getAlpha();
        CyGlobals.KPM.BETA = app.getBeta();
        CyGlobals.KPM.RHO = app.getRho();
        CyGlobals.KPM.MAX_ITERATIONS = app.getMaxIterations();
        CyGlobals.KPM.MAX_RUNS_WITHOUT_CHANGE = app.getMaxIterationsWOChange();
        CyGlobals.KPM.NUM_STARTNODES = app.getNoStartNodes();
        CyGlobals.KPM.TAU_MIN = app.getTauMin();
        CyGlobals.KPM.NUMBER_OF_SOLUTIONS_PER_ITERATION = app
                .getNoSolutionsPerIteration();
        CyGlobals.KPM.ITERATION_BASED = app.isIterationBasedGlone();

        // Compute the final expression values from the loaded data sets
        // and the logical formula connecting these data sets.
        KPMLinksTab lp = kpmmp.getKPMTabbedPane().getLinksPanel();
        Clause clause = lp.getLogicalConnections();

        // If the user specified a logical formula, set this logical formula.
        if (CyGlobals.KPM.COMBINE_OPERATOR.equals(Combine.CUSTOM)) {
            CyGlobals.KPM.COMBINE_FORMULA = ClauseFactory.getLogicalFormula(clause);
        }

        if (!kpmmp.finishLoadingProcess(lp.getDataSetFileMap())) {
            return false;
        }

        CyGlobals.KPM.N = CyGlobals.KPM.MAIN_GRAPH.getVertexCount();

        KPMPosNegTab pnp = kpmmp.getKPMTabbedPane().getPosNegPanel();
        // Get the positive and negative list from the corresponding KPM tab.
        Set<String> posList = new HashSet<String>();
        posList.addAll(pnp.getPositiveList());
        Set<String> negList = new HashSet<String>();
        negList.addAll(pnp.getNegativeList());

        CyGlobals.KPM.MAIN_GRAPH.setPositiveList(posList);
        CyGlobals.KPM.MAIN_GRAPH.setNegativeList(negList);

        // Set way unmapped nodes should be treated.
        char unmappedNodesChar = pp.treatUnmappedNodes();
        CyGlobals.KPM.MAIN_GRAPH.setTreatBackNodes(unmappedNodesChar);

        CyTable cyTable = CyGlobals.WORKING_GRAPH.getDefaultNodeTable();
        for (CyNode cyNode : CyGlobals.WORKING_GRAPH.getNodeList()) {
            String nodeId = CyGlobals.WORKING_GRAPH.getRow(cyNode).get(
                    CyNetwork.NAME, String.class).trim();
            GeneNode node = CyGlobals.KPM.MAIN_GRAPH.getNodeIdToGeneNode().get(nodeId);
            CyRow row = cyTable.getRow(cyNode.getSUID());
            for (String expId : node.getDifferenceMap().keySet()) {
                String externalID = CyGlobals.KPM.externalToInternalIDManager
                        .getExternalIdentifier(expId);
                int totalCases = node.getNumCases(expId);
                int totalDiff = node.getNumDiffExpressedCases(expId);
                double percentage = (double) totalDiff / (double) totalCases;
                row.set(externalID
                        + " - Total active/dysregulated", totalDiff);
                row.set(externalID
                        + " - % active/dysregulated", percentage);

            }
            row.set(CytoscapeFieldNames.NODE_IS_NEGATIVE_PROPERTY_NAME,
                    CyGlobals.KPM.MAIN_GRAPH.getNegativeList().contains(nodeId));
            row.set(CytoscapeFieldNames.NODE_IS_POSITIVE_PROPERTY_NAME,
                    CyGlobals.KPM.MAIN_GRAPH.getPositiveList().contains(nodeId));
        }

        if (CyGlobals.KPM.IS_BATCH_RUN == false && CyGlobals.KPM.CALCULATE_ONLY_SAME_L_VALUES == false) {
            for (String lid : CyGlobals.KPM.CASE_EXCEPTIONS_MAP.keySet()) {
                int percentage = CyGlobals.KPM.CASE_EXCEPTIONS_MAP.get(lid);
                if (CyGlobals.L_InPercentageMap.containsKey(lid) && CyGlobals.L_InPercentageMap.get(lid)) {
                    if (!CyGlobals.L_DatasetColumnSizeMap.containsKey(lid)) {
                        continue;
                    }

                    int setSize = CyGlobals.L_DatasetColumnSizeMap.get(lid);
                    int percentageVal = (int) Math.ceil(((double) setSize / (double) 100) * percentage);
                    CyGlobals.KPM.CASE_EXCEPTIONS_MAP.put(lid, percentageVal);
                }
            }
        }

        if (CyGlobals.KPM.IS_BATCH_RUN == true && CyGlobals.KPM.CALCULATE_ONLY_SAME_L_VALUES == false) {
            for (String varyingL : CyGlobals.KPM.VARYING_L_ID) {
                if (CyGlobals.L_InPercentageMap.containsKey(varyingL) && CyGlobals.L_InPercentageMap.get(varyingL)) {
                    CyGlobals.KPM.VARYING_L_ID_IN_PERCENTAGE.put(varyingL, Boolean.TRUE);
                }
            }
            for (String lid : CyGlobals.KPM.MIN_L.keySet()) {
                if (CyGlobals.L_InPercentageMap.containsKey(lid) && CyGlobals.L_InPercentageMap.get(lid)) {
                    if (!CyGlobals.L_DatasetColumnSizeMap.containsKey(lid)) {
                        continue;
                    }

                    int percentage = CyGlobals.KPM.MIN_L.get(lid);
                    int setSize = CyGlobals.L_DatasetColumnSizeMap.get(lid);
                    int percentageVal = (int) Math.ceil(((double) setSize / (double) 100) * percentage);
                    CyGlobals.KPM.MIN_L.put(lid, percentageVal);
                }
            }

            for (String lid : CyGlobals.KPM.INC_L.keySet()) {
                if (CyGlobals.L_InPercentageMap.containsKey(lid) && CyGlobals.L_InPercentageMap.get(lid)) {
                    if (!CyGlobals.L_DatasetColumnSizeMap.containsKey(lid)) {
                        continue;
                    }

                    int percentage = CyGlobals.KPM.INC_L.get(lid);
                    int setSize = CyGlobals.L_DatasetColumnSizeMap.get(lid);
                    int percentageVal = (int) Math.ceil(((double) setSize / (double) 100) * percentage);
                    CyGlobals.KPM.INC_L.put(lid, percentageVal);
                }
            }

            for (String lid : CyGlobals.KPM.MAX_L.keySet()) {
                if (CyGlobals.L_InPercentageMap.containsKey(lid) && CyGlobals.L_InPercentageMap.get(lid)) {
                    if (!CyGlobals.L_DatasetColumnSizeMap.containsKey(lid)) {
                        continue;
                    }

                    int percentage = CyGlobals.KPM.MAX_L.get(lid);
                    int setSize = CyGlobals.L_DatasetColumnSizeMap.get(lid);
                    int percentageVal = (int) Math.ceil(((double) setSize / (double) 100) * percentage);
                    CyGlobals.KPM.MAX_L.put(lid, percentageVal);
                }
            }
        }

        if (CyGlobals.HAS_RESULTS_TAB) {
            KPMResultsTab.clearResults();
        }

        return true;
    }

    private void sanityCheckAndSetCaseExceptionsMap(
            Map<String, Integer> caseExceptionsMap,
            Map<String, Integer> maxNumberofCaseExceptionsMap, int factor,
            StringBuffer warningMessage) {
        HashMap<String, Integer> checkedCaseExceptionsMap = new HashMap<String, Integer>();
        for (String dataSetID : caseExceptionsMap.keySet()) {
            int lTemp = caseExceptionsMap.get(dataSetID);
            int maxL = maxNumberofCaseExceptionsMap.get(dataSetID) * factor;
            if (CyGlobals.KPM.USE_INES) {
                if (lTemp > maxL && !CyGlobals.L_InPercentageMap.containsKey(dataSetID)) {
                    checkedCaseExceptionsMap.put(dataSetID, maxL);
                    warningMessage.append("L for data set ").append(CyGlobals.KPM.externalToInternalIDManager
                            .getExternalIdentifier(dataSetID)).append(" was set to ").append(maxL);
                    warningMessage.append(CyGlobals.KPM.lineSep);

                } else if (lTemp > 99 && CyGlobals.L_InPercentageMap.containsKey(dataSetID)) {
                    checkedCaseExceptionsMap.put(dataSetID, 99);
                    warningMessage.append("L for data set ").append(CyGlobals.KPM.externalToInternalIDManager
                            .getExternalIdentifier(dataSetID)).append(" was set to ").append("99 %");
                    warningMessage.append(CyGlobals.KPM.lineSep);
                } else {
                    checkedCaseExceptionsMap.put(dataSetID, lTemp);
                }
            } else {
                checkedCaseExceptionsMap.put(dataSetID, lTemp);
            }
        }
        CyGlobals.KPM.CASE_EXCEPTIONS_MAP = checkedCaseExceptionsMap;
    }

    private void sanityCheckAndSetMaxCaseExceptionsMap(
            Map<String, Integer> maxCaseExceptionsMap,
            Map<String, Integer> maxNumberofCaseExceptionsMap,
            Set<String> batchRunDataSets, int factor,
            StringBuffer warningMessage) {
        HashMap<String, Integer> checkedMaxCaseExceptionsMap = new HashMap<String, Integer>();
        for (String dataSetID : maxCaseExceptionsMap.keySet()) {
            if (batchRunDataSets.contains(dataSetID)) {
                int lMaxTemp = maxCaseExceptionsMap.get(dataSetID);
                int maxL = maxNumberofCaseExceptionsMap.get(dataSetID) * factor;
                if (CyGlobals.KPM.USE_INES) {
                    if (lMaxTemp > maxL && !CyGlobals.L_InPercentageMap.containsKey(dataSetID)) {
                        checkedMaxCaseExceptionsMap.put(dataSetID, maxL);
                        warningMessage.append("Max L for data set ").append(CyGlobals.KPM.externalToInternalIDManager
                                .getExternalIdentifier(dataSetID)).append(" was set to ").append(maxL);
                        warningMessage.append(CyGlobals.KPM.lineSep);
                    } else if (lMaxTemp > 99 && CyGlobals.L_InPercentageMap.containsKey(dataSetID)) {
                        checkedMaxCaseExceptionsMap.put(dataSetID, 99);
                        warningMessage.append("Max L for data set ").append(CyGlobals.KPM.externalToInternalIDManager
                                .getExternalIdentifier(dataSetID)).append(" was set to ").append("99 %");
                        warningMessage.append(CyGlobals.KPM.lineSep);
                    } else {
                        checkedMaxCaseExceptionsMap.put(dataSetID, lMaxTemp);
                    }
                } else {
                    checkedMaxCaseExceptionsMap.put(dataSetID, lMaxTemp);
                }
            } else {
                int lTemp = maxCaseExceptionsMap.get(dataSetID);
                int maxL = maxNumberofCaseExceptionsMap.get(dataSetID) * factor;
                if (lTemp > maxL) {
                    checkedMaxCaseExceptionsMap.put(dataSetID, maxL);
                    warningMessage.append("L for data set ").append(CyGlobals.KPM.externalToInternalIDManager
                            .getExternalIdentifier(dataSetID)).append(" was set to ").append(maxL);
                    warningMessage.append(CyGlobals.KPM.lineSep);
                } else {
                    checkedMaxCaseExceptionsMap.put(dataSetID, lTemp);
                }
            }
        }
        CyGlobals.KPM.MAX_L = checkedMaxCaseExceptionsMap;
    }

    private void sanityCheckAndSetMinCaseExceptionsMap(
            Map<String, Integer> minCaseExceptionsMap,
            Set<String> batchRunDataSets, StringBuffer warningMessage) {
        HashMap<String, Integer> checkedMinCaseExceptionsMap = new HashMap<String, Integer>();
        for (String dataSetID : minCaseExceptionsMap.keySet()) {
            if (batchRunDataSets.contains(dataSetID)) {
                int lMinTemp = minCaseExceptionsMap.get(dataSetID);
                int maxLMin = CyGlobals.KPM.MAX_L.get(dataSetID) - 1;
                if (lMinTemp > maxLMin) {
                    checkedMinCaseExceptionsMap.put(dataSetID, maxLMin);
                    warningMessage.append("Min L for data set ").append(CyGlobals.KPM.externalToInternalIDManager
                            .getExternalIdentifier(dataSetID)).append(" was set to ").append(maxLMin);
                    warningMessage.append(CyGlobals.KPM.lineSep);
                } else {
                    checkedMinCaseExceptionsMap.put(dataSetID, lMinTemp);
                }
            } else {
                checkedMinCaseExceptionsMap.put(dataSetID, CyGlobals.KPM.MAX_L.get(dataSetID));
            }
        }
        CyGlobals.KPM.MIN_L = checkedMinCaseExceptionsMap;
    }

    private void sanityCheckAndSetStepSizeCaseExceptionsMap(
            Map<String, Integer> stepSizeCaseExceptionsMap,
            Set<String> batchRunDataSets, StringBuffer warningMessage) {
        HashMap<String, Integer> checkedStepSizeCaseExceptionsMap = new HashMap<String, Integer>();
        for (String dataSetID : stepSizeCaseExceptionsMap.keySet()) {
            if (batchRunDataSets.contains(dataSetID)) {
                int lStepTemp = stepSizeCaseExceptionsMap.get(dataSetID);
                int maxStepSize = CyGlobals.KPM.MAX_L.get(dataSetID)
                        - CyGlobals.KPM.MIN_L.get(dataSetID);
                if (lStepTemp > maxStepSize) {
                    checkedStepSizeCaseExceptionsMap
                            .put(dataSetID, maxStepSize);
                    warningMessage.append("Step L for data set ").append(CyGlobals.KPM.externalToInternalIDManager
                            .getExternalIdentifier(dataSetID)).append(" was set to ").append(maxStepSize);
                    warningMessage.append(CyGlobals.KPM.lineSep);
                } else {
                    checkedStepSizeCaseExceptionsMap.put(dataSetID, lStepTemp);
                }
            } else {
                checkedStepSizeCaseExceptionsMap.put(dataSetID, 0);
            }
        }
        CyGlobals.KPM.INC_L = checkedStepSizeCaseExceptionsMap;
    }

}
