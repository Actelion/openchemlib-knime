package com.actelion.research.knime.nodes.manipulation;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class OCLScaffoldAnalysisNodeFactory extends NodeFactory<OCLScaffoldAnalysisNodeModel> {
    @Override
    public OCLScaffoldAnalysisNodeModel createNodeModel() {
        return new OCLScaffoldAnalysisNodeModel();
    }

    @Override
    public NodeView<OCLScaffoldAnalysisNodeModel> createNodeView(int i, OCLScaffoldAnalysisNodeModel oclScaffoldAnalysisNodeModel) {
        return null;
    }

    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new OCLScaffoldAnalysisNodeDialog();
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
