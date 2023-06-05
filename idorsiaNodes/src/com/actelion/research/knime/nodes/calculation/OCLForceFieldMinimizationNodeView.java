package com.actelion.research.knime.nodes.calculation;

import org.knime.core.node.NodeView;


/** 
 * Idorsia Pharmaceuticals Ltd.
 * January 2020 
 * 
 * @author Thomas Liphardt
 */

public class OCLForceFieldMinimizationNodeView extends NodeView<OCLForceFieldMinimizationNodeModel> {

    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link OCLForceFieldMinimizationNodeModel})
     */
    protected OCLForceFieldMinimizationNodeView(final OCLForceFieldMinimizationNodeModel nodeModel) {
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
        OCLForceFieldMinimizationNodeModel nodeModel = 
            (OCLForceFieldMinimizationNodeModel)getNodeModel();
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

