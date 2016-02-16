package de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels;

import javax.swing.JPanel;

/**
 * All JPanel objects which serve as a tab in KPM extend this class. Its method
 * {@link KPMTab#containsError()} is used to check if any tab contains an error
 * which makes the search for key pathways impossible. Hence, the 'Search
 * Pathways' button is deactivate whenever an error is present in one of the
 * tabs.
 * 
 * Furthermore implements methods which add or remove an error icon to the tab
 * name depending on the presence of errors in this tab.
 * 
 * @author ajunge
 * 
 */
abstract class KPMTab extends JPanel {

	/**
	 * The {@link KPMTabbedPane} this tab is part of. This reference is needed
	 * to update the error icon shown next to the name of the tab.
	 */
	private final KPMTabbedPane kpmtp;

	/**
	 * The name of this tab in {@link KPMTab#kpmtp}.
	 */
	private final String tabName;

	/**
	 * 
	 * @param kpmtp
	 *            - The {@link KPMTabbedPane} which contains this {@link KPMTab}
	 *            .
	 * @param tabName
	 *            - The name of the tab in the {@link KPMTabbedPane}
	 *            <code>kpmtp</code>.
	 */
    KPMTab(KPMTabbedPane kpmtp, String tabName) {
		this.kpmtp = kpmtp;
		this.tabName = tabName;
	}

	/**
	 *
	 * @return <code>true</code> if this tab of KPM contains and error and
	 *         <code>false</code> otherwise.
	 */
	public abstract boolean containsError();

	/**
	 * Removes the error symbol from the KPM tab which contains this
	 */
    void removeTabErrorIcon() {
		kpmtp.markAsCorrect(tabName);
	}

	/**
	 * Marks the KPM tab which contains this with an error
	 * symbol. The given string is used as a tool-tip of the tab.
	 * 
	 * @param msg
	 *            The error message shown as a tool-tip of the tab, if
	 *            <code>null</code>, no tool-tip is shown.
	 */
    void markTabWithErrorIcon(String msg) {
		kpmtp.markWithError(tabName, msg);
	}

	/**
	 * 
	 * @return The KPMTabbedPane which contains this panel as a tab.
	 */
	public KPMTabbedPane getKPMTabbedPane() {
		return kpmtp;
	}
}
