package com.actelion.research.knime.data;

import javax.swing.Icon;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataValue;
import org.knime.core.data.DataValueComparator;
import org.knime.core.data.StringValue;
import org.knime.core.data.DataValue.UtilityFactory;
import org.knime.core.data.renderer.DataValueRendererFamily;
import org.knime.core.data.renderer.DefaultDataValueRenderer;
import org.knime.core.data.renderer.DefaultDataValueRendererFamily;

import com.actelion.research.chem.StereoMolecule;

/** 
 * Idorsia Pharmaceuticals Ltd.
 * January 2020 
 * 
 * @author Thomas Liphardt
 * 
 */

public interface OCLConformerDataValue extends DataValue , StringValue {
	 
		
		public StereoMolecule getMolecule2D();
		
		public StereoMolecule getMolecule3D();		
		
		public String getIDCode();
		
		public String getCoordinatesIDCode();
		
	    /**
	     * Implementations of the meta information of this value class.
	     * TODO: implement all these things.. 
	     */
	    public static class OCLUtilityFactory extends UtilityFactory {

	        /**
	         * Singleton icon to be used to display this cell type.
	         */
	        private static final Icon ICON = loadIcon(OCLMoleculeDataValue.class, "ocl_type.png");
	        private static final DataValueComparator COMPARATOR = new DataValueComparator() {
	        	// TODO: implement..
	            @Override
	            protected int compareDataValues(final DataValue v1, final DataValue v2) {
	                return v1.hashCode() - v2.hashCode();
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
	        	return new DefaultDataValueRendererFamily(new DefaultDataValueRenderer());
	            //return new DefaultDataValueRendererFamily(new OCLMoleculeValueRenderer());
	        }
	    }	
	

}
