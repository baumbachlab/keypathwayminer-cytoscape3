package de.mpg.mpiinf.csb.kpmcytoplugin.gui.tree;


/*
 * MyMouseMenus.java
 *
 * Created on March 21, 2007, 3:34 PM; Updated May 29, 2007
 *
 * Copyright March 21, 2007 Grotto Networking
 * 
 * Code downloaded from http://www.grotto-networking.com/JUNG/MouseMenu/ and 
 * modified by Alexander Junge 
 *
 */
 
import de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels.KPMLinksTab;
import dk.sdu.kpm.gui.clause.BinaryOperatorType;
import dk.sdu.kpm.gui.tree.BinaryOperatorNode;
import dk.sdu.kpm.gui.tree.DataSetNode;
import dk.sdu.kpm.gui.tree.TreeEdge;
import dk.sdu.kpm.gui.tree.TreeNode;
import edu.uci.ics.jung.visualization.VisualizationViewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A collection of classes used to assemble pop-up mouse menus for the classes
 * DataSetNode, BinaryOperatorNode and TreeEdge.
 * 
 */
public class NodeEdgeMenu {
	
	/**
	 * A pop-up menu which is shown when a DataSetNode node is clicked on.
	 */ 
	public static class EdgeMenu extends JDialog {
		
		/**
		 * The main panel of the pop-up window where all sub-panels are added
		 * to.
		 */
		private final JPanel panel;
		
		public EdgeMenu() {
			setTitle("Edge Menu");
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			panel = new JPanel();
			assemble();
		}
		
		/**
		 * Assembles the pop-up;
		 */
		public void assemble() {
			JPanel panel = getPanel();
			JButton button = new DeleteEdgeButton<TreeEdge>();
			panel.add(button);
			button.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					EdgeMenu.this.dispose();

				}
			});
			add(panel);
		}

		/**
		 *
		 * @return The main panel of the pop-up which contains all other panels
		 *         holding the actual information on the edge.
		 */
		public JPanel getPanel() {
			return panel;
		}

	}

	/**
	 * A pop-up menu which is shown when a DataSetNode node is clicked on. There
	 * is a separate class, BinaryOperatorNodeMenu, which inherits from this one
	 * and adds additional information which are only interesting for
	 * BinaryOperatorNodes.
	 *
	 */
	public static class DataSetNodeMenu extends JDialog {

		/**
		 * The main panel of the pop-up window where all sub-panels are added
		 * to.
		 */
		private final JPanel panel;

		public DataSetNodeMenu() {
			setTitle("Data Set Node Menu");
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			panel = new JPanel();
			assemble();
		}

		/**
		 * Adds all menu items which are useful for a DataSetNode to this popup.
		 * Note: Popups for BinaryOperatorNodes should overwrite this method
		 * since there are additional menu items which should be added.
		 */
		public void assemble() {
			JPanel panel = getPanel();
			addNodeInformationPanel(panel);
			add(panel);
		}

		/**
		 * Adds a panel object to the given JPanel. The added panel displays the
		 * data set file paths and file name abbreviations. Furthermore, a case
		 * exception parameter, L, for the data set can be specified here.
		 *
		 * @param pan
		 *            - The panel the new JPanel object is added to.
		 */
        void addNodeInformationPanel(JPanel pan) {
			pan.add(new NodeInformationPanel(this));
		}

		/**
		 *
		 * @return The main panel of the pop-up which contains all other panels
		 *         holding the actual information on the nodes.
		 */
		public JPanel getPanel() {
			return panel;
		}

	}

	/**
	 * Part of the pop-up which is shown when a node is clicked on. Displays the
	 * file path and the abbreviation of the data set linked to the node. Allows
	 * the number of case exceptions, L, for this node.
	 *
	 * @author ajunge
	 *
	 */
	public static class NodeInformationPanel extends JPanel implements
			NodeMenuListener<DataSetNode> {

		/**
		 * The currently processed node, set to the node which was clicked on
		 * whenever the node popup is shown.
		 */
		DataSetNode v;

		/**
		 * The node's name (i.e. the abbreviation of the data set, which was
		 * chosen by the user) which is displayed in the GUI.
		 */
        final JLabel nameLabel;

		/**
		 * The file path of the data set which is linked to this node.
		 */
		JLabel filePathLabel;

		/**
		 * Visualization viewer for the graph the node is in.
		 */
		VisualizationViewer visView;

		/**
		 * Saves the currently specified settings as attributes of the data set
		 * node.
		 */
        final JButton saveButton;

		/**
		 * The instance is part of this {@link DataSetNodeMenu} instance.
		 */
		private final DataSetNodeMenu dsnm;

		/**
		 *
		 * @param dsnm
		 *            - This {@link NodeInformationPanel} is part of the given
		 *            {@link DataSetNodeMenu}.
		 */
		public NodeInformationPanel(final DataSetNodeMenu dsnm) {
			this.dsnm = dsnm;

//			JLabel filePath = new JLabel("Data file: ");
//			filePathLabel = new JLabel();
//			Font filePathLabelFont = new Font(
//					filePathLabel.getFont().getName(), Font.BOLD, filePathLabel
//							.getFont().getSize());
//			filePathLabel.setFont(filePathLabelFont);

			JLabel name = new JLabel("Name: ");
			nameLabel = new JLabel();
			Font nameLabelFont = new Font(nameLabel.getFont().getName(),
					Font.BOLD, nameLabel.getFont().getSize());
			nameLabel.setFont(nameLabelFont);

			saveButton = new JButton("Close");
			saveButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					visView.updateUI();
					dsnm.dispose();
				}
			});

//			add(filePath);
//			add(filePathLabel);
//			add(new JSeparator());
			add(name);
			add(nameLabel);
			add(new JSeparator());
			add(saveButton);
		}

		@Override
		public void setVertexAndView(DataSetNode v, VisualizationViewer visView) {
			this.visView = visView;
			this.v = v;
			this.nameLabel.setText(v.getExternalName());
//			this.filePathLabel.setText(v.getFile().getAbsolutePath());
		}
	}

	/**
	 * A pop-up menu which is shown when a BinaryOperatorNode is clicked on. In
	 * contrast to the corresponding class for DataSetNodes, this pop-up menu
	 * contains an additional drop-down menu which allows the user to choose the
	 * type of the operator.
	 */
	public static class BinaryOperatorNodeMenu extends DataSetNodeMenu {

		public final KPMLinksTab linksPanel;

		public BinaryOperatorNodeMenu(KPMLinksTab lp) {
			setTitle("Binary Operator Node Menu");
			this.linksPanel = lp;
		}

		@Override
		public void assemble() {
			JPanel pan = getPanel();
			addBinOpMenuItem(pan);
			pan.add(new JSeparator());
			addDeleteNodeButton(pan);
			add(pan);
		}

		/**
		 * The sub-panel containing the drop-down menu where the type of the
		 * binary operator can be set.
		 * 
		 * @param pan
		 */
		private void addBinOpMenuItem(JPanel pan) {
			pan.add(new BinOpPanel(this));
		}

		/**
		 * Adds a button for deleting the respective node to the pop-up.
		 * 
		 * @param pan
		 */
		private void addDeleteNodeButton(JPanel pan) {
			JButton button = new DeleteNodeButton<TreeNode>();
			button.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					BinaryOperatorNodeMenu.this.dispose();

				}
			});
			pan.add(button);
		}

		/**
		 * Part of the popup which is shown when a BinaryOperatorNode is clicked
		 * on. Allows to modify the type of the binary operator.
		 * 
		 * @author ajunge
		 * 
		 */
		public static class BinOpPanel extends JPanel implements
				NodeMenuListener<BinaryOperatorNode> {

			/**
			 * The BinaryOperatorNodeMenu which created this panel. This panel
			 * is part of this BinaryOperatorNodeMenu.
			 */
			BinaryOperatorNodeMenu bonm;

			/**
			 * The node for which the pop-up is displayed.
			 */
			BinaryOperatorNode v;

			/**
			 * Drop-down menu for specifying the type of the binary operator.
			 */
			JComboBox box;

			VisualizationViewer visView;

			/**
			 * The type of the binary operator.
			 */
            final BinaryOperatorType[] comboBoxValues = BinaryOperatorType.values();

			final JButton saveButton;

			public BinOpPanel(final BinaryOperatorNodeMenu bonm) {
				saveButton = new JButton("Save");

				saveButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						int idx = box.getSelectedIndex();
						v.setBinaryOperator(comboBoxValues[idx]);
						bonm.linksPanel.updateErrorIconAndFormulaLabel();
						visView.updateUI();
						bonm.dispose();
					}
				});

				JLabel lab = new JLabel("Binary Operator = ");
				box = new JComboBox(comboBoxValues);
				box.setSelectedIndex(1);

				add(lab);
				add(box);
				add(saveButton);
			}

			@Override
			public void setVertexAndView(BinaryOperatorNode v,
					VisualizationViewer visView) {
				this.v = v;
				this.visView = visView;
				int idx = -1;
				for (int i = 0; i < comboBoxValues.length; i++) {
					if (comboBoxValues[i] == v.getBinaryOperator()) {
						idx = i;
						break;
					}
				}
				this.box.setSelectedIndex(idx);
			}
		}
	}
}
