package com.actelion.research.knime.ui;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.gui.generic.GenericDepictor;
import com.actelion.research.gui.generic.GenericRectangle;
import com.actelion.research.gui.swing.SwingDrawContext;
import com.actelion.research.knime.data.OCLMoleculeDataValue;
import com.actelion.research.knime.utils.ValueHelper;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.renderer.AbstractDataValueRendererFactory;
import org.knime.core.data.renderer.AbstractPainterDataValueRenderer;
import org.knime.core.data.renderer.DataValueRenderer;

import javax.accessibility.Accessible;
import java.awt.*;

//~--- JDK imports ------------------------------------------------------------

public class OCLMoleculeValueRenderer extends AbstractPainterDataValueRenderer implements Accessible {
    private static final String DESCRIPTION = "Actelion 2D depiction";

    //~--- fields -------------------------------------------------------------

    private OCLMoleculeDataValue molCell;

    //~--- methods ------------------------------------------------------------

    @Override
    public boolean accepts(DataColumnSpec spec) {
        return spec.getType().isCompatible(OCLMoleculeDataValue.class) || spec.getType().isAdaptable(OCLMoleculeDataValue.class);
    }

    //~--- get methods --------------------------------------------------------

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(150, 150);
    }

    //~--- methods ------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
    	//System.out.println("RENDER!");
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        Rectangle r = new Rectangle(new java.awt.Point(0, 0), getSize());

        r.grow(-2, -2);

        Insets insets = getInsets();

        r.x      += insets.left;
        r.y      += insets.top;
        r.width  -= insets.left + insets.right;
        r.height -= insets.top + insets.bottom;

        
        SwingDrawContext sdc = new SwingDrawContext((Graphics2D)g);
        if ((molCell != null) && (r.width > 0) && (r.height > 0)) {
            GenericDepictor d = new GenericDepictor((StereoMolecule) molCell.getMolecule(), GenericDepictor.cDModeSuppressChiralText);//new Depictor2D((StereoMolecule) molCell.getMolecule(), Depictor2D.cDModeSuppressChiralText);

            d.validateView(sdc, new GenericRectangle(r.x, r.y, r.width, r.height), GenericDepictor.cModeInflateToMaxAVBL);
            d.paint(sdc);
        }
    }

    //~--- set methods --------------------------------------------------------

    @Override
    protected void setValue(Object value) {
        if (value instanceof DataCell) {
            if (((DataCell) value).isMissing()) {
                molCell = null;
            } else {
                molCell = ValueHelper.getOCLCell((DataCell) value);
            }
        }
    }

    //~--- inner classes ------------------------------------------------------

    public static final class Factory extends AbstractDataValueRendererFactory {

        /**
         * {@inheritDoc}
         */
        @Override
        public DataValueRenderer createRenderer(final DataColumnSpec colSpec) {
            return new OCLMoleculeValueRenderer();
        }

        //~--- get methods ----------------------------------------------------

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescription() {
            return DESCRIPTION;
        }
    }
}
