package com.actelion.research.knime.nodes.manipulation;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class OCLClusterMoleculesNodeFactory extends NodeFactory<OCLClusterMoleculesNodeModel> {
    @Override
    public OCLClusterMoleculesNodeModel createNodeModel() {
        return new OCLClusterMoleculesNodeModel();
    }

    @Override
    public NodeView<OCLClusterMoleculesNodeModel> createNodeView(int i, OCLClusterMoleculesNodeModel oclClusterMoleculesNodeModel) {
        return null;
    }

    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new OCLClusterMoleculesNodeDialog();
    }

    //~--- get methods --------------------------------------------------------

    @Override
    protected int getNrNodeViews() {
        return 0;
    }

    @Override
    protected boolean hasDialog() {
        return true;
    }
}
