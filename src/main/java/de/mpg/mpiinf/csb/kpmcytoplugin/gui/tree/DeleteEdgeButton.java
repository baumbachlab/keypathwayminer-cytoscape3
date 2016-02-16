package de.mpg.mpiinf.csb.kpmcytoplugin.gui.tree;

import de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels.KPMLinksTab;
import edu.uci.ics.jung.visualization.VisualizationViewer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * A class to implement the deletion of an edge from within a
 * PopupVertexEdgeMenuMousePlugin.
 * 
 * @author Dr. Greg M. Bernstein
 */
public class DeleteEdgeButton<E> extends JButton implements EdgeMenuListener<E> {
	private E edge;
	private VisualizationViewer visComp;
	private KPMLinksTab linksPanel;

	public DeleteEdgeButton() {
		super("Delete Edge");
		this.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				visComp.getPickedEdgeState();
				visComp.getGraphLayout().getGraph().removeEdge(edge);
				linksPanel.updateErrorIconAndFormulaLabel();
				visComp.repaint();
			}
		});
	}

	public void setLinksPanel(KPMLinksTab lp) {
		this.linksPanel = lp;
	}

	@Override
	public void setEdgeAndView(E e, VisualizationViewer visView) {
		edge = e;
		visComp = visView;		
	}
}
