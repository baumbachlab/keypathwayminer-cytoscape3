package de.mpg.mpiinf.csb.kpmcytoplugin.gui.tree;
/*
 * PopupVertexEdgeMenuMousePlugin.java
 *
 * Created on March 21, 2007, 12:56 PM; Updated May 29, 2007
 *
 * Copyright March 21, 2007 Grotto Networking
 * 
 * Code downloaded from http://www.grotto-networking.com/JUNG/MouseMenu/ and 
 * modified by Alexander Junge 
 *
 */

import de.mpg.mpiinf.csb.kpmcytoplugin.gui.tree.NodeEdgeMenu.EdgeMenu;
import dk.sdu.kpm.gui.tree.BinaryOperatorNode;
import dk.sdu.kpm.gui.tree.DataSetNode;
import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

//import cytoscape.Cytoscape;

/**
 * A GraphMousePlugin that brings up distinct popup menus when an edge or node
 * is appropriately clicked in a graph. If these menus contain components that
 * implement either the EdgeMenuListener or NodeMenuListener then the
 * corresponding interface methods will be called prior to the display of the
 * menus (so that they can display context sensitive information for the edge or
 * vertex).
 */
public class PopupNodeEdgeMenuMousePlugin<V, E> extends
		AbstractPopupGraphMousePlugin {
	/**
	 * Pop-up menus which is shown after data set nodes are clicked on
	 */
	private NodeEdgeMenu.DataSetNodeMenu dataSetNodePopup;

	/**
	 * Pop-up menus which is shown after logical operator nodes are clicked on
	 */
	private NodeEdgeMenu.BinaryOperatorNodeMenu binaryOperatorNodePopup;

	/**
	 * Pop-up menus which is shown after an edge is clicked on.
	 */
	private NodeEdgeMenu.EdgeMenu edgePopup;

    /**
	 * Implementation of the AbstractPopupGraphMousePlugin method. This is where
	 * the work gets done.
	 * 
	 * @param e
	 */
	protected void handlePopup(MouseEvent e) {
		final VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e
				.getSource();
		Point2D p = e.getPoint();

		GraphElementAccessor<V, E> pickSupport = vv.getPickSupport();
		if (pickSupport != null) {
			final V v = pickSupport.getVertex(vv.getGraphLayout(), p.getX(),
					p.getY());
			if (v != null) {
				// System.out.println("Vertex " + v + " was right clicked");
				if (v instanceof DataSetNode) {
					updateDataSetNodeMenu(v, vv, p);
					dataSetNodePopup.setVisible(true);
					dataSetNodePopup.pack();
				} else if (v instanceof BinaryOperatorNode) {
					updateBinaryOperatorNodeMenu(v, vv, p);
					binaryOperatorNodePopup.setVisible(true);
					binaryOperatorNodePopup.pack();
				} else {
					throw new IllegalArgumentException("Unknown node class.");
				}
			} else {
				final E edge = pickSupport.getEdge(vv.getGraphLayout(),
						p.getX(), p.getY());
				if (edge != null) {
					updateEdgeMenu(edge, vv, p);
					edgePopup.setVisible(true);
					edgePopup.pack();
				}
			}
		}
	}

	private void updateEdgeMenu(E edge, VisualizationViewer<V, E> vv, Point2D p) {
		if (edgePopup == null) {
			return;
		}
		Component[] menuComps = edgePopup.getPanel().getComponents();
		edgePopup.setLocation(MouseInfo.getPointerInfo().getLocation());
		for (Component comp : menuComps) {
			if (comp instanceof EdgeMenuListener) {
				((EdgeMenuListener) comp).setEdgeAndView(edge, vv);
			}
			if (comp instanceof DeleteEdgeButton<?>) {
				((DeleteEdgeButton<E>) comp)
						.setLinksPanel(binaryOperatorNodePopup.linksPanel);
			}
		}
	}

	/**
	 * Updates the menu which is shown after a DataSetNode is selected by the
	 * user.
	 * 
	 * @param v
	 * @param vv
	 * @param point
	 */
	private void updateDataSetNodeMenu(V v, VisualizationViewer<V, E> vv,
			Point2D point) {
		if (dataSetNodePopup == null)
			return;
		Component[] menuComps = dataSetNodePopup.getPanel().getComponents();
		dataSetNodePopup.setLocation(MouseInfo.getPointerInfo().getLocation());
		for (Component comp : menuComps) {
			if (comp instanceof NodeMenuListener) {
				((NodeMenuListener) comp).setVertexAndView(v, vv);
			}
			if (comp instanceof MenuPointListener) {
				((MenuPointListener) comp).setPoint(point);
			}
		}
	}

	/**
	 * Updates the menu which is shown after a BinaryOperatorNode is selected by
	 * the user.
	 * 
	 * @param v
	 * @param vv
	 * @param point
	 */
	private void updateBinaryOperatorNodeMenu(V v,
			VisualizationViewer<V, E> vv, Point2D point) {
		if (binaryOperatorNodePopup == null)
			return;
		Component[] menuComps = binaryOperatorNodePopup.getPanel()
				.getComponents();
		binaryOperatorNodePopup.setLocation(MouseInfo.getPointerInfo().getLocation());
		for (Component comp : menuComps) {
			if (comp instanceof NodeMenuListener) {
				((NodeMenuListener) comp).setVertexAndView(v, vv);
			}
			if (comp instanceof MenuPointListener) {
				((MenuPointListener) comp).setPoint(point);
			}
			if (comp instanceof DeleteNodeButton<?>) {
				((DeleteNodeButton<V>) comp)
						.setLinksPanel(binaryOperatorNodePopup.linksPanel);
			}
		}
	}

	/**
	 * Setter for the popup shown for a DataSetNode.
	 */
	public void setDataSetNodePopup(
			NodeEdgeMenu.DataSetNodeMenu dataSetNodePopup) {
		this.dataSetNodePopup = dataSetNodePopup;
	}

	/**
	 * Getter for the popup shown for a BinaryOperatorNode.
	 *
	 * @return
	 */
	public JDialog getBinaryOperatorNodePopup() {
		return binaryOperatorNodePopup;
	}

	/**
	 * Setter for the popup shown for a BinaryOperatorNode.
	 */
	public void setBinaryOperatorNodePopup(
			NodeEdgeMenu.BinaryOperatorNodeMenu binaryOperatorNodePopup) {
		this.binaryOperatorNodePopup = binaryOperatorNodePopup;
	}

	public void setEdgePopup(EdgeMenu edgeMenu) {
		this.edgePopup = edgeMenu;		
	}

}
