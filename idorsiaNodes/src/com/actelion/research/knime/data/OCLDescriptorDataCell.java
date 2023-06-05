/*
 * @(#)OCLMoleculeDataCell.java   16/01/19
 *
 * Copyright (c) 2010-2011 Actelion Pharmaceuticals Ltd.
 *
 *  Gewerbestrasse 16, CH-4123 Allschwil, Switzerland
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information
 *  of Actelion Pharmaceuticals Ltd. ("Confidential Information").  You
 *  shall not disclose such Confidential Information and shall use
 *  it only in accordance with the terms of the license agreement
 *  you entered into with Actelion Pharmaceuticals Ltd.
 *
 *  Author: finkt
 */


package com.actelion.research.knime.data;

import com.actelion.research.chem.descriptor.DescriptorEncoder;
import com.actelion.research.chem.descriptor.DescriptorHelper;
import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.knime.utils.BitHelpers;
import com.actelion.research.knime.utils.DescriptorHelpers;

import java.nio.LongBuffer;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.vector.bitvector.DenseBitVector;
import org.knime.core.node.NodeLogger;

//~--- JDK imports ------------------------------------------------------------

public class OCLDescriptorDataCell extends DataCell implements OCLDescriptorDataValue, StringValue {
    public static final DataType TYPE = DataType.getType(OCLDescriptorDataCell.class);
    private static final OCLDescriptorDataCellSerializer SERIALIZER = new OCLDescriptorDataCellSerializer();
    private static NodeLogger LOG = NodeLogger.getLogger(OCLDescriptorDataCell.class);
    // ~--- fields -------------------------------------------------------------
    private byte[] descriptor;
    private DescriptorInfo descriptorInfo;

    // used to provide the BitVectorValue methods
    private DenseBitVector bitVector;
    //~--- constructors -------------------------------------------------------

    public OCLDescriptorDataCell(String descriptorShortName, byte[] descriptor) {
    	
    	DescriptorEncoder de = new DescriptorEncoder();
    	if(DescriptorHelper.isBinaryFingerprint(descriptorShortName)) {
    		try {
	    		int[] fp = de.decode(descriptor);
	    		this.bitVector = new DenseBitVector(convertIntArrayToLong(fp), fp.length*32);
    		}
    		catch(Exception ex) {
    			ex.printStackTrace();  
    		}
    	}
    	else {
    		// just put in dummy "0" value.. ;)
    		this.bitVector = new DenseBitVector(1);
    		this.bitVector.clear(0);
    	}
    	
    	
    	//System.out.println("init descriptor, "+ descriptorShortName +" , length="+descriptor.length);
    	
    	//super( new DenseBitVector(convertByteArrayToLong(longBytes)) );
        this.descriptor = descriptor;
        this.descriptorInfo = DescriptorHelpers.getDescriptorInfoByShortName(descriptorShortName);
        
        //DescriptorHelpers.getDescriptorHandler(descriptorInfo).               
    }
    //~--- get methods --------------------------------------------------------

    // ~--- constructors -------------------------------------------------------
    public static final OCLDescriptorDataCellSerializer getCellSerializer() {
        return SERIALIZER;
    }

    //~--- methods ------------------------------------------------------------

    // ~--- methods ------------------------------------------------------------
    @Override
    public int hashCode() {
        return (descriptor != null)
                ? new String(descriptor).hashCode()
                : 0;
    }

    @Override
    public String toString() {
        return getEncodedDescriptor();
    }

    @Override
    public Object getDescriptor() {
        if (getEncodedDescriptor().isEmpty()) {
            return null;
        }
        return DescriptorHelpers.decode(getEncodedDescriptor(), descriptorInfo);
    }

    @Override
    public String getEncodedDescriptor() {
        if (descriptor == null) {
            return "";
        }
        return new String(descriptor);
    }

    @Override
    public DescriptorInfo getDescriptorInfo() {
        return descriptorInfo;
    }

    @Override
    public String getStringValue() {
        Object descriptor = getDescriptor();
        if (descriptor instanceof int[]) {
            return BitHelpers.toBinaryString((int[]) descriptor);
        }
        return getEncodedDescriptor();
    }

    // ~--- methods ------------------------------------------------------------
    @Override
    protected boolean equalsDataCell(DataCell dc) {
        if (dc instanceof OCLDescriptorDataValue) {
            OCLDescriptorDataCell descriptorDataCell = (OCLDescriptorDataCell) dc;
            if (descriptorInfo.shortName.equals(descriptorDataCell.descriptorInfo.shortName)) {
                return getEncodedDescriptor().equals(descriptorDataCell.getEncodedDescriptor());
            }
        }
        return false;
    }

    
    public static long[] convertIntArrayToLong(int[] data){
    	if(data.length==0) {
    		return new long[0];
    	}
    	long a[] = new long[ (data.length-1)/2 + 1 ];
    	for( int zi=0;zi<a.length;zi++ ) {
    		a[zi]  =  ((long)data[zi/2      ]) << 32;
    		if( data.length > 2*zi + 1 ) {
    			a[zi] |=  data[(zi/2) + 1];
    		}
    	}
    	return a;
    }
    
    
    public static long[] convertByteArrayToLong(byte[] bytes){
    	if(bytes.length==0) {
    		return new long[0];
    	}
    	long a[] = new long[ (bytes.length-1)/8 + 1 ];
    	for( int zi=0;zi<a.length;zi++ ) {
    		a[zi] = 0x0;
    		for ( int zj=0;zj<8;zj++) {
    			int pos = zi*8 + zj; 
    			if( pos >= bytes.length ) {
    				break;
    			}
    			a[zi] |= ( bytes[pos] << ((7-zj)*8) );
    		}    		
    	}
    	return a;
//    	 java.nio.ByteBuffer byteBuffer = java.nio.ByteBuffer.allocateDirect(bytes.length);
//    	 byteBuffer.put(bytes);
//    	 byteBuffer.flip();    	 
//    	 return byteBuffer.asLongBuffer().array();
    }

	@Override
	public long length() {		
		return this.bitVector.length();
	}

	@Override
	public long cardinality() {
		return this.bitVector.cardinality();
	}

	@Override
	public boolean get(long index) {
		//System.out.println("get bit: "+index);
		return this.bitVector.get(index);		
	}

	@Override
	public boolean isEmpty() {
		return this.bitVector.isEmpty();
	}

	@Override
	public long nextClearBit(long startIdx) {
		return this.bitVector.nextClearBit(startIdx);
	}

	@Override
	public long nextSetBit(long startIdx) {
		return this.bitVector.nextSetBit(startIdx);
	}

	@Override
	public String toHexString() {
		return this.bitVector.toHexString();
	}

	@Override
	public String toBinaryString() {
		return this.bitVector.toBinaryString();
	}    
        
}
