package com.actelion.research.knime.nodes.conversion;

import java.io.File;
import java.io.IOException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.stream.Collectors;

import org.knime.chem.types.MolValue;
import org.knime.chem.types.SdfValue;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.database.DatabaseHelper;

import com.actelion.research.chem.MolfileParser;
import com.actelion.research.chem.SmilesParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.knime.data.OCLConformerListDataCell;
import com.actelion.research.knime.data.OCLConformerListDataValue;
import com.actelion.research.knime.data.OCLPheSAMoleculeDataCell;
import com.actelion.research.knime.utils.SpecHelper;
import com.actelion.research.knime.utils.ValueHelper;
import com.actelion.research.knime.utils.dwar.MoleculeValue;


/**
 *
 * @author liphath1
 * 
 * Idorsia Pharmaceuticals Ltd
 */
public class MolToConformerListNodeModel extends NodeModel {
    
    public static final int IN_PORT = 0;

	private static final NodeLogger LOGGER = NodeLogger.getLogger(MolToConformerListNodeModel.class);


	private final MolToConformerListNodeSettings m_Settings = new MolToConformerListNodeSettings();

	/**
	 * Constructor for the node model.
	 */
	protected MolToConformerListNodeModel() {
		/**
		 * Here we specify how many data input and output tables the node should have.
		 * In this case its one input and one output table.
		 */
		super(1, 1);
	}


	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec) throws Exception {
					
		ColumnRearranger c = createColumnRearranger(inData[IN_PORT].getDataTableSpec(),m_Settings.getInputColumnName());
        BufferedDataTable out = exec.createColumnRearrangeTable(inData[IN_PORT], c, exec);

        return new BufferedDataTable[]{out};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
		// find all conformer list columns:
		List<DataColumnSpec> columns = SpecHelper.getMoleculeColumnSpecs(inSpecs[0]);
		System.out.println();
		
		if(columns.size()==0) {
			throw new InvalidSettingsException("No Molecule columns found.");
		}
		
		List<String> column_names = columns.stream().map( ci -> ci.getName() ).collect(Collectors.toList());
		
		String input = m_Settings.getInputColumnName();
		
		if(input != null) {
			if(input.equals("")) {
				input = null;
			}
		}
		
		if(input!=null) {
			if(!column_names.contains(input)) {
				LOGGER.info("No Molecules column supplied. Try to automatically reconfigure the node.");
				m_Settings.setInputColumnName(column_names.get(0));
			}
			else {
				m_Settings.setInputColumnName(input);
			}
		}
		else{
			m_Settings.setInputColumnName(column_names.get(0));
		}

				
		/*
		 * Similar to the return type of the execute method, we need to return an array
		 * of DataTableSpecs with the length of the number of outputs ports of the node
		 * (as specified in the constructor). The resulting table created in the execute
		 * methods must match the spec created in this method. As we will need to
		 * calculate the output table spec again in the execute method in order to
		 * create a new data container, we create a new method to do that.
		 */
		DataTableSpec inputTableSpec = inSpecs[0];				
		return new DataTableSpec[] { createColumnRearranger(inputTableSpec , m_Settings.getInputColumnName()).createSpec() };
	}


	/**
	 * Creates the output table spec from the input spec. For each double column in
	 * the input, one String column will be created containing the formatted double
	 * value as String.
	 * 
	 * @p)aram inputTableSpec
	 * @return
	 */
	private ColumnRearranger createColumnRearranger(DataTableSpec inputTableSpec, String inputCol) {
					
        ColumnRearranger c = new ColumnRearranger(inputTableSpec);

        String newColumnName = "ConformerList["+inputCol+"]";
        
        MolfileParser parser = new MolfileParser();
        SmilesParser  parserSmiles = new SmilesParser();        
        
        newColumnName = SpecHelper.getUniqueColumnName(inputTableSpec, newColumnName);

        DataColumnSpec newColSpec3d   = SpecHelper.createConformerSetColumnSpec(newColumnName,null);
        final int   inputMolColumnIdx = SpecHelper.getColumnIndex(inputTableSpec, m_Settings.getInputColumnName());

        CellFactory factory_3d = new SingleCellFactory(newColSpec3d) {
            public DataCell getCell(DataRow row) {
                DataCell orgCell = row.getCell(inputMolColumnIdx);

                if (orgCell.isMissing()) {
                    return DataType.getMissingCell();
                }

                DataType type = orgCell.getType();
                StereoMolecule molecule = null;
                try {
                    if (type.isCompatible(SdfValue.class) || type.isAdaptable(SdfValue.class)) {
                        SdfValue value = ValueHelper.getSDFCell(orgCell);
                        String sdfValue = value.getSdfValue();
                        String molFile = sdfValue.replaceAll("\n$$$$", "");
                        molecule = parser.getCompactMolecule(molFile);
                    } else if (type.isCompatible(MolValue.class) || type.isAdaptable(MolValue.class)) {
                        MolValue value = ValueHelper.getMolCell(orgCell);
                        String molFile = value.getMolValue();
                        molecule = parser.getCompactMolecule(molFile);
                    } else if (type.isCompatible(SmilesValue.class) || type.isAdaptable(SmilesValue.class)) {
                        String value = ValueHelper.getStringCell(orgCell).getStringValue().toString();
                        //String molFile = value.getMolValue();
                        molecule = new StereoMolecule();
                        parserSmiles.parse(molecule, value);   
                        molecule.toString();
                    }
                } catch (Throwable ignored) {
                }
                if (molecule == null) {
                    return DataType.getMissingCell();
                }
                
                // check if we actually have any 3d coordinates (aka non-zero z-coordinates):
                boolean has_z_coordinates = false;
                for(int zi=0;zi<molecule.getAllAtoms();zi++) {
                	has_z_coordinates |= molecule.getCoordinates(zi).z != 0;
                }
                if(!has_z_coordinates) {
                	setWarningMessage("There may be molecules without 3D coordinates!");
                }
                
                return new OCLConformerListDataCell(molecule);
            }
        };            
        c.append(factory_3d);        
        
//        if (m_settings.isRemoveInputColumn()) {
//            c.replace(factory, orgMolColumnIdx);
//        } else {
//            c.append(factory);
//        }

        return c;	
    }
	
	
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	m_Settings.saveSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    	m_Settings.loadSettingsForModel(settings);    	
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }


	@Override
	protected void reset() {
		// TODO Auto-generated method stub
		
	}
    
    
    
}

