package de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels;

import de.mpg.mpiinf.csb.kpmcytoplugin.CyGlobals;
import dk.sdu.kpm.Combine;
import dk.sdu.kpm.KPMSettings;
import dk.sdu.kpm.gui.clause.Clause;
import dk.sdu.kpm.gui.clause.ClauseFactory;
import dk.sdu.kpm.gui.tree.DataSetNode;
import dk.sdu.kpm.gui.tree.TreeNode;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

/**
 * A JPanel derived object which displays a tree where each node is either a
 * data set or a logical operator which connects data sets.
 * 
 * This panel is shown as a tab in the KPM window. An edge connects operators
 * with its operands. The user can edited the tree, add operators and and link
 * data sets using these operators. Note that the shape of the tree also
 * specifies the order of evaluation of the logical expressions.
 * 
 * @author ajunge
 */
public class KPMLinksTab extends KPMTab {

    /**
	 *
	 */
	private static final long serialVersionUID = -7068022931842072230L;

	/**
	 * A sub-panel which contains the tree where the data sets are connected.
	 */
	private final TreePanel treePanel;

	/**
	 * Fills up the space of the treePanel, when treePanel is not visible.
	 */
	private JPanel fillingPanel;

	/**
	 * Allows the user to switch between a custom logical connection or pure
	 * and/or connections.
	 */
	private final JComboBox defaultOperatorBox;

	/**
	 * True if and only if this panel contains an error and the currently
	 * specified tree is cannot be parsed into a logical clause, i.e., it is not
	 * connected.
	 */
	private boolean containsError;

	/**
	 * Holds the logical formula contained in this panel.
	 */
	private final JLabel logicalFormulaLabel;

	/**
	 * Creates a TreePanel object containing the data set tree and a button
	 * panel.
	 *  @param kpmtp
	 *            The {@link de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels.KPMTabbedPane} containing this panel.
	 *
     *
	 */
	public KPMLinksTab(KPMTabbedPane kpmtp) {
		super(kpmtp, de.mpg.mpiinf.csb.kpmcytoplugin.util.CytoscapePanelNames.LINKSNAME);

		treePanel = new TreePanel(this);
		logicalFormulaLabel = new JLabel();
        String logicalFormulaToolTip = "The current logical formula";
        logicalFormulaLabel.setToolTipText(logicalFormulaToolTip);

		// Add an observer to the graph which gets notified whenever a node or
		// edge
		// is added or removed.
		// The observer updates the nodes containsError attribute which
		// indicates if the
		// currently specified graph is connected or not.
		treePanel.addTreeObserver(new TreeObserver());

		// Create box for choosing the default operator connecting
		// all data sets.
		JLabel defaultOperatorLabel = new JLabel(
				"Logically connect experiments: ");
		defaultOperatorBox = new JComboBox(Combine.values());
        String logicalOperatorToolTip = "Please select the logical operator connecting the experiments";
        defaultOperatorLabel.setToolTipText(logicalOperatorToolTip);
		defaultOperatorBox.setToolTipText(logicalOperatorToolTip);
		defaultOperatorBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox menu = (JComboBox) e.getSource();
				Combine operator = (Combine) menu.getSelectedItem();

				switch (operator) {
				case OR:
                    CyGlobals.KPM.COMBINE_OPERATOR = Combine.OR;
					treePanel.setVisible(false);
					fillingPanel.setVisible(true);
					refreshLogicalFormulaLabel();
					removeTabErrorIcon();
					break;
				case AND:
                    CyGlobals.KPM.COMBINE_OPERATOR = Combine.AND;
					treePanel.setVisible(false);
					fillingPanel.setVisible(true);
					refreshLogicalFormulaLabel();
					removeTabErrorIcon();
					break;
				case CUSTOM:
                    CyGlobals.KPM.COMBINE_OPERATOR = Combine.CUSTOM;
					fillingPanel.setVisible(false);
					treePanel.setVisible(true);
					updateErrorIconAndFormulaLabel();
					break;
				default:
					throw new UnsupportedOperationException("Unknown operator");
				}
				updateUI();
			}
		});
		JPanel defaultOperatorPanel = new JPanel();
		defaultOperatorPanel.add(defaultOperatorLabel);
		defaultOperatorPanel.add(defaultOperatorBox);

		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.PAGE_START;
		c.weightx = 0.5;
		c.gridx = 0;

		c.gridy = 0;
		c.weighty = 0;
		add(defaultOperatorPanel, c);

		c.gridy = 1;
		c.weighty = 0;
		JPanel logicalFormulaPanel = new JPanel();
		logicalFormulaPanel.add(logicalFormulaLabel);
		add(logicalFormulaPanel, c);

		c.gridy = 2;
		c.gridheight = 2;
		c.weighty = 1;
		add(treePanel, c);
		treePanel.setVisible(false);

		fillingPanel = new JPanel();
		fillingPanel.setPreferredSize(new Dimension(treePanel
				.getPreferredSize()));
		fillingPanel.setVisible(true);
		add(fillingPanel, c);

		updateUI();

	}

	/**
	 * Adds the given data set node to the displayed tree. Furthermore removes
	 * all edges and initializes the layout of the tree.
	 * 
	 * @param dataSetNode
	 *            - The node to be added to the tree.
	 */
	public void addDataSetNode(DataSetNode dataSetNode) {
		treePanel.addDataSetNode(dataSetNode);
	}

	/**
	 * Removes the given data set node from the displayed tree. Furthermore
	 * removes all edges and initializes the layout of the tree.
	 * 
	 * @param dataSetNode
	 *            - The node to be removed from the tree.
	 */
	public void removeDataSetNode(DataSetNode dataSetNode) {
		treePanel.removeDataSetNode(dataSetNode);
	}

	@Override
	public boolean containsError() {
		// Can only be true, if custom combine operator was selected.
		if (CyGlobals.KPM.COMBINE_OPERATOR == Combine.CUSTOM) {
			return containsError;
		} else {
			return false;
		}
	}

	/**
	 * This observer is notified whenever a node or edge is added to the tree
	 * which specifies how the data sets are connected logically.
	 *
	 * @author ajunge
	 *
	 */
	public class TreeObserver implements Observer {

		@Override
		public void update(Observable o, Object arg) {
			TreeNode root = treePanel.isConnected();
			containsError = (root == null);
			// Do not show an error when no data sets have been specified so
			// far.
			if (treePanel.getDataSetCount() == 0) {
				containsError = false;
			}
			if (((Combine) defaultOperatorBox.getSelectedItem())
					.equals(Combine.CUSTOM)) {
				updateErrorIconAndFormulaLabel();
			}
			treePanel.updateUI();
		}
	}

	/**
	 * Updates the error icon of this tab, i.e. adds an error icon if there was
	 * an error and removes it if this is not the case. Furthermore, the label
	 * containing the currently specified logical expression is updated.
	 */
	public void updateErrorIconAndFormulaLabel() {
		Clause clause = null;
		try {
			clause = getLogicalConnections();
		} catch (IllegalStateException e) {
			setLogicalErrorLabel();
			markTabWithErrorIcon("Error in logical formula");
		}
		if (containsError || clause == null) {
			setLogicalErrorLabel();
			markTabWithErrorIcon("Error in logical formula");
		} else {
			String formula = ClauseFactory
					.getLogicalStringRepresentation(clause, CyGlobals.KPM);
			setLogicalFormulaLabel(formula);
			removeTabErrorIcon();
		}
	}

	/**
	 * Returns a map which maps the identifier of each data set present in the
	 * tree to the respective indicator matrix.
	 * 
	 * @return A map: data set identifier -> file path
	 */
	public HashMap<String, Map<String, int[]>> getDataSetFileMap() {
		Map<String, Map<String, int[]>> datasetfileMap = treePanel.getDataSetFileMap();

		return new HashMap<String, Map<String, int[]>>(datasetfileMap);
	}

	/**
	 * 
	 * @return The (logical) clause which is currently specified in this panel
	 *         or {@code null} if the current graph is not connected.
	 */
	public Clause getLogicalConnections() throws IllegalStateException {
		return treePanel.getLogicalConnections();
	}

	List<DataSetNode> getDataSetNodes() {
		return treePanel.getDataSetNodes();
	}

	/**
	 * Activates or deactivates the {@link JComboBox} where the user can specify
	 * the mode for logically connecting the data sets.
	 * 
	 * @param activated
	 */
	public void setOperatorBoxActived(boolean activated) {
		defaultOperatorBox.setEnabled(activated);
	}

	/**
	 * Updates the label showing the logical formula with the given String.
	 */
    void setLogicalFormulaLabel(String formula) {
		logicalFormulaLabel.setText(formula);
		logicalFormulaLabel.setForeground(Color.BLACK);
	}

	/**
	 * Updates the label showing the logical formula with the given String.
	 */
    void setLogicalErrorLabel() {
		setLogicalFormulaLabel("Error in logical formula");
		logicalFormulaLabel.setForeground(Color.RED);
	}

	/**
	 * Updates the label showing the logical formula as a String representation.
	 */
	public void refreshLogicalFormulaLabel() {
		if (getDataSetNodes().size() > 1) {
			Combine operator = (Combine) defaultOperatorBox.getSelectedItem();
			if (operator == Combine.CUSTOM) {
				// Should be handled elsewhere.
			} else {
				setLogicalFormulaLabel(operator
						.getLogicalClause(getDataSetNodes()));
			}
		} else {
			logicalFormulaLabel.setText("");
		}
	}

	/**
	 * Resets the initial layout of this panel.
	 */
	public void backToInitialLayout() {
		if (getDataSetNodes().size() <= 1) {
            CyGlobals.KPM.COMBINE_OPERATOR = Combine.OR;
			defaultOperatorBox.setSelectedIndex(0);
			treePanel.setVisible(false);
			fillingPanel.setVisible(true);
			refreshLogicalFormulaLabel();
			removeTabErrorIcon();
		}
	}

}
