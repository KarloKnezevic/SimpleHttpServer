package hr.fer.zemris.java.custom.scripting.parser;

/**
 * Iznimka parsera.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class SmartScriptParserException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Predodređeni konstruktor. Predodređena poruka konstruktora
	 * "Parsing exception.".
	 */
	public SmartScriptParserException() {
		super("Parsing exception.");
	}

	/**
	 * Konstruktor s određenom porukom.
	 * 
	 * @param echo
	 */
	public SmartScriptParserException(final String echo) {
		super(echo);
	}
}
