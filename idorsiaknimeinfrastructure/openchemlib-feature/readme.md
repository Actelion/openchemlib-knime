# KNIME OpenChemLib Nodes (Feature)

To distribube the KNIME nodes, this project is needed to expose the plugin (located in ../actelionNodes) as a feature.

# Usage
When doing a release, simply update the feature.xml file with relevant information.  Specifically be sure to bump the version number
to match the actelionNodes plugin version number. Next import the feature into the actelionRepo repository. Finally, sync the new plugins to the webserver where the update site is hosted (ew1al-knimesvp1 at the time of writing). 

