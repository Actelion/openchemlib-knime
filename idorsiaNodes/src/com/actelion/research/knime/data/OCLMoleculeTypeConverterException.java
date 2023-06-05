package com.actelion.research.knime.data;

public class OCLMoleculeTypeConverterException extends RuntimeException {

    //
    // Constants
    //

    /** The serial number. */
    private static final long serialVersionUID = 4754887531605515749L;

    //~--- constructors -------------------------------------------------------

    //
    // Constructors
    //
    public OCLMoleculeTypeConverterException() {
        super();
    }

    public OCLMoleculeTypeConverterException(final String message) {
        super(message);
    }

    public OCLMoleculeTypeConverterException(final Throwable cause) {
        super(cause);
    }

    public OCLMoleculeTypeConverterException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
