package de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels;

import de.mpg.mpiinf.csb.kpmcytoplugin.CyGlobals;
import de.mpg.mpiinf.csb.kpmcytoplugin.CyProvider;
import de.mpg.mpiinf.csb.kpmcytoplugin.gui.SpringUtilities;
import de.mpg.mpiinf.csb.kpmcytoplugin.gui.actions.ParameterTextFieldListener;
import de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels.robustness.KPMRobustnessPanel;
import de.mpg.mpiinf.csb.kpmcytoplugin.interfaces.ISearchEnabledListener;
import dk.sdu.kpm.Algo;
import org.cytoscape.model.CyNetwork;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Set;


/**
 * The panel where the algorithm is chosen and parameters of the algorithms such
 * as K and L are specified.
 *
 * @author ajunge
 *
 */
public class KPMParameterTab extends KPMTab implements ISearchEnabledListener{

    private static final String storePathwaysCheckBoxLabel = "Check to store detailed information on the pathways";
    /**
     * A sub-panel where advanced parameters for the ACO algorithm are set.
     */
    private final AdvancedParameterPanel app;
    /**
     * A sub-panel where the K and L parameters for all data sets can be
     * specified.
     */
    private final KLPanel klp;
    /**
     * Names of the algorithms offered to the user.
     */
    private final String[] algoNames = new String[]{"Greedy", "ACO", "Exact (FPT)"};
    /**
     * Names of the strategies offered to the user.
     */
    private final String[] strategyNames = new String[]{"INES", "GLONE"};
    /**
     * In this box, the user can choose the algorithm to use.
     */
    private final JComboBox<String> algoMenu;
    /**
     * The strategy chosen by the user.
     */
    private final JComboBox<String> strategyMenu;
    /**
     * The current graph we are working on
     */
    private final JComboBox<String> graphMenu;
    
    private final JCheckBox removeBENsSetting;
    /**
     * A slider for choosing the number of processors for the computations.
     */
    private final JSlider procSlider;
    /**
     * The button which starts the search for key pathways.
     */
    private final JButton searchPathwaysButton;
    /**
     * Number of key pathways that are returned as a solution.
     */
    private final JFormattedTextField numberOfPathwaysField;
    /**
     * Allows the user to select the treatment of nodes not contained in an
     * experiment.
     */
    private final JComboBox<String> treatUnmappedNodesBox;
    /**
     * Spacer for the {@link AdvancedParameterPanel}. Used it the latter is
     * closed.
     */
    private final JPanel appDummyPanel;

    /**
     * Constructs a new ParameterPanel which allows the user to choose the
     * algorithm to be used and to set various parameters such as K and L.
     *
     * @param kpmmp - The central KPMMainPanel reference needed for running the
     * algorithm.
     * @param kpmtp - The {@link KPMTabbedPane} containing this panel.
     * @param tabName - The name of this panel in the corresponding
     * {@link KPMTabbedPane}.
     */
    public KPMParameterTab(KPMMainPanel kpmmp, KPMTabbedPane kpmtp, String tabName) {
        super(kpmtp, tabName);
                
        app = new AdvancedParameterPanel(this);
        appDummyPanel = new JPanel();
        app.setVisible(false);

        klp = new KLPanel(this);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));        
        JPanel p1 = new JPanel(new SpringLayout());
              
        
        

        JLabel algoLabel = new JLabel("Search algorithm: ");
        algoMenu = new JComboBox<String>(algoNames);
        /*
      The index of the algorithm chosen by default, normally the greedy
      algorithm for INEs.
     */
        int defaultAlgoIndex = 0;
        algoMenu.setSelectedIndex(defaultAlgoIndex);
        algoMenu.setPreferredSize(new Dimension(145, 25));
        algoMenu.setToolTipText("Please select the algorithm for extracting pathways");
        algoMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JComboBox<String> menu = (JComboBox<String>) ae.getSource();
                int index = menu.getSelectedIndex();
                String algorithm = algoNames[index];

                // Show the AdvancedParameterPanel iff the ACO algorithm
                // was chosen.
                if (algorithm.equals("ACO")) {
                    // app.setCollapsed(false);
                    app.setVisible(true);
                    appDummyPanel.setVisible(false);
                    // app.validate();
                } else if (algorithm.equals("Greedy")) {
                    // app.setCollapsed(true);
                    app.setVisible(false);
                    appDummyPanel.setVisible(true);

                } else if (algorithm.equals("Exact (FPT)")) {
                    JOptionPane
                            .showConfirmDialog(
                            menu,
                            "This method runs in exponential time and can take a considerable time"
                            + CyGlobals.KPM.lineSep
                            + " for a large number of gene (K) and case exceptions (L)."
                            + CyGlobals.KPM.lineSep
                            + "Furthermore, concurrency is not supported.",
                            "Warning", JOptionPane.DEFAULT_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    // app.setCollapsed(true);
                    app.setVisible(false);
                    appDummyPanel.setVisible(true);

                }
                app.updateUI();
                checkEnableProcessorSlider(app.isIterationBasedGlone());
            }
        });
        algoLabel.setLabelFor(algoMenu);
        p1.add(algoLabel);
        p1.add(algoMenu);  
        
        

        JLabel strategyLabel = new JLabel("Search strategy: ");        
        strategyMenu = new JComboBox<String>(strategyNames);
        /*
      Index of the strategy chosen by default, normally INEs.
     */
        int defaultStrategyIndex = 0;
        strategyMenu.setSelectedIndex(defaultStrategyIndex);
        strategyMenu.setPreferredSize(new Dimension(145, 25));
        strategyMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JComboBox<String> menu = (JComboBox<String>) ae.getSource();
                int index = menu.getSelectedIndex();
                String strategy = strategyNames[index];

                // Show the AdvancedParameterPanel iff the ACO algorithm
                // was chosen.
                if (strategy.equals("INES")) {
                    // Enable gene exceptions textField.
                    klp.setKParameterEnabled(true);
                    if (klp.getNumberOfRangesSpecified() >= 2
                            && klp.getGeneExceptionsPanel().isBatchRunMode()) {
                        //klp.getGeneExceptionsPanel().setRangePanel(false);
                        klp.getGeneExceptionsPanel().setBatchRunCheckBoxSelected(false);
                    }
                    CyGlobals.KPM.USE_INES = true;
                } else if (strategy.equals("GLONE")) {
                    // Disable gene exceptions textField.
                    klp.setKParameterEnabled(false);
                    if (klp.getGeneExceptionsPanel().isBatchRunMode()) {
                        //klp.getGeneExceptionsPanel().setRangePanel(false);
                        klp.getGeneExceptionsPanel().setBatchRunCheckBoxSelected(false);
                    }
                    CyGlobals.KPM.USE_INES = false;
                }
                checkEnableProcessorSlider(app.isIterationBasedGlone());
                checkCanRemoveBENs(strategy);
            }
        });
        strategyMenu.setToolTipText("Please select the strategy for extracting pathways");
        strategyLabel.setLabelFor(strategyMenu);
        p1.add(strategyLabel);
        p1.add(strategyMenu);
        
        
        

        JLabel graphLabel = new JLabel("Search graph: ");
        graphMenu = new JComboBox<String>();        
        graphMenu.setPreferredSize(new Dimension(145, 25));
        if(CyProvider.networkManager != null){
        setInitialGraphSelectionModel(CyProvider.networkManager.getNetworkSet());
        }
        graphMenu.setToolTipText("Please select the graph for extracting pathways");
        graphMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JComboBox<String> menu = (JComboBox<String>) ae.getSource();
                String networkTitle = (String) menu.getSelectedItem();
                
                CyGlobals.WORKING_GRAPH = CyProvider.networkManager != null ? CyProvider.networkManager.getNetwork(
                        CyGlobals.TITLE_TO_NETWORK_ID_MAP.get(networkTitle)) : null;
            }
        });
        graphLabel.setLabelFor(graphMenu);
        p1.add(graphLabel);
        p1.add(graphMenu);
  

        // Add checkbox for removing BENs.
        JLabel benLabel = new JLabel("Remove border exception nodes:");
        benLabel.setToolTipText("Border Exception Nodes");
        removeBENsSetting = new JCheckBox();
        removeBENsSetting.setToolTipText("Border Exception Nodes");
        removeBENsSetting.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(removeBENsSetting.isSelected()){
					CyGlobals.KPM.REMOVE_BENs = true;
					//System.out.println("KPMParameters.REMOVE_BENs = true;");
				}else{
					CyGlobals.KPM.REMOVE_BENs = false;
					//System.out.println("KPMParameters.REMOVE_BENs = false;");
				}
			}
		});
        removeBENsSetting.setToolTipText("Only enabled if the search strategy is set to INES.");
        removeBENsSetting.setSelected(true);
        benLabel.setLabelFor(removeBENsSetting);
        p1.add(benLabel);
        p1.add(removeBENsSetting);
            
      
        int procNum = Runtime.getRuntime().availableProcessors();
        procSlider = new JSlider(1, procNum, procNum);
        procSlider.setMajorTickSpacing(1);
        procSlider.setPaintTicks(true);
        procSlider.setSnapToTicks(true);
        procSlider.setPaintLabels(true);
        procSlider.setPaintTrack(true);
        JLabel processorLabel = new JLabel("Number of processors: ");
        String procSliderTip = "Please select the number of processors used for parallel computing (Max: " + procNum + ")";
        processorLabel.setToolTipText(procSliderTip);
        processorLabel.setLabelFor(procSlider);
        p1.add(processorLabel);
        p1.add(procSlider);
        
        
        
        // ComboBox for selecting treatment of nodes not contained in the data
        // set.
        JLabel treatUnmappedNodesLabel = new JLabel("Treat unmapped nodes: ");
        /*
      The options offered for treating unmapped nodes
     */
        String[] unMappedNodesOptions = new String[]{
                "Add to negative list", "Add to positive list"};
        treatUnmappedNodesBox = new JComboBox<String>(unMappedNodesOptions);
        treatUnmappedNodesBox.setPreferredSize(new Dimension(145, 25));
        treatUnmappedNodesBox.setSelectedIndex(0);
        treatUnmappedNodesBox.setToolTipText("Please choose how nodes not contained in the experiment(s) should be treated");
        treatUnmappedNodesLabel.setLabelFor(treatUnmappedNodesBox);
        p1.add(treatUnmappedNodesLabel);
        p1.add(treatUnmappedNodesBox);
        
        
        // Add text field for specifying the number of pathways to be returned.
        JLabel numberOfPathwaysLabel = new JLabel("# computed pathways:");
        numberOfPathwaysLabel.setToolTipText("(-1 for all)");
        numberOfPathwaysField = new JFormattedTextField(NumberFormat.getIntegerInstance(Locale.ENGLISH));
        numberOfPathwaysField.setPreferredSize(new Dimension(145, 25));
        numberOfPathwaysField.setValue(CyGlobals.KPM.NUM_SOLUTIONS);
        numberOfPathwaysField.addPropertyChangeListener("value", new ParameterTextFieldListener(-1, Double.POSITIVE_INFINITY));
        numberOfPathwaysField.setToolTipText("Please enter an integer");
        numberOfPathwaysLabel.setLabelFor(numberOfPathwaysField);
        p1.add(numberOfPathwaysLabel);
        p1.add(numberOfPathwaysField);
        
        
        // Add button for running KPM.
        String searchPathwaysTip = "Starts the search for key pathways";
        JPanel startButtonPanel = new JPanel();
        searchPathwaysButton = new JButton("Search key pathways");
        searchPathwaysButton.setToolTipText(searchPathwaysTip);
        searchPathwaysButton.addActionListener(new StartButtonListener(CyGlobals.RunTypeEnum.Normal, kpmmp));
        Font oldFont = searchPathwaysButton.getFont();
        Font newFont = new Font(oldFont.getFontName(), Font.BOLD,
                oldFont.getSize());
        searchPathwaysButton.setFont(newFont);
        searchPathwaysButton.setEnabled(false);
        startButtonPanel.add(searchPathwaysButton);

        if(!CyGlobals.SearchEnabledListeners.contains(this)){
			CyGlobals.SearchEnabledListeners.add(this);
		}

        KPMRobustnessPanel robustnessPanel = new KPMRobustnessPanel(kpmmp);

        SpringUtilities.makeCompactGrid(p1, 7, 2, 5, 5, 5, 5);
		p1.setOpaque(true);
		setOpaque(true);
        add(p1);
        add(Box.createVerticalStrut(20));
        add(klp);
        add(Box.createVerticalStrut(20));
        add(startButtonPanel);
        add(app);        
        add(Box.createVerticalStrut(20));
        add(robustnessPanel);
    }
    
    void checkCanRemoveBENs(String newStrategy){
    	if(newStrategy.equals("INES")){
    		if(!removeBENsSetting.isEnabled()){
    			removeBENsSetting.setEnabled(true);
    			removeBENsSetting.setSelected(true);
    			CyGlobals.KPM.REMOVE_BENs = true;
    		}
    	}else{
    		removeBENsSetting.setEnabled(false);
			removeBENsSetting.setSelected(false);
			CyGlobals.KPM.REMOVE_BENs = false;
    	}
    }
    
    /**
     *
     * @return The {@link AdvancedParameterPanel} which is part of this instance
     * and in which advanced ACO algorithm parameters are specified.
     */
    public AdvancedParameterPanel getAdvancedParameterPanel() {
        return app;
    }

    /**
     *
     * @return the currently selected algorithm.
     */
    public Algo getAlgorithm() {
        String strategy = strategyNames[strategyMenu.getSelectedIndex()];
        String algorithm = algoNames[algoMenu.getSelectedIndex()];
        if (strategy.equals("INES")) {
            if (algorithm.equals("Greedy")) {
                return Algo.GREEDY;
            } else if (algorithm.equals("ACO")) {
                return Algo.LCG;
            } else if (algorithm.equals("Exact (FPT)")) {
                return Algo.OPTIMAL;
            } else {
                throw new IllegalStateException("Invalid algorithm chosen "
                        + algorithm);
            }
        } else if (strategy.equals("GLONE")) {
            if (algorithm.equals("Greedy")) {
                return Algo.EXCEPTIONSUMGREEDY;
            } else if (algorithm.equals("ACO")) {
                return Algo.EXCEPTIONSUMACO;
            } else if (algorithm.equals("Exact (FPT)")) {
                return Algo.EXCEPTIONSUMOPTIMAL;
            } else {
                throw new IllegalStateException("Invalid algorithm chosen "
                        + algorithm);
            }
        } else {
            throw new IllegalStateException("Invalid strategy chosen "
                    + strategy);
        }
    }
    
    public void checkEnableProcessorSlider(boolean isIterationBasedACO) {
        Algo algo = getAlgorithm();
        if (algo == Algo.GREEDY || 
                algo == Algo.EXCEPTIONSUMGREEDY ||
                algo == Algo.LCG) {
            procSlider.setEnabled(true);
            
        } else if (algo == Algo.OPTIMAL || 
                algo == Algo.EXCEPTIONSUMOPTIMAL) {
            procSlider.setEnabled(false);
            
        } else if (algo == Algo.EXCEPTIONSUMACO) {
            if (isIterationBasedACO) {
                procSlider.setEnabled(true);
            } else {
                procSlider.setEnabled(false);
            }
        }
    }

    /**
     *
     * @return true if the INEs strategy for identifying pathways was selected.
     */
    public boolean isINEs() {
        String strategy = strategyNames[strategyMenu.getSelectedIndex()];
        if (strategy.equals("INES")) {
            return true;
        } else if (strategy.equals("GLONE")) {
            return false;
        } else {
            throw new IllegalStateException("Invalid strategy chosen "
                    + strategy);
        }
    }

    /**
     *
     * @return The currently selected number of processors to be used.
     */
    public int getNoProcessors() {
        return procSlider.getValue();
    }

    @Override
    public boolean containsError() {
        return false;
    }

    /**
     *
     * @return <code>true</code> iff at least a range of values was specified
     * for at least one parameter.
     */
    public boolean isBatchRunMode() {
        return klp.isBatchRunMode();
    }

    public int getNumberOfPathways() {
        Number value = (Number) numberOfPathwaysField.getValue();
        return value.intValue();
    }

    /**
     * Adds a new sub-panel for setting a single L parameter to the
     * {@link KLPanel}.
     *
     * @param externalName - The external, user-specified name of the data set
     * corresponding to the L parameter.
     * @param internalName - The internal name of the corresponding data set.
     * @param maxNumberOfCaseExceptions
	 *            - The maximal number of case exceptions.
     * @return The newly created and added sub-panel.
     */
    public LParameterPanel addParameterPanel(String externalName,
            String internalName, int maxNumberOfCaseExceptions) {
        return klp.addParameterPanel(externalName, internalName, maxNumberOfCaseExceptions);
    }

    /**
     * Removes a sub-panel, in which an L parameter can be set, from the GUI.
     *
     * @param lpp - The sub-panel to be removed.
     */
    public void deleteParameterPanel(LParameterPanel lpp) {
        klp.deleteParameterPanel(lpp);
    }

    /**
     *
     * @return The {@link KLPanel} where K and L parameters are specified.
     */
    public KLPanel getKLPanel() {
        return klp;
    }

    /**
     * The way nodes not contained in the experiments should be treated.
     *
     * @return 'n' for treating them as nodes in the negative list, 'p' for
     * treating them as nodes in the positive list
     */
    public char treatUnmappedNodes() {
        if (treatUnmappedNodesBox.getSelectedIndex() == 0) {
            return 'n';
        } else {
            return 'p';
        }
    }

    /**
     * Method to modify the search graph selection list depending of available
     * networks.
     *
     * @param destroy Flag that determines if a network was destroyed (due to a
     * bug in cytoscape that fires the NETWORK_DESTROYED event before it has
     * actually been removed from memory, this has to be implemented)
     *
     * @param networkId the network ID of the destroyed networks
     */
    public void setGraphSelectionModel(boolean destroy, long networkId) {
        Set<CyNetwork> networks = CyProvider.networkManager.getNetworkSet();
        if (destroy) {
            CyNetwork destroyedNetwork = CyProvider.networkManager.getNetwork(networkId);
            networks.remove(destroyedNetwork);
            String destroyedNetTitle = 
                    destroyedNetwork.getRow(destroyedNetwork).get(CyNetwork.NAME, String.class);
            if (CyGlobals.TITLE_TO_NETWORK_ID_MAP.containsKey(destroyedNetTitle)) {
                CyGlobals.TITLE_TO_NETWORK_ID_MAP.remove(destroyedNetTitle);
            }
            if (CyGlobals.WORKING_GRAPH.getSUID() == networkId) {
                CyGlobals.WORKING_GRAPH = null;
            }
        } else {
            CyNetwork createdNetwork = CyProvider.networkManager.getNetwork(networkId);
            String createdNetTitle = 
                    createdNetwork.getRow(createdNetwork).get(CyNetwork.NAME, String.class);
            CyGlobals.TITLE_TO_NETWORK_ID_MAP.put(createdNetTitle,
                    networkId);
        }
        setInitialGraphSelectionModel(networks);

    }

    private void setInitialGraphSelectionModel(Set<CyNetwork> networks) {

        String[] graphNames;
        if (networks.isEmpty()) {
            graphNames = new String[]{"-- Please import a network --"};
            graphMenu.setModel(new DefaultComboBoxModel<String>(graphNames));
            graphMenu.setEnabled(false);
            graphMenu.setSelectedIndex(0);
            CyGlobals.WORKING_GRAPH = null;
        } else {
            graphNames = new String[networks.size()];
            int ind = 0;
            for (CyNetwork network : networks) {
                long networkId = network.getSUID();
                String networkTitle = network.getRow(network).get(CyNetwork.NAME, String.class);
                CyGlobals.TITLE_TO_NETWORK_ID_MAP.put(networkTitle, networkId);
                graphNames[ind] = networkTitle;
                ind++;
            }
            graphMenu.setModel(new DefaultComboBoxModel<String>(graphNames));
            CyNetwork focusedNetwork = CyProvider.appManager.getCurrentNetwork();
            
            if (CyGlobals.WORKING_GRAPH != null) {
                graphMenu.setSelectedItem(
                        CyGlobals.WORKING_GRAPH.getRow(CyGlobals.WORKING_GRAPH).get(
                        CyNetwork.NAME, String.class));
            } else if (focusedNetwork == null) {
                long networkId = CyGlobals.TITLE_TO_NETWORK_ID_MAP.get(graphMenu.getItemAt(0));
                graphMenu.setSelectedIndex(0);
                CyGlobals.WORKING_GRAPH = CyProvider.networkManager.getNetwork(networkId);
            } else {
                graphMenu.setSelectedItem(focusedNetwork.getRow(focusedNetwork)
                        .get(CyNetwork.NAME, String.class));
                CyGlobals.WORKING_GRAPH = focusedNetwork;
            }
            graphMenu.setEnabled(true);
        }
        graphMenu.validate();
    }

	@Override
	public void setEnabled() {
		searchPathwaysButton.setEnabled(true);
	}

	@Override
	public void setDisabled() {
		searchPathwaysButton.setEnabled(false);
	}
}
