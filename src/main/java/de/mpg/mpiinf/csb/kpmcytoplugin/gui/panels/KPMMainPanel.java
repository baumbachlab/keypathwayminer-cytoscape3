/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels;

import de.mpg.mpiinf.csb.kpmcytoplugin.CyGlobals;
import de.mpg.mpiinf.csb.kpmcytoplugin.CyProvider;
import de.mpg.mpiinf.csb.kpmcytoplugin.interfaces.IResultsPanelHandler;
import de.mpg.mpiinf.csb.kpmcytoplugin.task.AddResultsPanelTask;
import de.mpg.mpiinf.csb.kpmcytoplugin.util.CytoscapePanelNames;
import de.mpg.mpiinf.csb.kpmcytoplugin.util.Parser;
import dk.sdu.kpm.graph.KPMGraph;
import dk.sdu.kpm.results.IKPMResultSet;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.work.TaskIterator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author nalcaraz
 */
public class KPMMainPanel extends JPanel implements CytoPanelComponent, IResultsPanelHandler {

    /**
     * Holds the initially created KPM tabs "Data", "Help", "Links", "Pos/Neg
     * List", "Run", etc. tabs which are created after starting KPM. After
     * running KPM, "Results" tabs are added.
     */
    private KPMTabbedPane kpmTabbedPane;

    public KPMMainPanel() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(new KPMBannerPanel());
        addKPMTabbedPane();
        this.setPreferredSize(new Dimension(480, 700));
        this.setVisible(true);
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public CytoPanelName getCytoPanelName() {
        return CytoPanelName.WEST;
    }

    @Override
    public String getTitle() {
        return "KeyPathwayMiner";
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    public KPMTabbedPane getKPMTabbedPane() {
        return kpmTabbedPane;
    }

    void addKPMTabbedPane() {
        kpmTabbedPane = new KPMTabbedPane();

        //Add the different tabs. 
        kpmTabbedPane.addTab(CytoscapePanelNames.INPUTNAME,
                new KPMDataTab(kpmTabbedPane));
        kpmTabbedPane.addTab(CytoscapePanelNames.LINKSNAME,
                new KPMLinksTab(kpmTabbedPane));
        kpmTabbedPane.addTab(CytoscapePanelNames.POSNEGNAME,
                new KPMPosNegTab(kpmTabbedPane));
        kpmTabbedPane.addTab(CytoscapePanelNames.RUNNAME,
                new KPMParameterTab(this, kpmTabbedPane, CytoscapePanelNames.RUNNAME));
        kpmTabbedPane.addTab(CytoscapePanelNames.HELPNAME,
                new KPMHelpTab(kpmTabbedPane));
//        
//        // Display the data input panel.
        int indexInputPanel = kpmTabbedPane.indexOfTab(CytoscapePanelNames.INPUTNAME);
        kpmTabbedPane.setSelectedIndex(indexInputPanel);

        kpmTabbedPane.setBorder(new EmptyBorder(0, 5, 0, 0));

        this.add(kpmTabbedPane);

    }

    /**
     * Finishes the file loading process using the previously given indicator
     * matrix. In this process, the {@link KPMGraph} serving as an input for the
     * algorithm is created.
     *
     * @param dataSetFiles - Maps data set identifiers to the respective file
     * paths of the indicator matrix.
     */
    public boolean finishLoadingProcess(HashMap<String, Map<String, int[]>> dataSetFiles) {

        Parser parser = new Parser(dataSetFiles);
        KPMGraph g = parser.createGraph();
        double amountNodes = (double) g.getVertexCount(); //g.getNodeIdSet().size();
        HashMap<String, Integer> nodesExpressionNetwork = parser.getSizeIntersectionDataSetsNetwork();
        StringBuffer buf = new StringBuffer("Number of nodes with measurements: ");
        StringBuffer buf2 = new StringBuffer("The following measurement(s) were not mapped"
                + " to the network: ");
        buf.append(CyGlobals.KPM.lineSep);
        buf2.append(CyGlobals.KPM.lineSep);
        boolean hasNotMapped = false;
        int totalMapped = 0;
        for (String dataSetID : nodesExpressionNetwork.keySet()) {
            int mapped = nodesExpressionNetwork.get(dataSetID);
            if (mapped == 0) {
                buf2.append(dataSetID);
                buf2.append(CyGlobals.KPM.lineSep);
                hasNotMapped = true;
            }

            String internal = CyGlobals.KPM.externalToInternalIDManager.getInternalIdentifier(dataSetID);

            int setSize = 1;
  //          System.out.println("dataSetID: " + dataSetID + ", internal: " + internal);
            if(CyGlobals.L_DatasetFileSizeMap.containsKey(internal)){
                setSize = CyGlobals.L_DatasetFileSizeMap.get(internal);
            }else{
    //            System.out.println("Not found dataSetID: " + internal);
                for(String key: dataSetFiles.keySet()){
                    System.out.println("key: " + key);
                }
                return false;
            }
            int ofSet = (int) Math.ceil(((double) mapped / (double)setSize) * 100);

            int networkCover = (int) Math.ceil(((double) mapped / (double)amountNodes) * 100);

            String mappedStr = String.format(
                    "%d%% of entities in the dataset mapped to the network, (%d%% nodes in the network with measurements)",
                    ofSet,
                    networkCover);



            buf.append(dataSetID).append(": ").append(mappedStr);
            buf.append(CyGlobals.KPM.lineSep);
            totalMapped += mapped;
        }
        if (totalMapped == 0) {
            JOptionPane.showMessageDialog(null,
                "No measurements were mapped to the network. Please check that the expression"
                    + " id's correspond to the node keys in the network.",
                "No measurements mapped",
                JOptionPane.ERROR_MESSAGE);
            return false;
        } else if (hasNotMapped) {
            String[] options = {"Continue", "Cancel"};
            int chose = JOptionPane.showOptionDialog(null,
                    buf2,
                    "Warning", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE, null, options,
                    options[0]);

            if (chose == 1) {
                return false;
            }
        }
        CyGlobals.KPM.MAIN_GRAPH = g;
        //KPMParameters.MAIN_GRAPH.refreshGraph();
        JOptionPane.showMessageDialog(null,
                buf,
                "Measurements mapped",
                JOptionPane.INFORMATION_MESSAGE);
        return true;
    }

    public void addBatchResultsPanel(IKPMResultSet results,  Map<Integer, String> indexLMap, boolean isInes) {
        AddResultsPanelTask task = new AddResultsPanelTask(this.kpmTabbedPane, results, indexLMap, isInes);
        CyProvider.dialogTaskManager.execute(new TaskIterator(task));
    }
}
