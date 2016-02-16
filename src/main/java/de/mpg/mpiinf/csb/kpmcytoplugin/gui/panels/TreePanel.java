package de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels;
 
import de.mpg.mpiinf.csb.kpmcytoplugin.gui.tree.NodeEdgeMenu;
import de.mpg.mpiinf.csb.kpmcytoplugin.gui.tree.PopupNodeEdgeMenuMousePlugin;
import dk.sdu.kpm.gui.clause.Clause;
import dk.sdu.kpm.gui.clause.ClauseFactory;
import dk.sdu.kpm.gui.tree.*;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.EditingModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import org.apache.commons.collections15.Transformer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.Map;

class TreePanel extends JPanel {

    /**
	 *
	 */
	private static final long serialVersionUID = 1349773227536925300L;

    /**
	 * The Tree/Forest object which contains the data set nodes and logical
	 * operators connecting them.
	 */
	private final MyForest forest;

	/**
	 * Used to layout the tree.
	 */
	private final VerticalTreeLayout layout;

	/**
	 * Visualizes the tree.
	 */
	private final VisualizationViewer<TreeNode, TreeEdge> vv;


    /**
	 * Creates a collapse-able panel where data sets can be connected using
	 * logical operators.
	 */
	public TreePanel(KPMLinksTab lp) {
		/*
	  The panel which created this object.
	 */
        KPMLinksTab linksPanel = lp;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// setMinimumSize(new Dimension(100, Integer.MAX_VALUE));
		forest = new MyForest();
		layout = new VerticalTreeLayout(forest, 0, 300, 0, 400);
		// TODO Size set automatically depending on the size of the KPM tab

		vv = new VisualizationViewer<TreeNode, TreeEdge>(layout, new Dimension(
				300, 450));
		layout.setVisualizationViewer(vv);
		vv.getRenderContext().setVertexLabelTransformer(
				new ToStringLabeller<TreeNode>());
		vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		vv.setBackground(Color.white);

		/*
	  The factory used to create binary operator nodes added by the user.
	 */
        NodeFactory nf = new NodeFactory();
		final EditingModalGraphMouse<TreeNode, TreeEdge> gm = new EditingModalGraphMouse<TreeNode, TreeEdge>(
				vv.getRenderContext(), nf, new EdgeFactory());
		PopupNodeEdgeMenuMousePlugin<TreeNode, TreeEdge> myPlugin = new PopupNodeEdgeMenuMousePlugin<TreeNode, TreeEdge>();
		NodeEdgeMenu.DataSetNodeMenu dataSetNodeMenu = new NodeEdgeMenu.DataSetNodeMenu();
		NodeEdgeMenu.BinaryOperatorNodeMenu binaryOperatorMenu = new NodeEdgeMenu.BinaryOperatorNodeMenu(
                linksPanel);
		NodeEdgeMenu.EdgeMenu edgeMenu = new NodeEdgeMenu.EdgeMenu();
		myPlugin.setDataSetNodePopup(dataSetNodeMenu);
		myPlugin.setBinaryOperatorNodePopup(binaryOperatorMenu);
		myPlugin.setEdgePopup(edgeMenu);
		gm.remove(gm.getPopupEditingPlugin());
		gm.add(myPlugin);
		// TODO Make edges vanish immediately after releasing the mouse button
		// and no target node is hit.
		// TODO Add menu for deleting edges

		// Setup up a new vertex to paint transformer
		Transformer<TreeNode, Paint> vertexPaint = new Transformer<TreeNode, Paint>() {
			public Paint transform(TreeNode n) {
				if (n instanceof DataSetNode) {
					return Color.ORANGE;
				} else if (n instanceof BinaryOperatorNode) {
					return Color.CYAN;
				} else {
					throw new IllegalArgumentException(
							"Unsupported type of TreeNode.");
				}
			}
		};
		// Setup a new vertex to shape transformer
		Transformer<TreeNode, Shape> vertexShape = new Transformer<TreeNode, Shape>() {
			public Shape transform(TreeNode n) {
				if (n instanceof DataSetNode) {
					// Rectangle r = new Rectangle(60, 40);
					// return r;
					Ellipse2D.Double e = new Ellipse2D.Double(-30, -20, 60, 40);
					return e;
				} else if (n instanceof BinaryOperatorNode) {
					return new Ellipse2D.Double(-30, -20, 60, 40);
				} else {
					throw new IllegalArgumentException(
							"Unsupported type of TreeNode.");
				}
			}
		};

		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		vv.getRenderContext().setVertexShapeTransformer(vertexShape);
		// Use lines as edges.
		vv.getRenderContext().setEdgeShapeTransformer(
				new EdgeShape.Line<TreeNode, TreeEdge>());

		// Build the panel which controls the view of the graph.
		final ScalingControl scaler = new CrossoverScalingControl();

		vv.scaleToLayout(scaler);
		JButton plus = new JButton("+");
		plus.setToolTipText("Zoom in");
		plus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scaler.scale(vv, 1.1f, vv.getCenter());
			}
		});
		JButton minus = new JButton("-");
		minus.setToolTipText("Zoom out");
		minus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scaler.scale(vv, 1 / 1.1f, vv.getCenter());
			}
		});
		JPanel scaleGrid = new JPanel(new GridLayout(1, 0));
		scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));
		gm.setMode(Mode.EDITING);

		JPanel controls = new JPanel();
		scaleGrid.add(plus);
		scaleGrid.add(minus);
		controls.add(scaleGrid);

		vv.setGraphMouse(gm);
		addSidePanel();
		GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv);
        String treePanelTip = "Please specify the way the data sets are connected.";
        vv.setToolTipText(treePanelTip);
		add(gzsp);
		JPanel controlsDescriptionsPanel = new JPanel();
		controlsDescriptionsPanel.setLayout(new BoxLayout(controlsDescriptionsPanel, BoxLayout.X_AXIS));
		controlsDescriptionsPanel.add(controls);
		JPanel descriptionsPanel = assembleDescriptionsPanel();
		controlsDescriptionsPanel.add(descriptionsPanel);
		add(controlsDescriptionsPanel);
	}

	private JPanel assembleDescriptionsPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.Y_AXIS));
		JLabel label1 = new JLabel("Left-click adds operator");
		ret.add(label1);
		JLabel label2 = new JLabel("Right-click + drag adds connection");
		ret.add(label2);
		JLabel label3 = new JLabel("Richt-click node/edge opens menu");
		ret.add(label3);
		return ret;
	}

	public void addTreeObserver(KPMLinksTab.TreeObserver treeObserver) {
		forest.addObserver(treeObserver);
	}

	public int getDataSetCount() {
		return forest.getDataSetCount();
	}

	public TreeNode isConnected() {
		return forest.isConnected();
	}

	/**
	 * 
	 * @return The number of nodes which are present in the current tree.
	 */
	public int getNoNodes() {
		return forest.getVertexCount();
	}

	public Clause getLogicalConnections() throws IllegalStateException {
		TreeNode root = forest.isConnected();
		if (root == null) {
			return null;
		} else {
			Clause c = ClauseFactory.create(forest, root);
			return c;
		}
	}

	/**
	 * Adds a sub-panel which allows to add binary operator nodes, delete all
	 * edges or nodes or to align the nodes.
	 */
	private void addSidePanel() {
		JPanel sidePanel = new JPanel();

		// Graph modification buttons.
		// JButton addOperatorNodeButton = new JButton("Add new Operator");
		// JButton alignButton = new JButton("Align Nodes");
		JButton deleteOperatorsButton = new JButton("Delete operators");
		deleteOperatorsButton.setToolTipText("Deletes all logical operators");
		JButton deleteEdgesButton = new JButton("Delete connections");
		deleteEdgesButton.setToolTipText("Deletes all logical connections");

		// alignButton.addActionListener(new ActionListener() {

		// @Override
		// public void actionPerformed(ActionEvent e) {
		// layout.alignDataSetNodes();
		// }
		// });

		deleteOperatorsButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				forest.removeOperatorNodes();
				vv.updateUI();
			}
		});

		deleteEdgesButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				forest.removeAllEdges();
				vv.updateUI();
			}
		});

		add(sidePanel);

		JPanel treeModificationPanel = new JPanel();
		// treeModificationPanel.add(alignButton);
		treeModificationPanel.add(deleteOperatorsButton);
		treeModificationPanel.add(deleteEdgesButton);
		add(treeModificationPanel);
	}

	public void addDataSetNode(DataSetNode dataSetNode) {
		forest.removeAllEdges();
		forest.addVertex(dataSetNode);
		// And finally initialize the graph layout.
		forest.removeOperatorNodes();
		layout.initialize();
	}

	public void removeDataSetNode(DataSetNode dataSetNode) {
		forest.removeAllEdges();
		forest.removeDataSetNode(dataSetNode);
		// And finally initialize the graph layout.
		forest.removeOperatorNodes();
		layout.initialize();
	}

	public Map<String, Map<String, int[]>> getDataSetFileMap() {
		return forest.getDataSetFileMap();
	}

	public List<DataSetNode> getDataSetNodes() {
		return forest.getDataSetNodes();
	}

}
