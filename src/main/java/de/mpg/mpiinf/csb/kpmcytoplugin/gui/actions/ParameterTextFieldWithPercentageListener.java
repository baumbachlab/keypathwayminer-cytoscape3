package de.mpg.mpiinf.csb.kpmcytoplugin.gui.actions;

import de.mpg.mpiinf.csb.kpmcytoplugin.CyGlobals;
import dk.sdu.kpm.utils.DialogUtils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;

public class ParameterTextFieldWithPercentageListener implements PropertyChangeListener{
	
	private final double min;
	private final double max;
	private final IFieldWithPercentage fieldWithPercentage;
	
	public ParameterTextFieldWithPercentageListener(IFieldWithPercentage fieldWithPercentage){
		this.min = (double) 0;
		this.max = Double.POSITIVE_INFINITY;
		this.fieldWithPercentage = fieldWithPercentage;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		JFormattedTextField source = (JFormattedTextField) evt.getSource();
		String message = "The entered value is invalid."
				+ System.getProperty("line.separator");
		Number value = (Number) source.getValue();
		boolean invalid = false;
		if (value != null) {
			double doubleValue = value.doubleValue();
			if (doubleValue < min) {
				message += "The value has to be greater than or equal to "
						+ min + ".";
				source.setValue(min);
				invalid = true;
			} else if (doubleValue > max) {
				message += "The value has to be less than or equal to " + max
						+ ".";
				source.setValue(max);
				invalid = true;
			}else if (doubleValue > 99 && fieldWithPercentage.isInPercentage() && CyGlobals.KPM.USE_INES) {
				message += "The value has to be less than or equal to 99, when calculating percentage.";
				source.setValue(99);
				invalid = true;
			}
			if (invalid) {
                DialogUtils.showNonModalDialog(message, "Parameter out of bounds", DialogUtils.MessageTypes.Warn);
			}
		}else{
			source.setValue(1);
		}
	}

}
