package com.actelion.research.knime.data;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.knime.ui.OCLMoleculeValueRenderer;

import org.knime.chem.types.MolValue;
import org.knime.chem.types.SdfValue;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataValue;
import org.knime.core.data.DataValueComparator;
import org.knime.core.data.renderer.DataValueRendererFamily;
import org.knime.core.data.renderer.DefaultDataValueRendererFamily;

import javax.swing.*;

//~--- JDK imports ------------------------------------------------------------

public interface OCLMoleculeDataValue extends DataValue, SdfValue, MolValue, SmilesValue {

    /**
     * Derived locally.
     */
    public static final UtilityFactory UTILITY = new OCLUtilityFactory();
    ;

    //~--- get methods --------------------------------------------------------

    String getIdCode();

    //String[] getIdCodeAndCoordinates();

    StereoMolecule getMolecule();

    //~--- inner classes ------------------------------------------------------

    /**
     * Implementations of the meta information of this value class.
     */
    public static class OCLUtilityFactory extends UtilityFactory {

        /**
         * Singleton icon to be used to display this cell type.
         */
        private static final Icon ICON = loadIcon(OCLMoleculeDataValue.class, "ocl_type.png");
        private static final DataValueComparator COMPARATOR = new DataValueComparator() {
            @Override
            protected int compareDataValues(final DataValue v1, final DataValue v2) {
                int atomCount1;
                int atomCount2;
                final StereoMolecule mol1 = ((OCLMoleculeDataValue) v1).getMolecule();

                atomCount1 = mol1.getAtoms();

                final StereoMolecule mol2 = ((OCLMoleculeDataValue) v2).getMolecule();

                atomCount2 = mol2.getAtoms();

                return atomCount1 - atomCount2;
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
            return new DefaultDataValueRendererFamily(new OCLMoleculeValueRenderer());
        }
    }
}
