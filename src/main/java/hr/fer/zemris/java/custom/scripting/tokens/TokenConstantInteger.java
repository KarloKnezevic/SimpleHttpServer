package hr.fer.zemris.java.custom.scripting.tokens;

/**
 * Token reprezentant INTEGER vrijednosti.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class TokenConstantInteger extends Token {

	// vrijednost
	private final int value;

	/**
	 * Konstruktor.
	 * 
	 * @param value
	 */
	public TokenConstantInteger(final int value) {
		this.value = value;
	}

	/**
	 * Vrijednost.
	 * 
	 * @return
	 */
	public int getValue() {
		return value;
	}

	// override
	@Override
	public String asText() {
		return Integer.toString(value);
	}

}
