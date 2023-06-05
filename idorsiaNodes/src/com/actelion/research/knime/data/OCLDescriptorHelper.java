package com.actelion.research.knime.data;

import java.util.HashMap;
import java.util.Map;

import com.actelion.research.chem.descriptor.DescriptorConstants;
import com.actelion.research.chem.descriptor.DescriptorHandler;
import com.actelion.research.chem.descriptor.DescriptorHelper;
import com.actelion.research.chem.descriptor.DescriptorInfo;

/**
 * 
 * NOTES: If you want to change / update the available OpenChemLib descriptors, you have to change only
 *        the fields: DESCRIPTORS_2D , DESCRIPTORS_3D
 * 
 * @author Thomas Liphardt
 *
 */

public class OCLDescriptorHelper {
		
	public static final String[] DESCRIPTORS_3D = {
                                    DescriptorConstants.DESCRIPTOR_ShapeAlign.shortName ,
                                    DescriptorConstants.DESCRIPTOR_ShapeAlignSingleConf.shortName	
	};
	

	public static final String[] DESCRIPTORS_2D = {
			                        DescriptorConstants.DESCRIPTOR_FFP512.shortName ,
			                        DescriptorConstants.DESCRIPTOR_PFP512.shortName ,
			                        DescriptorConstants.DESCRIPTOR_HashedCFp.shortName ,
			                        DescriptorConstants.DESCRIPTOR_SkeletonSpheres.shortName ,
			                        DescriptorConstants.DESCRIPTOR_OrganicFunctionalGroups.shortName ,
                                    DescriptorConstants.DESCRIPTOR_Flexophore.shortName , 
                                    //DescriptorConstants.DESCRIPTOR_Flexophore_HighRes.shortName			                        
			                        //DescriptorConstants.DESCRIPTOR_ReactionFP.shortName ,			                        
	};
	
	
	public static Map<String,DescriptorInfo> getAvailableDescriptors3D() {
		
		Map<String,DescriptorInfo> all_descriptors = new HashMap<>();		
		for(String dsci : DESCRIPTORS_3D) {
			all_descriptors.put(dsci,DescriptorHelper.getDescriptorInfo(dsci));
		}
		
		return all_descriptors;
				
	}
	
	public static Map<String,DescriptorInfo> getAvailableDescriptors2D() {
					
		Map<String,DescriptorInfo> all_descriptors = new HashMap<>();		
		for(String dsci : DESCRIPTORS_2D) {
			all_descriptors.put(dsci,DescriptorHelper.getDescriptorInfo(dsci));
		}
		
		return all_descriptors;
		
	}
	

}
