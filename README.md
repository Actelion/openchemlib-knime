# Idorsia OpenChemLib KNIME Nodes

## Installation
Supported KNIME Analytics Platform versions: 4.6.x , newer versions probably as well

1. In your KNIME Analytics Platform, select the menu point "Help" -> "Install new software.."
2. add the following software repository: https://actelion.github.io/openchemlib-knime/update_site
3. select the OpenChemLib Knime nodes for installation.

## Getting started
Please check the file ./ExampleWorkflows/oclnodes-validation.knar
It contains a couple of workflows that show the basic usage for some of the nodes nodes:
(there is currently a bug in the reader node, such that you have to pick the correct input file manually. This will be fixed soon..)

- ValidationWorkflow_Descriptors_01 : illustrates .dwar file loading, descriptor calculation and similarity calculation
- ValidationWorkflow_Descriptors_02 : illustrates .dwar file loading, and multiple similarity calculation
- ValidationWorkflow_IO_01          : illustrates .dwar file loading and saving
- ValidationWorkflow_Sketcher_01    : illustrates usage of the the OCL sketcher and file saving
- ValidationWorkflow_3D_01          : illustrates conformer generation, forcefield minimization, and PheSA-based 3D similarity computation
- ValidationWorkflow_3D_02          : illustrates ligand pose import via SDF file and PheSA-based 3D similarity computation


- ValidationWorkflow_DiverseSelection_01 : illustrates ultra-fast diverse selection algoritm (works only for binary fingerprints)
- ValidationWorkflow_ClusterMolecules_01 : illustrates clustering based on descriptor-based similarity
- ValidationWorkflow_ScaffoldAnalysis    : illustrates computation of scaffolds and scaffold statistics


## Developers
Thomas Liphardt, Tobias Fink, Thomas Sander
