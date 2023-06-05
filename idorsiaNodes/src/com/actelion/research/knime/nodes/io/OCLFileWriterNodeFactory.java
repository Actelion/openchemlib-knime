package com.actelion.research.knime.nodes.io;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class OCLFileWriterNodeFactory extends NodeFactory<OCLFileWriterNodeModel> {
    @Override
    public OCLFileWriterNodeModel createNodeModel() {
        return new OCLFileWriterNodeModel();
    }

    @Override
    public NodeView<OCLFileWriterNodeModel> createNodeView(int i, OCLFileWriterNodeModel oclFileWriterNodeModel) {
        return null;
    }

    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new OCLFileWriterNodeDialog();
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
