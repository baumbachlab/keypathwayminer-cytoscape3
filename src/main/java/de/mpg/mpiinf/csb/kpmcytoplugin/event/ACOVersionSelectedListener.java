/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mpg.mpiinf.csb.kpmcytoplugin.event;

import org.cytoscape.event.CyListener;

/**
 *
 * @author nalcaraz
 */
public interface ACOVersionSelectedListener extends CyListener {

    /**
      * The method that should handle the specified event.
      * @param e The event to be handled.
      */
    public void handleEvent(ACOVersionSelectedEvent e);
}
