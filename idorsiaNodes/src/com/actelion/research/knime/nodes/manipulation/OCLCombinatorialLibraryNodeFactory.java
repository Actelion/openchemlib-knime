package com.actelion.research.knime.nodes.manipulation;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class OCLCombinatorialLibraryNodeFactory extends NodeFactory<OCLCombinatorialLibraryNodeModel> {
    @Override
    public OCLCombinatorialLibraryNodeModel createNodeModel() {
        return new OCLCombinatorialLibraryNodeModel();
    }

    @Override
    public NodeView<OCLCombinatorialLibraryNodeModel> createNodeView(int i, OCLCombinatorialLibraryNodeModel oclCombinatorialLibraryNodeModel) {
        return null;
    }

    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new OCLCombinatorialLibraryNodeDialog();
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
