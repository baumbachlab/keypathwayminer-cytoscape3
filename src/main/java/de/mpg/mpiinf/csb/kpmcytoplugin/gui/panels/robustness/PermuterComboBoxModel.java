package de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels.robustness;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import dk.sdu.kpm.perturbation.IPerturbation;


@SuppressWarnings("serial")
public class PermuterComboBoxModel extends AbstractListModel<IPerturbation> implements ComboBoxModel<IPerturbation>{
	private final List<IPerturbation> permuters;
	
	private int selectedIndex;
	
	public PermuterComboBoxModel(List<IPerturbation> ipermuters){
		permuters = ipermuters;
		Collections.sort(permuters, new Comparator<IPerturbation>(){
			@Override
			public int compare(IPerturbation o1, IPerturbation o2) {
				return o1.getName().compareTo(o2.getName());
			}
        });
		selectedIndex = 0;
	}
	
	@Override
	public int getSize() {
		return permuters.size();
	}

	@Override
	public IPerturbation getElementAt(int index) {
		return permuters.get(index);
	}

	@Override
	public void setSelectedItem(Object anItem) {
		// Only set if its an instance of the IPermuter.
		if(anItem instanceof IPerturbation){
			if(permuters.contains(anItem)){
				selectedIndex = permuters.indexOf(anItem);
			}
		}
	}

	@Override
	public IPerturbation getSelectedItem() {
		return permuters.get(selectedIndex);
	}

}
