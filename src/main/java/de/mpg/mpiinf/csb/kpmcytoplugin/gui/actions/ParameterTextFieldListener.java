package de.mpg.mpiinf.csb.kpmcytoplugin.gui.actions;

import dk.sdu.kpm.utils.DialogUtils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;

// 

/**
 * Performs sanity checks on the ACO advanced parameter JTextFields of the GUI
 * which hold numbers. <br>
 * Checks whether the value assigned to a text field is inside a given interval,
 * e.g. the pheromone decay rate rho. If the value is outside this interval, a
 * warning is shown and the value is reset to a valid value.
 */
public class ParameterTextFieldListener implements PropertyChangeListener {

	private final double min;
	private final double max;

	/**
	 * Listens to property changes of JTextFields and checks after each
	 * invocation whether the entered Number value is inside the interval [min,
	 * max]. If this is not the case, a warning message is shown and the text
	 * field's value is set to an allowed value.
	 * 
	 * @param min
	 *            - the left-hand limit of the allowed interval
	 * @param max
	 *            - the right-hand limit of the allowed interval
	 */
	public ParameterTextFieldListener(double min, double max) {
		this.min = min;
		this.max = max;
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
			}
			if (invalid) {
                DialogUtils.showNonModalDialog(message, "Parameter out of bounds", DialogUtils.MessageTypes.Warn);
			}
		}

	}

}