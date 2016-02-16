package de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels.robustness;

import de.mpg.mpiinf.csb.kpmcytoplugin.CyGlobals;
import de.mpg.mpiinf.csb.kpmcytoplugin.CyProvider;
import de.mpg.mpiinf.csb.kpmcytoplugin.gui.SpringUtilities;
import de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels.KPMMainPanel;
import de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels.StartButtonListener;
import de.mpg.mpiinf.csb.kpmcytoplugin.interfaces.IRobustnessSettings;
import de.mpg.mpiinf.csb.kpmcytoplugin.interfaces.ISearchEnabledListener;
import dk.sdu.kpm.perturbation.IPerturbation;
import dk.sdu.kpm.perturbation.IPerturbation.PerturbationTags;
import dk.sdu.kpm.perturbation.PerturbationService;
import dk.sdu.kpm.utils.DialogUtils;
import org.cytoscape.model.CyRow;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Created by: Martin
 * Date: 14-02-14
 */
@SuppressWarnings("serial")
public class KPMRobustnessPanel extends JPanel implements IRobustnessSettings, ISearchEnabledListener {

	private final JComboBox<IPerturbation> chosenPermuter;

	private final JFormattedTextField startPerturb;

	private final JFormattedTextField maxPerturb;

	private final JFormattedTextField stepPerturb;

	private final JFormattedTextField graphsPerStep;
	
	private final JButton startPerturbationButton;

    private final static String[] DATA_EXTENSIONS = new String[] { "csv",
            "txt", "dat" };
    private final List<FileChooserFilter> filterList;

    private final JButton loadValidationButton;

    private final JTextPane validationList;

    private final JScrollPane validationListScroll;

    private final JLabel goldStandardLabel;

    private final JPanel goldStandardPanel;

    private final JPanel goldStandardContentPanel;

    private volatile String lastSelectedOption;

    private final JComboBox<String> runTypeComboBox;

    private final JPanel panel;

    public KPMRobustnessPanel(KPMMainPanel kpmmp) {
        FileChooserFilter fileChooserFilter = new FileChooserFilter("*.csv,*.txt,*.dat",
                DATA_EXTENSIONS);
        filterList = new ArrayList<FileChooserFilter>(1);
        filterList.add(fileChooserFilter);
		CyGlobals.RobustnessSettings = this;

		KeyAdapter listener = new KeyAdapter(){
			public void keyTyped(KeyEvent e){
				String key = "" + e.getKeyChar();
				try{
					Integer.parseInt(key);

				}catch(NumberFormatException nfe){
					e.consume();
				}
			}
		};
		
		
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder("Robustness Analysis:"));
		panel = new JPanel(new SpringLayout());
		
        String[] runTypes = { "Robustness", "Validation" };
        runTypeComboBox = new JComboBox<String>(runTypes);
        runTypeComboBox.setPreferredSize(new Dimension(100, 25));
        runTypeComboBox.setSelectedIndex(0);
        runTypeComboBox.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String chosen = (String) runTypeComboBox.getSelectedItem();
                String selec = chosen.toLowerCase();

                if (selec.equals(lastSelectedOption)) {
                    return;
                }

                if (selec.equals("validation")) {
                    int length = panel.getComponents().length - 1;
                    panel.add(goldStandardPanel, length - 1);
                    panel.add(goldStandardContentPanel, length);
                    SpringUtilities.makeCompactGrid(panel, 8, 2, 5, 5, 5, 5);
                    KPMRobustnessPanel.this.updateUI();

                    lastSelectedOption = selec;
                    return;
                }

                panel.remove(goldStandardPanel);
                panel.remove(goldStandardContentPanel);
                SpringUtilities.makeCompactGrid(panel, 7, 2, 5, 5, 5, 5);
                KPMRobustnessPanel.this.updateUI();
                lastSelectedOption = selec;
            }
        });
        lastSelectedOption = "Robustness";
        panel.add(new JLabel("Type:"));
        panel.add(runTypeComboBox);

		JLabel l1 = new JLabel("Min graph perturbation (in %):");
		startPerturb = new JFormattedTextField();
		startPerturb.setValue(10);
		startPerturb.addKeyListener(listener);
		startPerturb.setPreferredSize(new Dimension(100, 25));
		l1.setLabelFor(startPerturb);
		panel.add(l1);
		panel.add(startPerturb);
		
			
		JLabel l2 = new JLabel("Step graph perturbation (in %):");
		stepPerturb = new JFormattedTextField();
		stepPerturb.setValue(10);
		stepPerturb.addKeyListener(listener);
		stepPerturb.setPreferredSize(new Dimension(100, 25));
		l2.setLabelFor(stepPerturb);
		panel.add(l2);
		panel.add(stepPerturb);
		
		JLabel l3 = new JLabel("Max graph perturbation (in %):");
		maxPerturb = new JFormattedTextField();
		maxPerturb.setValue(20);
		maxPerturb.addKeyListener(listener);
		maxPerturb.setPreferredSize(new Dimension(100, 25));
		l3.setLabelFor(maxPerturb);
		panel.add(l3);
		panel.add(maxPerturb);
		
		JLabel l4 = new JLabel("# graphs per step:");
		graphsPerStep = new JFormattedTextField();
		graphsPerStep.setValue(1);
		graphsPerStep.addKeyListener(listener);
		graphsPerStep.setPreferredSize(new Dimension(100, 25));
		l4.setLabelFor(graphsPerStep);
		panel.add(l4);
		panel.add(graphsPerStep);

		// Getting the list of permuters dynamically
		List<IPerturbation> permuters = new ArrayList<IPerturbation>();
        permuters.add(PerturbationService.getPerturbation(PerturbationTags.NodeRemoval));
        permuters.add(PerturbationService.getPerturbation(PerturbationTags.NodeSwap));
        permuters.add(PerturbationService.getPerturbation(PerturbationTags.EdgeRemoval));
        permuters.add(PerturbationService.getPerturbation(PerturbationTags.EdgeRewire));

		// Setting up the combobox
		PermuterComboBoxModel model = new PermuterComboBoxModel(permuters);
		chosenPermuter = new JComboBox<IPerturbation>(model);
		chosenPermuter.setPreferredSize(new Dimension(100, 25));
        chosenPermuter.setSelectedIndex(2);
        chosenPermuter.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IPerturbation pert = (IPerturbation) chosenPermuter.getSelectedItem();
                if(pert == null){
                    return;
                }

                if(pert.getTag().equals(PerturbationTags.EdgeRewire)){
                    startPerturb.setText("400");
                    stepPerturb.setText("0");
                    maxPerturb.setText("400");
                    graphsPerStep.setText("10");
                }else{
                    startPerturb.setText("10");
                    stepPerturb.setText("10");
                    maxPerturb.setText("20");
                    graphsPerStep.setText("10");
                }

            }
        });


		JLabel pertTechniqueLabel = new JLabel("Perturbation technique:");
		pertTechniqueLabel.setLabelFor(chosenPermuter);
		panel.add(pertTechniqueLabel);
		panel.add(chosenPermuter);

        goldStandardLabel = new JLabel("Use gold standard set:");
        goldStandardLabel.setToolTipText("Compare list of relevant genes with results from pathways.");
        validationList = new JTextPane();
        String posTextTip = "Please enter a newline-separated list of nodes for validation";
        validationList.setToolTipText(posTextTip);
        validationList.setPreferredSize(new Dimension(100, 200));
        validationListScroll = new JScrollPane(validationList);
        validationListScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        validationListScroll.setPreferredSize(new Dimension(100, 200));
        goldStandardLabel.setLabelFor(validationList);
        panel.add(validationListScroll);
        loadValidationButton = new JButton("From file..");
        loadValidationButton
                .setToolTipText("Loads a file containing newline-separated node identifiers");
        loadValidationButton.setPreferredSize(new Dimension(90, 25));
        loadValidationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File posFile = CyProvider.fileUtil.getFile(KPMRobustnessPanel.this,
                        "Load validation nodes from file", FileUtil.LOAD,
                        filterList);
                if (posFile != null) {
                    checkAndAddToValidationField(parseNodesFromFile(posFile));
                    addToTextField("", Color.BLACK);
                }
            }
        });
        JButton checkValidationButton = new JButton("Check");
        checkValidationButton.setToolTipText("Checks for presence in the graph.");
        checkValidationButton.setPreferredSize(new Dimension(90, 25));
        checkValidationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkGeneIDs();
            }

        });

        goldStandardPanel = new JPanel(new BorderLayout());
        goldStandardPanel.add(goldStandardLabel, BorderLayout.NORTH);
        goldStandardContentPanel = new JPanel(new BorderLayout());
        goldStandardContentPanel.add(validationListScroll, BorderLayout.CENTER);


        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(loadValidationButton, BorderLayout.WEST);
        buttonPanel.add(checkValidationButton, BorderLayout.EAST);

        goldStandardContentPanel.add(buttonPanel, BorderLayout.SOUTH);


        JLabel l0 = new JLabel("");
        startPerturbationButton = new JButton("Start run");
        startPerturbationButton.setEnabled(false);
        startPerturbationButton.addActionListener(new StartButtonListener(CyGlobals.RunTypeEnum.Robustness, kpmmp));
        l0.setLabelFor(startPerturbationButton);
        panel.add(l0);
        panel.add(startPerturbationButton);

		panel.setOpaque(true);
		setOpaque(true);

        SpringUtilities.makeCompactGrid(panel, 7, 2, 5, 5, 5, 5);
		add(panel, BorderLayout.NORTH);
		setEnabled(false);

        if(!CyGlobals.SearchEnabledListeners.contains(this)){
            CyGlobals.SearchEnabledListeners.add(this);
        }
	}
    public synchronized void checkGeneIDs() {
        String[] geneIDs = validationList.getText().split(CyGlobals.KPM.lineSep);
        validationList.setText("");
        List<String> geneIDSet = new LinkedList<String>();
        Collections.addAll(geneIDSet, geneIDs);
        checkAndAddToTextField(geneIDSet, validationList);
        addToTextField("", Color.BLACK, validationList);
    }

    private void addToTextField(String s, Color c, JTextPane jtp) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY,
                StyleConstants.Foreground, c);
        int len = jtp.getDocument().getLength(); // Length of the current
        // text.
        jtp.setCaretPosition(len); // Sets the caret to the end of the text.
        jtp.setCharacterAttributes(aset, false);
        if (s.length() > 0) {
            jtp.replaceSelection(s + CyGlobals.KPM.lineSep); // Nothing selected ->
            // inserts at the end of
            // the document
        } else {
            jtp.replaceSelection(s);
        }
    }

    private synchronized void checkAndAddToTextField(List<String> nodeIDs, JTextPane jtp) {
        jtp.setText("");
        int notMappedAmount = 0;
        ArrayList<String> mapped = new ArrayList<String>();
        for (String nodeID : nodeIDs) {
            if (nodeID.length() > 0) {
                if (checkIfNodeIsPresent(nodeID.trim())) {
                    addToTextField(nodeID.trim(), Color.BLACK, jtp);
                    mapped.add(nodeID);
                } else {
                    addToTextField(nodeID.trim(), Color.RED, jtp);
                    notMappedAmount++;
                }
            }
        }

        if(notMappedAmount > 0){
            if (JOptionPane.showConfirmDialog(null, notMappedAmount + "  nodes were not found in the network.\nDo you want them removed?", "Unmapped gold standard nodes",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                checkAndAddToTextField(mapped, jtp);
            }
        }
    }

	@Override
	public int getPerturbationValue() {
		return Integer.parseInt(startPerturb.getText());
	}

	@Override
	public int getStepPerturbationValue() {
		return Integer.parseInt(stepPerturb.getText());
	}

	@Override
	public int getMaxPerturbationValue() {
		return Integer.parseInt(maxPerturb.getText());
	}

	@Override
	public int getGraphsPerStep() {
		return Integer.parseInt(graphsPerStep.getText());
	}

	@Override
	public IPerturbation getPerturbationTechnique() {
		Object chosen = chosenPermuter.getSelectedItem();
		if(chosen instanceof IPerturbation){
			return (IPerturbation) chosen;
		}

		return null;
	}


    /**
     * From ISearchEnabledListener
     */
    @Override
    public void setEnabled() {
        this.startPerturbationButton.setEnabled(true);
    }

    /**
     * From ISearchEnabledListener
     */
    @Override
    public void setDisabled() {
        this.startPerturbationButton.setEnabled(false);
    }
    private void addToTextField(String s, Color c) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY,
                StyleConstants.Foreground, c);
        int len = validationList.getDocument().getLength(); // Length of the
        // current
        // text.
        validationList.setCaretPosition(len); // Sets the caret to the end of
        // the text.
        validationList.setCharacterAttributes(aset, false);
        if (s.length() > 0) {
            validationList.replaceSelection(s + CyGlobals.KPM.lineSep); // Nothing
            // selected
            // ->
            // inserts at the end of
            // the document
        } else {
            validationList.replaceSelection(s);
        }
    }

    /**
     * Copy from PosNegtab
     *
     * @param text
     * @return
     */
    private List<String> parseNodesFromText(String text) {
        if (text == null || (text.trim().length() == 0)) {
            return new LinkedList<String>();
        }
        List<String> nodeIDs = new LinkedList<String>();
        String sep = CyGlobals.KPM.lineSep;
        String[] split = text.split(sep);
        for (String s : split) {
            String tmp = s.trim();
            if (tmp.length() != 0) {
                nodeIDs.add(tmp);
            }
        }
        return nodeIDs;
    }

    /**
     * Copy from PosNegtab
     *
     * @param file
     * @return
     */
    private List<String> parseNodesFromFile(File file) {
        if (file == null) {
            return new LinkedList<String>();
        }
        BufferedReader br = null;
        List<String> nodeIDs = null;
        try {
            nodeIDs = new LinkedList<String>();
            br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            while (line != null) {
                nodeIDs.addAll(parseNodesFromText(line));
                line = br.readLine();
            }
        } catch (FileNotFoundException e) {
            return new LinkedList<String>();
        } catch (IOException e) {
            return new LinkedList<String>();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
            }
        }
        return nodeIDs;
    }

    /**
     * Copy from PosNegtab
     *
     * @param nodeIDs
     */
    private void checkAndAddToValidationField(List<String> nodeIDs) {
        for (String nodeID : nodeIDs) {
            if (nodeID.length() > 0) {
                if (checkIfNodeIsPresent(nodeID)) {
                    addToTextField(nodeID, Color.BLACK);
                } else {
                    addToTextField(nodeID, Color.RED);
                }
            }
        }
    }

    /**
     * Copy from PosNegtab
     *
     * @param nodeId
     * @return
     */
    private boolean checkIfNodeIsPresent(String nodeId) {
        if (CyGlobals.WORKING_GRAPH == null) {
            return false;
        }

        Collection<CyRow> cyRows = CyGlobals.WORKING_GRAPH
                .getDefaultNodeTable().getMatchingRows(
                        CyGlobals.CYNODE_KEY_COL_NAME, nodeId);

        if (cyRows == null) {
            return false;
        } else if (cyRows.size() == 0) {
            return false;
        }
        return true;
    }

    @Override
    public List<String> getValidationList() {
        return parseNodesFromText(validationList.getText());
    }

    @Override
    public CyGlobals.RunTypeEnum getRunType() {
        String chosen = (String) runTypeComboBox.getSelectedItem();
        String selec = chosen.toLowerCase();

        if(selec.equals("validation")){
            return CyGlobals.RunTypeEnum.Validation;
        }

        return CyGlobals.RunTypeEnum.Robustness;
    }

}
