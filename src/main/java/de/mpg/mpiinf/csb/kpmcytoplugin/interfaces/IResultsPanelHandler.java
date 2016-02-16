package de.mpg.mpiinf.csb.kpmcytoplugin.interfaces;

import dk.sdu.kpm.results.IKPMResultSet;
import java.util.Map;

public interface IResultsPanelHandler {
	void addBatchResultsPanel(IKPMResultSet results, Map<Integer, String> indexLMap, boolean isInes);
}
