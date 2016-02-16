package de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels;

import de.mpg.mpiinf.csb.kpmcytoplugin.CyGlobals;
import de.mpg.mpiinf.csb.kpmcytoplugin.gui.SpringUtilities;
import de.mpg.mpiinf.csb.kpmcytoplugin.gui.actions.IFieldWithPercentage;
import de.mpg.mpiinf.csb.kpmcytoplugin.gui.actions.ParameterTextFieldWithPercentageListener;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Locale;

public class LParameterPanel extends JPanel implements IFieldWithPercentage {

    private static final long serialVersionUID = -8920196640932021733L;

    private boolean isInPercentage;

    private String internalName;

    private final String externalName;

    JCheckBox batchRunCheckBox;

    JCheckBox percentageCheckBox;

    private JLabel percentageCheckBoxLabel;

    JLabel batchRunCheckBoxLabel;

    private JPanel singleRunPanel;

    private JPanel batchRunPanel;

    protected JLabel nameLabel;

    JFormattedTextField valueField;

    JFormattedTextField valueMinField;

    JFormattedTextField valueMaxField;

    private JFormattedTextField valueStepSizeField;

    private final int maxNumberOfCaseExceptions;

    private final KLPanel klPanel;

    private static final String batchRunTip = "Check to specify a range of values for this parameter";
    private static final String minTip = "Smallest value to be tested";
    private static final String maxTip = "Largest value to be tested";
    private static final String stepSizeTip = "Step size of the values";

    public LParameterPanel(String externalName, String internalName,
            int maxNumberOfCaseExceptions, final KLPanel klPanel) {
        GridBagConstraints gbc = new GridBagConstraints();
        this.internalName = internalName;
        this.externalName = externalName;
        this.klPanel = klPanel;
        this.maxNumberOfCaseExceptions = maxNumberOfCaseExceptions;
        this.isInPercentage = false;

        setLayout(new BorderLayout());
        setBorder(new CompoundBorder(new EmptyBorder(10, 0, 0, 0),
                BorderFactory.createTitledBorder(externalName + " (" + internalName + ")")));
        setOpaque(true);

        JPanel checkboxes = getCheckBoxesPanel();
        addSingleRunPanel();
        addBatchRunPanel();
        singleRunPanel.setVisible(true);
        batchRunPanel.setVisible(false);

        JPanel runPanel = new JPanel(new GridBagLayout());
        runPanel.add(singleRunPanel, gbc);
        runPanel.add(batchRunPanel, gbc);

        add(runPanel, BorderLayout.WEST);
        add(checkboxes, BorderLayout.EAST);

    }

    public void disableRange() {
        batchRunCheckBox.setEnabled(false);
        batchRunCheckBox.setSelected(false);
    }

    JPanel getCheckBoxesPanel() {
        batchRunCheckBoxLabel = new JLabel("Use range");
        batchRunCheckBox = new JCheckBox();
        batchRunCheckBox.setToolTipText(batchRunTip);
        batchRunCheckBoxLabel.setToolTipText(batchRunTip);
        batchRunCheckBoxLabel.setPreferredSize(new Dimension(65, 25));
        batchRunCheckBoxLabel.setMinimumSize(new Dimension(65, 25));
        batchRunCheckBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    singleRunPanel.setVisible(false);
                    batchRunPanel.setVisible(true);
                    if (klPanel.isLastCheckBoxToBeSelected()) {
                        klPanel.disableAllUncheckedRangeCheckBoxes();
                    }
                    if (!internalName.trim().equals("")) {                        
                        CyGlobals.KPM.VARYING_L_ID.add(internalName);
                        if (CyGlobals.KPM.VARYING_L_ID.size() > 1) {
                             Collections.sort(CyGlobals.KPM.VARYING_L_ID);                            
                        }
                   }
                } else {
                    singleRunPanel.setVisible(true);
                    batchRunPanel.setVisible(false);
                    klPanel.checkBoxUnselected();
                    klPanel.enableAllRangeCheckBoxes();
                    CyGlobals.KPM.VARYING_L_ID.remove(internalName);
                }
            }
        });
        batchRunCheckBoxLabel.setLabelFor(batchRunCheckBox);

        percentageCheckBoxLabel = new JLabel("Use percentage");
        percentageCheckBox = new JCheckBox();
        percentageCheckBoxLabel.setPreferredSize(new Dimension(90, 25));
        percentageCheckBoxLabel.setMinimumSize(new Dimension(90, 25));
        percentageCheckBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                updatePercentageSetting();
            }
        });
        percentageCheckBox.setToolTipText("Use percentage instead of actual values");
        percentageCheckBoxLabel.setToolTipText("Use percentage instead of actual values");

        JPanel checkboxes = new JPanel(new SpringLayout());
        checkboxes.add(batchRunCheckBoxLabel);
        checkboxes.add(batchRunCheckBox);
        checkboxes.add(percentageCheckBoxLabel);
        checkboxes.add(percentageCheckBox);
        SpringUtilities.makeCompactGrid(checkboxes, 2, 2, 0, 0, 10, 0);
        checkboxes.setOpaque(true);

        return checkboxes;
    }

    public void setPercentageEnabled(boolean enabled) {
        if (this.percentageCheckBox != null) {
            this.percentageCheckBox.setEnabled(enabled);
        }
        if (this.percentageCheckBoxLabel != null) {
            if (enabled) {
                this.percentageCheckBoxLabel.setForeground(Color.BLACK);
            } else {
                this.percentageCheckBoxLabel.setForeground(Color.GRAY);
            }
        }

        updatePercentageSetting();
    }

    public void setPercentageCheckBoxSelected(boolean selected) {
        this.percentageCheckBox.setSelected(selected);
        updatePercentageSetting();
    }

    void updatePercentageSetting() {
        if (percentageCheckBox.isSelected()) {
            this.isInPercentage = true;
            CyGlobals.L_InPercentageMap.put(internalName, true);
            CyGlobals.KPM.VARYING_L_ID_IN_PERCENTAGE.put(internalName, true);
        } else {
            this.isInPercentage = false;
            CyGlobals.L_InPercentageMap.put(internalName, false);
            
            CyGlobals.KPM.VARYING_L_ID_IN_PERCENTAGE.put(internalName, false);
        }
    }

    private void addSingleRunPanel() {
        JPanel srp = new JPanel(new SpringLayout());
        valueField = new JFormattedTextField(
                NumberFormat.getIntegerInstance(Locale.ENGLISH));
        valueField.setValue(CyGlobals.KPM.CASE_EXCEPTIONS_DEFAULT);
        valueField.addPropertyChangeListener("value",
                new ParameterTextFieldWithPercentageListener(this));
        String lTip = "The number of case exceptions for this experiment, please enter a positive integer";
        valueField.setToolTipText(lTip);
        srp.add(valueField);
        valueField.setPreferredSize(new Dimension(180, 25));
        valueField.setMaximumSize(new Dimension(180, 25));
        valueField.setMinimumSize(new Dimension(180, 25));
        SpringUtilities.makeCompactGrid(srp, 1, 1, 5, 5, 5, 5);
        this.singleRunPanel = srp;
    }

    private void addBatchRunPanel() {
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
                new ParameterTextFieldWithPercentageListener(this));
        String lMinTip = "Please enter a positive integer";
        valueMinField.setToolTipText(lMinTip);
        brp.add(lMinLabel);
        brp.add(valueMinField);

        valueMaxField = new JFormattedTextField(
                NumberFormat.getIntegerInstance(Locale.ENGLISH));
        valueMaxField.setValue(CyGlobals.KPM.MAX_L_DEFAULT);
        valueMaxField.setColumns(2);
        valueMaxField.setMinimumSize(new Dimension(25, 25));
        valueMaxField.setPreferredSize(new Dimension(25, 25));
        JLabel lMaxLabel = new JLabel("Max ");
        lMaxLabel.setToolTipText(maxTip);
        valueMaxField.addPropertyChangeListener("value",
                new ParameterTextFieldWithPercentageListener(this));
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
                new ParameterTextFieldWithPercentageListener(this));
        String lStepSizeTip = "Please enter a positive integer";
        valueStepSizeField.setToolTipText(lStepSizeTip);
        valueStepSizeField.setMinimumSize(new Dimension(25, 25));
        valueStepSizeField.setPreferredSize(new Dimension(25, 25));
        brp.add(lStepSizeLabel);
        brp.add(valueStepSizeField);
        SpringUtilities.makeCompactGrid(brp, 1, 6, 5, 5, 5, 5);
        this.batchRunPanel = brp;
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

    public String getInternalName() {
        return internalName;
    }

    public void setExternalName(String externalID) {
        setBorder(BorderFactory.createTitledBorder(externalID));
    }

    public int getValue() {
        return ((Number) valueField.getValue()).intValue();
    }

    public int getMinimumValue() {
        if (batchRunCheckBox.isSelected()) {
            return ((Number) valueMinField.getValue()).intValue();
        } else {
            return getValue();
        }
    }

    public int getMaximumValue() {
        if (batchRunCheckBox.isSelected()) {
            return ((Number) valueMaxField.getValue()).intValue();
        } else {
            return getValue();
        }
    }

    public int getStepSize() {
        if (batchRunCheckBox.isSelected()) {
            return ((Number) valueStepSizeField.getValue()).intValue();
        } else {
            return 0;
        }
    }

    public Integer getMaxNumberOfCaseExceptions() {
        return maxNumberOfCaseExceptions;
    }

    @Override
    public boolean isInPercentage() {
        return this.isInPercentage;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
        setBorder(new CompoundBorder(new EmptyBorder(10, 0, 0, 0),
                BorderFactory.createTitledBorder(externalName + " (" + internalName + ")")));

    }

    public String getExternalName() {
        return externalName;
    }

}
