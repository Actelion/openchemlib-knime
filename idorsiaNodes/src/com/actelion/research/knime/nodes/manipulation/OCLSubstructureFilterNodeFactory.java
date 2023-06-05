package com.actelion.research.knime.nodes.manipulation;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class OCLSubstructureFilterNodeFactory extends NodeFactory<OCLSubstructureFilterNodeModel> {
    @Override
    public OCLSubstructureFilterNodeModel createNodeModel() {
        return new OCLSubstructureFilterNodeModel();
    }

    @Override
    public NodeView<OCLSubstructureFilterNodeModel> createNodeView(int i, OCLSubstructureFilterNodeModel oclSubstructureFilterNodeModel) {
        return null;
    }

    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new OCLSubstructureFilterNodeDialog();
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
