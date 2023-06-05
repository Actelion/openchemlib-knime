package com.actelion.research.knime.nodes.manipulation;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class OCLDiverseSelectionNodeFactory extends NodeFactory<OCLDiverseSelectionNodeModel> {
    @Override
    public OCLDiverseSelectionNodeModel createNodeModel() {
        return new OCLDiverseSelectionNodeModel();
    }

    @Override
    public NodeView<OCLDiverseSelectionNodeModel> createNodeView(int i, OCLDiverseSelectionNodeModel oclDiverseSelectionNodeModel) {
        return null;
    }

    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new OCLDiverseSelectionNodeDialog();
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
