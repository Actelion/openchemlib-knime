package com.actelion.research.knime.utils;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.descriptor.DescriptorHandler;
import com.actelion.research.chem.descriptor.DescriptorHandlerFlexophore;
import com.actelion.research.chem.descriptor.DescriptorHandlerStandard2DFactory;
//import com.actelion.research.chem.descriptor.DescriptorHandlerExtended2DFactory;
//import com.actelion.research.chem.descriptor.DescriptorHandlerExtendedFactory;
import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.chem.phesa.DescriptorHandlerShape;
import com.actelion.research.chem.phesa.DescriptorHandlerShapeOneConf;

import org.knime.core.node.NodeLogger;

import java.util.HashMap;
import java.util.Map;

//~--- JDK imports ------------------------------------------------------------

public class DescriptorHelpers {
    //    //    public static final DescriptorInfo[] DESCRIPTOR_LIST = DESCRIPTOR_EXTENDED_LIST;
    public static final DescriptorInfo[] DESCRIPTOR_LIST = {
            DescriptorHandlerStandard2DFactory.DESCRIPTOR_FFP512,
            DescriptorHandlerStandard2DFactory.DESCRIPTOR_PFP512,
            DescriptorHandlerStandard2DFactory.DESCRIPTOR_HashedCFp,
            DescriptorHandlerStandard2DFactory.DESCRIPTOR_SkeletonSpheres,
            DescriptorHandlerStandard2DFactory.DESCRIPTOR_OrganicFunctionalGroups,
            DescriptorHandlerFlexophore.getDefaultInstance().getInfo()//,
            //DescriptorHandlerExtended2DFactory.DESCRIPTOR_Flexophore_HighRes
    };
    
    public static final DescriptorInfo[] FULL_DESCRIPTOR_LIST = {
    		DescriptorHandlerStandard2DFactory.DESCRIPTOR_FFP512,
    		DescriptorHandlerStandard2DFactory.DESCRIPTOR_PFP512,
    		DescriptorHandlerStandard2DFactory.DESCRIPTOR_HashedCFp,
    		DescriptorHandlerStandard2DFactory.DESCRIPTOR_SkeletonSpheres,
    		DescriptorHandlerStandard2DFactory.DESCRIPTOR_OrganicFunctionalGroups,
    		DescriptorHandlerFlexophore.getDefaultInstance().getInfo(),
    		//DescriptorHandlerExtended2DFactory.DESCRIPTOR_Flexophore,
            //DescriptorHandlerExtended2DFactory.DESCRIPTOR_Flexophore_HighRes,
            DescriptorHandlerShape.getDefaultInstance().getInfo(),
            DescriptorHandlerShapeOneConf.getDefaultInstance().getInfo()
    		//DescriptorHandlerExtended2DFactory.DESCRIPTOR_ShapeAlign,
            //DescriptorHandlerExtended2DFactory.DESCRIPTOR_ShapeAlignSingleConf
    };
    
    
//    };
    //private static DescriptorHandler dhf = new DescriptorHandlerExtendedFactory();
    private static DescriptorHandlerStandard2DFactory  dhf = new DescriptorHandlerStandard2DFactory();
    private static Map<DescriptorInfo, DescriptorHandler> dhMap = new HashMap<DescriptorInfo, DescriptorHandler>();
    private static NodeLogger LOG = NodeLogger.getLogger(DescriptorHelpers.class);

    //~--- methods ------------------------------------------------------------


    public static DescriptorInfo[] getDescriptorInfos() {
        //return DescriptorHandlerExtendedFactory.DESCRIPTOR_LIST;
    	//return DescriptorHandlerExtended2DFactory.DESCRIPTOR_LIST;
    	return DESCRIPTOR_LIST;
    }
    
    // includes PheSA
    public static DescriptorInfo[] getFullDescriptorInfos() {
    	return FULL_DESCRIPTOR_LIST;
    }

    public static Object calculateDescriptor(StereoMolecule mol, DescriptorInfo descriptorInfo) {
        if ((mol == null) || (descriptorInfo == null)) {
            return null;
        }

        DescriptorHandler dh = getDescriptorHandler(descriptorInfo);

        try {
            return dh.createDescriptor(mol);
        } catch (Throwable t) {
            LOG.error("Error while calculating descriptor, dt="+dh.getInfo().shortName, t);
        }

        return null;
    }

    public static String encode(Object descriptor, DescriptorInfo descriptorInfo) {
        DescriptorHandler dh = getDescriptorHandler(descriptorInfo);
        if (descriptor == null) {
            return "";
        }
        return dh.encode(descriptor);
    }

    public static Object decode(String encodedDescriptor, DescriptorInfo descriptorInfo) {
        DescriptorHandler dh = getDescriptorHandler(descriptorInfo);
        if (encodedDescriptor.isEmpty()) {
            return null;
        }
        return dh.decode(encodedDescriptor);
    }

    public static double calculateSimilarity(DescriptorInfo descriptorInfo, Object queryDescriptor, Object targetDescriptor) {
        if (queryDescriptor == null || targetDescriptor == null) {
            return Double.NaN;
        }
        DescriptorHandler dh = dhMap.get(descriptorInfo);
        return dh.getSimilarity(queryDescriptor, targetDescriptor);
    }

    public static DescriptorInfo getDescriptorInfoByShortName(String descriptorShortName) {
        for (DescriptorInfo descriptorInfo : DescriptorHelpers.getFullDescriptorInfos()) {
            if (descriptorInfo.shortName.equals(descriptorShortName)) {
                return descriptorInfo;
            }
        }
        return null;
    }

    /**
     * Returns a thread-safe copy of the requested descriptor handler
     * 
     * @param descriptorInfo
     * @return
     */
    public static DescriptorHandler getDescriptorHandler(DescriptorInfo descriptorInfo) {
        DescriptorHandler dh = dhMap.get(descriptorInfo);        

        if (dh == null) {
            dh = dhf.create(descriptorInfo.shortName);
            if(dh==null) {
            	// load additional dhs, like flexophore:
            	if(descriptorInfo.shortName.equals(DescriptorHandlerFlexophore.getDefaultInstance().getInfo().shortName)) {
            		dh = DescriptorHandlerFlexophore.getDefaultInstance();
            	}
            }
            dhMap.put(descriptorInfo, dh);
        }
        return dh.getThreadSafeCopy();
    }

}
