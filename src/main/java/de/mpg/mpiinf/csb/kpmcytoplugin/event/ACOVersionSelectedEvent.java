/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mpg.mpiinf.csb.kpmcytoplugin.event;


import de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels.AdvancedParameterPanel;
import org.cytoscape.event.AbstractCyEvent;
import org.cytoscape.event.CyEvent;



/**
 *
 * @author nalcaraz
 */
class ACOVersionSelectedEvent implements CyEvent {
    
    private final AdvancedParameterPanel source;
    
    private final ACOVersionSelectedListener listenerClass;
    
    private final boolean isIterationBased;

    public ACOVersionSelectedEvent(final AdvancedParameterPanel source,
            final ACOVersionSelectedListener listenerClass,
            final boolean isIterationBased) {
        if (source == null) {
            throw new NullPointerException("event source is null");
        }

        if (listenerClass == null) {
            throw new NullPointerException("listener class source is null");
        }

        this.source = source;
        this.listenerClass = listenerClass;
        this.isIterationBased = isIterationBased;
    }
    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public Class getListenerClass() {
        return listenerClass.getClass();
    }
    
    public boolean getIsIterationBased() {
        return isIterationBased;
    }

}
