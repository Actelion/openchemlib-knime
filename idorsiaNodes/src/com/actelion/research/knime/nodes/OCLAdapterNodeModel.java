package com.actelion.research.knime.nodes;

import com.actelion.research.knime.data.OCLDataHelper;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.PortType;

public abstract class OCLAdapterNodeModel extends OCLNodeModel {
    protected OCLAdapterNodeModel(int nrInDataPorts, int nrOutDataPorts) {
        super(nrInDataPorts, nrOutDataPorts);
    }

    protected OCLAdapterNodeModel(PortType[] inPortTypes, PortType[] outPortTypes) {
        super(inPortTypes, outPortTypes);
    }

    //~--- methods ------------------------------------------------------------

    protected abstract DataTableSpec[] prepare(DataTableSpec[] inSpecs) throws InvalidSettingsException;

//  private DataTableSpec[] extractSpecs(BufferedDataTable[] data) {
//      DataTableSpec[] specs = new DataTableSpec[data.length];
//      for (int i = 0; i < data.length; i++) {
//          specs[i] = data[i].getDataTableSpec();
//      }
//      return specs;
//  }
    protected abstract BufferedDataTable[] process(BufferedDataTable[] inData, ExecutionContext exec) throws Exception;

    @Override
    protected final DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
        DataTableSpec[] convertedInSpecs = OCLDataHelper.convertInputTables(inSpecs);

        return prepare(convertedInSpecs);
    }

    @Override
    protected final BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {

//      DataTableSpec[] inSpecs = extractSpecs(inData);
        BufferedDataTable[] convertedInData = OCLDataHelper.convertInputTables(inData, exec);

        return process(convertedInData, exec);
    }
}
