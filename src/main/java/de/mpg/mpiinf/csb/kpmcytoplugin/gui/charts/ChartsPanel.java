package de.mpg.mpiinf.csb.kpmcytoplugin.gui.charts;

import de.mpg.mpiinf.csb.kpmcytoplugin.CyGlobals;
import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import de.mpg.mpiinf.csb.kpmcytoplugin.gui.SpringUtilities;
import dk.sdu.kpm.KPMSettings;
import dk.sdu.kpm.charts.BaseChart;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;

import dk.sdu.kpm.charts.IChart;

@SuppressWarnings("serial")
public class ChartsPanel extends JPanel implements CytoPanelComponent {

    private final JTabbedPane tabbedPane;

    private final ArrayList<JPanel> charts;
    public ChartsPanel(){
        // Preparing for displaying multiple charts
        this.setLayout(new BorderLayout());
        tabbedPane = new JTabbedPane();
        this.setPreferredSize(new Dimension(500, 400));
        this.add(tabbedPane, BorderLayout.CENTER);
        this.charts = new ArrayList<JPanel>();
        setVisible(true);
    }

    private void addTab(String title, JPanel content){
        if(content == null){
            return;
        }
        JPanel pan1 = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.gridx = 0;
        c.gridy = 0;
        JLabel lbl = getTitleLabel(title);
        pan1.add(lbl, c);
        c.gridy++;
        pan1.add(content, c);
        pan1.setBorder(new EmptyBorder(10,10,10,10 ));
        JScrollPane scroll = new JScrollPane();
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setViewportView(pan1);
        pan1.setAutoscrolls(true);
        this.tabbedPane.add(title, scroll);
    }

    public void hidePanel(){
        setVisible(false);
    }

    @Override
    public Component getComponent() {
        return tabbedPane;
    }

    @Override
    public CytoPanelName getCytoPanelName() {
        return CytoPanelName.SOUTH;
    }

    @Override
    public String getTitle() {
        return "KPM Result Charts";
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    public void addCharts(List<IChart> charts){
        KPMSettings kpmSettings = CyGlobals.KPM;
        boolean kVaries = kpmSettings.MIN_K < kpmSettings.MAX_K;
        String var1 = "K";
        if (!kpmSettings.USE_INES) {
                var1 = "L";
        }
        String var2 = "L";
        if (kpmSettings.VARYING_L_ID.size() == 1) {
            var2 = kpmSettings.VARYING_L_ID.get(0);
        } else if (kpmSettings.VARYING_L_ID.size() == 2) {
            var1 = kpmSettings.VARYING_L_ID.get(1);
            var2 = kpmSettings.VARYING_L_ID.get(0);
        }
        if(charts == null || charts.size() == 0){
            return;
        }

        List<BaseChart> commons = new ArrayList<BaseChart>();
        List<BaseChart> commonsAvg = new ArrayList<BaseChart>();
        for(IChart chart: charts){
            if(chart == null){
                continue;
            }
            
            if(chart.containsTag(IChart.TagEnum.STANDARD) && chart.containsTag(IChart.TagEnum.AVG)){
                commonsAvg.add((BaseChart)chart);
            }else if(chart.containsTag(IChart.TagEnum.STANDARD)){
                commons.add((BaseChart)chart);
            }
        }
        
        if(commons.size() > 0){
            Collections.sort(commons);
            String title = "Size of best solution";
            this.addTab(title, getChartsPanel(new ArrayList<IChart>(commons), ""));
        }

        if(commons.size() > 0){
            Collections.sort(commonsAvg);
            String title = "Size of all best solutions";
            this.addTab(title, getChartsPanel(new ArrayList<IChart>(commonsAvg), ""));
        }

        HashMap<String, List<IChart>> perturbationLevelsK = new HashMap<String, List<IChart>>();
        HashMap<String, List<IChart>> perturbationLevelsL = new HashMap<String, List<IChart>>();
        HashMap<String, List<IChart>> validationLevelsK = new HashMap<String, List<IChart>>();
        HashMap<String, List<IChart>> validationLevelsL = new HashMap<String, List<IChart>>();
        HashMap<String, List<IChart>> validationLevelsPerturbation = new HashMap<String, List<IChart>>();
        HashMap<String, List<IChart>> KandL = new HashMap<String, List<IChart>>();
        for(IChart chart: charts){
            if(chart.containsTag(IChart.TagEnum.PERTURB_LEVEL_FIXED) && chart.containsTag(IChart.TagEnum.K_FIXED) && !chart.containsTag(IChart.TagEnum.GOLD_STANDARD)){
                if(!perturbationLevelsK.containsKey(chart.getSortName())){
                    perturbationLevelsK.put(chart.getSortName(), new ArrayList<IChart>());
                }

                perturbationLevelsK.get(chart.getSortName()).add(chart);
            }

            if(chart.containsTag(IChart.TagEnum.PERTURB_LEVEL_FIXED) && chart.containsTag(IChart.TagEnum.L_FIXED) && !chart.containsTag(IChart.TagEnum.GOLD_STANDARD)){
                if(!perturbationLevelsL.containsKey(chart.getSortName())){
                    perturbationLevelsL.put(chart.getSortName(), new ArrayList<IChart>());
                }

                perturbationLevelsL.get(chart.getSortName()).add(chart);
            }

            if(chart.containsTag(IChart.TagEnum.PERTURB_LEVEL_FIXED) && chart.containsTag(IChart.TagEnum.K_FIXED) && chart.containsTag(IChart.TagEnum.GOLD_STANDARD)){
                if(!validationLevelsK.containsKey(chart.getSortName())){
                    validationLevelsK.put(chart.getSortName(), new ArrayList<IChart>());
                }

                validationLevelsK.get(chart.getSortName()).add(chart);
            }

            if(chart.containsTag(IChart.TagEnum.K_FIXED) && chart.containsTag(IChart.TagEnum.L_FIXED) && chart.containsTag(IChart.TagEnum.GOLD_STANDARD)){
                if(!validationLevelsPerturbation.containsKey(chart.getSortName())){
                    validationLevelsPerturbation.put(chart.getSortName(), new ArrayList<IChart>());
                }

                validationLevelsPerturbation.get(chart.getSortName()).add(chart);
            }

            if(chart.containsTag(IChart.TagEnum.PERTURB_LEVEL_FIXED) && chart.containsTag(IChart.TagEnum.L_FIXED) && chart.containsTag(IChart.TagEnum.GOLD_STANDARD)){
                if(!validationLevelsL.containsKey(chart.getSortName())){
                    validationLevelsL.put(chart.getSortName(), new ArrayList<IChart>());
                }

                validationLevelsL.get(chart.getSortName()).add(chart);
            }

            if(chart.containsTag(IChart.TagEnum.K_FIXED) && chart.containsTag(IChart.TagEnum.L_FIXED)
                    && chart.containsTag(IChart.TagEnum.ORIGINAL_RESULT)){
                if(!KandL.containsKey(chart.getSortName())){
                    KandL.put(chart.getSortName(), new ArrayList<IChart>());
                }

                KandL.get(chart.getSortName()).add(chart);
            }
        }


        
        List<List<IChart>> sortPertLs = this.sortCharts(perturbationLevelsL);
        if(sortPertLs.size() > 0 && kVaries){
            this.addRobustTabFromList(sortPertLs, "Robustness (Varying K)");
        }

        List<List<IChart>> sortValLs = this.sortCharts(validationLevelsL);
        if(sortValLs.size() > 0 && kVaries){
            this.addRobustTabFromList(sortValLs, "Validation (Varying K)");
        }
        
        List<List<IChart>> sortPertKs = this.sortCharts(perturbationLevelsK);
        List<List<IChart>> sortValKs = this.sortCharts(validationLevelsK);
        if (CyGlobals.KPM.CALCULATE_ONLY_SAME_L_VALUES && !CyGlobals.KPM.VARYING_L_ID.isEmpty()) {
            if (sortPertKs.size() > 0) {
                this.addRobustTabFromList(sortPertKs, "Robustness (Varying L%)");
            }
            if (sortValKs.size() > 0) {
                this.addRobustTabFromList(sortValKs, "Validation (Varying L%)");
            }
        } else if (CyGlobals.KPM.VARYING_L_ID.size() == 1) {
            if (sortPertKs.size() > 0) {
                this.addRobustTabFromList(sortPertKs,
                        "Robustness (Varying " + CyGlobals.KPM.VARYING_L_ID.get(0) + ")");
            }
            if (sortValKs.size() > 0) {
                this.addRobustTabFromList(sortValKs,
                        "Validation (Varying " + CyGlobals.KPM.VARYING_L_ID.get(0) + ")");
            }
        } else if (CyGlobals.KPM.VARYING_L_ID.size() == 2) {
            if (sortPertKs.size() > 0) {
                this.addRobustTabFromList(sortPertKs,
                        "Robustness (Varying " + CyGlobals.KPM.VARYING_L_ID.get(0) + ")");
            }
            if (sortValKs.size() > 0) {
                this.addRobustTabFromList(sortValKs,
                        "Validation (Varying " + CyGlobals.KPM.VARYING_L_ID.get(0) + ")");
            }
            if (sortPertLs.size() > 0) {
                this.addRobustTabFromList(sortPertLs,
                        "Robustness (Varying " + CyGlobals.KPM.VARYING_L_ID.get(1) + ")");
            }
            if (sortValLs.size() > 0) {
                this.addRobustTabFromList(sortValLs,
                        "Validation (Varying " + CyGlobals.KPM.VARYING_L_ID.get(1) + ")");
            }
        }

//        if (CyGlobals.KPM.VARYING_L_ID.size() == 2) {
//            List<List<IChart>> sortValKs = this.sortCharts(validationLevelsK);
//            if (sortValKs.size() > 0) {
//                this.addRobustTabFromList(sortValKs,
//                        "Validation (Varying " + CyGlobals.KPM.VARYING_L_ID.get(1) + ")");
//            }
//
//            List<List<IChart>> sortValLs = this.sortCharts(validationLevelsL);
//            if (sortValLs.size() > 0) {
//                this.addRobustTabFromList(sortValLs, 
//                        "Validation (Varying " + CyGlobals.KPM.VARYING_L_ID.get(0) + ")");
//            }
//        } else {
//            List<List<IChart>> sortValKs = this.sortCharts(validationLevelsK);
//            if (sortValKs.size() > 0) {
//                this.addRobustTabFromList(sortValKs, "Validation (Varying L)");
//            }
//
//            List<List<IChart>> sortValLs = this.sortCharts(validationLevelsL);
//            if (sortValLs.size() > 0 && kVaries) {
//                this.addRobustTabFromList(sortValLs, "Validation (Varying K)");
//            }
//        }
//        List<List<IChart>> sortValKs = this.sortCharts(validationLevelsK);
//        if(sortValKs.size() > 0){
//            this.addRobustTabFromList(sortValKs, "Validation (Varying L)");
//        }
//
//
//        List<List<IChart>> sortValPerts = this.sortCharts(validationLevelsPerturbation);
//        if(sortValPerts.size() > 0){
//            this.addRobustTabFromList(sortValPerts, "Validation (Varying Pert.)");
//        }
//
//
//        List<List<IChart>> sortPertKs = this.sortCharts(perturbationLevelsK);
//        if(sortPertKs.size() > 0){
//           this.addRobustTabFromList(sortPertKs, "Robustness (Varying L)");
//        }




        List<List<IChart>> sortKsLs = this.sortCharts(KandL);
        if(sortKsLs.size() > 0){

            ArrayList<JPanel> perturbationCharts = new ArrayList<JPanel>();
            String tabTitle= "Robustness (Varying Pert.)";
            for(int i = 0; i < sortKsLs.size(); i++) {
                if(sortKsLs.get(i).size() > 0) {
                    String title = "";
                    if (kpmSettings.USE_INES || kpmSettings.VARYING_L_ID.size() == 2) {
                        title = var1 + " = " + Integer.parseInt(sortKsLs.get(i).get(0).getSortName());
                    }
                    perturbationCharts.add(getChartsPanel(sortKsLs.get(i), title));
                }
            }

            JPanel pan1 = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.FIRST_LINE_START;
            c.gridx = 0;
            c.gridy = 0;
            for(int i = 0; i < perturbationCharts.size(); i++){
                c.gridy++;
                pan1.add(perturbationCharts.get(i), c);
            }

            this.addTab(tabTitle, pan1);
        }
        List<List<IChart>> sortValPerts = this.sortCharts(validationLevelsPerturbation);
        boolean addTitle = kpmSettings.USE_INES || kpmSettings.VARYING_L_ID.size() == 2;
        if(sortValPerts.size() > 0){
            this.addRobustTabFromList(sortValPerts, "Validation (Varying Pert.)", var1, addTitle);
        }
    }

    public void setNoChartsTab(){
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("No charts found for run.");
        panel.add(label, BorderLayout.CENTER);

        this.clearCharts();
        this.addTab("No charts", panel);
    }

    private void addRobustTabFromList(List<List<IChart>> listList, String title){
        ArrayList<JPanel> jpanel = new ArrayList<JPanel>();
        for(int i = 0; i < listList.size(); i++){
            if(listList.get(i).size() > 0) {
                String title1 = "Level: " + Integer.parseInt(listList.get(i).get(0).getSortName()) + "%";
                jpanel.add(getChartsPanel(listList.get(i), title1));
            }
        }

        JPanel pan1 = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.gridx = 0;
        c.gridy = 0;
        for(int i = 0; i < jpanel.size(); i++){
            c.gridy++;
            pan1.add(jpanel.get(i), c);
        }

        this.addTab(title, pan1);
    }
    
    private void addRobustTabFromList(List<List<IChart>> listList, String title, String var,
            boolean addTitle){
        ArrayList<JPanel> jpanel = new ArrayList<JPanel>();
        for(int i = 0; i < listList.size(); i++){
            if(listList.get(i).size() > 0) {
                String title1 = "";
                if (addTitle) {
                    title1 = var + " = " + Integer.parseInt(listList.get(i).get(0).getSortName());
                }
                
                jpanel.add(getChartsPanel(listList.get(i), title1));
            }
        }

        JPanel pan1 = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.gridx = 0;
        c.gridy = 0;
        for(int i = 0; i < jpanel.size(); i++){
            c.gridy++;
            pan1.add(jpanel.get(i), c);
        }

        this.addTab(title, pan1);
    }

    private JLabel getTitleLabel(String title){
        Font font = new Font("SansSerif", Font.BOLD, 18);
        JLabel label = new JLabel(title);
        label.setBorder(new EmptyBorder(10,10,10,10 ));
        label.setFont(font);
        return label;
    }

    private List<List<IChart>> sortCharts(HashMap<String, List<IChart>> charts){
        List<List<IChart>> res = new ArrayList<List<IChart>>();

        if(charts == null || charts.keySet().size() == 0){
            return res;
        }

        ArrayList<String> keyList = new ArrayList<String>(charts.keySet());
        Collections.sort(keyList);
        for(int j = 0; j < keyList.size(); j++){
            res.add(j, new ArrayList<IChart>());
            String key = keyList.get(j);
            List<IChart> chartsList = charts.get(key);


            ArrayList<String> chartNames = new ArrayList<String>();
            HashMap<String, IChart> chartsMap = new HashMap<String, IChart>();
            for(IChart chart: chartsList){
                chartsMap.put(chart.getSortTitle(), chart);
                chartNames.add(chart.getSortTitle());
            }

            Collections.sort(chartNames);
            for(int i = 0; i < chartNames.size(); i++){
                res.get(j).add(chartsMap.get(chartNames.get(i)));
            }
        }

        return res;
    }

    private JPanel getChartsPanel(List<IChart> charts, String title){
        JPanel panel = new JPanel(new SpringLayout());
        Font font = new Font("SansSerif", Font.BOLD, 15);
        LineBorder line = new LineBorder(Color.DARK_GRAY);

        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleFont(font);
        panel.setBorder(border);



        for(int i = 0; i < charts.size(); i++) {
            JPanel chartPanel = charts.get(i).getChartPanel();
            chartPanel.setBorder(line);
            chartPanel.setBackground(Color.WHITE);
            panel.add(chartPanel);
        }

        int cols;
        if(charts.size() > 1){
            cols = 2;
        }else{
            cols = 1;
        }

        int rows = (int) Math.ceil(charts.size() / 2);

        if(charts.size() % 2 != 0){
            rows++;
            panel.add(new JLabel());
        }

        SpringUtilities.makeCompactGrid(panel, rows, cols, 5, 5, 5, 5);

        return panel;
    }

//    private void addComponentRow(JComponent component){
//        int chartsPerColumn = 1;
//        if(this.gridX % chartsPerColumn == 0){
//            this.gridX = 0;
//            this.gridY++;
//        }
//        this.gridBagConstraints.gridx = this.gridX;
//        this.gridBagConstraints.gridy = this.gridY;
//        add(component, this.gridBagConstraints);
//    }

    public void addChart(IChart chart){
        int chartsPerColumn = 2;
//        if(this.gridX % chartsPerColumn == 0){
//			this.gridX = 0;
//			this.gridY++;
//		}

//		this.gridBagConstraints.gridx = this.gridX;
//		this.gridBagConstraints.gridy = this.gridY;
//		JPanel chartPanel = chart.getChartPanel();
//		chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//		chartPanel.setBackground(Color.WHITE);
//        add(chartPanel, this.gridBagConstraints);
//        charts.add(chartPanel);
//        this.gridX++;
//
//        // Update last label:
//        String formattedTime = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
//        this.description.setText(String.format("%s [last updated: %s]", standardText, formattedTime));

        this.invalidate();
        this.repaint();
    }

    public void clearCharts(){
        this.tabbedPane.removeAll();
        this.removeAll();
        this.charts.clear();

        this.invalidate();
        this.repaint();
    }

}
