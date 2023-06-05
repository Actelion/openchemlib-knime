package com.actelion.research.knime.nodes.manipulation;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "OCLLargestFragment" Node.
 * 
 *
 * @author Actelion Pharmaceuticals Ltd.
 */
public class OCLLargestFragmentNodeFactory
        extends NodeFactory<OCLLargestFragmentNodeModel> {


    /**
     * {@inheritDoc}
     */
    @Override
    public OCLLargestFragmentNodeModel createNodeModel() {
        return new OCLLargestFragmentNodeModel();
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
    public NodeView<OCLLargestFragmentNodeModel> createNodeView(final int viewIndex, final OCLLargestFragmentNodeModel nodeModel) {
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
        return new OCLLargestFragmentNodeDialog();
    }

}

