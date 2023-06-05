package com.actelion.research.knime.nodes.conversion;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "OCLLargestFragment" Node.
 * 
 *
 * @author Actelion Pharmaceuticals Ltd.
 */
public class OCLToMolNodeFactory
        extends NodeFactory<OCLToMolNodeModel> {


    /**
     * {@inheritDoc}
     */
    @Override
    public OCLToMolNodeModel createNodeModel() {
        return new OCLToMolNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<OCLToMolNodeModel> createNodeView(final int viewIndex, final OCLToMolNodeModel nodeModel) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new OCLToMolNodeDialog();
    }

}

