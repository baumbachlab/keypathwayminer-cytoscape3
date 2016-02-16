package de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * This panel is shown as a tab in the KPM UI. It allows the user to specify
 * files which are used as input data sets for KPM. Furthermore, abbreviations
 * for the data sets can be specified.
 * 
 * @author ajunge
 * 
 */
public class KPMDataTab extends KPMTab {

	/**
	 *
	 */
	private static final long serialVersionUID = 4330798916867309479L;

	/**
	 * Contains all currently existing {@code FileLoadingPanel} objects.
	 */
	private final List<KPMFileLoadingPanel> fileLoadingPanels = new LinkedList<KPMFileLoadingPanel>();

	/**
	 * The number of {@code FileLoadingPanel} objects which is initially shown
	 * in this tab.
	 */
	private static final int DEFAULTNOFILELOADINGPANELS = 1;

	/**
	 * Clicking the button inside this panel allows the user to specify an
	 * additional data set.
	 */
	private JPanel anotherFileButtonPanel;

	/**
	 * Contains all the panels which allow the user to specify a data set.
	 */
	private final JPanel fileLoadingContainer;

	/**
	 * Creates a new panel where data set files can be specified by the user.
	 *  @param kpmtp
	 *            The {@link de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels.KPMTabbedPane} object this panel is part of.
	 *
     */
	public KPMDataTab(KPMTabbedPane kpmtp) {
		super(kpmtp, de.mpg.mpiinf.csb.kpmcytoplugin.util.CytoscapePanelNames.INPUTNAME);
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.anchor = GridBagConstraints.FIRST_LINE_START;

		c.gridx = 0;
		c.gridy = 0;
		c.weighty = 0;
		JLabel infoLabel = new JLabel(
				"Please specify the experimental data set(s):");
		JPanel infoPanel = new JPanel();
		infoPanel.add(infoLabel);
		infoPanel.setBorder(new EmptyBorder(5, 0, 5, 0));
		add(infoPanel, c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridheight = 1;
		c.weighty = 1;
		fileLoadingContainer = new JPanel();
		fileLoadingContainer.setLayout(new BoxLayout(fileLoadingContainer,
				BoxLayout.Y_AXIS));
		add(fileLoadingContainer, c);

		for (int i = 0; i < DEFAULTNOFILELOADINGPANELS; i++) {
			addFileLoadingPanel();
		}
		addAnotherFileButton();
	}

	/**
	 * Adds a new FileLoadingPanel to the UI.
	 */
	private void addFileLoadingPanel() {
		KPMFileLoadingPanel flp = new KPMFileLoadingPanel(this);
		fileLoadingPanels.add(flp);
		fileLoadingContainer.add(flp);
	}

	/**
	 * Removes the given {@code FileLoadingPanel} from the UI.
	 * 
	 * @param flp
	 */
	public void removeFileLoadingPanel(KPMFileLoadingPanel flp) {
		fileLoadingPanels.remove(flp);
		fileLoadingContainer.remove(flp);
		updateUI();
	}

	/**
	 * Creates and adds a button to the fileChooserPanel. Clicking this button
	 * makes a new FileLoadingPanel appear.
	 */
	private void addAnotherFileButton() {
		anotherFileButtonPanel = new JPanel();
		anotherFileButtonPanel.setLayout(new BorderLayout());
		JButton anotherFileButton = new JButton();
		anotherFileButton.setBorder(new EmptyBorder(8, 8, 8, 8));
		ImageIcon addIcon = new ImageIcon(this.getClass().getResource(
				"/plus.png"), "Add");
		anotherFileButton.setIcon(addIcon);
		anotherFileButton.setToolTipText("Adds another experimental data set");
		anotherFileButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				addFileLoadingPanel();
				moveAnotherFileButtonToBottom();
				updateUI();
			}
		});
		JLabel anotherFileLabel = new JLabel("Add another experiment");
		JPanel anotherFileButtonSubPanel = new JPanel();
		anotherFileButtonSubPanel.add(anotherFileButton);
		anotherFileButtonPanel
				.add(anotherFileButtonSubPanel, BorderLayout.WEST);
		anotherFileButtonPanel.add(anotherFileLabel, BorderLayout.CENTER);
		fileLoadingContainer.add(anotherFileButtonPanel);
	}

	/**
	 * 
	 * @return The number of data files which were successfully loaded.
	 */
	public int getNoFilesLoaded() {
		int noLoadedFiles = 0;
		for (KPMFileLoadingPanel flp : fileLoadingPanels) {
			if (flp.getDysregMap() != null) {
				noLoadedFiles++;
			}
		}
		return noLoadedFiles;
	}

	@Override
	public boolean containsError() {
		if (!dataSetNamesUnique()) {
			return true;
		}
		// Report an error when no data file has been specified yet.
		for (KPMFileLoadingPanel flp : fileLoadingPanels) {
			if (flp.getDysregMap() != null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if all data sets have unique identifiers. If this is not the case,
	 * this tab is marked as containing an error. The names of the data sets
	 * must be unique to evaluate the logical formula correctly.
	 * 
	 * Called whenever a data set name is changed.
	 */
	public boolean dataSetNamesUnique() {
		Set<String> names = new HashSet<String>();
		for (KPMFileLoadingPanel flp : fileLoadingPanels) {
			String name = flp.getAbbreviation();
			if (name != null) {
				if (names.contains(name)) {
					markTabWithErrorIcon("The names of the data sets must be unique");
					return false;
				} else {
					names.add(name);
				}
			}
		}
		removeTabErrorIcon();
		return true;
	}

	/**
	 * Removes the button for adding additional data sets and adds it again.
	 * Hence, the button moves to the bottom of the KPM tab.
	 */
	private void moveAnotherFileButtonToBottom() {
		fileLoadingContainer.remove(anotherFileButtonPanel);
		fileLoadingContainer.add(anotherFileButtonPanel);
	}

}
