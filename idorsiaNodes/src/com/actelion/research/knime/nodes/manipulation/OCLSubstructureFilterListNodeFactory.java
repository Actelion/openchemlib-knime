package com.actelion.research.knime.nodes.manipulation;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class OCLSubstructureFilterListNodeFactory extends NodeFactory<OCLSubstructureFilterListNodeModel> {
    @Override
    public OCLSubstructureFilterListNodeModel createNodeModel() {
        return new OCLSubstructureFilterListNodeModel();
    }

    @Override
    public NodeView<OCLSubstructureFilterListNodeModel> createNodeView(int i, OCLSubstructureFilterListNodeModel oclSubstructureFilterNodeModel) {
        return null;
    }

    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new OCLSubstructureFilterListNodeDialog();
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
