package de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels;

import de.mpg.mpiinf.csb.kpmcytoplugin.CyGlobals;
import de.mpg.mpiinf.csb.kpmcytoplugin.CyProvider;
import de.mpg.mpiinf.csb.kpmcytoplugin.gui.actions.AddKPMMainPanelAction;
import dk.sdu.kpm.gui.tree.DataSetNode;

import org.cytoscape.model.CyRow;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//import cytoscape.Cytoscape;


/**
 * A JPanel derived object which contains all necessary functionality to specify
 * and load a single data file. It holds a button to select a data file, a label
 * showing the data file's path, a text field which allows to specify an
 * abbreviation for that file and a button which deletes this panel. In case the
 * {@link KPMDataTab} is removed and a file was specified in the
 * {@link KPMDataTab}, the file is not taken into account in succeeding
 * calculations and especially not while computing the key pathways.
 *
 * @author ajunge
 *
 */
@SuppressWarnings("ALL")
public class KPMFileLoadingPanel extends JPanel {

    /**
     * The dysregulation indicator matrix specified in
     * this panel or <code>null</code> if no file was loaded or the file was in
     * the wrong format.
     */
    private Map<String, int[]> dysregMap;

    /**
     * The {@link KPMDataTab} which created this object. This
     * {@link KPMDataTab} is part of the {@link KPMDataTab}.
     */
    private KPMDataTab dataPanel;

    /**
     * Clicking this button opens Cytoscape's FileChooser.
     */
    private JButton loadButton;

    /**
     * Displays the path of the chosen file.
     */
    private JLabel label;

    /**
     * Allows to specify an abbreviation for the data file. This abbreviations
     * is displayed in the graph where the data sets are connected with logical
     * operators.
     */
    private JTextField textField;

    /**
     * Clicking this button removes the panel from the UI.
     */
    private JButton removeButton;

    /**
     * The number of {@link FileLoadingPanel} objects created so far, used to
     * hash the panel.
     */
    private static int NOOFPANELS = 0;

    /**
     * An ID, unique among all {@link FileLoadingPanel} objects, which is used
     * as a hash value for this object.
     */
    private int id;

    /**
     * The {@link DataSetNode} which represents the data file specified in this
     * panel in the {@link KPMLinksTab}. This is non-null if and only if a valid
     * data set was specified in this panel.
     */
    private DataSetNode dataSetNode;

    /**
     * The default path which is shown when no file has been selected yet.
     */
    private static final String PATHLABELDEFAULT = "no file chosen";

    /**
     * The default abbreviation for data files which can be changed by the user.
     */
    private final static String ABBREVIATIONFIELDDEFAULT = "";

    /**
     * Text of the tool-tip which is shown at the text field used for specifying
     * the abbreviation of the data file.
     */
    private final static String TEXTFIELDTIP = "Please choose an identifier for the experiment";

    private final static String LOADBUTTONTIP = "Loads a file containing an experimental data set";

    private final static String REMOVEBUTTONTIP = "Removes the experiment from the analysis";

    private final static String PATHLABELTIP = "The file conatining the experimental data set";

    private final JLabel nodesCountLabel;

    private final JLabel samplesCountLabel;
    /**
     * A red 'X' displayed on the button used to delete a file panel.
     */
    private static ImageIcon removeIcon;

    /**
     * In this panel, the L parameter for this data set can be specified. This
     * is non-null if and only if a valid data set was specified in this panel.
     */
    private LParameterPanel lParameterPanel;

    private final static String[] DATA_EXTENSIONS = new String[]{"csv","txt","dat"};

    private FileChooserFilter fileChooserFilter;

    private List<FileChooserFilter> filterList;


    /**
     * Constructs a new {@link FileLoadingPanel} object containing:
     *
     * <ul>
     * <li>loadButton- clicking opens Cytoscape's FileChooser to choose a data
     * file</li>
     * <li>label - displays the path of the chosen file</li>
     * <li>textField - can be edited such that an abbreviation for the data set
     * can be given</li>
     * <li>removeButton - removes this object from the GUI</li>
     * </ul>
     *
     * @param dataPanel
     *            - The {@link DataPanel} which created this
     *            {@link FileLoadingPanel}.
     */
    public KPMFileLoadingPanel(KPMDataTab dataPanel) {
        this.dataPanel = dataPanel;
        fileChooserFilter = new FileChooserFilter("*.csv,*.txt,*.dat",DATA_EXTENSIONS);

        filterList = new ArrayList<FileChooserFilter>(1);
        filterList.add(fileChooserFilter);
        loadButton = new JButton("Load");
        loadButton.setToolTipText(LOADBUTTONTIP);
        JPanel loadButtonPanel = new JPanel();
        loadButtonPanel.add(loadButton);
        loadButtonPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        label = new JLabel(PATHLABELDEFAULT);
        label.setToolTipText(PATHLABELTIP);
        textField = new JTextField(ABBREVIATIONFIELDDEFAULT, 20);
        textField.setToolTipText(TEXTFIELDTIP);
        nodesCountLabel = new JLabel("");
        nodesCountLabel.setToolTipText("Number of entities in the dataset, for instance genes or proteins.");
        samplesCountLabel = new JLabel("");

        removeButton = new JButton();
        JPanel removeButtonPanel = new JPanel();
        removeButtonPanel.add(removeButton);
        removeButtonPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        removeIcon = new ImageIcon(this.getClass().getResource(
                "/minus.png"), "Remove");
        removeButton.setIcon(removeIcon);
        removeButton.setToolTipText(REMOVEBUTTONTIP);
        id = NOOFPANELS++;

        addLoadButtonListener();
        addRemoveButtonListener();
        addTextFieldListener();

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;

        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 3;
        add(loadButtonPanel, c);

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = .5;
        c.gridheight = 1;
        c.gridwidth = 3;
        add(label, c);


        c.gridwidth = 1;
        c.gridx = 1;
        c.gridy = 2;
        add(nodesCountLabel, c);

        c.gridx = 2;
        c.gridy = 2;
        add(samplesCountLabel, c);

        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 3;
        add(textField, c);

        c.gridx = 4;
        c.gridy = 0;
        c.weightx = 0;
        c.gridheight = 3;
        c.gridwidth = 1;
        add(removeButtonPanel, c);
        // setMaximumSize(new Dimension(Integer.MAX_VALUE,
        // 3 * removeIcon.getIconHeight()));

        // Add a border.
        Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        this.setBorder(border);
    }

    /**
     * Adds a listener to the text field holding the abbreviation of this data
     * set.
     */
    private void addTextFieldListener() {
        textField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateNodeName();

            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateNodeName();

            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateNodeName();
            }

            private void updateNodeName() {
                if (dataSetNode != null) {
                    String newExternalName = textField.getText();
                    String internalID = CyGlobals.KPM.externalToInternalIDManager
                            .getInternalIdentifier(dataSetNode
                                    .getExternalName());
                    CyGlobals.KPM.externalToInternalIDManager
                            .updateExternalIdentifier(internalID,
                                    newExternalName);
                    dataSetNode.setExternalName(newExternalName);
                    dataPanel.dataSetNamesUnique();
                    KPMLinksTab lp = dataPanel.getKPMTabbedPane()
                            .getLinksPanel();
                    lp.refreshLogicalFormulaLabel();
                    lParameterPanel.setExternalName(newExternalName);


                }
            }
        });
    }

    /**
     * @return The currently picked dysregulation matrix and <code>null</code> when no
     *         well-formatted file has been selected using this
     *         {@link FileLoadingPanel}.
     */
    public Map<String, int[]> getDysregMap() {
        return dysregMap;
    }

    /**
     * @return The abbreviation of the currently picked file and
     *         <code>null</code> when no valid file has been selected using this
     *         {@link FileLoadingPanel}.
     */
    public String getAbbreviation() {
        if (dysregMap != null) {
            return textField.getText();
        } else {
            return null;
        }
    }

    /**
     * Adds a listener to the load button which opens the Cytoscape's
     * FileChooser and updates the file path label and data set abbreviation
     * after a file has been chosen. The listener also makes sure that the
     * specified data file is checked for being in the correct format. If there
     * occurs an error while checking the data file, an error pop-up window is
     * shown and the method returns immediately.
     */
    private void addLoadButtonListener() {
        loadButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                File dataFile = null;
                try {
                    if(CyProvider.fileUtil == null){
                        JFileChooser fc = new JFileChooser();
                        int returnVal = fc.showOpenDialog(KPMFileLoadingPanel.this);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            dataFile = fc.getSelectedFile();
                        }
                    }else{
                        dataFile = CyProvider.fileUtil.getFile(KPMFileLoadingPanel.this, "Load data file", FileUtil.LOAD, filterList);
                    }

                } catch (StringIndexOutOfBoundsException se) {
                    JOptionPane.showMessageDialog(null,"KeyPathwayMiner "
                                    + "only recognizes files with extensions '"
                                    + "txt, csv, dat"
                                    + "' . Please choose another file.",
                            "File extension incorrect",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (dataFile == null) {
                    return;
                } else {
                    // Check format of the data file.
                    final KPMLinksTab lp = dataPanel.getKPMTabbedPane()
                            .getLinksPanel();
                    final KPMParameterTab pp = dataPanel.getKPMTabbedPane()
                            .getParameterPanel();
                    final Map<String, int[]> dysregulationMap = readDysregulationMatrix(dataFile);
                    if (dysregulationMap == null) {
                        // Wrong format, so set everything the dataFile,
                        // all labels and textFields to the defaults.
                        dysregMap = null;
                        label.setText(PATHLABELDEFAULT);
                        textField.setText(ABBREVIATIONFIELDDEFAULT);
                        // Remove potential old data set node.
                        // Remove also entries in the
                        // external/internal identifier mapping
                        if (dataSetNode != null) {
                            CyGlobals.KPM.externalToInternalIDManager
                                    .removeExternalIdentifier(dataSetNode
                                            .getExternalName());
                            lp.removeDataSetNode(dataSetNode);
                            dataSetNode = null;
                            pp.deleteParameterPanel(lParameterPanel);
                            lParameterPanel = null;
                        }

                        nodesCountLabel.setText("");
                        samplesCountLabel.setText("");
                        return;
                    } else {
                        // The specified file is in the correct format.
                        final File finalDataFile = dataFile;
                        CyProvider.dialogTaskManager.execute(new TaskIterator(new AbstractTask() {
                            @Override
                            public void run(TaskMonitor taskMonitor) throws Exception {
                                taskMonitor.setTitle("Producing dataset stats");
                                taskMonitor.setStatusMessage("Retrieving file...");
                                dysregMap = dysregulationMap;
                                // Update the JLabels and JTextFields in this panel.
                                String fileName = finalDataFile.getAbsolutePath();
                                if (fileName.length() > 35) {
                                    fileName = "..."
                                            + fileName.substring(
                                            fileName.length() - 30,
                                            fileName.length());
                                }
                                label.setText(fileName);
                                textField
                                        .setText(finalDataFile.getName());
                                dataPanel.dataSetNamesUnique();

                                taskMonitor.setProgress(0.2);
                                taskMonitor.setStatusMessage("Calculating...");

                                // Remove potential old data set node.
                                if (dataSetNode != null) {
                                    CyGlobals.KPM.externalToInternalIDManager
                                            .removeExternalIdentifier(dataSetNode
                                                    .getExternalName());
                                    lp.removeDataSetNode(dataSetNode);
                                    dataSetNode = null;
                                    pp.deleteParameterPanel(lParameterPanel);
                                    lParameterPanel = null;
                                }


                                taskMonitor.setProgress(0.4);
                                // Create a new data set node in the TreePanel which
                                // represents this data set.
                                String externalID = textField.getText();
                                String internalID = CyGlobals.KPM.externalToInternalIDManager
                                        .createInternalIdentifier(externalID);
                                dataSetNode = new DataSetNode(externalID, internalID,
                                        dysregMap, CyGlobals.KPM.CASE_EXCEPTIONS_DEFAULT);
                                // Add data set node to the tree in LinkPanel.
                                lp.addDataSetNode(dataSetNode);
                                lp.refreshLogicalFormulaLabel();
                                taskMonitor.setProgress(0.6);

                                int columns = dysregMap.get(dysregMap.keySet().iterator().next()).length;

                                // Add L parameter to the ParameterPanel.
                                lParameterPanel = pp.addParameterPanel(externalID,
                                        internalID, dysregMap.values().iterator().next().length);

                                taskMonitor.setProgress(0.8);
                                int count = 0;
                                String sampleNodeId = "";
                                int length = dysregMap.keySet().size();
                                CyGlobals.L_DatasetFileSizeMap.put(internalID, length);
                                CyGlobals.L_DatasetColumnSizeMap.put(internalID, columns);
                                nodesCountLabel.setText("#Entities: "+ length);
                                if(dysregMap.size() > 0) {
                                    samplesCountLabel.setText("#Samples: " + columns);
                                }
                                taskMonitor.setProgress(1);
                                if (dataPanel.getNoFilesLoaded() > 1) {
                                    pp.getKLPanel().enableSameLCheckBox(true);
                                } else{
                                    pp.getKLPanel().enableSameLCheckBox(false);
                                }
                            }
                        }));


                    }
                }

            }

        });
    }

    private synchronized boolean checkIfNodeIsPresent(String nodeId) {
        if (CyGlobals.WORKING_GRAPH == null) {
            return false;
        }

        Collection<CyRow> cyRows = CyGlobals.WORKING_GRAPH
                .getDefaultNodeTable().getMatchingRows(
                        CyGlobals.CYNODE_KEY_COL_NAME, nodeId.trim());

        if (cyRows == null) {
            return false;
        } else if (cyRows.size() == 0) {
            return false;
        }

        return true;
    }

    /**
     * Adds a listener to remove button. After clicking, this panel is removed
     * from the UI and also from the HashSet in {@link MultiFilePanel} which
     * holds all currently displayed {@link FileLoadingPanel}s. Furthermore, the
     * data set representing this vertex is deleted from the {@link LinksPanel}.
     */
    private void addRemoveButtonListener() {
   
        removeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final KPMParameterTab pp = dataPanel.getKPMTabbedPane()
                            .getParameterPanel();
 
                dataPanel.removeFileLoadingPanel(KPMFileLoadingPanel.this);
                if (dataSetNode != null) {
                    CyGlobals.KPM.externalToInternalIDManager
                            .removeExternalIdentifier(dataSetNode
                                    .getExternalName());
                    dataPanel.getKPMTabbedPane().getLinksPanel()
                            .removeDataSetNode(dataSetNode);
                    KPMLinksTab lp = dataPanel.getKPMTabbedPane()
                            .getLinksPanel();
                    lp.refreshLogicalFormulaLabel();
                    lp.backToInitialLayout();
                    dataPanel.getKPMTabbedPane().getParameterPanel()
                            .deleteParameterPanel(lParameterPanel);
                    if (dataPanel.getNoFilesLoaded() > 1) {
                                    pp.getKLPanel().enableSameLCheckBox(true);
                                } else{
                                    pp.getKLPanel().enableSameLCheckBox(false);
                    }
                    dataPanel.getKPMTabbedPane().getParameterPanel().validate();
                    dysregMap = null;
                }
                dataPanel.dataSetNamesUnique();
            }
        });
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof KPMFileLoadingPanel) {
            KPMFileLoadingPanel flp = (KPMFileLoadingPanel) o;
            return flp.id == id;
        }
        return false;
    }

    /**
     * Reads the data file from the given file. Returns <code>null</code> if the
     * data file was not in the correct format, i.e., columns are separated by
     * tabs and all rows, except the first one holding the gene names, only
     * contain 1,-1 or 0. Furthermore, there must be the same number of columns
     * in each row.
     *
     * @param file
     *            - the file to be loaded
     *
     * @return The indicator matrix read from the file.
     */
    private Map<String, int[]> readDysregulationMatrix(File file) {
        // TODO Show Cytoscape progress bar while loading data set
        Map<String, int[]> testResult = readDataFile(file);
        if (testResult == null) {
            JOptionPane.showMessageDialog(null,
                    "There was an error parsing the data set "
                            + CyGlobals.KPM.lineSep + file.getAbsolutePath()
                            + CyGlobals.KPM.lineSep + "Please check that it is in "
                            + "the correct format.", "Parsing error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        } else {
            return testResult;
        }
    }

    /**
     *
     * @param inputData
     *            a string
     * @return \<code>true</code> if the input string is a numerical value and
     *         <code>false</code> otherwise.
     */
    public static boolean isNumeric(String inputData) {
        return inputData.matches("[-+]?\\d+(\\.\\d+)?");
    }

    /**
     * Parses a given expression/methylation/copy number/etc. file. If the
     * parsing is not successful, <code>null</code> returned. Data files must be tab
     * delimited. The first column contains the Node IDs. All other columns are
     * only allowed to hold either 1 or 0 or -1. Each column must have the same
     * number of rows and there must be at least two columns.
     *
     * @param filepath
     *            - The path of the data file to be parsed.
     * @return the indicator matrix or <code>null</code>
     */
    private static Map<String, int[]> readDataFile(File file) {
        Map<String, int[]> dysregulationMap = new HashMap<String, int[]>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            try {
                String line = br.readLine();

                // Check if the loaded file is empty.
                if (line == null) {
                    return null;
                }

                // Set the delimiting character.
                String delim = CyGlobals.KPM.columnDelim;

                // Determine the numbers of columns in the first row.
                // This must be the same in all subsequent rows.
                String[] values = line.split(delim);

                int numCols = values.length;
                if (numCols == 1) {
                    return null;
                }

                if (!(values[1].equals("1")
                        || values[1].equals("0")
                        || values[1].equals("-1"))) {
                    line = br.readLine();
                    if (line == null) {
                        return null;
                    }
                    numCols = line.split(delim).length;
                }




                do {
                    values = line.split(delim);

                    // Compare to the number of columns of first row.
                    if (values.length != numCols) {
                        return null;
                    }

                    // First entry in each row is the node's ID.
                    String nodeId = values[0].trim();
                    // Hence, start from second element of values.
                    int[] exp = new int[values.length - 1];
                    for (int i = 1; i < values.length; i++) {
                        String value = values[i].trim();
                        // Check if the value is numeric and
                        // whether it is 1, 0 or -1.
                        if (value.equals("1")) {
                            exp[i - 1] = 1;
                        } else if (value.equals("-1")) {
                            exp[i - 1] = -1;
                        } else if (value.equals("0")) {
                            exp[i - 1] = 0;
                        } else {
                            return null;
                        }
                    }
                    dysregulationMap.put(nodeId, exp);
                    line = br.readLine();
                } while (line != null);

            } catch (FileNotFoundException ex) {
                Logger.getLogger(AddKPMMainPanelAction.class.getName()).log(
                        Level.SEVERE, null, ex);
                return null;
            } catch (IOException ioe) {
                Logger.getLogger(AddKPMMainPanelAction.class.getName()).log(
                        Level.SEVERE, null, ioe);
                return null;
            } finally {
                br.close(); // Might throw an exception. Hence, the second
                // try-catch.
            }
        } catch (IOException ioe) {
            Logger.getLogger(AddKPMMainPanelAction.class.getName()).log(
                    Level.SEVERE, null, ioe);
            return null;
        }

        return dysregulationMap;
    }

    public JTextField getNameTextField() {
        return this.textField;
    }
}
