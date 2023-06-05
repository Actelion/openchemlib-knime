package com.actelion.research.knime.nodes.calculation;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class OCLCalculateSimilarityNodeFactory extends NodeFactory<OCLCalculateSimilarityNodeModel> {
    @Override
    public OCLCalculateSimilarityNodeModel createNodeModel() {
        return new OCLCalculateSimilarityNodeModel();
    }

    @Override
    public NodeView<OCLCalculateSimilarityNodeModel> createNodeView(int i, OCLCalculateSimilarityNodeModel oclCalculateSimilarityNodeModel) {
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
        return new OCLCalculateSimilarityNodeDialog();
    }
}
