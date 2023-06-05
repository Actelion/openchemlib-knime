package com.actelion.research.knime.nodes.calculation;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class OCLCalculateSimilarityListNodeFactory extends NodeFactory<OCLCalculateSimilarityListNodeModel> {
    @Override
    public OCLCalculateSimilarityListNodeModel createNodeModel() {
        return new OCLCalculateSimilarityListNodeModel();
    }

    @Override
    public NodeView<OCLCalculateSimilarityListNodeModel> createNodeView(int i, OCLCalculateSimilarityListNodeModel oclSubstructureFilterNodeModel) {
        return null;
    }

    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new OCLCalculateSimilarityListNodeDialog();
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
