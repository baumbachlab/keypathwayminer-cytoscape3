package de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels;

// import cytoscape.Cytoscape;
import de.mpg.mpiinf.csb.kpmcytoplugin.CyGlobals;
import de.mpg.mpiinf.csb.kpmcytoplugin.gui.SpringUtilities;
import de.mpg.mpiinf.csb.kpmcytoplugin.gui.actions.ParameterTextFieldListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Offers the possibility to specify advanced parameters for the ant-colony
 * optimization algorithm.
 * 
 * Default values are taken from the {@link de.mpg.mpiinf.csb.kpmcytoplugin.CyGlobals} class.
 * 
 */
public class AdvancedParameterPanel extends JPanel {

	private final JFormattedTextField pheromoneLevelField;
	private final JFormattedTextField edgeImportanceField;
	private final JFormattedTextField pheromoneDecayRateField;
	private final JFormattedTextField maxIterationsField;
	private final JFormattedTextField maxIterationsWOChangeField;
	private final JFormattedTextField minTauField;
	private final JFormattedTextField solutionsPerIterationField;
	private final JFormattedTextField startNodesField;
	private final JComboBox gloneVersionBox;


    @SuppressWarnings("serial")
	public AdvancedParameterPanel(final KPMParameterTab kpmpt) {
        KPMParameterTab kpmpt1 = kpmpt;
        setLayout(new BorderLayout());      
        setBorder(BorderFactory.createTitledBorder("ACO advanced parameters"));
        
        JPanel p1 = new JPanel(new SpringLayout());

		// IDEA 'Default' button which sets all parameters to its default values

		// Alpha parameter
		JLabel phermoneLevel = new JLabel("Importance of pheromone value: ");
		pheromoneLevelField = new JFormattedTextField(
				NumberFormat.getNumberInstance(Locale.ENGLISH));
		String phermoneLevelTip = "Please enter a number greater "
				+ "than 0.0.";
		phermoneLevel.setToolTipText(phermoneLevelTip);
		pheromoneLevelField.setToolTipText(phermoneLevelTip);
		pheromoneLevelField.setPreferredSize(new Dimension(50, 25));
		pheromoneLevelField.setValue(CyGlobals.KPM.ALPHA);
		pheromoneLevelField.addPropertyChangeListener("value",
				new ParameterTextFieldListener(0.0, Double.POSITIVE_INFINITY));
		phermoneLevel.setLabelFor(pheromoneLevelField);
		p1.add(phermoneLevel);
		p1.add(pheromoneLevelField);
		
		
				

		// Beta parameter
		JLabel edgeImportance = new JLabel(
				"Importance of Edge desirability value: ");
		edgeImportanceField = new JFormattedTextField(
				NumberFormat.getNumberInstance(Locale.ENGLISH));
		edgeImportanceField.setValue(CyGlobals.KPM.BETA);
		edgeImportanceField.setPreferredSize(new Dimension(50, 25));
		edgeImportanceField.addPropertyChangeListener("value",
				new ParameterTextFieldListener(0.0, Double.POSITIVE_INFINITY));
		String edgeImportanceTip = "Please enter a number greater than 0.0";
		edgeImportance.setToolTipText(edgeImportanceTip);
		edgeImportanceField.setToolTipText(edgeImportanceTip);
		edgeImportance.setLabelFor(edgeImportanceField);
		p1.add(edgeImportance);
		p1.add(edgeImportanceField);

		
		
		// Rho parameter
		JLabel phermoneDecayRate = new JLabel("Phermone decay rate: ");
		pheromoneDecayRateField = new JFormattedTextField(
				NumberFormat.getNumberInstance(Locale.ENGLISH));
		pheromoneDecayRateField.setPreferredSize(new Dimension(50, 25));
		pheromoneDecayRateField.setValue(CyGlobals.KPM.RHO);
		pheromoneDecayRateField.addPropertyChangeListener("value",
				new ParameterTextFieldListener(0.0, 1.0));
		String phermoneDecayRateTip = "Please enter a number greater "
				+ "than 0.0 and less than 1.0.";
		phermoneDecayRate.setToolTipText(phermoneDecayRateTip);
		pheromoneDecayRateField.setToolTipText(phermoneDecayRateTip);
		phermoneDecayRate.setLabelFor(pheromoneDecayRateField);
		p1.add(phermoneDecayRate);
		p1.add(pheromoneDecayRateField);

		
				
		
		
		// Maximal number of iterations
		JLabel maxIterations = new JLabel("Maximal number of iterations: ");
		maxIterationsField = new JFormattedTextField(
				NumberFormat.getIntegerInstance(Locale.ENGLISH));
		maxIterationsField.setPreferredSize(new Dimension(50, 25));
		maxIterationsField.setValue(CyGlobals.KPM.MAX_ITERATIONS);	
		maxIterationsField.addPropertyChangeListener("value",
				new ParameterTextFieldListener(0.0, Double.POSITIVE_INFINITY));
		String maxIterationsTip = "Please enter an integer greater than 0.";
		maxIterations.setToolTipText(maxIterationsTip);
		maxIterationsField.setToolTipText(maxIterationsTip);
		maxIterations.setLabelFor(maxIterationsField);
		p1.add(maxIterations);
		p1.add(maxIterationsField);

		
		
		// Maximal number of iterations without improvements before the ACO
		// algorithm stops
		JLabel maxIterationsWOChange = new JLabel(
				"Max. no. of iterarions without change: ");
		maxIterationsWOChangeField = new JFormattedTextField(
				NumberFormat.getIntegerInstance(Locale.ENGLISH));
		maxIterationsWOChangeField.setPreferredSize(new Dimension(50, 25));
		maxIterationsWOChangeField.setValue(CyGlobals.KPM.MAX_RUNS_WITHOUT_CHANGE);
		maxIterationsWOChangeField.addPropertyChangeListener("value",
				new ParameterTextFieldListener(0.0, Double.POSITIVE_INFINITY));
		String maxIterationsWOChangeTip = "Please enter an integer greater than 0";
		maxIterationsWOChange.setToolTipText(maxIterationsWOChangeTip);
		maxIterationsWOChangeField.setToolTipText(maxIterationsWOChangeTip);		
		maxIterationsWOChange.setLabelFor(maxIterationsWOChangeField);
		p1.add(maxIterationsWOChange);
		p1.add(maxIterationsWOChangeField);
		
		
		

		// tau_min - the minimal pheromone value on each node
		JLabel minTau = new JLabel("Minimal pheromone value: ");
		minTauField = new JFormattedTextField(
				NumberFormat.getNumberInstance(Locale.ENGLISH));
		minTauField.setValue(CyGlobals.KPM.TAU_MIN);
		minTauField.setPreferredSize(new Dimension(50, 25));
		minTauField.addPropertyChangeListener("value",
				new ParameterTextFieldListener(0.0, 1.0));
		String minTauTip = "Please enter a number greater than 0.0 and "
				+ "less than 1.0.";
		minTau.setToolTipText(minTauTip);
		minTauField.setToolTipText(minTauTip);
		minTau.setLabelFor(minTauField);
		p1.add(minTau);
		p1.add(minTauField);
		
		
		
		

		// Number of solutions considered in an iteration step before the
		// pheromones are updated and the next iterations begins
		JLabel solutionsPerIteration = new JLabel(
				"No. of solutions considered in each iteration: ");
		solutionsPerIterationField = new JFormattedTextField(
				NumberFormat.getIntegerInstance(Locale.ENGLISH));
		solutionsPerIterationField.setPreferredSize(new Dimension(50, 25));
		solutionsPerIterationField
				.setValue(CyGlobals.KPM.NUMBER_OF_SOLUTIONS_PER_ITERATION);
		solutionsPerIterationField.addPropertyChangeListener("value",
				new ParameterTextFieldListener(0.0, Double.POSITIVE_INFINITY));
		String solutionsPerIterationTip = "Please enter an integer greater than 0.";
		solutionsPerIteration.setToolTipText(solutionsPerIterationTip);
		solutionsPerIterationField.setToolTipText(solutionsPerIterationTip);
		solutionsPerIteration.setLabelFor(solutionsPerIterationField);
		p1.add(solutionsPerIteration);
		p1.add(solutionsPerIterationField);
		
		
		

		// Number of start nodes considered by the algorithm (GloNE parameter)
		JLabel startNodes = new JLabel(
				"(GLONE) No. of start nodes per iteration: ");
		startNodesField = new JFormattedTextField(
				NumberFormat.getIntegerInstance(Locale.ENGLISH));
		startNodesField.setValue(CyGlobals.KPM.NUM_STARTNODES);
		startNodesField.setPreferredSize(new Dimension(50, 25));
		startNodesField.addPropertyChangeListener("value",
				new ParameterTextFieldListener(0.0, Double.POSITIVE_INFINITY));
		String startNodesTip = "Please enter an integer greater than 0.";
		startNodes.setToolTipText(startNodesTip);
		startNodesField.setToolTipText(startNodesTip);		
		startNodes.setLabelFor(startNodesField);
		p1.add(startNodes);
		p1.add(startNodesField);
		
		
		// GLONE version to use
		JLabel gloneVersion = new JLabel("(GLONE) Version to use: ");
        String[] gloneVersionOptions = new String[]{"Iteration based", "Global based"};
        gloneVersionBox = new JComboBox<String>(gloneVersionOptions);
		gloneVersionBox.setPreferredSize(new Dimension(50, 25));
		gloneVersionBox.setSelectedIndex(0);
		String gloneVersionTip = "Please select the GLONE version to use. " +
				"'Iteration based' is parallelizable.";
		gloneVersion.setToolTipText(gloneVersionTip);
		gloneVersionBox.setToolTipText(gloneVersionTip);
                gloneVersionBox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        boolean isIterationBased = isIterationBasedGlone();
                        kpmpt.checkEnableProcessorSlider(isIterationBased);
                    }
                });
        gloneVersion.setLabelFor(gloneVersionBox);
		p1.add(gloneVersion);
		p1.add(gloneVersionBox);

        SpringUtilities.makeCompactGrid(p1, 9, 2, 5, 5, 5, 5);
		p1.setOpaque(true);
		setOpaque(true);
		
		add(p1, BorderLayout.NORTH);

		setEnabled(false);
	}

	public double getAlpha() {
		Number value = (Number) pheromoneLevelField.getValue();
		return value.doubleValue();
	}

	public double getBeta() {
		Number value = (Number) edgeImportanceField.getValue();
		return value.doubleValue();
	}

	public double getRho() {
		Number value = (Number) pheromoneDecayRateField.getValue();
		return value.doubleValue();
	}

	public int getMaxIterations() {
		Number value = (Number) maxIterationsField.getValue();
		return value.intValue();
	}

	public int getMaxIterationsWOChange() {
		Number value = (Number) maxIterationsWOChangeField.getValue();
		return value.intValue();
	}

	public int getNoStartNodes() {
		Number value = (Number) startNodesField.getValue();
		return value.intValue();
	}

	public double getTauMin() {
		Number value = (Number) minTauField.getValue();
		return value.doubleValue();
	}

	public int getNoSolutionsPerIteration() {
		Number value = (Number) solutionsPerIterationField.getValue();
		return value.intValue();
	}
	
	public boolean isIterationBasedGlone() {
        return gloneVersionBox.getSelectedIndex() == 0;
	}

}
