package de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels;

import de.mpg.mpiinf.csb.kpmcytoplugin.CyGlobals;
import de.mpg.mpiinf.csb.kpmcytoplugin.CyProvider;
import de.mpg.mpiinf.csb.kpmcytoplugin.util.CytoscapeFieldNames;
import de.mpg.mpiinf.csb.kpmcytoplugin.util.KPMUtilities;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.vizmap.*;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

import java.awt.*;

/**
 * This is class for creating certain visual styles specific to the KeyPathwayMinerPlugin
 * Example of how to create a visual style and applying to a network:
 * <code>
 *    VisualStyle vs = KPMVisualStyleFactory.createKPMExceptionNodesStyle(true, "Exception");
 * 
 *    CyNetworkView networkView = Cytoscape.getNetworkView(network.getIdentifier());
 *    networkView.setVisualStyle(vs.getName());
 *    Cytoscape.getDesktop().setFocus(networkView.getIdentifier());
 * </code>
 * 
 * @author Nicola Alcaraz Millman
 */
class KPMVisualStyleFactory {



  private DiscreteMapping createNodeExpressionMappingsMapper() {
        DiscreteMapping shapeMap = (DiscreteMapping) CyProvider.vmfFactoryD
                .createVisualMappingFunction(CytoscapeFieldNames.NODE_IS_MAPPED_PROPERTY_NAME,
                String.class, BasicVisualLexicon.NODE_SHAPE);
        shapeMap.putMapValue("no", NodeShapeVisualProperty.TRIANGLE);
        shapeMap.putMapValue("partial", NodeShapeVisualProperty.HEXAGON);
        shapeMap.putMapValue("yes", NodeShapeVisualProperty.ELLIPSE);        
        return shapeMap;
  }
  
  private DiscreteMapping createNodeLabelColorMappingsMapper() {
        DiscreteMapping labelMap = (DiscreteMapping) CyProvider.vmfFactoryD
                .createVisualMappingFunction(CytoscapeFieldNames.NODE_EXCEPTION_PROPERTY_NAME,
                Boolean.class, BasicVisualLexicon.NODE_LABEL_COLOR);
        Color labelColor = new Color(0,153,0);
        labelMap.putMapValue(false, labelColor);
        labelMap.putMapValue(true, labelColor);
        return labelMap;
  }
  
  
    private ContinuousMapping createNodeColorExpressionMappingsMapper() {
        ContinuousMapping mapper = (ContinuousMapping) CyProvider.vmfFactoryC
                .createVisualMappingFunction(CytoscapeFieldNames.NODE_EXPRESSION_MAPPINGS_PROPERTY_NAME,
                Integer.class, BasicVisualLexicon.NODE_FILL_COLOR);
       
        Color underColor = Color.DARK_GRAY;
        Color minColor = Color.RED;
        Color midColor = Color.YELLOW;
        Color maxColor = Color.GREEN;
        Color overColor = Color.LIGHT_GRAY;
        
        BoundaryRangeValues<Paint> brv1 =
                new BoundaryRangeValues<Paint>(underColor, minColor, minColor);
        
        BoundaryRangeValues<Paint> brv2 =
                new BoundaryRangeValues<Paint>(midColor, midColor, midColor);
        
        BoundaryRangeValues<Paint> brv3 =
                new BoundaryRangeValues<Paint>(maxColor, maxColor, overColor);
        
        Double midValue = CyGlobals.KPM.MIN_L.size() / 2.0;
        
        mapper.addPoint(0.0, brv1);
        mapper.addPoint(midValue, brv2);
        mapper.addPoint(CyGlobals.KPM.MIN_L.size(), brv3);
        
        return mapper;
    }
    
    private PassthroughMapping createEdgeWidthMapper(String attr) {
        PassthroughMapping mapper = (PassthroughMapping) CyProvider.vmfFactoryP
                .createVisualMappingFunction(attr, 
                Double.class, BasicVisualLexicon.EDGE_WIDTH);
        
        return mapper;
    }
  
  
  private ContinuousMapping createHitsMapper(double minValue,
            double maxValue, String attr, VisualProperty lexicon) {
        
        ContinuousMapping mapper = (ContinuousMapping) CyProvider.vmfFactoryC
                .createVisualMappingFunction(attr, Double.class, lexicon);
        
        Color underColor = Color.LIGHT_GRAY;
        Color darkBlue = new Color(0, 0, 140);
        Color minColor = Color.WHITE;
        Color midColor = Color.CYAN;
        Color maxColor = darkBlue;
        Color overColor = Color.BLACK;
        
        if (lexicon.equals(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT)) {
            underColor = Color.WHITE;
            minColor = Color.LIGHT_GRAY;
            midColor = Color.GRAY;
            maxColor = Color.DARK_GRAY;
            overColor = Color.BLACK;
        }
        
        BoundaryRangeValues<Paint> brv1 =
                new BoundaryRangeValues<Paint>(underColor, minColor, minColor);
        
        BoundaryRangeValues<Paint> brv2 =
                new BoundaryRangeValues<Paint>(midColor, midColor, midColor);
        
        BoundaryRangeValues<Paint> brv3 =
                new BoundaryRangeValues<Paint>(maxColor, maxColor, overColor);
        
        Double midValue = (maxValue - minValue) / 2.0;
        
        mapper.addPoint(minValue, brv1);
        mapper.addPoint(midValue, brv2);
        mapper.addPoint(maxValue, brv3);
        
        return mapper;
    }
  
  private ContinuousMapping createNodeColorExpressionMapper(String attr, double minValue,
          double maxValue) {
        ContinuousMapping mapper = (ContinuousMapping) CyProvider.vmfFactoryC
                .createVisualMappingFunction(attr, 
                Double.class, BasicVisualLexicon.NODE_FILL_COLOR);
        
        Color underColor = Color.GRAY;
        Color minColor = Color.RED;
        Color midColor = Color.WHITE;
        Color maxColor = Color.GREEN;
        Color overColor = Color.BLUE;
        
        Color maxRed = new Color(255, 43, 0);
        Color midRed = new Color(255, 147, 125);
        Color maxGreen = new Color(0, 255, 26);
        Color midGreen = new Color(127, 255, 140);
        

        if (CyGlobals.KPM.TOTAL_ACTIVE_CASES_MIN < 0.0) {
            if (CyGlobals.KPM.TOTAL_ACTIVE_CASES_MAX == 0.0) {
                minColor = maxRed;
                midColor = midRed;     
                maxColor = Color.WHITE;
            }
        } else {
            if (CyGlobals.KPM.TOTAL_ACTIVE_CASES_MAX > 0.0) {
                minColor = Color.WHITE;
                midColor = midGreen;
                maxColor = maxGreen;
            }
        }
        
        double midValue = (maxValue - minValue) / 2.0;
        
        BoundaryRangeValues<Paint> brv1 =
                new BoundaryRangeValues<Paint>(underColor, minColor, minColor);
        
        BoundaryRangeValues<Paint> brv2 =
                new BoundaryRangeValues<Paint>(midColor, midColor, midColor);
        
        BoundaryRangeValues<Paint> brv3 =
                new BoundaryRangeValues<Paint>(maxColor, maxColor, overColor);
        
        mapper.addPoint(minValue, brv1);
        mapper.addPoint(midValue, brv2);
        mapper.addPoint(maxValue, brv3);        
        
        return mapper;

  }
  

    /**
     * Creates a visual style that colors and changes the shape of nodes
     * if they are exception nodes or not in KPM.
     * 
     * @return the visual style.
     */
    public VisualStyle createKPMExceptionNodesStyle() {
        if(CyProvider.visualStyleFactoryServiceRef == null){
            return null;
        }

        String title = CytoscapeFieldNames.KPM_EXCEPTION_NODE_VISUAL_STYLE_NAME;
        VisualStyle vs = CyProvider.visualStyleFactoryServiceRef.createVisualStyle(title);

        
        DiscreteMapping mapping = (DiscreteMapping) CyProvider.vmfFactoryD
                .createVisualMappingFunction(CytoscapeFieldNames.NODE_EXCEPTION_PROPERTY_NAME,
                Boolean.class, BasicVisualLexicon.NODE_FILL_COLOR);
        mapping.putMapValue(true, Color.RED);
        mapping.putMapValue(false, Color.GREEN);
  
        DiscreteMapping shapeMap = createNodeExpressionMappingsMapper();
            VisualStyle defaultStyle = CyProvider.vmmServiceRef.getDefaultVisualStyle();
            for (VisualMappingFunction vmf: defaultStyle.getAllVisualMappingFunctions()) {
                vs.addVisualMappingFunction(vmf);
            }

        vs.addVisualMappingFunction(mapping);
        vs.addVisualMappingFunction(shapeMap);
        
        if (!KPMUtilities.containsVisualStyle(vs, CyProvider.vmmServiceRef.getAllVisualStyles())) {
            CyProvider.vmmServiceRef.addVisualStyle(vs);
        }
      
        return vs;
    }

    /**
     * Creates a visual style that colors nodes based on how many key pathways in the solutions
     * contain them. Also changes shape of nodes that are exceptions.
     * 
     * @param useDefaultStyle if the default visual style should be used
     * as base or not.
     * @param minHits the minimal value for the "hits" attribute. 
     * @param maxHits the maximal value for the "hits" attribute.
     * @return the visual style. 
     */
    public VisualStyle createKPMHitsWExceptionsStyle(boolean useDefaultStyle,
            double minHits, double maxHits, double edgeMinHits, double edgeMaxHits) {


        String title = CytoscapeFieldNames.KPM_HITS_VISUAL_STYLE_NAME;
        VisualStyle vs = CyProvider.visualStyleFactoryServiceRef.createVisualStyle(title);

        DiscreteMapping shapeMap = createNodeExpressionMappingsMapper();
        ContinuousMapping nodeMap = createHitsMapper(minHits, maxHits, 
                CytoscapeFieldNames.NODE_HITS_PROPERTY_NAME, BasicVisualLexicon.NODE_FILL_COLOR);
        ContinuousMapping edgeMap = createHitsMapper(edgeMinHits, edgeMaxHits, 
                CytoscapeFieldNames.EDGE_HITS_PROPERTY_NAME, BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
        DiscreteMapping nodeLabelMap = createNodeLabelColorMappingsMapper();
        PassthroughMapping edgeWidthMap = 
                createEdgeWidthMapper(CytoscapeFieldNames.EDGE_WIDTH_BY_HITS_PROPERTY_NAME);

                
        if (useDefaultStyle) {
            VisualStyle defaultStyle = CyProvider.vmmServiceRef.getDefaultVisualStyle();
            for (VisualMappingFunction vmf: defaultStyle.getAllVisualMappingFunctions()) {
                vs.addVisualMappingFunction(vmf);
            }
        }
        vs.addVisualMappingFunction(shapeMap);
        vs.addVisualMappingFunction(nodeMap);
        vs.addVisualMappingFunction(edgeMap);
        vs.addVisualMappingFunction(nodeLabelMap);
        vs.addVisualMappingFunction(edgeWidthMap);
        if (!KPMUtilities.containsVisualStyle(vs, CyProvider.vmmServiceRef.getAllVisualStyles())) {
            CyProvider.vmmServiceRef.addVisualStyle(vs);
        }
        return vs;
    }

    /**
     * Creates a visual style that colors nodes based on how many key pathways in the solutions
     * contain them. Also changes shape of nodes that are exceptions.
     * 
     * @return the visual style.
     */
    public VisualStyle createKPMMappingsWExceptionsStyle() {
        if(CyProvider.visualStyleFactoryServiceRef == null){
            return null;
        }


        String title = CytoscapeFieldNames.KPM_MAPPINGS_NODE_VISUAL_STYLE_NAME;
        VisualStyle vs = CyProvider.visualStyleFactoryServiceRef.createVisualStyle(title);

        DiscreteMapping shapeMap = createNodeExpressionMappingsMapper();
        ContinuousMapping colorMap = createNodeColorExpressionMappingsMapper();

        if (true) {
            VisualStyle defaultStyle = CyProvider.vmmServiceRef.getDefaultVisualStyle();
            for (VisualMappingFunction vmf: defaultStyle.getAllVisualMappingFunctions()) {
                vs.addVisualMappingFunction(vmf);
            }
        } 
        vs.addVisualMappingFunction(shapeMap);
        vs.addVisualMappingFunction(colorMap);
        
        if (!KPMUtilities.containsVisualStyle(vs, CyProvider.vmmServiceRef.getAllVisualStyles())) {
            CyProvider.vmmServiceRef.addVisualStyle(vs);
        }
 
        return vs;
    }    
    
    /**
     * Creates a visual style that colors nodes based on how many key pathways in the solutions
     * contain them. Also changes shape of nodes that are exceptions.
     * 
     * @return the visual style.
     */
    public VisualStyle createKPMHitsWExceptionsNormStyle() {
        if(CyProvider.visualStyleFactoryServiceRef == null){
            return null;
        }

        String title = CytoscapeFieldNames.KPM_HITS_VISUAL_STYLE_NAME;
        VisualStyle vs = CyProvider.visualStyleFactoryServiceRef.createVisualStyle(title);

        ContinuousMapping nodeColorMap = createHitsMapper(0.0, 1.0, 
                CytoscapeFieldNames.NODE_HITS_NORMALIZED_PROPERTY_NAME, BasicVisualLexicon.NODE_FILL_COLOR);
        ContinuousMapping edgeColorMap = createHitsMapper(0.0, 1.0, 
                CytoscapeFieldNames.EDGE_HITS_NORMALIZED_PROPERTY_NAME, BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
        DiscreteMapping nodeShapeMap = createNodeExpressionMappingsMapper();
        DiscreteMapping nodeLabelMap = createNodeLabelColorMappingsMapper();
        PassthroughMapping edgeWidthMap = createEdgeWidthMapper(CytoscapeFieldNames.EDGE_WIDTH_BY_HITS_PROPERTY_NAME);

        VisualStyle defaultStyle = CyProvider.vmmServiceRef.getDefaultVisualStyle();
        for (VisualMappingFunction vmf: defaultStyle.getAllVisualMappingFunctions()) {
            vs.addVisualMappingFunction(vmf);
        }

        vs.addVisualMappingFunction(nodeColorMap);
        vs.addVisualMappingFunction(edgeColorMap);        
        vs.addVisualMappingFunction(nodeShapeMap);
        vs.addVisualMappingFunction(nodeLabelMap);
        vs.addVisualMappingFunction(edgeWidthMap);
        if (!KPMUtilities.containsVisualStyle(vs, CyProvider.vmmServiceRef.getAllVisualStyles())) {
            CyProvider.vmmServiceRef.addVisualStyle(vs);
        }
        return vs;
    }    
    
        /**
     * Creates a visual style that colors nodes based on how many key pathways in the solutions
     * contain them. Also changes shape of nodes that are exceptions.
     * 
     * @param useDefaultStyle if the default visual style should be used
     * as base or not.
     * @param minHits the minimal value for the "hits" attribute. 
     * @param maxHits the maximal value for the "hits" attribute.
     * @return the visual style. 
     */
    public VisualStyle createKPMTotalHitsWExceptionsStyle(boolean useDefaultStyle,
            double minHits, double maxHits, double edgeMinHits, double edgeMaxHits) {


        String title = CytoscapeFieldNames.KPM_TOTAL_HITS_VISUAL_STYLE_NAME;
        VisualStyle vs = CyProvider.visualStyleFactoryServiceRef.createVisualStyle(title);
        ContinuousMapping nodeColorMap = createHitsMapper(minHits, maxHits, 
                CytoscapeFieldNames.NODE_TOTAL_HITS_PROPERTY_NAME, BasicVisualLexicon.NODE_FILL_COLOR);
        ContinuousMapping edgeColorMap = createHitsMapper(edgeMinHits, edgeMaxHits, 
                CytoscapeFieldNames.EDGE_TOTAL_HITS_PROPERTY_NAME, BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
        DiscreteMapping nodeShapeMap = createNodeExpressionMappingsMapper();
        DiscreteMapping nodeLabelMap = createNodeLabelColorMappingsMapper();
        PassthroughMapping edgeWidthMap = createEdgeWidthMapper(CytoscapeFieldNames.EDGE_WIDTH_BY_TOTAL_HITS_PROPERTY_NAME);


        if (useDefaultStyle) {
            VisualStyle defaultStyle = CyProvider.vmmServiceRef.getDefaultVisualStyle();
            for (VisualMappingFunction vmf: defaultStyle.getAllVisualMappingFunctions()) {
                vs.addVisualMappingFunction(vmf);
            }           
        }
        vs.addVisualMappingFunction(nodeColorMap);
        vs.addVisualMappingFunction(edgeColorMap);        
        vs.addVisualMappingFunction(nodeShapeMap);
        vs.addVisualMappingFunction(nodeLabelMap);
        vs.addVisualMappingFunction(edgeWidthMap);
        if (!KPMUtilities.containsVisualStyle(vs, CyProvider.vmmServiceRef.getAllVisualStyles())) {
            CyProvider.vmmServiceRef.addVisualStyle(vs);
        }
 
        return vs;
    }

            /**
     * Creates a visual style that colors nodes based on how many key pathways in the solutions
     * contain them. Also changes shape of nodes that are exceptions.
     * 
     * @return the visual style.
     */
    public VisualStyle createKPMTotalHitsWExceptionsNormStyle() {


        String title = CytoscapeFieldNames.KPM_TOTAL_HITS_VISUAL_STYLE_NAME;
        if(CyProvider.visualStyleFactoryServiceRef == null){
            return null;
        }

        VisualStyle vs = CyProvider.visualStyleFactoryServiceRef.createVisualStyle(title);
        ContinuousMapping nodeColorMap = createHitsMapper(0.0, 1.0, 
                CytoscapeFieldNames.NODE_TOTAL_HITS_NORMALIZED_PROPERTY_NAME, BasicVisualLexicon.NODE_FILL_COLOR);
        ContinuousMapping edgeColorMap = createHitsMapper(0.0, 1.0, 
                CytoscapeFieldNames.EDGE_TOTAL_HITS_NORMALIZED_PROPERTY_NAME, BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
        DiscreteMapping nodeShapeMap = createNodeExpressionMappingsMapper();
        DiscreteMapping nodeLabelMap = createNodeLabelColorMappingsMapper();
        PassthroughMapping edgeWidthMap = createEdgeWidthMapper(CytoscapeFieldNames.EDGE_WIDTH_BY_TOTAL_HITS_PROPERTY_NAME);

        VisualStyle defaultStyle = CyProvider.vmmServiceRef.getDefaultVisualStyle();
        for (VisualMappingFunction vmf: defaultStyle.getAllVisualMappingFunctions()) {
            vs.addVisualMappingFunction(vmf);
        }

        vs.addVisualMappingFunction(nodeColorMap);
        vs.addVisualMappingFunction(edgeColorMap);        
        vs.addVisualMappingFunction(nodeShapeMap);
        vs.addVisualMappingFunction(nodeLabelMap);
        vs.addVisualMappingFunction(edgeWidthMap);
        if (!KPMUtilities.containsVisualStyle(vs, CyProvider.vmmServiceRef.getAllVisualStyles())) {
            CyProvider.vmmServiceRef.addVisualStyle(vs);
        }
 
        return vs;
    }
    

   
    
    /**
     * Creates a visual style that colors nodes based on how many differentially expressed
     * cases it has. Also changes shape of nodes that are exceptions.
     * 
     * @return the visual style.
     */
    public VisualStyle createKPMExpressionWExceptionsStyle() {
        if(CyProvider.visualStyleFactoryServiceRef == null){
            return null;
        }


        String title = CytoscapeFieldNames.KPM_EXPRESSION_VISUAL_STYLE_NAME;
        VisualStyle vs = CyProvider.visualStyleFactoryServiceRef.createVisualStyle(title);

        ContinuousMapping nodeColorMap = createNodeColorExpressionMapper(
                CytoscapeFieldNames.NODE_ACTIVE_CASES_PROPERTY_NAME,
                CyGlobals.KPM.TOTAL_ACTIVE_CASES_MIN, CyGlobals.KPM.TOTAL_ACTIVE_CASES_MAX);
        DiscreteMapping nodeShapeMap = createNodeExpressionMappingsMapper();

             VisualStyle defaultStyle = CyProvider.vmmServiceRef.getDefaultVisualStyle();
            for (VisualMappingFunction vmf: defaultStyle.getAllVisualMappingFunctions()) {
                vs.addVisualMappingFunction(vmf);
            }           

        vs.addVisualMappingFunction(nodeColorMap);
        vs.addVisualMappingFunction(nodeShapeMap);
        
        if (!KPMUtilities.containsVisualStyle(vs, CyProvider.vmmServiceRef.getAllVisualStyles())) {
            CyProvider.vmmServiceRef.addVisualStyle(vs);
        }

        return vs;
    }
    
    /**
     * Creates a visual style that colors nodes based on how many differentially expressed
     * cases it has.
     * 
     * @param useDefaultStyle if the default visual style should be used
     * as base or not.
     * @param expressionAttr the name of the node attribute that holds how many
     * differentially expressed cases it contains (must be an integer or double value,
     * can also be negative). 
     * @param minExpr the minimal value for the "expression" attribute. 
     * @param maxExpr the maximal value for the "expression" attribute.
     * @return the visual style. 
     */
    public VisualStyle createKPMExpressionStyle(boolean useDefaultStyle,
            String expressionAttr, double minExpr, double maxExpr) {


        String title = "KPMColorExpression";
        VisualStyle vs = CyProvider.visualStyleFactoryServiceRef.createVisualStyle(title);

        ContinuousMapping nodeColorMap = createNodeColorExpressionMapper(
                expressionAttr, minExpr, maxExpr);
        DiscreteMapping nodeShapeMap = createNodeExpressionMappingsMapper();
        
        if (useDefaultStyle) {
             VisualStyle defaultStyle = CyProvider.vmmServiceRef.getDefaultVisualStyle();
            for (VisualMappingFunction vmf: defaultStyle.getAllVisualMappingFunctions()) {
                vs.addVisualMappingFunction(vmf);
            }           
        }
        vs.addVisualMappingFunction(nodeColorMap);
        vs.addVisualMappingFunction(nodeShapeMap);
        if (!KPMUtilities.containsVisualStyle(vs, CyProvider.vmmServiceRef.getAllVisualStyles())) {
            CyProvider.vmmServiceRef.addVisualStyle(vs);
        }
        
        return vs;
    }
}