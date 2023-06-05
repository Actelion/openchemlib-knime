package com.actelion.research.knime.nodes.conversion;

import org.knime.core.node.NodeView;


/** 
 * Idorsia Pharmaceuticals Ltd.
 * January 2020 
 * 
 * @author Thomas Liphardt
 * 
 */

public class ConformerListToMolNodeView extends NodeView<ConformerListToMolNodeModel> {

    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link ConformerListToMolNodeModel})
     */
    protected ConformerListToMolNodeView(final ConformerListToMolNodeModel nodeModel) {
        super(nodeModel);

        // TODO instantiate the components of the view here.

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {

        // TODO retrieve the new model from your nodemodel and 
        // update the view.
        ConformerListToMolNodeModel nodeModel = 
            (ConformerListToMolNodeModel)getNodeModel();
        assert nodeModel != null;
        
        // be aware of a possibly not executed nodeModel! The data you retrieve
        // from your nodemodel could be null, emtpy, or invalid in any kind.
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
    
        // TODO things to do when closing the view
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {

        // TODO things to do when opening the view
    }

}

