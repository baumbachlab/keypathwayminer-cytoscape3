package de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels;
 
import de.mpg.mpiinf.csb.kpmcytoplugin.CyGlobals;
import de.mpg.mpiinf.csb.kpmcytoplugin.CyProvider;
import dk.sdu.kpm.utils.DialogUtils;
import org.cytoscape.model.CyRow;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Creates a JPanel were the user can specify positive/negative lists. This
 * JPanel is shown as a tab in the KPM window. The IDs of the positive/negative
 * nodes can be pasted as plain text into TextFields in the GUI or specified in
 * a text file containing nodes' IDs.
 *
 * Nodes in the positive list are marked as differentially
 * methylated/expressed/... in all cases and the corresponding GeneNode's
 * 'valid' attribute is set to true. Nodes in the negative list are marked as
 * not differentially methylated/expressed/... in all cases and their GeneNode's
 * valid attribute is set to false.
 *
 * @author ajunge
 *
 */
public class KPMPosNegTab extends KPMTab {

    /**
     * User can enter the positive list here.
     */
    private final JTextPane posText;
    /**
     * User can enter the negative list here.
     */
    private final JTextPane negText;
    private final static String[] DATA_EXTENSIONS = new String[]{"csv", "txt", "dat"};
    private final List<FileChooserFilter> filterList;

    /**
     * Creates a new PosNegPanel object which allows the user to specify files
     * containing positive/negative lists.
     *  @param kpmtp - The {@link de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels.KPMTabbedPane} which contains this panels as a
     * tab.
     */
    public KPMPosNegTab(KPMTabbedPane kpmtp) {
        super(kpmtp, de.mpg.mpiinf.csb.kpmcytoplugin.util.CytoscapePanelNames.POSNEGNAME);
        FileChooserFilter fileChooserFilter = new FileChooserFilter("*.csv,*.txt,*.dat", DATA_EXTENSIONS);
        filterList = new ArrayList<FileChooserFilter>(1);
        filterList.add(fileChooserFilter);
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(7, 7, 7, 7));

        posText = new JTextPane();
        JScrollPane posTextScroll = new JScrollPane(posText);
        posTextScroll.setPreferredSize(new Dimension(200, 200));
        String posTextTip = "Please enter a newline-separated list of node identifiers";
        posText.setToolTipText(posTextTip);
        JPanel pos = new JPanel();
        pos.setBorder(BorderFactory
                .createTitledBorder("Positive nodes"));
        JPanel posFileChooser = createPosFileChooser();
        JPanel posFCButton = new JPanel();
        posFCButton.setLayout(new BoxLayout(posFCButton, BoxLayout.Y_AXIS));
        posFCButton.add(posTextScroll);
        posFCButton.add(posFileChooser);
        pos.add(posFCButton);

        negText = new JTextPane();
        JScrollPane negTextScroll = new JScrollPane(negText);
        negTextScroll.setPreferredSize(new Dimension(200, 200));

        String negTextTip = "Please enter a newline-separated list of node identifiers";
        negText.setToolTipText(negTextTip);
        JPanel neg = new JPanel();
        neg.setBorder(BorderFactory
                .createTitledBorder("Negative nodes"));
        //neg.add(negTextScroll);
        JPanel negFileChooser = createNegFileChooser();
        JPanel negFCButton = new JPanel();
        negFCButton.setLayout(new BoxLayout(negFCButton, BoxLayout.Y_AXIS));
        negFCButton.add(negTextScroll);
        negFCButton.add(negFileChooser);
        neg.add(negFCButton);


        // setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.5;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.weighty = 0;

        JButton checkGeneIDsButton = new JButton("Check");
        checkGeneIDsButton
                .setToolTipText("Check for duplicates, and presence in network");
        checkGeneIDsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkGeneIDs();
            }
        });

        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0.5;
        add(pos, c);

//		c.gridx = 0;
//		c.gridy = 1;
//		c.weighty = 0;
//		add(posFileChooser, c);

//		c.gridx = 0;
//		c.gridy = 1;
//		c.weighty = 0;
//		add(new JSeparator(SwingConstants.HORIZONTAL), c);

        c.gridx = 0;
        c.gridy = 1;
        c.weighty = 0.5;
        add(neg, c);

//		c.gridx = 0;
//		c.gridy = 4;
//		c.weighty = 0;
//		add(negFileChooser, c);

//		c.gridx = 0;
//		c.gridy = 3;
//		c.weighty = 0;
//		add(new JSeparator(SwingConstants.HORIZONTAL), c);

        c.gridx = 0;
        c.gridy = 2;
        c.weighty = 0;
        add(checkGeneIDsButton, c);

    }

    /**
     * @return A panel containing a 'Load' button which allows to specify a file
     * using a Cytoscape's FileChooser. This file is then interpreted as
     * containing the IDs of nodes for the positive list.
     */
    private JPanel createPosFileChooser() {
        JPanel posFCPanel = new JPanel();
        /*
      Button which is clicked to load a specify a file containing node IDs for
      the positive list.
     */
        JButton posLoadButton = new JButton("Load positive nodes from file");
        posLoadButton
                .setToolTipText("Loads a file containing newline-separated node identifiers");

        posFCPanel.add(posLoadButton);

        posLoadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File posFile = CyProvider.fileUtil.getFile(KPMPosNegTab.this, "Load positive nodes from file",
                        FileUtil.LOAD, filterList);
                if (posFile == null) {
                } else {
                    HashSet<String> duplicates = getDuplicateNodes();
                    checkAndAddToTextField(parseNodesFromFile(posFile), posText, duplicates);
                    addToTextField("", Color.BLACK, posText);
                }
            }
        });
        return posFCPanel;
    }

    /**
     * @return A panel containing a 'Load' button which allows to specify a file
     * using Cytoscape's FileChooser. This file is then interpreted as
     * containing the IDs of nodes for the negative list.
     */
    private JPanel createNegFileChooser() {
        JPanel negFCPanel = new JPanel();
        /*
      Button which is clicked to load a specify a file containing node IDs for
      the negative list.
     */
        JButton negLoadButton = new JButton("Load negative nodes from file");
        negLoadButton
                .setToolTipText("Loads a file containing newline-separated node identifiers");

        negFCPanel.add(negLoadButton);

        negLoadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File negFile = CyProvider.fileUtil.getFile(KPMPosNegTab.this, "Load positive nodes from file",
                        FileUtil.LOAD, filterList);
                if (negFile == null) {
                } else {
                    HashSet<String> duplicates = getDuplicateNodes();
                    checkAndAddToTextField(parseNodesFromFile(negFile), negText, duplicates);
                    addToTextField("", Color.BLACK, negText);
                }
            }
        });
        return negFCPanel;
    }

    /**
     *
     * @return A list of IDs of nodes which have been specified as positive
     * either in the positive node text box or in a file. If no nodes have been
     * specified, an empty list is returned.
     */
    public List<String> getPositiveList() {
        List<String> ret = new LinkedList<String>();
        List<String> pos = parseNodesFromText(posText.getText());
        // Check for each node if it is present in the network.
        for (String s : pos) {
            if (checkIfNodeIsPresent(s)) {
                ret.add(s);
            }
        }
        // Also update the positive list text field.
        checkGeneIDs();
        return ret;
    }

    /**
     *
     * @return A list of IDs of nodes which have been specified as negative
     * either in the negative node text box or in a file. If no nodes have been
     * specified, an empty list is returned.
     */
    public List<String> getNegativeList() {
        List<String> ret = new LinkedList<String>();
        List<String> neg = parseNodesFromText(negText.getText());
        // Check for each node if it is present in the network.
        for (String s : neg) {
            if (checkIfNodeIsPresent(s)) {
                ret.add(s);
            }
        }
        // Also update negative list text field.
        checkGeneIDs();
        return ret;
    }

    /**
     * Reads node IDs from a given file. The IDs must be separated by line
     * breaks.
     *
     * @param file - The file to read the node IDs from.
     * @return A list of read node IDs or an empty list, if the file is null,
     * could not be read/opened or contains no node IDs in the correct format.
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
     * Parses node IDs from the given String. Individual IDs must be separated
     * by new line characters.
     *
     * @param text - The string containing node IDs.
     * @return A list of node IDs which were contained in the input string. If
     * the text is null or contains only whitespace or no text at all, an empty
     * set is returned.
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

    @Override
    public boolean containsError() {
        // TODO Show error message or, better, warning icon indicating
        // a gene name which was not present in the network.
        return false;
    }

    /**
     * Checks for each element of the given list of {@link String}s if a node
     * with the corresponding name is present in the network. If this is the
     * case, the node is added in regular, black font to the given
     * {@link JTextPane}. If not, the node is added in red font.
     *
     * @param nodeIDs - the strings to be checked and added to the
     * {@link JTabbedPane}.
     * @param jtp - The {@link JTabbedPane} to add the node names to.
     */
    private synchronized int checkAndAddToTextField(List<String> nodeIDs, JTextPane jtp, HashSet<String> duplicates) {
        int duplicateAmount = 0;
        jtp.setText("");
        for (String nodeID : nodeIDs) {
            if (nodeID.length() > 0) {
                if(duplicates.contains(nodeID.toLowerCase().trim())){
                    addToTextField(nodeID.trim(), Color.BLUE, jtp);
                    duplicateAmount++;
                } else if (checkIfNodeIsPresent(nodeID.trim())) {
                    addToTextField(nodeID.trim(), Color.BLACK, jtp);
                } else {
                    addToTextField(nodeID.trim(), Color.RED, jtp);
                }
            }
        }

        return duplicateAmount;
    }

    private HashSet<String> getDuplicateNodes(){
        HashSet<String> duplicateNodes = new HashSet<String>();
        String[] positiveNodes = posText.getText().split(CyGlobals.KPM.lineSep);
        String[] negativeNodes = negText.getText().split(CyGlobals.KPM.lineSep);

        for(String positiveNode : positiveNodes){
            String positiveNode1 = positiveNode.toLowerCase().trim();
            for(String negativeNode : negativeNodes){
                String negativeNode1 = negativeNode.toLowerCase().trim();
                if(positiveNode1.equals(negativeNode1)){
                    duplicateNodes.add(positiveNode1);
                }
            }
        }

        return duplicateNodes;
    }

    /**
     * Appends a string in a specified color to a {@link JTextPane}. The
     * appended string is followed by a new-line character.
     *
     * @param s - the String to be added.
     * @param c - the color of s.
     * @param jtp - the target {@link JTextPane}.
     */
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

    /**
     * Reads all gene IDs from the positive and negative list text fields
     * (geneIDs have to be new-line separated in the text fields). Afterwards
     * each gene ID is checked for presence in the network. Those gene IDs that
     * were not found in the network are colored red in the text fields.
     */
    private synchronized void checkGeneIDs() {

        HashSet<String> duplicates = getDuplicateNodes();
        List<JTextPane> textFields = new LinkedList<JTextPane>();
        textFields.add(posText);
        textFields.add(negText);
        int duplicateAmount = 0;
        for (JTextPane currText : textFields) {
            String[] geneIDs = currText.getText().split(CyGlobals.KPM.lineSep);
            currText.setText("");
            List<String> geneIDSet = new LinkedList<String>();
            Collections.addAll(geneIDSet, geneIDs);
            duplicateAmount = checkAndAddToTextField(geneIDSet, currText, duplicates);
            addToTextField("", Color.BLACK, currText);
        }

        if(duplicateAmount > 0){
            String msg = duplicateAmount + " duplicates were found.\nThese are marked in blue.";
            if(duplicateAmount == 1){
                msg = "A single duplicate was found.\nIt is marked in blue.";
            }
            DialogUtils.showNonModalDialog(msg, "Duplicates", DialogUtils.MessageTypes.Warn);
        }
    }

    // public static void main(String[] args){
    // JFrame f = new JFrame();
    // f.add(new PosNegPanel(null, null, null));
    // f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    // f.setVisible(true);
    // }
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
}
