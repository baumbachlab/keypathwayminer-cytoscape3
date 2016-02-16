package de.mpg.mpiinf.csb.kpmcytoplugin.gui.tree;

/*
 * DeleteVertexMenuItem.java
 *
 * Created on March 21, 2007, 2:03 PM; Updated May 29, 2007
 *
 * Copyright March 21, 2007 Grotto Networking
 * 
 * Code downloaded from http://www.grotto-networking.com/JUNG/MouseMenu/ and 
 * modified by Alexander Junge 
 *
 *
 */

import de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels.KPMLinksTab;
import edu.uci.ics.jung.visualization.VisualizationViewer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A class to implement the deletion of a vertex from within a
 * PopupVertexEdgeMenuMousePlugin.
 * 
 * @author Dr. Greg M. Bernstein
 */
public class DeleteNodeButton<V> extends JButton implements NodeMenuListener<V> {
	private V vertex;
	private VisualizationViewer visComp;
	private KPMLinksTab linksPanel;

	/** Creates a new instance of DeleteVertexMenuItem */
	public DeleteNodeButton() {
		super("Delete Vertex");
		this.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				visComp.getPickedVertexState().pick(vertex, false);
				visComp.getGraphLayout().getGraph().removeVertex(vertex);
				linksPanel.updateErrorIconAndFormulaLabel();
				visComp.repaint();
			}
		});
	}

	/**
	 * Implements the VertexMenuListener interface.
	 * 
	 * @param v
	 * @param visComp
	 */
	public void setVertexAndView(V v, VisualizationViewer visComp) {
		this.vertex = v;
		this.visComp = visComp;
		// this.setText("Delete Vertex " + v.toString());
	}

	public void setLinksPanel(KPMLinksTab lp) {
		this.linksPanel = lp;
	}

}
