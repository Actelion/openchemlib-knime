package com.actelion.research.knime.data;

import com.actelion.research.chem.descriptor.DescriptorInfo;


import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataValue;
import org.knime.core.data.DataValueComparator;
import org.knime.core.data.StringValue;
import org.knime.core.data.renderer.BitVectorValuePixelRenderer;
import org.knime.core.data.renderer.DataValueRendererFamily;
import org.knime.core.data.renderer.DefaultDataValueRendererFamily;
import org.knime.core.data.renderer.StringValueRenderer;
import org.knime.core.data.vector.bitvector.BitVectorValue;

import javax.swing.*;

//~--- JDK imports ------------------------------------------------------------

public interface OCLDescriptorDataValue extends DataValue, StringValue, BitVectorValue {

    /**
     * Derived locally.
     */
    public static final UtilityFactory UTILITY = new OCLUtilityFactory();
    ;

    //~--- get methods --------------------------------------------------------

    Object getDescriptor();

    String getEncodedDescriptor();

    DescriptorInfo getDescriptorInfo();
        
    //boolean is3D();

    //~--- inner classes ------------------------------------------------------

    /**
     * Implementations of the meta information of this value class.
     */
    class OCLUtilityFactory extends UtilityFactory {

        /**
         * Singleton icon to be used to display this cell type.
         */
        private static final Icon ICON = loadIcon(OCLDescriptorDataValue.class, "ocl_type.png");
        private static final DataValueComparator COMPARATOR = new DataValueComparator() {
            @Override
            protected int compareDataValues(final DataValue v1, final DataValue v2) {
                final String desc01 = ((OCLDescriptorDataValue) v1).getEncodedDescriptor();
                final String desc02 = ((OCLDescriptorDataValue) v2).getEncodedDescriptor();

                if (desc01 == null) {
                    if (desc02 == null) {
                        return 0;
                    } else {
                        return -1;
                    }
                }

                if (desc02 == null) {
                    return 1;
                }

                return desc01.compareTo(desc02);
            }
        };

        //~--- get methods ----------------------------------------------------

        /**
         * {@inheritDoc}
         */
        @Override
        public Icon getIcon() {
            return ICON;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected DataValueComparator getComparator() {
            return COMPARATOR;
        }

        @Override
        protected DataValueRendererFamily getRendererFamily(DataColumnSpec spec) {
            return new DefaultDataValueRendererFamily(new BitVectorValuePixelRenderer());
        }
    }
}
