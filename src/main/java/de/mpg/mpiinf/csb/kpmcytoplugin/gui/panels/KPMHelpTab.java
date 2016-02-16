package de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels;

import de.mpg.mpiinf.csb.kpmcytoplugin.gui.actions.AddKPMMainPanelAction;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;



/**
 * Contains a help page.
 * 
 * @author ajunge
 * 
 */
public class KPMHelpTab extends KPMTab {

	public KPMHelpTab(KPMTabbedPane kpmtp) {
		super(kpmtp, de.mpg.mpiinf.csb.kpmcytoplugin.util.CytoscapePanelNames.HELPNAME);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JEditorPane editorPane = null;
		try {
			editorPane = new JEditorPane(getClass().getResource(
					"/help.html"));
		} catch (IOException ex) {
			Logger.getLogger(AddKPMMainPanelAction.class.getName()).log(
					Level.SEVERE, null, ex);
            return;
		}

		editorPane.setEditable(false);
		editorPane.addHyperlinkListener(new HyperlinkListener() {

			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				javax.swing.event.HyperlinkEvent.EventType type = e
						.getEventType();
				if (type == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {

					String url = e.getURL().toExternalForm();

					if (url != null) {
						if (Desktop.isDesktopSupported()) {
							try {
								URI newUri = new URI(url);
								Desktop.getDesktop().browse(newUri);
							} catch (URISyntaxException ex) {
								Logger.getLogger(
										AddKPMMainPanelAction.class.getName())
										.log(Level.SEVERE, null, ex);
							} catch (IOException ioe) {
								Logger.getLogger(
										AddKPMMainPanelAction.class.getName())
										.log(Level.SEVERE, null, ioe);
							}

						}
					}

				}
			}
		});
		add(editorPane);
	}

    @Override
    public boolean containsError() {
        return false;
    }



}
