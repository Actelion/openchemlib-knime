package com.actelion.research.knime.ui;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.knime.data.OCLConformerListDataValue;
import com.actelion.research.knime.data.OCLMoleculeDataCell;
import com.actelion.research.knime.utils.SpecHelper;

public class OCLConformerListValueRenderer extends OCLMoleculeValueRenderer {

	
	OCLConformerListDataValue conformers = null;
	
	@Override	
	public boolean accepts(DataColumnSpec spec) {
		return SpecHelper.isConformerListSpec(spec);
	}

	@Override
	protected void paintComponent(Graphics g) {
					
		super.paintComponent(g);
		
		
        int num_conformers = 0;
        if(this.conformers!=null) {
    		if(! ((DataCell) conformers).isMissing() ) {
    			num_conformers = this.conformers.size();
    		}                	
        }               
		
		Rectangle r = new Rectangle(new java.awt.Point(0, 0), getSize());

        r.grow(-2, -2);

        Insets insets = getInsets();

        r.x      += insets.left;
        r.y      += insets.top;
        r.width  -= insets.left + insets.right;
        r.height -= insets.top + insets.bottom;

        int y_center = r.height / 2; 
        g.drawString( "Conformers: "+num_conformers, 4, y_center);        
	}

	@Override
	protected void setValue(Object value) {
		
		if(value==null) {
			this.conformers = null;
			StereoMolecule sm = new StereoMolecule();
			super.setValue(sm);
			return;
		}			
		if( ((DataCell) value).isMissing() ) {
			this.conformers = null;
			StereoMolecule sm = new StereoMolecule();
			super.setValue(sm);			
			return;
		}                	
					
		OCLConformerListDataValue conformers = (OCLConformerListDataValue) value;
		this.conformers = conformers;		
		super.setValue( new OCLMoleculeDataCell(conformers.getMolecule2D()));				
	}

	
	
}
