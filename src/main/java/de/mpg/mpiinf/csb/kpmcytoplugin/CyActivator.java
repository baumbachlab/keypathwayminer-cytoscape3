/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mpg.mpiinf.csb.kpmcytoplugin;

import de.mpg.mpiinf.csb.kpmcytoplugin.event.ACOVersionSelectedListener;
import de.mpg.mpiinf.csb.kpmcytoplugin.kpmhandlers.GraphAttributesHandler;
import de.mpg.mpiinf.csb.kpmcytoplugin.event.KPMEventListener;
import de.mpg.mpiinf.csb.kpmcytoplugin.gui.actions.AddKPMMainPanelAction;
import de.mpg.mpiinf.csb.kpmcytoplugin.gui.panels.KPMMainPanel;
import de.mpg.mpiinf.csb.kpmcytoplugin.interfaces.IActivator;

import java.util.Properties;
import java.util.logging.Level;

import dk.sdu.kpm.logging.KpmLogger;
import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.create.NewNetworkSelectedNodesOnlyTaskFactory;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

/**
 *
 * @author nalcaraz
 */
public class CyActivator extends AbstractCyActivator implements IActivator {

	private BundleContext bundleContext;
	private CySwingApplication desktopService;

    public CyActivator() {
        super();
    }

    @Override
    public void start(BundleContext bc) throws Exception {
        //KpmLogger.log(Level.SEVERE, "Testing");
        FileUtil fileUtil = getService(bc, FileUtil.class);
        CySwingAppAdapter swingAppAdapter = getService(bc, CySwingAppAdapter.class);
        CyApplicationManager appManager = getService(bc, CyApplicationManager.class);
        CyNetworkManager networkManager = getService(bc, CyNetworkManager.class);
        CyEventHelper evenHelper = getService(bc, CyEventHelper.class);
        CySwingApplication cytoscapeDesktopService = getService(bc, CySwingApplication.class);
        CyNetworkFactory networkFactory = getService(bc, CyNetworkFactory.class);
        CyRootNetworkManager rootNetworkManager = getService(bc, CyRootNetworkManager.class);
        CyNetworkViewManager networkViewManager = getService(bc, CyNetworkViewManager.class);
        CyNetworkViewFactory networkViewFactory = getService(bc, CyNetworkViewFactory.class);
        CyLayoutAlgorithmManager layoutManager = getService(bc, CyLayoutAlgorithmManager.class);
        VisualMappingManager vmmServiceRef = getService(bc, VisualMappingManager.class);
        VisualStyleFactory visualStyleFactoryServiceRef = getService(bc, VisualStyleFactory.class);
        VisualMappingFunctionFactory vmfFactoryC = getService(bc, 
                VisualMappingFunctionFactory.class,"(mapping.type=continuous)");
        VisualMappingFunctionFactory vmfFactoryD = getService(bc, 
                VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
        VisualMappingFunctionFactory vmfFactoryP = getService(bc, 
                VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
        NewNetworkSelectedNodesOnlyTaskFactory nnSNFactory =
                getService(bc, NewNetworkSelectedNodesOnlyTaskFactory.class);
        TaskManager taskManager = getService(bc, TaskManager.class);       
        DialogTaskManager dialogTaskManager = getService(bc, DialogTaskManager.class);

        CyProvider.networkManager = networkManager;
        CyProvider.rootNetworkManager = rootNetworkManager;
        CyProvider.networkViewManager = networkViewManager;
        CyProvider.networkViewFactory = networkViewFactory;
        CyProvider.layoutManager = layoutManager;
        CyProvider.vmmServiceRef = vmmServiceRef;
        CyProvider.visualStyleFactoryServiceRef = visualStyleFactoryServiceRef;
        CyProvider.vmfFactoryC = vmfFactoryC;
        CyProvider.vmfFactoryD = vmfFactoryD;
        CyProvider.vmfFactoryP = vmfFactoryP;
        CyProvider.appManager = appManager;
        CyProvider.taskManager = taskManager;
        CyProvider.dialogTaskManager = dialogTaskManager;
        CyProvider.fileUtil = fileUtil;

        KPMMainPanel kpmmp = new KPMMainPanel();
        
        CyGlobals.ResultsPanelHandler = kpmmp;
        
        AddKPMMainPanelAction sample02Action = new AddKPMMainPanelAction(cytoscapeDesktopService, kpmmp);

        registerService(bc, kpmmp, CytoPanelComponent.class, new Properties());

        registerService(bc, sample02Action, CyAction.class, new Properties());

        KPMEventListener kpmel = new KPMEventListener(kpmmp.getKPMTabbedPane().getParameterPanel());

        registerService(bc, kpmel, ACOVersionSelectedListener.class, new Properties());
        registerService(bc, kpmel, NetworkAddedListener.class, new Properties());
        registerService(bc, kpmel, NetworkAboutToBeDestroyedListener.class, new Properties());
        System.out.println("\nLoaded KeyPathwayMiner");

        // Save information for the IActivator interface actions
    	this.bundleContext = bc;
    	this.desktopService = cytoscapeDesktopService;
    	CyGlobals.ACTIVATOR = this;
    	
    	// Making sure the handler is set.
        GraphAttributesHandler graphAttributesHandler = new GraphAttributesHandler();
    	CyGlobals.GraphAttributeHandler = graphAttributesHandler;
    }

	@Override
	public void registerAndFocusCytoPanel(CytoPanelComponent comp) {

		// Registering the panel.
		registerService(this.bundleContext, comp, CytoPanelComponent.class, new Properties());
		
		// Focusing the panel
		CytoPanel panel = this.desktopService.getCytoPanel(comp.getCytoPanelName());


		// If the state of the cytoPanel is HIDE, show it
        if (panel.getState() == CytoPanelState.HIDE) {
        	panel.setState(CytoPanelState.DOCK);
        }

        int index = panel.indexOfComponent(comp.getComponent());
        if (index == -1) {
            return;
        }

        panel.setSelectedIndex(index);

	}

	@Override
	public CytoPanel getCytoPanel(CytoPanelName panelName) {
		return this.desktopService.getCytoPanel(panelName);
	}

}
