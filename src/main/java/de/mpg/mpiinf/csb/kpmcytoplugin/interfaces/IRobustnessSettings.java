package de.mpg.mpiinf.csb.kpmcytoplugin.interfaces;

import de.mpg.mpiinf.csb.kpmcytoplugin.CyGlobals;
import dk.sdu.kpm.perturbation.IPerturbation;

import java.util.List;

public interface IRobustnessSettings {
	int getPerturbationValue();
	
	int getStepPerturbationValue();
	
	int getMaxPerturbationValue();
	
	int getGraphsPerStep();
	
	IPerturbation getPerturbationTechnique();

    List<String> getValidationList();

    CyGlobals.RunTypeEnum getRunType();
}
