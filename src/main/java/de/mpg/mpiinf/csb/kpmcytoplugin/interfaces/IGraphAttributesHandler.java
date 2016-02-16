package de.mpg.mpiinf.csb.kpmcytoplugin.interfaces;

import dk.sdu.kpm.KPMSettings;

/***
 * Used when a KPM run has completed. performs the task of updating the Cytoscape graph, based on the results of the KPM run.
 * @author Martin
 *
 */
public interface IGraphAttributesHandler {
	void update(KPMSettings settings);
}
