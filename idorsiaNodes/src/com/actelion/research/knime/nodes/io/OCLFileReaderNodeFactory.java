package com.actelion.research.knime.nodes.io;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "OCLFileReader" Node.
 * 
 *
 * @author Actelion Pharmaceuticals Ltd.
 */
public class OCLFileReaderNodeFactory 
        extends NodeFactory<OCLFileReaderNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public OCLFileReaderNodeModel createNodeModel() {
        return new OCLFileReaderNodeModel();
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
    public NodeView<OCLFileReaderNodeModel> createNodeView(final int viewIndex, final OCLFileReaderNodeModel nodeModel) {
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
        return new OCLFileReaderNodeDialog();
    }

}

