package de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels;

import de.mpg.mpiinf.csb.kpmcytoplugin.CyGlobals;
import de.mpg.mpiinf.csb.kpmcytoplugin.CyProvider;
import de.mpg.mpiinf.csb.kpmcytoplugin.task.KPMCreateSelectedPathwayTask;
import de.mpg.mpiinf.csb.kpmcytoplugin.task.KPMSaveSelectedPathwayTask;
import de.mpg.mpiinf.csb.kpmcytoplugin.util.CytoscapeFieldNames;
import de.mpg.mpiinf.csb.kpmcytoplugin.util.CytoscapePanelNames;
import de.mpg.mpiinf.csb.kpmcytoplugin.util.KPMUtilities;
import dk.sdu.kpm.Algo;
import dk.sdu.kpm.KPMSettings;
import dk.sdu.kpm.RunStats;
import dk.sdu.kpm.graph.GeneEdge;
import dk.sdu.kpm.graph.GeneNode;
import dk.sdu.kpm.graph.Result;
import dk.sdu.kpm.logging.KpmLogger;
import dk.sdu.kpm.results.IKPMResultSet;
import dk.sdu.kpm.results.PercentageParameters;
import dk.sdu.kpm.utils.DialogUtils;
import org.cytoscape.model.*;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains functionalities which allow the user to explore the K- & L-parameter
 * space by depicting the best scoring key pathways over a user-specified range
 * of values for K and L.
 *
 * The user is able to dynamically change K and L while the effects of this
 * change on the identified key pathways is shown in the network.
 *
 */
public class KPMResultsTab extends JPanel {

    private static final long serialVersionUID = 7989405129757131918L;
    private JLabel kLabelValue;
    private final List<Integer> currentValues;
    private volatile PercentageParameters currentValuesPercentages;
    private ViewResultsPanel resultsPanel;
    private VisualStyle hitsStyle;
    private VisualStyle totalHitsStyle;
    private VisualStyle activeCasesStyleAll;
    private VisualStyle exceptionsStyle;
    private VisualStyle backNodesStyle;
    private VisualStyle currentStyle;
    private final KPMTabbedPane parentPane;
    private final boolean isInes;
    private final CyNetwork parentNetwork;
    //private final boolean CALCULATE_ONLY_SAME_L_VALUES;
    private final HashMap<String, CyRow> edgeRowMap;
    private JButton colorGraphButton;
    private static KPMSettings kpmSettings;

    public KPMResultsTab(KPMTabbedPane parentPane, IKPMResultSet results, Map<Integer, String> indexLMap, boolean isInes, HashMap<String, CyRow> edgeRowMap) {
        if (results != null) {
            KPMResultsTab.kpmSettings = results.getKpmSettings();
        } else {
            KPMResultsTab.kpmSettings = CyGlobals.KPM;
        }
        //this.CALCULATE_ONLY_SAME_L_VALUES = kpmSettings.CALCULATE_ONLY_SAME_L_VALUES == true;
        parentNetwork = CyGlobals.WORKING_GRAPH;
        if (CyProvider.networkViewManager != null && !CyProvider.networkViewManager.viewExists(parentNetwork)) {
            CyNetworkView netView
                    = CyProvider.networkViewFactory.createNetworkView(parentNetwork);
            CyProvider.networkViewManager.addNetworkView(netView);
        }
//        int test = kpmSettings.STATS_MAP.values().iterator().next().getResults().get(0).getFitness();
//        System.out.println("RESULTS LARGEST FROM STATS MAP IN KPMResultsTab: " + test);
        this.edgeRowMap = edgeRowMap;
        setLayout(new BorderLayout());
        this.isInes = isInes;
        this.parentPane = parentPane;
        JPanel varPanel = new JPanel();
        JPanel fixPanel = new JPanel();
        JSeparator jsp = new JSeparator();
        varPanel.setLayout(new BoxLayout(varPanel, BoxLayout.Y_AXIS));
        fixPanel.setLayout(new BoxLayout(fixPanel, BoxLayout.Y_AXIS));

        int datasets = indexLMap.size();
        currentValues = new ArrayList<Integer>(datasets + 1);
        for (int i = 0; i <= datasets; i++) {
            currentValues.add(0);
        }
        if (!kpmSettings.IS_BATCH_RUN) {
            currentValues.set(0, kpmSettings.GENE_EXCEPTIONS);
            for (int i = 1; i < currentValues.size(); i++) {
                currentValues.set(i, kpmSettings.CASE_EXCEPTIONS_MAP.get(indexLMap.get(i - 1)));
            }
        } else {
            currentValues.set(0, kpmSettings.MIN_K);
            for (int i = 1; i < currentValues.size(); i++) {
                currentValues.set(i, kpmSettings.MIN_L.get(indexLMap.get(i - 1)));
            }
        }
        createVisualStyles();
        JLabel stratLabel = new JLabel("Strategy: ");
        String strat = "INES";
        if (!isInes) {
            strat = "GLONE";
        }
        JLabel strat2Label = new JLabel(strat);
        JPanel stratPanel = new JPanel();
        stratPanel.setLayout(new BoxLayout(stratPanel, BoxLayout.X_AXIS));
        stratPanel.add(stratLabel, BorderLayout.WEST);
        stratPanel.add(strat2Label, BorderLayout.CENTER);
        stratPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        fixPanel.add(stratPanel);
        JLabel algoLabel = new JLabel("Algorithm: ");
        String algoL = "GREEDY";
        if (kpmSettings.ALGO == Algo.LCG || kpmSettings.ALGO == Algo.EXCEPTIONSUMACO) {
            algoL = "ACO";
        } else if (kpmSettings.ALGO == Algo.OPTIMAL || kpmSettings.ALGO == Algo.EXCEPTIONSUMOPTIMAL) {
            algoL = "OPTIMAL";
        }
        JLabel algo2Label = new JLabel(algoL);
        JPanel algoPanel = new JPanel();
        algoPanel.setLayout(new BoxLayout(algoPanel, BoxLayout.X_AXIS));
        algoPanel.add(algoLabel, BorderLayout.WEST);
        algoPanel.add(algo2Label, BorderLayout.CENTER);
        algoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        fixPanel.add(algoPanel);

        colorGraphButton = new JButton("Update table and graph coloring");
        colorGraphButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                colorGraphButton.setForeground(Color.BLACK);
                CyProvider.dialogTaskManager.execute(new TaskIterator(new AbstractTask() {
                    @Override
                    public void run(TaskMonitor taskMonitor) throws Exception {
                        taskMonitor.setTitle("Updating graph coloring");
                        taskMonitor.setStatusMessage("Updating table");
                        resultsPanel.updateTableModel();
                        taskMonitor.setStatusMessage("Updating view");
                        taskMonitor.setProgress(0.5);
                        updateView();
                        taskMonitor.setProgress(1);
                    }
                }));
            }
        });

        if (isInes) {
            JPanel kPanel = new JPanel();
            JLabel kLabel
                    = new JLabel("Number of Gene Exceptions (K parameter): ");
            kLabelValue = new JLabel(String.valueOf(kpmSettings.MIN_K));
            kPanel.setLayout(new BoxLayout(kPanel, BoxLayout.X_AXIS));
            kPanel.add(kLabel, BorderLayout.WEST);
            kPanel.add(kLabelValue, BorderLayout.CENTER);
            kPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

            if (kpmSettings.MIN_K == kpmSettings.MAX_K) {
                fixPanel.add(kPanel);
            } else {
                ChangeListener kChangeListener = new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        JSlider slider = (JSlider) e.getSource();
                        if (!slider.getValueIsAdjusting()) {
                            int value = slider.getValue();
                            kLabelValue.setText(Integer.toString(value));
                            kLabelValue.validate();

                            if (kpmSettings.CALCULATE_ONLY_SAME_L_VALUES) {
                                PercentageParameters previous = new PercentageParameters(currentValuesPercentages.getK(), currentValuesPercentages.getlPer());

                                if (kpmSettings.STATS_MAP_PER == null) {
                                    return;
                                }

                                currentValuesPercentages = new PercentageParameters(value, currentValuesPercentages.getlPer());
                                if (!kpmSettings.STATS_MAP_PER.containsKey(currentValuesPercentages)) {
                                    currentValuesPercentages = previous;
                                    DialogUtils.showNonModalDialog("No pathways were found for the given parameters.\nTry some other combination.", "No results", DialogUtils.MessageTypes.Warn);
                                }

                            } else {
                                int previous = currentValues.get(0);
                                currentValues.set(0, value);

                                if (kpmSettings.STATS_MAP == null) {
                                    return;
                                }

                                if (!kpmSettings.STATS_MAP.containsKey(currentValues)) {
                                    currentValues.set(0, previous);
                                    DialogUtils.showNonModalDialog("No pathways were found for the given parameters.\nTry some other combination.", "No results", DialogUtils.MessageTypes.Warn);
                                    slider.setValue(previous);
                                }
                            }
                        }
                    }
                };
                String kTip = "Please select the value for the Gene Exceptions parameter";

                JPanel kSliderPanel = getSlider(kpmSettings.MIN_K, kpmSettings.INC_K, kpmSettings.MAX_K, kChangeListener, kTip, "");
                varPanel.add(kPanel);
                varPanel.add(kSliderPanel);
            }

        }

        int l = 0;
        if (kpmSettings != null && kpmSettings.CASE_EXCEPTIONS_MAP != null) {
            for (String key : kpmSettings.CASE_EXCEPTIONS_MAP.keySet()) {
                l = kpmSettings.CASE_EXCEPTIONS_MAP.get(key);
                break;
            }
        }

        JLabel casesLabel = new JLabel("Number of Case Exceptions (L parameter):");
        JLabel dummyLabel = new JLabel("");
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
        labelPanel.add(casesLabel, BorderLayout.WEST);
        labelPanel.add(dummyLabel, BorderLayout.CENTER);
        labelPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        fixPanel.add(labelPanel);

        int fixLAdded = 0;
 
        if (kpmSettings.CALCULATE_ONLY_SAME_L_VALUES && datasets > 0) {

            if (kpmSettings.MIN_L != null && kpmSettings.MIN_L.size() > 0) {
                JPanel lPanel = new JPanel();

                String lid = kpmSettings.MIN_L.keySet().iterator().next();

                for (PercentageParameters parameters : kpmSettings.STATS_MAP_PER.keySet()) {
                    if (currentValuesPercentages == null) {
                        currentValuesPercentages = parameters;
                    }
                }

                int minL = (int) kpmSettings.MIN_PER;

                int maxL = (int) kpmSettings.MAX_PER;

                int incL = (int) kpmSettings.INC_PER;

  
                JLabel lLabel = new JLabel("Collective dataset values, in %:");

                if (minL == maxL) {
                    fixPanel.add(lPanel);
                    fixLAdded = datasets;
                } else {
                    final JLabel lLabelValue = new JLabel(String.valueOf(minL));
                    lPanel.setLayout(new BoxLayout(lPanel, BoxLayout.X_AXIS));
                    lPanel.add(lLabel, BorderLayout.WEST);
                    lPanel.add(lLabelValue, BorderLayout.CENTER);
                    lPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
                    ChangeListener lChangeListener = new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            JSlider slider = (JSlider) e.getSource();
                            if (!slider.getValueIsAdjusting()) {
                                int value = slider.getValue();
                                lLabelValue.setText(Integer.toString(value));
                                lLabelValue.validate();

                                PercentageParameters previous = new PercentageParameters(currentValuesPercentages.getK(), currentValuesPercentages.getlPer());

                                if (kpmSettings.STATS_MAP_PER == null) {
                                    return;
                                }

                                currentValuesPercentages = new PercentageParameters(currentValuesPercentages.getK(), value);
                                if (!kpmSettings.STATS_MAP_PER.containsKey(currentValuesPercentages)) {
                                    currentValuesPercentages = previous;
                                    DialogUtils.showNonModalDialog("No pathways were found for the given parameters.\nTry some other combination.", "No results", DialogUtils.MessageTypes.Warn);
                                }

                                markColorButtonRed();
                            }
                        }
                    };
                    String lTip = "Please select the percentage value for the Case Exceptions parameter";
                    JPanel lSliderPanel = getSlider(minL, incL, maxL, lChangeListener, lTip, lid);

                    jsp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
                    varPanel.add(lPanel);
                    varPanel.add(lSliderPanel);
                    varPanel.add(jsp);
                }
            }
        } else {

            for (int i = 0; i < datasets; i++) {
                final int index = i;
                JPanel lPanel = new JPanel();
                String lid = indexLMap.get(i);
                int minL = kpmSettings.MIN_L.get(lid);
                int maxL = kpmSettings.MAX_L.get(lid);

                String externalId = kpmSettings.externalToInternalIDManager.getExternalIdentifier(lid);
                JLabel lLabel = new JLabel(externalId + ":");

                final JLabel lLabelValue = new JLabel(String.valueOf(minL));

                lPanel.setLayout(new BoxLayout(lPanel, BoxLayout.X_AXIS));
                lPanel.add(lLabel, BorderLayout.WEST);
                lPanel.add(lLabelValue, BorderLayout.CENTER);
                lPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
                if (minL == maxL) {
                    fixPanel.add(lPanel);
                    fixLAdded++;
                } else {
                    int incL = kpmSettings.INC_L.get(lid);
                    ChangeListener lChangeListener = new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            JSlider slider = (JSlider) e.getSource();
                            if (!slider.getValueIsAdjusting()) {
                                int value = slider.getValue();
                                lLabelValue.setText(Integer.toString(value));
                                lLabelValue.validate();
                                int j = index + 1;
                                int previous = currentValues.get(j);
                                currentValues.set(j, value);

                                if (kpmSettings.STATS_MAP == null) {
                                    return;
                                }

                                if (!kpmSettings.STATS_MAP.containsKey(currentValues)) {
                                    currentValues.set(j, previous);
                                    DialogUtils.showNonModalDialog("No pathways were found for the given parameters.\nTry some other combination.", "No results", DialogUtils.MessageTypes.Warn);
                                    slider.setValue(previous);
                                }

                                markColorButtonRed();
                            }
                        }
                    };
                    String lTip = "Please select the value for the Case Exceptions parameter";
                    JPanel lSliderPanel = getSlider(minL, incL, maxL, lChangeListener, lTip, lid);

                    jsp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
                    varPanel.add(lPanel);
                    varPanel.add(lSliderPanel);
                    varPanel.add(jsp);
                }
            }
        }
        JPanel mainVarPanel = new JPanel();
        mainVarPanel.setLayout(new BoxLayout(mainVarPanel, BoxLayout.Y_AXIS));
        if (kpmSettings.IS_BATCH_RUN) {
            varPanel.setBorder(BorderFactory.createTitledBorder("Select Parameters"));
            JScrollPane varScrollPane = new JScrollPane(varPanel);
            mainVarPanel.add(varScrollPane);
        }
        if (fixLAdded > 0) {
            String lid = kpmSettings.MIN_L.keySet().iterator().next();
            if (kpmSettings.CALCULATE_ONLY_SAME_L_VALUES && (kpmSettings.MIN_PER == kpmSettings.MAX_PER)
                    && CyGlobals.L_InPercentageMap.containsKey(lid) && CyGlobals.L_InPercentageMap.get(lid)) {
                JPanel lPanel = new JPanel();
                JLabel lLabel = new JLabel("Collective dataset values, in %:");
                final JLabel lLabelValue = new JLabel(String.valueOf(kpmSettings.MIN_PER));
                lPanel.setLayout(new BoxLayout(lPanel, BoxLayout.X_AXIS));
                lPanel.add(lLabel, BorderLayout.WEST);
                lPanel.add(lLabelValue, BorderLayout.CENTER);
                lPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
                fixPanel.add(lPanel);
            }

        }

        fixPanel.setBorder(BorderFactory.createTitledBorder("Fixed Parameters"));
        JScrollPane fixScrollPane = new JScrollPane(fixPanel);
        mainVarPanel.add(fixScrollPane);

        JPanel colorPanel = createVisualPanel();
        colorPanel.setBorder(BorderFactory.createTitledBorder("Pathway Coloring"));
        mainVarPanel.add(colorPanel);
        resultsPanel = new ViewResultsPanel();
        resultsPanel.setBorder(BorderFactory.createTitledBorder("Pathways:"));
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                mainVarPanel, resultsPanel);

        add(splitPane);
    }

    private void markColorButtonRed() {
        this.colorGraphButton.setForeground(Color.RED);
    }

    public JPanel getSlider(int minVal, int stepVal, int maxVal, ChangeListener changeListener, String tooltip, String lid) {

        JSlider slider = new JSlider(SwingConstants.HORIZONTAL);

        slider.setMinimum(minVal);
        slider.setMaximum(maxVal);
        slider.setValue(minVal);
        slider.setMinorTickSpacing(stepVal);
        slider.setMajorTickSpacing(stepVal);

        slider.setPaintTicks(true);
        slider.setSnapToTicks(true);
        slider.setPaintTrack(true);
        slider.setPaintLabels(true);

        if (changeListener != null) {
            slider.addChangeListener(changeListener);
        }

        slider.setToolTipText(tooltip);

        JPanel sliderPanel = new JPanel();
        sliderPanel.add(slider);
        sliderPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        return sliderPanel;
    }

    @SuppressWarnings("unchecked")
    private JPanel createVisualPanel() {
        JPanel panel = new JPanel();
        JLabel pLabel = new JLabel("Color by: ");
        JPanel pPanel = new JPanel();
        pPanel.setLayout(new BoxLayout(pPanel, BoxLayout.X_AXIS));
        pPanel.add(pLabel, BorderLayout.WEST);
        panel.add(pPanel);

        Map<Integer, String> indexMap = new HashMap<Integer, String>();
        indexMap.put(0, "Active Cases");
        indexMap.put(1, "Mapped");
        indexMap.put(2, "Pathway Hits");

        int excpIndex = 3;
        if (kpmSettings.IS_BATCH_RUN) {
            indexMap.put(3, "Pathway Hits (all runs)");
            excpIndex++;
        }
        if (isInes) {
            indexMap.put(excpIndex, "Exceptions (INEs)");
        }

        final String[] options = new String[indexMap.size()];
        for (int ind : indexMap.keySet()) {
            options[ind] = indexMap.get(ind);
        }
        JComboBox<String> selectList = new JComboBox<String>(options);
        if (!kpmSettings.IS_BATCH_RUN) {
            selectList.setSelectedIndex(2);
        } else {
            selectList.setSelectedIndex(3);
        }

        selectList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox<String> menu = (JComboBox<String>) e.getSource();

                int index = menu.getSelectedIndex();
                currentStyle = hitsStyle;
                switch (index) {
                    case 0:
                        currentStyle = activeCasesStyleAll;
                        break;
                    case 1:
                        currentStyle = backNodesStyle;
                        break;
                    case 2:
                        currentStyle = hitsStyle;
                        break;
                    case 3:
                        if (kpmSettings.IS_BATCH_RUN) {
                            currentStyle = totalHitsStyle;
                        } else {
                            currentStyle = exceptionsStyle;
                        }
                        break;
                    case 4:
                        currentStyle = exceptionsStyle;
                        break;
                    default:
                        currentStyle = hitsStyle;
                }

                markColorButtonRed();
            }
        });
        panel.add(selectList);

        panel.add(colorGraphButton);
        return panel;
    }

    private void createKPMAttributes() {
        if (CyGlobals.WORKING_GRAPH == null) {
            return;
        }

        CyTable nodeTable = CyGlobals.WORKING_GRAPH.getDefaultNodeTable();
        CyTable edgeTable = CyGlobals.WORKING_GRAPH.getDefaultEdgeTable();

        if (nodeTable.getColumn(CytoscapeFieldNames.NODE_EXCEPTION_PROPERTY_NAME)
                == null) {
            nodeTable.createColumn(CytoscapeFieldNames.NODE_EXCEPTION_PROPERTY_NAME,
                    Boolean.class, false);
        }

        if (nodeTable.getColumn(CytoscapeFieldNames.NODE_HITS_PROPERTY_NAME)
                == null) {
            nodeTable.createColumn(CytoscapeFieldNames.NODE_HITS_PROPERTY_NAME,
                    Integer.class, false);
        }

        if (nodeTable.getColumn(CytoscapeFieldNames.NODE_HITS_NORMALIZED_PROPERTY_NAME)
                == null) {
            nodeTable.createColumn(CytoscapeFieldNames.NODE_HITS_NORMALIZED_PROPERTY_NAME,
                    Double.class, false);
        }

        if (nodeTable.getColumn(CytoscapeFieldNames.NODE_EXCEPTION_PROPERTY_NAME)
                == null) {
            nodeTable.createColumn(CytoscapeFieldNames.NODE_EXCEPTION_PROPERTY_NAME,
                    Boolean.class, false);
        }

        if (edgeTable.getColumn(CytoscapeFieldNames.EDGE_HITS_PROPERTY_NAME)
                == null) {
            edgeTable.createColumn(CytoscapeFieldNames.EDGE_HITS_PROPERTY_NAME,
                    Integer.class, false);
        }

        if (edgeTable.getColumn(CytoscapeFieldNames.EDGE_HITS_NORMALIZED_PROPERTY_NAME)
                == null) {
            edgeTable.createColumn(CytoscapeFieldNames.EDGE_HITS_NORMALIZED_PROPERTY_NAME,
                    Double.class, false);
        }

        if (edgeTable.getColumn(CytoscapeFieldNames.EDGE_WIDTH_BY_HITS_PROPERTY_NAME)
                == null) {
            edgeTable.createColumn(CytoscapeFieldNames.EDGE_WIDTH_BY_HITS_PROPERTY_NAME,
                    Double.class, false);
        }
        RunStats rs;
        if (kpmSettings.CALCULATE_ONLY_SAME_L_VALUES) {
            rs = kpmSettings.STATS_MAP_PER.get(currentValuesPercentages);
        } else {
            rs = kpmSettings.STATS_MAP.get(currentValues);
        }

        if (rs == null) {
            System.out.println("rs == null");
        }
        List<CyNode> cyNodes = KPMUtilities.getCyNodes(CyGlobals.WORKING_GRAPH,
                kpmSettings.MAIN_GRAPH.getNodeIdSet());

        for (CyNode cyNode : cyNodes) {
            boolean isException = false;

            if (rs != null && rs.getExceptionMap() != null && rs.getExceptionMap().containsKey(KPMUtilities.getCyNodeName(
                    CyGlobals.WORKING_GRAPH, cyNode))) {
                isException = rs.getExceptionMap().get(KPMUtilities.getCyNodeName(
                        CyGlobals.WORKING_GRAPH, cyNode));
            }

            CyRow nodeRow = nodeTable.getRow(cyNode.getSUID());
            nodeRow.set(CytoscapeFieldNames.NODE_EXCEPTION_PROPERTY_NAME, isException);
        }
    }

    private void createVisualStyles() {
        createKPMAttributes();
        KPMVisualStyleFactory kpmvsFactory = new KPMVisualStyleFactory();
        totalHitsStyle = kpmvsFactory.createKPMTotalHitsWExceptionsNormStyle();
        hitsStyle = kpmvsFactory.createKPMHitsWExceptionsNormStyle();
        exceptionsStyle = kpmvsFactory.createKPMExceptionNodesStyle();
        backNodesStyle = kpmvsFactory.createKPMMappingsWExceptionsStyle();
        activeCasesStyleAll = kpmvsFactory.createKPMExpressionWExceptionsStyle();
        if (kpmSettings.IS_BATCH_RUN) {
            currentStyle = totalHitsStyle;
        } else {
            currentStyle = hitsStyle;
        }
    }

    private void updateHitsAttribute() {
        if (kpmSettings == null || kpmSettings.STATS_MAP == null || CyGlobals.WORKING_GRAPH == null) {
            return;
        }

        RunStats rs;
        if (kpmSettings.CALCULATE_ONLY_SAME_L_VALUES) {
            rs = kpmSettings.STATS_MAP_PER.get(currentValuesPercentages);
        } else {
            rs = kpmSettings.STATS_MAP.get(currentValues);
        }

        CyTable nodeTable = CyGlobals.WORKING_GRAPH.getDefaultNodeTable();

        for (CyNode cyNode : CyGlobals.WORKING_GRAPH.getNodeList()) {
            String nodeName = KPMUtilities.getCyNodeName(
                    CyGlobals.WORKING_GRAPH, cyNode).trim();

            if (!rs.getExceptionMap().containsKey(nodeName)) {
                continue;
            }

            boolean isException = rs.getExceptionMap().get(nodeName);
            CyRow nodeRow = nodeTable.getRow(cyNode.getSUID());
            nodeRow.set(CytoscapeFieldNames.NODE_EXCEPTION_PROPERTY_NAME, isException);
            nodeRow.set(CytoscapeFieldNames.NODE_HITS_PROPERTY_NAME, rs.getNodeHits().get(nodeName));
            nodeRow.set(CytoscapeFieldNames.NODE_HITS_NORMALIZED_PROPERTY_NAME, rs.getNodeHitsNorm().get(nodeName));
        }

        for (GeneEdge edge : kpmSettings.MAIN_GRAPH.getEdges()) {
            String edgeId = edge.getEdgeId();
            if (this.edgeRowMap.containsKey(edgeId)) {
                CyRow edgeRow = this.edgeRowMap.get(edgeId);

                if (!rs.getEdgeHits().containsKey(edgeId)) {
                    continue;
                }

                int edgeHits = rs.getEdgeHits().get(edgeId);
                edgeRow.set(CytoscapeFieldNames.EDGE_HITS_PROPERTY_NAME, edgeHits);
                double edgeHitsNorm = rs.getEdgeHitsNorm().get(edgeId);
                double width = edgeHitsNorm * 10.0;
                if (edgeHits == 0) {
                    width = 1.0;
                }
                edgeRow.set(CytoscapeFieldNames.EDGE_HITS_NORMALIZED_PROPERTY_NAME, edgeHitsNorm);
                edgeRow.set(CytoscapeFieldNames.EDGE_WIDTH_BY_HITS_PROPERTY_NAME, width);
            }
        }
    }

    private void updateView() {

        updateHitsAttribute();

        if (CyProvider.vmmServiceRef == null || CyProvider.networkViewManager == null) {
            return;
        }

        CyProvider.vmmServiceRef.setCurrentVisualStyle(currentStyle);

        CyNetwork currentNetwork = CyProvider.appManager.getCurrentNetwork();
        CyNetworkView currentView;

        if (CyProvider.networkViewManager.getNetworkViewSet().isEmpty()) {
            currentView = CyProvider.networkViewFactory.createNetworkView(currentNetwork);
            CyProvider.networkViewManager.addNetworkView(currentView);

        } else if (!CyProvider.networkViewManager.viewExists(currentNetwork)) {
            currentView = CyProvider.networkViewFactory.createNetworkView(currentNetwork);
            CyProvider.networkViewManager.addNetworkView(currentView);

        } else {
            currentView = CyProvider.networkViewManager.getNetworkViews(currentNetwork).iterator().next();
        }

        CyProvider.appManager.setCurrentNetworkView(currentView);
        currentView.updateView();
    }

    public static void clearResults() {
        if (kpmSettings == null) {
            return;
        }

        CyGlobals.ClearEdgeRowMap();
        if (kpmSettings.STATS_MAP != null) {
            kpmSettings.STATS_MAP.clear();
        }
        if (kpmSettings.STATS_MAP_PER != null) {
            kpmSettings.STATS_MAP_PER.clear();
        }
        kpmSettings.TOTAL_NODE_HITS.clear();
        kpmSettings.TOTAL_EDGE_HITS.clear();
        kpmSettings.TOTAL_NODE_HITS_MAX = kpmSettings.TOTAL_EDGE_HITS_MAX = 0;
        kpmSettings.TOTAL_NODE_HITS_MIN = kpmSettings.TOTAL_EDGE_HITS_MIN = Integer.MAX_VALUE;

        CyTable nodeTable = CyGlobals.WORKING_GRAPH.getDefaultNodeTable();
        CyTable edgeTable = CyGlobals.WORKING_GRAPH.getDefaultEdgeTable();

        for (CyNode cyNode : CyGlobals.WORKING_GRAPH.getNodeList()) {
            CyRow nodeRow = nodeTable.getRow(cyNode.getSUID());

            if (nodeRow.isSet(CytoscapeFieldNames.NODE_TOTAL_HITS_PROPERTY_NAME)) {
                nodeRow.set(CytoscapeFieldNames.NODE_TOTAL_HITS_PROPERTY_NAME, 0);
            }

            if (nodeRow.isSet(CytoscapeFieldNames.NODE_TOTAL_HITS_NORMALIZED_PROPERTY_NAME)) {
                nodeRow.set(CytoscapeFieldNames.NODE_TOTAL_HITS_NORMALIZED_PROPERTY_NAME, 0.0);
            }
        }

        for (CyEdge cyEdge : CyGlobals.WORKING_GRAPH.getEdgeList()) {
            CyRow edgeRow = edgeTable.getRow(cyEdge.getSUID());

            if (edgeRow.isSet(CytoscapeFieldNames.EDGE_TOTAL_HITS_PROPERTY_NAME)) {
                edgeRow.set(CytoscapeFieldNames.EDGE_TOTAL_HITS_PROPERTY_NAME, 0);
            }

            if (edgeRow.isSet(CytoscapeFieldNames.EDGE_TOTAL_HITS_NORMALIZED_PROPERTY_NAME)) {
                edgeRow.set(CytoscapeFieldNames.EDGE_TOTAL_HITS_NORMALIZED_PROPERTY_NAME, 0.0);
            }

            if (edgeRow.isSet(CytoscapeFieldNames.EDGE_WIDTH_BY_HITS_PROPERTY_NAME)) {
                edgeRow.set(CytoscapeFieldNames.EDGE_WIDTH_BY_HITS_PROPERTY_NAME, 5.0);
            }

            if (edgeRow.isSet(CytoscapeFieldNames.EDGE_WIDTH_BY_TOTAL_HITS_PROPERTY_NAME)) {
                edgeRow.set(CytoscapeFieldNames.EDGE_WIDTH_BY_TOTAL_HITS_PROPERTY_NAME, 5.0);
            }
        }

    }

    public class ViewResultsPanel extends JPanel {

        /**
         *
         */
        private static final long serialVersionUID = 3334402554334657680L;
        public Object[][] values;
        private final String[] cols;
        public final JButton createView;
        public final JButton saveSelected;
        public final JButton closeButton;
        public final JPanel buttonPanel;
        public JTable pathwayTable;
        public RunStats runStats;
        public TableRowSorter<TableModel> sorter;

        public ViewResultsPanel() {
            super();
            String[] stdCols = {"ID", "Nodes", "Edges", "Avg. Exp.", "Info. Content"};
            if (kpmSettings.containsGoldStandardNodes()) {
                String[] cols2 = new String[stdCols.length + 2];

                for (int i = 0; i < stdCols.length; i++) {
                    cols2[i] = stdCols[i];
                }

                cols2[stdCols.length] = "overlap";
                cols2[stdCols.length + 1] = "jaccard";
                cols = cols2;
            } else {
                cols = stdCols;
            }

            setLayout(new BorderLayout());
            add(createTable(), BorderLayout.CENTER);
            buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
            createView = new JButton("View Selected");
            createView.addActionListener(createViewPathway());
            saveSelected = new JButton("Save Selected");
            saveSelected.addActionListener(savePathways());
            closeButton = new JButton("Close");
            closeButton.addActionListener(closeTab());
            buttonPanel.add(closeButton);
            buttonPanel.add(createView);
            buttonPanel.add(saveSelected);

            add(buttonPanel, BorderLayout.SOUTH);

        }

        private TableModel createTableModel() {
            Object[][] values = new Object[0][0];
            if (kpmSettings != null) {
                if (kpmSettings.CALCULATE_ONLY_SAME_L_VALUES && kpmSettings.STATS_MAP_PER != null) {
                    runStats = kpmSettings.STATS_MAP_PER.get(currentValuesPercentages);
                } else if (kpmSettings.STATS_MAP != null) {
                    runStats = kpmSettings.STATS_MAP.get(currentValues);
                }

                values = runStats.getValues();
            }

            return new DefaultTableModel(values, cols) {
                private static final long serialVersionUID = -8716524303691813157L;

                public Class<?> getColumnClass(int column) {

                    String nullStr = "";
                    if (column >= this.getColumnCount() || getValueAt(0, column) == null) {
                        return nullStr.getClass();
                    }

                    return getValueAt(0, column).getClass();
                }

                @Override
                public boolean isCellEditable(int row, int column) {
                    //all cells false
                    return false;
                }
            };
        }

        public void updateTableModel() {
            TableModel model = createTableModel();
            pathwayTable.setModel(model);
            sorter.setModel(model);
            pathwayTable.setRowSorter(sorter);
            pathwayTable.validate();
        }

        public JScrollPane createTable() {

            TableModel model = createTableModel();
            pathwayTable = new JTable(model);
            pathwayTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) { // check if a double click
                        createViewPathway();
                    }

                    super.mouseClicked(e);
                }
            });
            sorter = new TableRowSorter<TableModel>(model);

            pathwayTable.setRowSorter(sorter);
            pathwayTable.setPreferredScrollableViewportSize(new Dimension(Integer.MAX_VALUE,
                    pathwayTable.getRowHeight() * 10));

            pathwayTable.getSelectionModel().addListSelectionListener(
                    new ListSelectionListener() {
                        @Override
                        public void valueChanged(ListSelectionEvent e) {

                            if (pathwayTable.getSelectedRowCount() == 1) {

                                CyNetworkView currentNetworkView = CyProvider.appManager
                                .getCurrentNetworkView();
                                CyNetwork currentNetwork = parentNetwork;
                                if (currentNetworkView == null) {
                                    Set<CyNetworkView> views
                                    = CyProvider.networkViewManager.getNetworkViewSet();
                                    if (!views.isEmpty()) {
                                        if (CyProvider.networkViewManager.viewExists(parentNetwork)) {
                                            CyProvider.appManager.setCurrentNetwork(parentNetwork);
                                            currentNetworkView
                                            = CyProvider.networkViewManager.getNetworkViews(parentNetwork).iterator().next();
                                        } else {
                                            CyNetworkView auxView = views.iterator().next();
                                            CyProvider.appManager.setCurrentNetworkView(auxView);
                                            currentNetwork = CyProvider.appManager.getCurrentNetwork();
                                            currentNetworkView = auxView;
                                        }
                                    }
                                } else {
                                    currentNetwork = CyProvider.appManager.getCurrentNetwork();
                                }
                                List<CyNode> selectedNodes
                                = CyTableUtil.getNodesInState(currentNetwork,
                                        CyNetwork.SELECTED, true);
                                KPMUtilities.unselectNodes(currentNetwork, selectedNodes);

                                int viewRow = pathwayTable.getSelectedRow();
                                int row = pathwayTable
                                .convertRowIndexToModel(viewRow);

                                Result pathway = runStats.getResults().get(row);
                                ArrayList<GeneNode> nodes = new ArrayList<GeneNode>(
                                        pathway.getVisitedNodes().values());
                                List<String> nodeList = new ArrayList<String>(nodes.size());

                                for (GeneNode node : nodes) {
                                    nodeList.add(node.getNodeId());
                                }

                                List<CyNode> cyNodes = KPMUtilities.getCyNodes(
                                        currentNetwork, nodeList);
                                KPMUtilities.selectNodes(currentNetwork, cyNodes);

                            }
                        }
                    });

            return new JScrollPane(pathwayTable);
        }

        private ActionListener createViewPathway() {
            ActionListener listener = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // The do-nothing-action.
                }
            };

            if (kpmSettings == null) {
                return listener;
            }

            try {
                RunStats res = null;
                if (kpmSettings.CALCULATE_ONLY_SAME_L_VALUES) {
                    res = kpmSettings.STATS_MAP_PER.get(currentValuesPercentages);
                } else if (kpmSettings.STATS_MAP != null) {
                    res = kpmSettings.STATS_MAP.get(currentValues);
                }

                if (res == null) {
                    DialogUtils.showNonModalDialog("No run statistics were found.", "Results warning", DialogUtils.MessageTypes.Warn);
                    return listener;
                }

                List<Result> results = res.getResults();
                if (res == null || results.size() == 0) {
                    DialogUtils.showNonModalDialog("No run statistics were found.", "Results warning", DialogUtils.MessageTypes.Warn);
                    return listener;
                }

                if (results.size() == 0) {
                    DialogUtils.showNonModalDialog("No run results were found.", "Results warning", DialogUtils.MessageTypes.Warn);
                    return listener;
                }

                final DecimalFormat resultFormat = KPMUtilities.getFormatedInt(results.size());

                listener = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        RunStats rs = null;
                        if (kpmSettings.CALCULATE_ONLY_SAME_L_VALUES) {
                            rs = kpmSettings.STATS_MAP_PER.get(currentValuesPercentages);
                        } else if (kpmSettings.STATS_MAP != null) {
                            rs = kpmSettings.STATS_MAP.get(currentValues);
                        }
                        int[] rows = pathwayTable.getSelectedRows();

                        for (int row : rows) {
                            row = pathwayTable.convertRowIndexToModel(row);

                            Result pathway = rs.getResults().get(row);
                            String pathwayRank = resultFormat.format(row + 1);
                            String pathwayTitle = rs.getRunId() + "-Pathway-" + pathwayRank;
                            List<CyNode> cyNodes = KPMUtilities.getCyNodes(parentNetwork,
                                    pathway.getVisitedNodes().keySet());
                            KPMUtilities.unselectAllNodes(parentNetwork);
                            KPMUtilities.selectNodes(parentNetwork, cyNodes);
                            KPMCreateSelectedPathwayTask createPathwayTask
                                    = new KPMCreateSelectedPathwayTask(pathwayTitle, parentNetwork, currentStyle);
                            CyProvider.taskManager.execute(new TaskIterator(createPathwayTask));
                        }
                    }
                };
            } catch (Exception e) {
                KpmLogger.log(Level.SEVERE, e);
            }

            return listener;
        }

        private ActionListener closeTab() {
            return new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Object[] options = {"OK", "Cancel"};
                    int chose = JOptionPane
                            .showOptionDialog(
                                    ViewResultsPanel.this.createView,
                                    "All results will be lost if not saved. Are you sure you want to close this tab? ",
                                    "Warning", JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.WARNING_MESSAGE, null, options,
                                    options[0]);

                    if (chose == 0) {
                        clearResults();
                        int remove = parentPane.indexOfTab(CytoscapePanelNames.RESULTSNAME);
                        parentPane.removeTabAt(remove);
                        int index = parentPane.indexOfTab(CytoscapePanelNames.INPUTNAME);
                        parentPane.setSelectedIndex(index);

                        parentPane.revalidate();
                        CyGlobals.HAS_RESULTS_TAB = false;
                    }

                }
            };
        }

        private ActionListener savePathways() {

            RunStats rs = null;
            if (kpmSettings.CALCULATE_ONLY_SAME_L_VALUES && kpmSettings.STATS_MAP_PER.containsKey(currentValuesPercentages)) {
                rs = kpmSettings.STATS_MAP_PER.get(currentValuesPercentages);
            } else if (kpmSettings.STATS_MAP != null && kpmSettings.STATS_MAP.containsKey(currentValues)) {
                rs = kpmSettings.STATS_MAP.get(currentValues);
            }

            if (kpmSettings == null || kpmSettings.STATS_MAP == null || rs == null) {
                return new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // The do-nothing-action.
                    }
                };
            }

            final DecimalFormat resultFormat = KPMUtilities.getFormatedInt(rs.getResults().size());

            return new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser f = new JFileChooser();
                    f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    int returnVal = f.showSaveDialog(parentPane);
                    //f.setFileFilter(new NetworkFileFilter());
                    //f.addChoosableFileFilter(new NetworkFileFilter());

                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        int[] rows = pathwayTable.getSelectedRows();
                        File path = f.getSelectedFile();
                        boolean withErrors = false;
                        for (int row : rows) {
                            row = pathwayTable.convertRowIndexToModel(row);
                            RunStats rs = null;
                            if (kpmSettings.CALCULATE_ONLY_SAME_L_VALUES && kpmSettings.STATS_MAP_PER.containsKey(currentValuesPercentages)) {
                                rs = kpmSettings.STATS_MAP_PER.get(currentValuesPercentages);
                            } else if (kpmSettings.STATS_MAP != null && kpmSettings.STATS_MAP.containsKey(currentValues)) {
                                rs = kpmSettings.STATS_MAP.get(currentValues);
                            }
                            Result pathway = rs.getResults().get(row);
                            String pathwayRank = resultFormat.format(row + 1);
                            String pathwayTitle = rs.getRunId() + "-Pathway-" + pathwayRank;
                            List<CyNode> cyNodes = KPMUtilities.getCyNodes(parentNetwork,
                                    pathway.getVisitedNodes().keySet());
                            KPMUtilities.unselectAllNodes(parentNetwork);
                            KPMUtilities.selectNodes(parentNetwork, cyNodes);

                            KPMSaveSelectedPathwayTask savePathwayTask
                                    = new KPMSaveSelectedPathwayTask(path,
                                            pathwayTitle, parentNetwork);
                            try {
                                CyProvider.taskManager.execute(new TaskIterator(savePathwayTask));
                            } catch (Exception ex) {
                                withErrors = true;
                                Logger.getLogger(KPMResultsTab.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        if (withErrors) {

                            DialogUtils.showNonModalDialog("Files could not be written, please check write permissions",
                                    "Error", DialogUtils.MessageTypes.Error);
                        } else {
                            DialogUtils.showNonModalDialog("Files written to directory",
                                    "", DialogUtils.MessageTypes.Info);
                            JOptionPane.showMessageDialog(null, "Files written to directory");
                        }

                    }

                }
            };
        }
    }
}
