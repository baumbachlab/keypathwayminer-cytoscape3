package de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Contains the KPM banner and is part of every KPM tab.
 * 
 * @author ajunge
 * 
 */
class KPMBannerPanel extends JPanel {

	public KPMBannerPanel() {
		setBackground(Color.WHITE);
		ImageIcon icon = new ImageIcon(
                        this.getClass().getResource("/banner.png"));
		JLabel label = new JLabel();
		label.setIcon(icon);
		add(label);
		setMaximumSize(new Dimension(Integer.MAX_VALUE, icon.getIconHeight()));
		setMinimumSize(new Dimension(Integer.MAX_VALUE, icon.getIconHeight()));
	}

}
