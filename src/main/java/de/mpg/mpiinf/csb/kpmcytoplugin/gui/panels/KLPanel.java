package de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.mpg.mpiinf.csb.kpmcytoplugin.CyGlobals;

/**
 * In this sub-panel of the 'Run' tab, the user can specify values for K and the
 * L's (one for each data set). Additionally, instead of specifying just a
 * single value, a range of values can be specified for up to two parameters.
 *
 * @author ajunge
 *
 */
public class KLPanel extends JPanel {

    /**
     * The number of parameters for which ranges are currently specified.
     */
    private int numberOfRangesSpecified = 0;

    /**
     * Here, the user can specify the number of gene exceptions.
     */
    private final KParameterPanel geneExceptionsPanel;

    private LParameterPanel commonLParameterPanel;

    private boolean useSameRangeForAllL = false;

    /**
     * Contains all sub-panels in which an L for a data set can be specified.
     */
    private final List<LParameterPanel> caseExceptionsPanels = new LinkedList<LParameterPanel>();

    /**
     * A panel with a border which holds all K- and L-ParameterPanels.
     */
    private final JPanel caseExceptionsSubPanel;

    /**
     * Shown when no data set is specified.
     */
    private final JPanel noCaseExceptionsPanel;

    private final JPanel nodeExceptionsSubPanel;

    private final KPMParameterTab parentParameterPanel;

    private final JCheckBox sameValueAllLsCheckBox;
    
    public KLPanel(KPMParameterTab parentParameterPanel) {
        this.parentParameterPanel = parentParameterPanel;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        nodeExceptionsSubPanel = new JPanel();
        nodeExceptionsSubPanel.setLayout(new BoxLayout(nodeExceptionsSubPanel,
                BoxLayout.Y_AXIS));
        nodeExceptionsSubPanel.setBorder(BorderFactory
                .createTitledBorder("Node exceptions (K):"));
        nodeExceptionsSubPanel.setEnabled(false);
        geneExceptionsPanel = new KParameterPanel(this);
        nodeExceptionsSubPanel.add(geneExceptionsPanel);

        add(nodeExceptionsSubPanel);

        caseExceptionsSubPanel = new JPanel();
        caseExceptionsSubPanel.setLayout(new BoxLayout(caseExceptionsSubPanel,
                BoxLayout.Y_AXIS));
        caseExceptionsSubPanel.setBorder(BorderFactory
                .createTitledBorder("Case exceptions (L):"));
        caseExceptionsSubPanel.setEnabled(false);

        sameValueAllLsCheckBox = new JCheckBox();
        sameValueAllLsCheckBox.setToolTipText("Set the same L values for all the datasets.");
        sameValueAllLsCheckBox.setSelected(false);
        sameValueAllLsCheckBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                setUseSameValuesForAllIDs(sameValueAllLsCheckBox.isSelected());
            }
        });
        JPanel allSameValuePanel = new JPanel();
        allSameValuePanel.setLayout(new BoxLayout(allSameValuePanel, BoxLayout.X_AXIS));
        JLabel allSameValueLabel = new JLabel("Set same percentage for all datasets:");
        allSameValuePanel.add(allSameValueLabel);
        allSameValuePanel.add(sameValueAllLsCheckBox);
        allSameValuePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        caseExceptionsSubPanel.add(allSameValuePanel);
        createCommonLParameterPanel();
        caseExceptionsSubPanel.add(commonLParameterPanel);

        add(Box.createVerticalStrut(20));
        add(caseExceptionsSubPanel);
        add(Box.createVerticalStrut(10));

        noCaseExceptionsPanel = new JPanel();
        noCaseExceptionsPanel.add(new JLabel(
                "Please specify at least one data set."));
        noCaseExceptionsPanel.setMinimumSize(new Dimension(Integer.MAX_VALUE,
                10));
        caseExceptionsSubPanel.add(noCaseExceptionsPanel);
    }

    /**
     * Adds a new sub-panel for setting a single L parameter to the
     * {@link KLPanel}.
     *
     * @param externalName - The external, user-specified name of the data set
     * corresponding to the L parameter.
     * @param internalName - The internal name of the corresponding data set.
     * @param maxNumberOfCaseExceptions - The maximal number of case exceptions.
     * @return The newly created and added sub-panel.
     */
    public LParameterPanel addParameterPanel(String externalName,
            String internalName, int maxNumberOfCaseExceptions) {
        LParameterPanel lpp = new LParameterPanel(externalName, internalName,
                maxNumberOfCaseExceptions, this);
        if (caseExceptionsPanels.isEmpty()) {
            caseExceptionsSubPanel.remove(noCaseExceptionsPanel);
            commonLParameterPanel.setVisible(false);
        }

        if (useSameRangeForAllL) {
            lpp.setVisible(false);
            commonLParameterPanel.setVisible(true);
        }

        caseExceptionsPanels.add(lpp);
        caseExceptionsSubPanel.add(lpp);

        return lpp;
    }

    private void createCommonLParameterPanel() {

        commonLParameterPanel = new LParameterPanel("Common case exceptions:", CyGlobals.COMMON_L_PANEL_NAME, 100000, this);

        commonLParameterPanel.percentageCheckBox.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent arg0) {
                for (LParameterPanel lpp : caseExceptionsPanels) {
                    lpp.percentageCheckBox.setSelected(commonLParameterPanel.percentageCheckBox.isSelected());
                }
            }
        });

        commonLParameterPanel.batchRunCheckBox.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent arg0) {
                for (LParameterPanel lpp : caseExceptionsPanels) {
                    lpp.batchRunCheckBox.setSelected(commonLParameterPanel.batchRunCheckBox.isSelected());
                }
            }
        });

        KeyAdapter listener = new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                String key = "" + e.getKeyChar();

                try {
                    Integer.parseInt(key);

                } catch (NumberFormatException nfe) {
                    e.consume();
                }
                for (LParameterPanel lpp : caseExceptionsPanels) {
                    lpp.valueMinField.setText(commonLParameterPanel.valueMinField.getText() + key);
                }
            }
        };
        commonLParameterPanel.valueMinField.addKeyListener(listener);
        KeyAdapter listener2 = new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                String key = "" + e.getKeyChar();
                try {
                    Integer.parseInt(key);

                } catch (NumberFormatException nfe) {
                    e.consume();
                }
                for (LParameterPanel lpp : caseExceptionsPanels) {
                    lpp.valueField.setText(commonLParameterPanel.valueField.getText() + key);
                }
            }
        };
        commonLParameterPanel.valueField.addKeyListener(listener2);
        KeyAdapter listener3 = new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                String key = "" + e.getKeyChar();
                try {
                    Integer.parseInt(key);

                } catch (NumberFormatException nfe) {
                    e.consume();
                }
                for (LParameterPanel lpp : caseExceptionsPanels) {
                    lpp.valueMaxField.setText(commonLParameterPanel.valueMaxField.getText() + key);
                }
            }
        };
        commonLParameterPanel.valueMaxField.addKeyListener(listener3);

        commonLParameterPanel.setVisible(false);

        //commonLParameterPanel.batchRunCheckBoxLabel.setForeground(new Color(204, 204, 204));
        commonLParameterPanel.percentageCheckBox.setSelected(true);
        commonLParameterPanel.percentageCheckBox.setEnabled(false);
    }

    /**
     * Removes a sub-panel, in which an L parameter can be set, from the GUI.
     *
     * @param lpp - The sub-panel to be removed.
     */
    public void deleteParameterPanel(LParameterPanel lpp) {
        if (lpp.isBatchRunMode()) {
            checkBoxUnselected();
        }
        caseExceptionsPanels.remove(lpp);
        caseExceptionsSubPanel.remove(lpp);
        if (caseExceptionsPanels.isEmpty()) {
            caseExceptionsSubPanel.add(noCaseExceptionsPanel);
        } else {
            boolean anyEnabled = false;
            for (LParameterPanel lapp : caseExceptionsPanels) {
                if (lapp.isBatchRunMode()) {
                    anyEnabled = true;
                }
                String externalName = lapp.getExternalName();
                lapp.setInternalName(CyGlobals.KPM.externalToInternalIDManager.getInternalIdentifier(externalName));
            }

            if (!anyEnabled) {
                for (LParameterPanel lapp : caseExceptionsPanels) {
                    lapp.enableRangeCheckBox();
                }
            }
        }
    }

    /**
     *
     * @return <code>true</code> iff at least a range of values was specified
     * for at least one parameter.
     */
    public boolean isBatchRunMode() {
        if (geneExceptionsPanel.isBatchRunMode()) {
            return true;
        }

        for (LParameterPanel lpp : caseExceptionsPanels) {
            if (lpp.isBatchRunMode()) {
                return true;
            }
        }
        return false;

    }

    public void setKParameterEnabled(boolean b) {
        geneExceptionsPanel.setFieldsEnabled(b);
        nodeExceptionsSubPanel.setVisible(b);
    }

    public int getGeneExceptions() {
        return geneExceptionsPanel.getValue();
    }

    public int getMinGeneExceptions() {
        return geneExceptionsPanel.getMinimumValue();
    }

    public int getMaxGeneExceptions() {
        return geneExceptionsPanel.getMaximumValue();
    }

    public int getStepSizeGeneExceptions() {
        return geneExceptionsPanel.getStepSize();
    }

    public KParameterPanel getGeneExceptionsPanel() {
        return geneExceptionsPanel;
    }
    public Map<String, Integer> getCaseExceptionsMap() {
        Map<String, Integer> exceptionsMap = new HashMap<String, Integer>();
        for (LParameterPanel lpp : caseExceptionsPanels) {
            if (useSameRangeForAllL) {
                exceptionsMap.put(lpp.getInternalName(), commonLParameterPanel.getValue());
                CyGlobals.L_OriginalValueMapper.SetStdValue(lpp.getInternalName(), commonLParameterPanel.getValue());
            } else {
                exceptionsMap.put(lpp.getInternalName(), lpp.getValue());
                CyGlobals.L_OriginalValueMapper.SetStdValue(lpp.getInternalName(), lpp.getValue());
            }
        }
        return exceptionsMap;
    }

    public Map<String, Integer> getMinCaseExceptionsMap() {
        Map<String, Integer> minLMap = new HashMap<String, Integer>();
        for (LParameterPanel lpp : caseExceptionsPanels) {
            if (useSameRangeForAllL) {
                minLMap.put(lpp.getInternalName(), commonLParameterPanel.getMinimumValue());
                CyGlobals.L_OriginalValueMapper.SetMinValue(lpp.getInternalName(), commonLParameterPanel.getMinimumValue());
            } else {
                minLMap.put(lpp.getInternalName(), lpp.getMinimumValue());
                CyGlobals.L_OriginalValueMapper.SetMinValue(lpp.getInternalName(), lpp.getMinimumValue());
            }
        }
        return minLMap;
    }

    public Map<String, Integer> getMaxCaseExceptionsMap() {
        Map<String, Integer> maxLMap = new HashMap<String, Integer>();
        for (LParameterPanel lpp : caseExceptionsPanels) {
            if (useSameRangeForAllL) {
                maxLMap.put(lpp.getInternalName(), commonLParameterPanel.getMaximumValue());
                CyGlobals.L_OriginalValueMapper.SetMaxValue(lpp.getInternalName(), commonLParameterPanel.getMaximumValue());
            } else {
                maxLMap.put(lpp.getInternalName(), lpp.getMaximumValue());
                CyGlobals.L_OriginalValueMapper.SetMaxValue(lpp.getInternalName(), lpp.getMaximumValue());
            }
        }
        return maxLMap;
    }

    public Map<String, Integer> getStepSizeCaseExceptionsMap() {
        Map<String, Integer> stepSizeMap = new HashMap<String, Integer>();
        for (LParameterPanel lpp : caseExceptionsPanels) {
            if (useSameRangeForAllL) {
                stepSizeMap.put(lpp.getInternalName(), commonLParameterPanel.getStepSize());
                CyGlobals.L_OriginalValueMapper.SetStepValue(lpp.getInternalName(), commonLParameterPanel.getStepSize());
            } else {
                stepSizeMap.put(lpp.getInternalName(), lpp.getStepSize());
                CyGlobals.L_OriginalValueMapper.SetStepValue(lpp.getInternalName(), lpp.getStepSize());
            }
        }
        return stepSizeMap;
    }

    public boolean isLastCheckBoxToBeSelected() {
        /*
         The maximal number of parameters for which ranges can be specified at the
         same time.
         */
        int numberOfRangesMaximal = 2;
        return (++numberOfRangesSpecified) >= numberOfRangesMaximal;
    }

    public void checkBoxUnselected() {
        --numberOfRangesSpecified;
    }

    public void enableSameLCheckBox(boolean enable){
        this.sameValueAllLsCheckBox.setEnabled(enable);
    }
    
    public void disableAllUncheckedRangeCheckBoxes() {
        if (!geneExceptionsPanel.isBatchRunMode() && !CyGlobals.KPM.CALCULATE_ONLY_SAME_L_VALUES) {
            geneExceptionsPanel.disableRangeCheckBox();
        }
        for (LParameterPanel lpp : caseExceptionsPanels) {
            if (!lpp.isBatchRunMode()) {
                lpp.disableRangeCheckBox();
            }
        }

    }

    public void enableAllRangeCheckBoxes() {
        // Only enable the K checkbox if INES was selected.
        if (parentParameterPanel != null && parentParameterPanel.isINEs()) {
            geneExceptionsPanel.enableRangeCheckBox();
        }

        if (caseExceptionsPanels == null) {
            return;
        }

        for (LParameterPanel lpp : caseExceptionsPanels) {
            lpp.enableRangeCheckBox();
        }
    }

    public Map<String, Integer> getMaxNumberOfCaseExceptionsMap() {
        Map<String, Integer> maxCaseExceptionsMap = new HashMap<String, Integer>();
        for (LParameterPanel lpp : caseExceptionsPanels) {
            maxCaseExceptionsMap.put(lpp.getInternalName(),
                    lpp.getMaxNumberOfCaseExceptions());
        }
        return maxCaseExceptionsMap;
    }

    public boolean isKBatchRun() {
        return geneExceptionsPanel.isBatchRunMode();
    }

    public Set<String> getLBatchRunDataSetIDs() {
        Set<String> batchRunDataSets = new HashSet<String>();
        for (LParameterPanel lpp : caseExceptionsPanels) {
            if (lpp.isBatchRunMode()) {
                batchRunDataSets.add(lpp.getInternalName());
            }
        }
        return batchRunDataSets;
    }

    void setUseSameValuesForAllIDs(boolean useSameRangeForAllL) {
        this.useSameRangeForAllL = useSameRangeForAllL;
        CyGlobals.KPM.CALCULATE_ONLY_SAME_L_VALUES = useSameRangeForAllL;

        if (caseExceptionsPanels.size() > 0) {
            commonLParameterPanel.setVisible(this.useSameRangeForAllL);
        }

        if (!useSameRangeForAllL) {
            commonLParameterPanel.disableRange();
        } else {
            commonLParameterPanel.enableRangeCheckBox();
        }

        for (LParameterPanel lpp : caseExceptionsPanels) {
            if (lpp.getInternalName().equals(commonLParameterPanel.getInternalName())) {
                continue;
            }

            lpp.setVisible(!this.useSameRangeForAllL);

            if (!this.useSameRangeForAllL) {
                lpp.enableRangeCheckBox();
                lpp.setPercentageCheckBoxSelected(false);
                lpp.batchRunCheckBox.setSelected(false);
            } else {
                lpp.setPercentageCheckBoxSelected(true);
            }
        }

//        System.out.println(CyGlobals.KPM);
    }

    public int getNumberOfRangesSpecified() {
        return numberOfRangesSpecified;
    }
    
    

}
