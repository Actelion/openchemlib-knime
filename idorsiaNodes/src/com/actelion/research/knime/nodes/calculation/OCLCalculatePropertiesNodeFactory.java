package com.actelion.research.knime.nodes.calculation;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "OCLLargestFragment" Node.
 * 
 *
 * @author Actelion Pharmaceuticals Ltd.
 */
public class OCLCalculatePropertiesNodeFactory
        extends NodeFactory<OCLCalculatePropertiesNodeModel> {


    /**
     * {@inheritDoc}
     */
    @Override
    public OCLCalculatePropertiesNodeModel createNodeModel() {
        return new OCLCalculatePropertiesNodeModel();
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
    public NodeView<OCLCalculatePropertiesNodeModel> createNodeView(final int viewIndex, final OCLCalculatePropertiesNodeModel nodeModel) {
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
        return new OCLCalculatePropertiesNodeDialog();
    }

}

