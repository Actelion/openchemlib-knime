package com.actelion.research.knime.nodes;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NodeModel;
import org.knime.core.node.port.PortType;

import java.util.Arrays;

public abstract class OCLNodeModel extends NodeModel {
    public static final PortType OPTIONAL_PORT_TYPE = new PortType(BufferedDataTable.class, true);

    //~--- constructors -------------------------------------------------------

    protected OCLNodeModel(int nrInDataPorts, int nrOutDataPorts) {
        super(nrInDataPorts, nrOutDataPorts);
    }

    protected OCLNodeModel(PortType[] inPortTypes, PortType[] outPortTypes) {
        super(inPortTypes, outPortTypes);
    }

    //~--- methods ------------------------------------------------------------

    protected static PortType[] createOptionalPorts(final int nrDataPorts, final int... optionalPortsIds) {
        PortType[] portTypes = new PortType[nrDataPorts];

        Arrays.fill(portTypes, BufferedDataTable.TYPE);

        if (optionalPortsIds.length > 0) {
            for (int portId : optionalPortsIds) {
                if ((portId - 1) < nrDataPorts) {
                    portTypes[portId - 1] = OPTIONAL_PORT_TYPE;
                }
            }
        }

        return portTypes;
    }
}
