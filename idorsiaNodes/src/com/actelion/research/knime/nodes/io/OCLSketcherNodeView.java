package com.actelion.research.knime.nodes.io;

import org.knime.core.node.NodeView;


/**
 *
 * @author Idorsia Pharmaceuticals Ltd.
 */
public class OCLSketcherNodeView extends NodeView<OCLSketcherNodeModel> {

    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link OCLSketcherNodeModel})
     */
    protected OCLSketcherNodeView(final OCLSketcherNodeModel nodeModel) {
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
        OCLSketcherNodeModel nodeModel = 
            (OCLSketcherNodeModel)getNodeModel();
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

