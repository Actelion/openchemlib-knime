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
public class MolToOCLNodeFactory
        extends NodeFactory<MolToOCLNodeModel> {


    /**
     * {@inheritDoc}
     */
    @Override
    public MolToOCLNodeModel createNodeModel() {
        return new MolToOCLNodeModel();
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
    public NodeView<MolToOCLNodeModel> createNodeView(final int viewIndex, final MolToOCLNodeModel nodeModel) {
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
        return new MolToOCLNodeDialog();
    }

}

