/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mpg.mpiinf.csb.kpmcytoplugin.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.VirtualColumnInfo;
import org.cytoscape.work.AbstractTask;

/**
 *
 * @author nalcaraz
 */
abstract class AbstractKPMCreatePathwayTask extends AbstractTask {



    Collection<CyEdge> getEdges(CyNetwork netx, List<CyNode> nodes) {
        final Set<CyEdge> edges = new HashSet<CyEdge>();

        for (int i = 0; i < nodes.size(); i++) {
            CyNode n1 = nodes.get(i);
            for (int j = i; j < nodes.size(); j++) {
                CyNode n2 = nodes.get(j);
                edges.addAll(netx.getConnectingEdgeList(n1, n2, CyEdge.Type.ANY));
            }
        }
        return edges;
    }

    void addColumns(CyTable parentTable, CyTable subTable) {
        List<CyColumn> colsToAdd = new ArrayList<CyColumn>();

        for (CyColumn col : parentTable.getColumns()) {
            if (subTable.getColumn(col.getName()) == null) {
                colsToAdd.add(col);
            }
        }

        for (CyColumn col : colsToAdd) {
            VirtualColumnInfo colInfo = col.getVirtualColumnInfo();
            if (colInfo.isVirtual()) {
                addVirtualColumn(col, subTable);
            } else {
                copyColumn(col, subTable);
            }
        }
    }

    void addVirtualColumn(CyColumn col, CyTable subTable) {
        VirtualColumnInfo colInfo = col.getVirtualColumnInfo();
        CyColumn checkCol = subTable.getColumn(col.getName());
        if (checkCol == null) {
            subTable.addVirtualColumn(col.getName(), colInfo.getSourceColumn(), colInfo.getSourceTable(), colInfo.getTargetJoinKey(), col.isImmutable());
        } else if (!checkCol.getVirtualColumnInfo().isVirtual()
                || !checkCol.getVirtualColumnInfo().getSourceTable().equals(colInfo.getSourceTable())
                || !checkCol.getVirtualColumnInfo().getSourceColumn().equals(colInfo.getSourceColumn())) {
            subTable.addVirtualColumn(col.getName(), colInfo.getSourceColumn(), colInfo.getSourceTable(), colInfo.getTargetJoinKey(), col.isImmutable());
        }
    }

    void copyColumn(CyColumn col, CyTable subTable) {
        if (List.class.isAssignableFrom(col.getType())) {
            subTable.createListColumn(col.getName(), col.getListElementType(), false);
        } else {
            subTable.createColumn(col.getName(), col.getType(), false);
        }
    }

    void cloneRow(final CyRow from, final CyRow to) {
        for (final CyColumn column : from.getTable().getColumns()) {
            if (!column.getVirtualColumnInfo().isVirtual()) {
                to.set(column.getName(), from.getRaw(column.getName()));
            }
        }
    }
}
