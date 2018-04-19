# KeyPathwayMiner- Cytoscape 3 App
## Detects highly-connected sub-networks where most genes show similar expression behavior

We introduce the latest version of the Key-PathwayMiner software framework. Given a biological network and a set of case- control studies, KeyPathwayMiner efficiently extracts all maximal connected sub-networks. These sub-networks contain the genes that are mainly dysregulated, e.g., differentially expressed, in most cases studied. The exact quantities for “mainly” and “most” are modeled with two easy-to-interpret parameters (K, L) that allows the user to control the number of outliers (not dysregulated genes/cases) in the solutions. We developed two slightly varying models (INES and GLONE) that fall into the class of NP-Hard optimization problems. To tackle the combinatorial explosion of the search space, we designed a set of exact and heuristic algorithms.

New features in version 5.0: 
- Robustness analysis: Test how results vary under different network pertubations and parameter settings.
- Validation analysis: Upload a gold standard set and test how results vary with different parameter settings.

On the project website http://keypathwayminer.compbio.sdu.dk/ you can find additional documentation. We also provide a web frontend and RESTful web service that allows you to run KeyPathwayMiner directly in the browser or to embed it in other applications.

The Cytoscape 3 app can be found here: http://apps.cytoscape.org/apps/keypathwayminer
