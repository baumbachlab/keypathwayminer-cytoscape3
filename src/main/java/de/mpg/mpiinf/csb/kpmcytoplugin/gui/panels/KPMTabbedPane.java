package de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels;
 
import de.mpg.mpiinf.csb.kpmcytoplugin.CyGlobals;
import de.mpg.mpiinf.csb.kpmcytoplugin.interfaces.ISearchEnabledListener;
import de.mpg.mpiinf.csb.kpmcytoplugin.util.CytoscapePanelNames;

import javax.swing.*;
import java.awt.*;

/**
 * Contains all KPM tabs for data input, linking data sets, setting algorithm
 * parameters etc.. Overrides @link {@link JTabbedPane#setSelectedIndex(int)} to
 * update the next KPM tab selected by the user using information given in other
 * tabs.
 * 
 * For instance, makes sure that appropriate actions such as loading the data
 * sets or parsing the graph connecting the data sets are taken after the tab is
 * changed and that next KPM tab to be viewed is prepared by updating the
 * information given in other tabs.
 * 
 * @author ajunge
 * 
 */
public class KPMTabbedPane extends JTabbedPane {
	private static final long serialVersionUID = -2403044174894825583L;

	/**
	 * This icon is displayed next to the names of tabs where an error was
	 * found.
	 */
	private final ImageIcon errorIcon;

	/**
	 *
     */
	public KPMTabbedPane() {
		errorIcon = new ImageIcon(this.getClass().getResource(
				"/minus.png"), "Error");
	}

	/**
	 * Overrides the regular {@link JTabbedPane#setSelectedIndex(int)} method to
	 * prepare the next tab to be viewed using user input from other tabs. <br>
	 * <br>
	 * {@inheritDoc}
	 */
	public void setSelectedIndex(int index) {
		if (index == getSelectedIndex()) {
			return;
		} else if (getSelectedIndex() == -1) {
			// Allow the initial tab change.
			super.setSelectedIndex(index);
			return;
		}

		String nextTab = getTitleAt(index);
		super.setSelectedIndex(index);
		// Prepare the next visible tab.
		if (nextTab.equals(CytoscapePanelNames.INPUTNAME)) {

		} else if (nextTab.equals(CytoscapePanelNames.LINKSNAME)) {
			// Deactivate the drop-down menu which allows to choose a global
			// logical operator
			// if just a single data set is given.
			KPMDataTab dp = getDataPanel();
			KPMLinksTab lp = getLinksPanel();
			if (dp.getNoFilesLoaded() > 1) {
				lp.setOperatorBoxActived(true);
			} else {
				lp.setOperatorBoxActived(false);
			}

		} else if (nextTab.equals(CytoscapePanelNames.POSNEGNAME)) {

		} else if (nextTab.equals(CytoscapePanelNames.RUNNAME)) {
			// Activate the 'Search Pathways' button if and only if there where
			// no errors
			// found in the KPM tabs.
			boolean errorFound = getDataPanel().containsError()
					|| getLinksPanel().containsError()
					|| getPosNegPanel().containsError();
			if (errorFound) {
				for(ISearchEnabledListener listener : CyGlobals.SearchEnabledListeners){
					listener.setDisabled();
				}
			} else {
				for(ISearchEnabledListener listener : CyGlobals.SearchEnabledListeners){
					listener.setEnabled();
				}
			}
		} else if (nextTab.equals(CytoscapePanelNames.RESULTSNAME)) {

		}
	}

	/**
	 * Marks the tab with the given name with an error symbol.
	 * 
	 * @param tabName
	 *            - the name of the tab the error symbol is added to
	 * @param msg
	 *            The error message shown as a tool-tip of the tab, if
	 *            <code>null</code>, no tool-tip is shown.
	 */
	public void markWithError(String tabName, String msg) {
		int idx = indexOfTab(tabName);
		setToolTipTextAt(idx, msg);
		setIconAt(idx, errorIcon);
	}

	/**
	 * Marks the KPM tab which contains this {@link DataPanel} with an error
	 * symbol. The given string is used as a tool-tip of the error symbol.
	 * 
	 * @param msg
	 *            The error message shown as a tool-tip of the error symbol, if
	 *            <code>null</code>, no tool-tip is shown.
	 */

	/**
	 * Removes the error symbol from the tab with the given name.
	 * 
	 * @param tabName
	 *            - the name of the tab the error symbol is added to
	 */
	public void markAsCorrect(String tabName) {
		int idx = indexOfTab(tabName);
		setToolTipTextAt(idx, null);
		setIconAt(idx, null);
	}

	/**
	 * 
	 * @return The panel allowing the user to specify K, L, the algorithm to be
	 *         used and parameters of the algorithms.
	 */
	public KPMParameterTab getParameterPanel() {
		JScrollPane sp = (JScrollPane) getComponentAt(indexOfTab(CytoscapePanelNames.RUNNAME));
		return (KPMParameterTab) sp.getViewport().getView();
	}

	/**
	 * 
	 * @return The panel in which positive and negative list genes are
	 *         specified.
	 */
	public KPMPosNegTab getPosNegPanel() {
		JScrollPane sp = (JScrollPane) getComponentAt(indexOfTab(CytoscapePanelNames.POSNEGNAME));
		return (KPMPosNegTab) sp.getViewport().getView();
	}

	/**
	 * @return The panel where data sets are linked by logical operators.
	 */
	public KPMLinksTab getLinksPanel() {
		JScrollPane sp = (JScrollPane) getComponentAt(indexOfTab(CytoscapePanelNames.LINKSNAME));
		return (KPMLinksTab) sp.getViewport().getView();
	}

	/**
	 * 
	 * @return The panel where the data sets are specified by the user.
	 */
	public KPMDataTab getDataPanel() {
		JScrollPane sp = (JScrollPane) getComponentAt(indexOfTab(CytoscapePanelNames.INPUTNAME));
		return (KPMDataTab) sp.getViewport().getView();
	}

	@Override
	public void addTab(String title, Component component) {
		if (!(component instanceof JScrollPane)) {
			JScrollPane scroll = new JScrollPane(component);
			scroll.setBorder(null);
			super.addTab(title, scroll);
		} else {
			super.addTab(title, component);
		}
	}

}
