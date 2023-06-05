package com.actelion.research.knime.nodes.calculation;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class OCLCalculateDescriptorNodeFactory extends NodeFactory<OCLCalculateDescriptorNodeModel> {
    @Override
    public OCLCalculateDescriptorNodeModel createNodeModel() {
        return new OCLCalculateDescriptorNodeModel();
    }

    @Override
    public NodeView<OCLCalculateDescriptorNodeModel> createNodeView(int i, OCLCalculateDescriptorNodeModel oclCalculateDescriptorNodeModel) {
        return null;
    }

    @Override
    protected int getNrNodeViews() {
        return 0;
    }

    @Override
    protected boolean hasDialog() {
        return true;
    }

    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new OCLCalculateDescriptorNodeDialog();
    }
}
