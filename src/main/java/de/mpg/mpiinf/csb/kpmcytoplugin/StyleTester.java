package de.mpg.mpiinf.csb.kpmcytoplugin;

import de.mpg.mpiinf.csb.kpmcytoplugin.gui.charts.ChartsPanel;
import de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels.KPMDataTab;
import de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels.KPMPosNegTab;
import de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels.KPMResultsTab;
import de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels.robustness.KPMRobustnessPanel;
import dk.sdu.kpm.charts.BaseChart;
import dk.sdu.kpm.charts.BoxplotChart;
import dk.sdu.kpm.charts.IChart;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

class StyleTester {

	public static void main(String[] args) {
		JFrame frame = new JFrame("Test");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		/*frame.setContentPane(new KParameterPanel(new KLPanel(null)));*/
		/*frame.setContentPane(new LParameterPanel("COAD-EXP-DOWN-p0.05.txt", "wgwr",100, new KLPanel(null)));*/
		frame.setContentPane(new KPMRobustnessPanel(null));
		//frame.setContentPane(new KPMValidationPanel(null));
        //frame.setContentPane(new KPMDataTab(null));
        //KPMParameterTab tab = new KPMParameterTab(null,null,null,null,null,null,null);
        //tab.addParameterPanel("Test1", "Test1", 20);
        //tab.addParameterPanel("Test2", "Test2", 20);
		//frame.setContentPane(tab);

        int minL = 2;
        int maxL = 10;
        int stepL = 4;

        String lid1 = CyGlobals.KPM.externalToInternalIDManager.getOrCreateInternalIdentifier("test1");
        String lid2 = CyGlobals.KPM.externalToInternalIDManager.getOrCreateInternalIdentifier("test1");
        CyGlobals.KPM.INC_L.put(lid1, stepL);
        CyGlobals.KPM.INC_L.put(lid2, stepL);
        CyGlobals.KPM.MIN_L.put(lid1, minL);
        CyGlobals.KPM.MIN_L.put(lid2, minL);
        CyGlobals.KPM.MAX_L.put(lid1, maxL);
        CyGlobals.KPM.MAX_L.put(lid2, maxL);

        CyGlobals.KPM.MIN_K = 2;
        CyGlobals.KPM.MAX_K = 10;
        CyGlobals.KPM.INC_K = 2;
        CyGlobals.KPM.IS_BATCH_RUN = true;

        HashMap<Integer, String> indexLMap = new HashMap<Integer, String>();
        indexLMap.put(0, lid1);
        indexLMap.put(1, lid2);

        //KPMPosNegTab tab = new KPMPosNegTab(null);
        List<IChart> charts = new ArrayList<IChart>();
        for(int i = 5; i <= 10; i += 5){
            String pofTitle = String.format("Perturbation level %d%%, L = %d", i, 5);
            BoxplotChart bpc = new BoxplotChart(pofTitle, "", "Jaccard overlap with original network");
             bpc.getTags().add(IChart.TagEnum.PERTURB_LEVEL_FIXED);
            bpc.getTags().add(IChart.TagEnum.L_FIXED);
            bpc.getTags().add(IChart.TagEnum.ORIGINAL_RESULT);
            bpc.setSortName(String.format("%05d", i));
            bpc.setSortTitle(String.format("Perturbation level %05d%%, L = %05d", i, 5));
            System.out.println(String.format("Perturbation level %05d%%, L = %05d", i, 5));

            charts.add(bpc);

            String pofTitle2 = String.format("Perturbation level %d%%, L = %d", i, 10);
            BoxplotChart bpc2 = new BoxplotChart(pofTitle2, "", "Jaccard overlap with original network");
            bpc2.getTags().add(IChart.TagEnum.PERTURB_LEVEL_FIXED);
            bpc2.getTags().add(IChart.TagEnum.L_FIXED);
            bpc2.getTags().add(IChart.TagEnum.ORIGINAL_RESULT);
            bpc2.getTags().add(IChart.TagEnum.STANDARD);
            bpc2.setSortName(String.format("%05d", i));
            bpc2.setSortTitle(String.format("Perturbation level %05d%%, L = %05d", i, 10));
            System.out.println(String.format("Perturbation level %05d%%, L = %05d", i, 10));

            charts.add(bpc2);

            String pofTitle3 = String.format("Perturbation level %d%%, L = %d", i, 15);
            BoxplotChart bpc3 = new BoxplotChart(pofTitle3, "", "Jaccard overlap with original network");
            bpc3.getTags().add(IChart.TagEnum.PERTURB_LEVEL_FIXED);
            bpc3.getTags().add(IChart.TagEnum.L_FIXED);
            bpc3.getTags().add(IChart.TagEnum.ORIGINAL_RESULT);
            bpc3.getTags().add(IChart.TagEnum.STANDARD);
            bpc3.setSortName(String.format("%05d", i));
            bpc3.setSortTitle(String.format("Perturbation level %05d%%, L = %05d", i, 15));
            System.out.println(String.format("Perturbation level %05d%%, L = %05d", i, 15));

            charts.add(bpc3);
        }

        String pofTitle4 = String.format("K = %d%%, L = %d", 2, 15);
        BoxplotChart bpc4 = new BoxplotChart(pofTitle4, "", "Jaccard overlap with original network");
        bpc4.getTags().add(IChart.TagEnum.K_FIXED);
        bpc4.getTags().add(IChart.TagEnum.L_FIXED);
        bpc4.getTags().add(IChart.TagEnum.ORIGINAL_RESULT);
        bpc4.getTags().add(IChart.TagEnum.STANDARD);
        bpc4.setSortName(String.format("%05d", 2));
        bpc4.setSortTitle(String.format("Perturbation level %05d%%, L = %05d", 2, 15));
        System.out.println(String.format("Perturbation level %05d%%, L = %05d", 2, 15));

        charts.add(bpc4);

        ChartsPanel tab = new ChartsPanel();
        //tab.clearCharts();
        tab.addCharts(charts);

        frame.setContentPane(tab);

        //KPMResultsTab epsp = new KPMResultsTab( null, indexLMap, true, edgeRowMap);
        //JPanel sliderPanel = epsp.getSlider(2, 1, 10, null, "blalblah");
        //frame.setContentPane(epsp);


		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);


        //double nrRewires = 2000000;
        //double nrEdges = 5413413;
        // Convert into percentage of total amount of edges.
        //double percentage = (nrRewires / nrEdges) * 100;
		//System.out.println("percentage = " + percentage);
		/*String[] labels = {"Name: ", "Fax: ", "Email: ", "Address: "};
        int numPairs = labels.length;

        //Create and populate the panel.
        JPanel p = new JPanel(new SpringLayout());
        for (int i = 0; i < numPairs; i++) {
            JLabel l = new JLabel(labels[i], JLabel.TRAILING);
            p.add(l);
            JTextField textField = new JTextField(10);
            l.setLabelFor(textField);
            p.add(textField);
        }

        //Lay out the panel.
        SpringUtilities.makeCompactGrid(p,
                                        numPairs, 2, //rows, cols
                                        6, 6,        //initX, initY
                                        6, 6);       //xPad, yPad

        //Create and set up the window.
        JFrame frame = new JFrame("SpringForm");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Set up the content pane.
        p.setOpaque(true);  //content panes must be opaque
        frame.setContentPane(p);

        //Display the window.
        frame.pack();
        frame.setVisible(true);*/
	}

}
