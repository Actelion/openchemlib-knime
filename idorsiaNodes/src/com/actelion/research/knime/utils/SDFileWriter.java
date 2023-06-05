package com.actelion.research.knime.utils;

import com.actelion.research.chem.MolfileCreator;
import com.actelion.research.chem.MolfileV3Creator;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.knime.nodes.io.FileFormat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class SDFileWriter {

    private final File outputFile;
    private final FileFormat fileFormat;
    private Writer out;
    private String lDelim = System.getProperty("line.separator");

    public SDFileWriter(File outputFile, FileFormat fileFormat) {
        this.outputFile = outputFile;
        this.fileFormat = fileFormat;
    }

    public void writeMolecule(StereoMolecule mol) throws IOException {
        switch (this.fileFormat) {
            case SDF_V2: {
                MolfileCreator molfileCreator = new MolfileCreator(mol);
                out.write(molfileCreator.getMolfile());
                out.write(lDelim);
            }
            case SDF_V3: {
                MolfileV3Creator molfileCreator = new MolfileV3Creator(mol);
                out.write(molfileCreator.getMolfile());
                out.write(lDelim);
            }
        }
    }

    public void writeField(String name, String value) throws IOException {
        out.write("> <" + name + ">");
        out.write(lDelim);
        out.write(value);
        out.write(lDelim);
        out.write(lDelim);
    }

    public void endRecord() throws IOException {
        out.write("$$$$");
        out.write(lDelim);
    }

    public void open() throws IOException {
        out = new BufferedWriter(new FileWriter(outputFile));
    }

    public void close() throws IOException {
        if (out != null) {
            out.close();
        }
    }
}
