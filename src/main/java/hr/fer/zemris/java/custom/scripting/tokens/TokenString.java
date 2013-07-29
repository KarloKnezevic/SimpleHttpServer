package hr.fer.zemris.java.custom.scripting.tokens;

/**
 * Token reprezentant STRING vrijednosti.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class TokenString extends Token {

	// vrijednost
	private final String value;

	/**
	 * Konstruktor.
	 * 
	 * @param value
	 *            string
	 */
	public TokenString(final String value) {
		this.value = value;
	}

	/**
	 * String.
	 * 
	 * @return
	 */
	public String getValue() {
		return value;
	}

	// override
	@Override
	public String asText() {
		return value;
	}

}