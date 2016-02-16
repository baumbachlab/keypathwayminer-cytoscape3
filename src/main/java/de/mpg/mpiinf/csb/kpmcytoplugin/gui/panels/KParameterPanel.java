package de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import de.mpg.mpiinf.csb.kpmcytoplugin.CyGlobals;
import de.mpg.mpiinf.csb.kpmcytoplugin.gui.SpringUtilities;
import de.mpg.mpiinf.csb.kpmcytoplugin.gui.actions.ParameterTextFieldListener;


class KParameterPanel extends JPanel  {
	private static final long serialVersionUID = 4236102920873027977L;

	private JFormattedTextField valueField;

	private JFormattedTextField valueMinField;

	private JFormattedTextField valueMaxField;

	private JFormattedTextField valueStepSizeField;
	
	private final JCheckBox batchRunCheckBox;
	
	private JPanel singleRunPanel;

	private JPanel batchRunPanel;

	private boolean useRangeK;
        
        private final KLPanel klPanel;

    private static final String batchRunTip = "Check to specify a range of values for this parameter";
	private static final String minTip = "Smallest value to be tested";
	private static final String maxTip = "Largest value to be tested";
	private static final String stepSizeTip = "Step size of the values";
	
	public KParameterPanel(final KLPanel klPanel) {
            this.klPanel = klPanel;
		setLayout(new BorderLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		JPanel p1 = new JPanel(new SpringLayout());
        this.useRangeK = false;
		setupSingleRunPanel();
		setupBatchRunPanel();

		valueField.setValue(CyGlobals.KPM.CASE_EXCEPTIONS_DEFAULT);
		valueMinField.setValue(CyGlobals.KPM.MIN_K);
		valueMaxField.setValue(CyGlobals.KPM.MAX_K);
		valueStepSizeField.setValue(CyGlobals.KPM.INC_K);	
		
		JLabel batchRunCheckBoxLabel = new JLabel("Use range");
		batchRunCheckBox = new JCheckBox();
		batchRunCheckBox.setToolTipText(batchRunTip);
		batchRunCheckBoxLabel.setToolTipText(batchRunTip);
		batchRunCheckBoxLabel.setPreferredSize(new Dimension(90, 25));
		batchRunCheckBox.addItemListener(new ItemListener() {

			@Override
	               public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
//                        singleRunPanel.setVisible(false);
//                        batchRunPanel.setVisible(true);
//                        useRangeK = true;
//                        if (klPanel != null && klPanel.isLastCheckBoxToBeSelected()) {
//                            klPanel.disableAllUncheckedRangeCheckBoxes();
//                        }
                        setRangePanel(true);
                    } else {
//                        useRangeK = false;
//                        singleRunPanel.setVisible(true);
//                        batchRunPanel.setVisible(false);
//
//                        if (klPanel != null) {
//                            klPanel.checkBoxUnselected();
//                            klPanel.enableAllRangeCheckBoxes();
//                        }
                       setRangePanel(false); 
                       
                    }
                }
            });
		batchRunCheckBoxLabel.setLabelFor(batchRunCheckBox);

		JPanel runPanel = new JPanel(new GridBagLayout());
		runPanel.add(singleRunPanel, gbc);
		runPanel.add(batchRunPanel, gbc);
		
		add(runPanel, BorderLayout.WEST);		
		p1.add(batchRunCheckBoxLabel);
		p1.add(batchRunCheckBox);
		p1.setOpaque(true);
		SpringUtilities.makeCompactGrid(p1, 1, 2, 5, 5, 5, 5);
		
		add(p1, BorderLayout.EAST);	
	}

	
	public void setFieldsEnabled(boolean enabled) {
		valueField.setEnabled(enabled);
		valueMinField.setEnabled(enabled);
		valueMaxField.setEnabled(enabled);
		valueStepSizeField.setEnabled(enabled);
		batchRunCheckBox.setEnabled(enabled);
	}
	
        public void setRangePanel(boolean enable) {
            singleRunPanel.setVisible(!enable);
            batchRunPanel.setVisible(enable);
            useRangeK = enable;
            if (klPanel != null) {
                if (!enable) {
                    klPanel.checkBoxUnselected();
                    klPanel.enableAllRangeCheckBoxes();               
                } else if (klPanel.isLastCheckBoxToBeSelected()) {
                    klPanel.disableAllUncheckedRangeCheckBoxes();
                }
            }
        }
        
        public void setBatchRunCheckBoxSelected(boolean selected) {
            batchRunCheckBox.setSelected(selected);
        }
	
	private void setupSingleRunPanel() {
		JPanel srp = new JPanel(new SpringLayout());
		valueField = new JFormattedTextField(
				NumberFormat.getIntegerInstance(Locale.ENGLISH));
		valueField.setValue(CyGlobals.KPM.CASE_EXCEPTIONS_DEFAULT);
		valueField.addPropertyChangeListener("value",
				new ParameterTextFieldListener(0, Double.POSITIVE_INFINITY));
		String lTip = "The number of case exceptions for this experiment, please enter a positive integer";
		valueField.setToolTipText(lTip);
		srp.add(valueField);
		valueField.setPreferredSize(new Dimension(180, 25));
		valueField.setMaximumSize(new Dimension(180, 25));
		valueField.setMinimumSize(new Dimension(180, 25));
		SpringUtilities.makeCompactGrid(srp, 1, 1, 5, 5, 5, 5);
		
		this.singleRunPanel = srp;
	}

	private void setupBatchRunPanel() {
		JPanel brp = new JPanel(new SpringLayout());	
		
		valueMinField = new JFormattedTextField(
				NumberFormat.getIntegerInstance(Locale.ENGLISH));
		valueMinField.setValue(CyGlobals.KPM.MIN_L_DEFAULT);
		valueMinField.setColumns(2);
		valueMinField.setPreferredSize(new Dimension(25, 25));
		valueMinField.setMinimumSize(new Dimension(25, 25));
		JLabel lMinLabel = new JLabel("Min ");
		lMinLabel.setToolTipText(minTip);
		valueMinField.addPropertyChangeListener("value", 
				new ParameterTextFieldListener(0, Double.POSITIVE_INFINITY));
		String lMinTip = "Please enter a positive integer";
		valueMinField.setToolTipText(lMinTip);
		brp.add(lMinLabel);
		brp.add(valueMinField);

		valueMaxField = new JFormattedTextField(
				NumberFormat.getIntegerInstance(Locale.ENGLISH));
		valueMaxField.setValue(CyGlobals.KPM.MAX_L_DEFAULT);
		valueMaxField.setColumns(2);
		valueMaxField.setPreferredSize(new Dimension(25, 25));
		valueMaxField.setMinimumSize(new Dimension(25, 25));
		JLabel lMaxLabel = new JLabel("Max ");
		lMaxLabel.setToolTipText(maxTip);
		valueMaxField.addPropertyChangeListener("value",
				new ParameterTextFieldListener(0, Double.POSITIVE_INFINITY));
		String lMaxTip = "Please enter a positive integer";
		valueMaxField.setToolTipText(lMaxTip);
		brp.add(lMaxLabel);
		brp.add(valueMaxField);

		valueStepSizeField = new JFormattedTextField(
				NumberFormat.getIntegerInstance(Locale.ENGLISH));
		valueStepSizeField.setValue(CyGlobals.KPM.INC_L_DEFAULT);
		valueStepSizeField.setColumns(2);
		JLabel lStepSizeLabel = new JLabel("Step ");
		lStepSizeLabel.setToolTipText(stepSizeTip);
		valueStepSizeField.addPropertyChangeListener("value",
				new ParameterTextFieldListener(0, Double.POSITIVE_INFINITY));
		String lStepSizeTip = "Please enter a positive integer";
		valueStepSizeField.setToolTipText(lStepSizeTip);
		valueStepSizeField.setPreferredSize(new Dimension(25, 25));
		valueStepSizeField.setMinimumSize(new Dimension(25, 25));
		brp.add(lStepSizeLabel);
		brp.add(valueStepSizeField);
		SpringUtilities.makeCompactGrid(brp, 1, 6, 5, 5, 5, 5);
		
		brp.setVisible(false);
		this.batchRunPanel = brp;
	}


	public int getValue() {
        return ((Number) valueField.getValue()).intValue();
	}

	public int getMinimumValue(){
        if(useRangeK){
            return ((Number) valueMinField.getValue()).intValue();
        }else{
            return getValue();
		}
	}

	public int getMaximumValue() {
		if (useRangeK) {
			return ((Number) valueMaxField.getValue()).intValue();
		} else {
			return getValue();
		}
	}

	public int getStepSize() {
		if (useRangeK) {
			return ((Number) valueStepSizeField.getValue()).intValue();
		} else{
            return getValue();
        }
	}

	public boolean isBatchRunMode() {
		return batchRunCheckBox.isSelected();
	}


	public void enableRangeCheckBox() {
		batchRunCheckBox.setEnabled(true);
	}
	
	public void disableRangeCheckBox() {
		batchRunCheckBox.setEnabled(false);
	}

}
