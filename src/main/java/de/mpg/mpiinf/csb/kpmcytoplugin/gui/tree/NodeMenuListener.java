package de.mpg.mpiinf.csb.kpmcytoplugin.gui.tree;

/*
 * NodeMenuListener.java
 *
 * Created on March 21, 2007, 1:50 PM; Updated May 29, 2007
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 * 
 * Code downloaded from http://www.grotto-networking.com/JUNG/MouseMenu/ and 
 * modified by Alexander Junge 
 */

import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * Used to indicate that this class wishes to be told of a selected vertex along
 * with its visualization component context. Note that the VisualizationViewer
 * has full access to the graph and layout.
 * 
 * @author Dr. Greg M. Bernstein
 */
interface NodeMenuListener<V> {
	void setVertexAndView(V v, VisualizationViewer visView);
}
