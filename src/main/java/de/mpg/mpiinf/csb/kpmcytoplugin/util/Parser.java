/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mpg.mpiinf.csb.kpmcytoplugin.util;

import de.mpg.mpiinf.csb.kpmcytoplugin.CyGlobals;
import dk.sdu.kpm.graph.KPMGraph;
import org.cytoscape.model.*;

import java.util.*;



/**
 * 
 * @author nalcaraz, ajunge
 */
public class Parser {

	/**
	 * Maps identifiers of data sets to the the indicator matrix.
	 */
    private final HashMap<String, Map<String, int[]>> indicatorMatrices;

	/**
	 * Maps network node identifiers to the identifiers of the data sets which
	 * do not contain an indicator vector for this node.
	 */
    private final HashMap<String, Set<String>> backNodesMap;
	/**
	 * For each data set identifier: Nodes in network which are not in data set.
	 * 
	 */
    private final HashMap<String, Set<String>> backNodesByExpMap;
	/**
	 * For each data set identifier: Nodes in data set which are not in network
	 * 
	 */
    private final HashMap<String, Set<String>> backGenesMap;
	/**
	 * Number of cases in each data set.
	 */
    private final HashMap<String, Integer> numCasesMap;
	/**
	 * Number of genes in each data set.
	 */
    private final HashMap<String, Integer> numGenesMap;
	private final HashMap<String, Double> avgExpressedCasesMap;
	private final HashMap<String, Double> avgExpressedGenesMap;
	private final HashMap<String, Integer> totalExpressedMap;
	/**
	 * Number of nodes which are both present in a data set and a network.
	 */
    private final HashMap<String, Integer> sizeIntersectionDataSetsNetwork;
        

	public Parser(HashMap<String, Map<String, int[]>> expressionFiles) {
		this.indicatorMatrices = expressionFiles;
		backNodesMap = new HashMap<String, Set<String>>();
		backNodesByExpMap = new HashMap<String, Set<String>>();
		backGenesMap = new HashMap<String, Set<String>>();
		numCasesMap = new HashMap<String, Integer>();
		numGenesMap = new HashMap<String, Integer>();
		avgExpressedCasesMap = new HashMap<String, Double>();
		avgExpressedGenesMap = new HashMap<String, Double>();
		totalExpressedMap = new HashMap<String, Integer>();

		sizeIntersectionDataSetsNetwork = new HashMap<String, Integer>();
		for (String internalID: expressionFiles.keySet()) {
			String globalID = CyGlobals.KPM.externalToInternalIDManager.getExternalIdentifier(internalID);
			sizeIntersectionDataSetsNetwork.put(globalID, 0);
		}

		for (String expId : expressionFiles.keySet()) {
			backGenesMap.put(expId, new HashSet<String>());
			numCasesMap.put(expId, 0);
		}

	}

	public HashMap<String, Set<String>> getBackNodesMap() {
		return backNodesMap;
	}

	public HashMap<String, Set<String>> getBackGenesMap() {
		return backGenesMap;
	}

	public HashMap<String, Integer> getNumCasesMap() {
		return numCasesMap;
	}

	/**
	 * Creates a new KPMGraph using the data files specified by the user and
	 * Cytoscape's central list of nodes and edges.
	 * 
	 * 
	 * @return The newly created KPMGraph.
	 */
	public KPMGraph createGraph() {
                addKPMAttributes();
		// Node identifier in the network -> gene symbol
        HashMap<String, String> nodeId2Symbol = new HashMap<String, String>();

		// Node identifier -> (data set identifier -> expression indicator
		// vector)
        HashMap<String, Map<String, int[]>> expressionMap = new HashMap<String, Map<String, int[]>>();

		// Edges in the network, pairs of node identifiers
		LinkedList<String[]> edgeList = new LinkedList<String[]>();

		// Data set identifier -> number of nodes with missing indicator vector
		HashMap<String, Integer> without_exp = new HashMap<String, Integer>();

		// Node identifiers
		HashSet<String> inNetwork = new HashSet<String>();

		CyGlobals.KPM.EDGE_ID_MAP = new HashMap<String, String>();


		for (String fileId : indicatorMatrices.keySet()) {
			numCasesMap.put(fileId, 0);
			without_exp.put(fileId, 0);
		}
		// Save the identifiers of all nodes in the network.
                Map<String, Long> id2SuidMap = new HashMap<String, Long>();
		for (CyNode node : CyGlobals.WORKING_GRAPH.getNodeList()) {
			String nodeID = CyGlobals.WORKING_GRAPH.getRow(node).get(
                                CyNetwork.NAME, String.class).trim();
			nodeId2Symbol.put(nodeID, nodeID);
			inNetwork.add(nodeID);
                        id2SuidMap.put(nodeID, node.getSUID());
		}

		// Save all edges in the network.
		for (CyEdge edge : CyGlobals.WORKING_GRAPH.getEdgeList()){
			String[] pair = new String[2];
			pair[0] = CyGlobals.WORKING_GRAPH.getRow(
                                edge.getSource()).get(
                                CyNetwork.NAME, String.class).trim();
			pair[1] = CyGlobals.WORKING_GRAPH.getRow(
                                edge.getTarget()).get(
                                CyNetwork.NAME, String.class).trim();
			String internalId = pair[0] + " (pp) " + pair[1];
			String internalId2 = pair[1] + " (pp) " + pair[0];
                        String edgeId = CyGlobals.WORKING_GRAPH.getRow(
                                edge).get(
                                CyNetwork.NAME, String.class);
			CyGlobals.KPM.EDGE_ID_MAP.put(internalId, edgeId);
			CyGlobals.KPM.EDGE_ID_MAP.put(internalId2, edgeId);
			edgeList.add(pair);
		}

		for (String fileId : indicatorMatrices.keySet()) {
			String externalFileID = CyGlobals.KPM.externalToInternalIDManager
					.getExternalIdentifier(fileId);
                        String columnNameTotal = externalFileID + 
                                " - Total active/dysregulated";
                        String columnNameP = externalFileID + 
                                " - % active/dysregulated";
                        
                        CyTable nodeTable = CyGlobals.WORKING_GRAPH.getDefaultNodeTable();
                        if (nodeTable.getColumn(columnNameTotal) == null) {
                            nodeTable.createColumn(columnNameTotal, Integer.class, false);
                        }
                        if (nodeTable.getColumn(columnNameP) == null) {
                            nodeTable.createColumn(columnNameP, Double.class, false);
                        }
                        
			Map<String, int[]> indicatorMatrix = indicatorMatrices.get(fileId);
			int totalExp = 0;
			int numCases = indicatorMatrix.values().iterator().next().length;
			int numGenes = indicatorMatrix.keySet().size();
			Set<String> inExp = new HashSet<String>(indicatorMatrix.keySet());

			for (String nodeID : indicatorMatrix.keySet()) {
				int[] dysregVec = indicatorMatrix.get(nodeID);
				for (int value : dysregVec) {
					if (value == 1 || value == -1) {
						totalExp++;
					}
				}
				if (expressionMap.containsKey(nodeID)) {
					expressionMap.get(nodeID).put(fileId, dysregVec);
				} else {
					Map<String, int[]> aux = new HashMap<String, int[]>();
					aux.put(fileId, dysregVec);
					expressionMap.put(nodeID, aux);
				}
			}

			// Store the following attribute for each node:
			// (Node ID, fileId" - % dysregulated", D) where D is the
			// fraction of
			// cases where the gene is dysregulated among all cases
			// in the data set with the identifier fileId
			// CyAttributes attr = Cytoscape.getNodeAttributes();
			// attr.setAttribute(nodeId, externalFileID + " - % dysregulated",
			// (double) dysregulatedCases / (double) numCases);
			numCasesMap.put(fileId, numCases);
			totalExpressedMap.put(fileId, totalExp);
			double avgExpCases = 0;
			double avgExpGenes = 0;
			if (totalExp > 0) {
				avgExpCases = (double) numCases / (double) totalExp;
				avgExpGenes = (double) numGenes / (double) totalExp;
			}
			numGenesMap.put(fileId, inExp.size());
			avgExpressedCasesMap.put(fileId, avgExpCases);
			avgExpressedGenesMap.put(fileId, avgExpGenes);
			// Nodes in network which are not in expression studies.
			Set<String> bckN = new HashSet<String>(inNetwork);

			// Nodes in expression studies which are not in network.
			Set<String> bckG = new HashSet<String>(inExp);
			for (String id : inNetwork) {
				if (inExp.contains(id)) {
					bckN.remove(id);
				}
			}
			for (String id : inExp) {
				if (inNetwork.contains(id)) {
					bckG.remove(id);
				}
			}

			backNodesByExpMap.put(fileId, bckN);
			backGenesMap.put(fileId, bckG);
		}

		//
		for (String nodeId : inNetwork) {
                    for (String internalFileID : indicatorMatrices.keySet()) {
                        String globalFileID = CyGlobals.KPM.externalToInternalIDManager.getExternalIdentifier(internalFileID);
                        if (indicatorMatrices.get(internalFileID).keySet().contains(nodeId)) {
                            int oldValue = sizeIntersectionDataSetsNetwork.get(globalFileID);
                            sizeIntersectionDataSetsNetwork.put(globalFileID, oldValue + 1);
                        }
                    }
			int mapped = 0;
			double sum = 0.0;
			if (expressionMap.containsKey(nodeId)) {
	
				Map<String, int[]> expMap = expressionMap.get(nodeId);
				for (String expId : indicatorMatrices.keySet()) {
					if (!expMap.containsKey(expId)) {
						if (backNodesMap.containsKey(nodeId)) {
							backNodesMap.get(nodeId).add(expId);
						} else {
							HashSet<String> aux = new HashSet<String>();
							aux.add(expId);
							backNodesMap.put(nodeId, aux);
						}
					} else {
						mapped++;
						int diff = 0;
						int vectSize = expMap.get(expId).length;
						for (int val : expMap.get(expId)) {
							diff += val;
						}
						sum += ((double) diff / (double) vectSize);
					}
				}
			} else {
				if (backNodesMap.containsKey(nodeId)) {
					backNodesMap.get(nodeId).addAll(indicatorMatrices.keySet());
				} else {
					HashSet<String> aux = new HashSet<String>();
					aux.addAll(indicatorMatrices.keySet());
					backNodesMap.put(nodeId, aux);
				}
			}

			double totalExp = sum / (double)numCasesMap.size();
			if (totalExp > CyGlobals.KPM.TOTAL_ACTIVE_CASES_MAX) {
				CyGlobals.KPM.TOTAL_ACTIVE_CASES_MAX = totalExp;
			}

			if (totalExp < CyGlobals.KPM.TOTAL_ACTIVE_CASES_MIN) {
				CyGlobals.KPM.TOTAL_ACTIVE_CASES_MIN = totalExp;
			}

			String mapValue = "yes";
			if (mapped == 0) {
				mapValue = "no";
			} else if (mapped == numCasesMap.size()) {
				mapValue = "yes";
			} else if (mapped < numCasesMap.size()) {
				mapValue = "partial";
			}


                        CyTable nodeTable = CyGlobals.WORKING_GRAPH.getDefaultNodeTable();
                        
                        CyRow row = nodeTable.getRow(id2SuidMap.get(nodeId));
                        row.set(CytoscapeFieldNames.NODE_IS_MAPPED_PROPERTY_NAME, mapValue);
                        row.set(CytoscapeFieldNames.NODE_EXPRESSION_MAPPINGS_PROPERTY_NAME, mapped);
                        row.set(CytoscapeFieldNames.NODE_ACTIVE_CASES_PROPERTY_NAME, totalExp);
		}

		CyGlobals.KPM.NUM_CASES_MAP = numCasesMap;
		CyGlobals.KPM.NUM_STUDIES = numCasesMap.size();
		return new KPMGraph(expressionMap, edgeList, nodeId2Symbol,
				backNodesMap, backGenesMap, CyGlobals.KPM.NUM_CASES_MAP);
	}
        
        private void addKPMAttributes() {
            CyTable nodeTable = CyGlobals.WORKING_GRAPH.getDefaultNodeTable();
            
            if (nodeTable.getColumn(CytoscapeFieldNames.NODE_IS_MAPPED_PROPERTY_NAME)
                    == null) {
                nodeTable.createColumn(CytoscapeFieldNames.NODE_IS_MAPPED_PROPERTY_NAME, String.class, false);
            }
            if (nodeTable.getColumn(CytoscapeFieldNames.NODE_ACTIVE_CASES_PROPERTY_NAME)
                    == null) {
                nodeTable.createColumn(CytoscapeFieldNames.NODE_ACTIVE_CASES_PROPERTY_NAME, Double.class, false);
            }
            if (nodeTable.getColumn(CytoscapeFieldNames.NODE_EXPRESSION_MAPPINGS_PROPERTY_NAME)
                    == null) {
                nodeTable.createColumn(CytoscapeFieldNames.NODE_EXPRESSION_MAPPINGS_PROPERTY_NAME, Integer.class, false);
            }
            if (nodeTable.getColumn(CytoscapeFieldNames.NODE_IS_NEGATIVE_PROPERTY_NAME)
                    == null) {
                nodeTable.createColumn(CytoscapeFieldNames.NODE_IS_NEGATIVE_PROPERTY_NAME, Boolean.class, false);
            }
            if (nodeTable.getColumn(CytoscapeFieldNames.NODE_IS_POSITIVE_PROPERTY_NAME)
                    == null) {
                nodeTable.createColumn(CytoscapeFieldNames.NODE_IS_POSITIVE_PROPERTY_NAME, Boolean.class, false);
            }
            
                
            
        }

	public static boolean isNumber(String input) {
		return input.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+");
	}

	public HashMap<String, Double> getAvgExpressedCasesMap() {
		return avgExpressedCasesMap;
	}

	public HashMap<String, Double> getAvgExpressedGenesMap() {
		return avgExpressedGenesMap;
	}

	public HashMap<String, Integer> getTotalExpressedMap() {
		return totalExpressedMap;
	}

	public HashMap<String, Set<String>> getBackNodesByExpMap() {
		return backNodesByExpMap;
	}

	public HashMap<String, Integer> getNumGenesMap() {
		return numGenesMap;
	}

	/**
	 * 
	 * @return The number of node IDs which are both present in the network and
	 *         a data set file.
	 */
	public HashMap<String, Integer> getSizeIntersectionDataSetsNetwork() {
		return sizeIntersectionDataSetsNetwork;
	}
}
