/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mpg.mpiinf.csb.kpmcytoplugin.util;
 

import de.mpg.mpiinf.csb.kpmcytoplugin.CyGlobals;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.vizmap.VisualStyle;

import dk.sdu.kpm.graph.GeneNode;
import dk.sdu.kpm.graph.Result;

import java.text.DecimalFormat;
import java.util.*;

/**
 *
 * @author nalcaraz
 */
public class KPMUtilities {
    public static boolean allSameForVaryingL(){
        for (String lid: CyGlobals.KPM.VARYING_L_ID) {

            if(!CyGlobals.KPM.MIN_L.containsKey(lid) || !CyGlobals.KPM.INC_L.containsKey(lid) || !CyGlobals.KPM.MAX_L.containsKey(lid)){
                continue;
            }

            int val = CyGlobals.KPM.MIN_L.get(lid);

            if(val != CyGlobals.KPM.INC_L.get(lid)){
                return false;
            }

            if(val != CyGlobals.KPM.MAX_L.get(lid)){
                return false;
            }
        }

        return true;
    }

    public static DecimalFormat getFormatedInt(int maxValue) {
        String ret = "";
        int zeros = (int) Math.floor(Math.log10(maxValue)) + 1;
        for (int i = 0; i < zeros; i++) {
            ret += "0";
        }
        return new DecimalFormat(ret);
    }

	/**
	 * Removes duplicate solutions from the given list of resulting key
	 * pathways.
	 *
	 * @param results
	 *            The key pathways where duplicate solutions should be removed
	 *            from.
	 * @return The input list with duplicate results removed.
	 */
	public static List<Result> removeDoubleSolutions(List<Result> results) {
		List<Result> toReturn = new ArrayList<Result>();
		int i = 0;

		while (i < results.size()) {
			Result r = results.get(i);
			if (!toReturn.contains(r)) {
				toReturn.add(r);
			}
			i++;
		}
		return toReturn;
	}

        public static void selectAllNodes(CyNetwork net) {
            for (CyNode node: net.getNodeList()) {
                net.getRow(node).set(CyNetwork.SELECTED, true);
            }
        }

        public static void unselectAllNodes(CyNetwork net) {
            List<CyNode> nodes = CyTableUtil.getNodesInState(net,"selected",true);
            for (CyNode node: nodes) {
                net.getRow(node).set(CyNetwork.SELECTED, false);
            }
        }

        public static void selectNodes(CyNetwork net, Collection<CyNode> nodes) {
            for (CyNode node: nodes) {
                net.getRow(node).set(CyNetwork.SELECTED, true);
            }
        }

        public static void unselectNodes(CyNetwork net, Collection<CyNode> nodes) {
            for (CyNode node: nodes) {
                net.getRow(node).set(CyNetwork.SELECTED, false);
            }
        }


        public static String getCyNodeName(CyNetwork net, CyNode node) {
            return net.getRow(node).get(CyNetwork.NAME, String.class);
        }

        public static String getCyEdgeName(CyNetwork net, CyEdge edge) {
            return net.getRow(edge).get(CyNetwork.NAME, String.class);
        }

        public static List<CyNode> getCyNodes(CyNetwork net, Collection<String> nodes) {
            List<CyNode> retList = new ArrayList<CyNode>(nodes.size());
            for (CyNode node: net.getNodeList()){
                String name = net.getRow(node).get(CyNetwork.NAME, String.class);
                if (nodes.contains(name)) {
                    retList.add(node);
                }
            }
            return retList;
        }

        public static List<CyNode> getCyNodesByGeneNodes(CyNetwork net,
                List<GeneNode> geneNodes) {
            List<String> nodes = new ArrayList<String>(geneNodes.size());
            for (GeneNode node: geneNodes) {
                nodes.add(node.getNodeId());
            }
            List<CyNode> retList = new ArrayList<CyNode>(nodes.size());
            for (CyNode node: net.getNodeList()){
                String name = net.getRow(node).get(CyNetwork.NAME, String.class);
                if (nodes.contains(name)) {
                    retList.add(node);
                }
            }
            return retList;
        }

        public static boolean containsVisualStyle(VisualStyle vs,
                Set<VisualStyle> visualStyles) {
            for (VisualStyle vsIn: visualStyles) {
                if (vs.getTitle().equals(vsIn.getTitle())) {
                    return true;
                }
            }
            return false;
        }
        
}
