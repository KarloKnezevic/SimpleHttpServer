package hr.fer.zemris.java.custom.scripting.tokens;

/**
 * Token reprezentant DOUBLE vrijednosti.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class TokenConstantDouble extends Token {

	// vrijednost
	private final double value;

	/**
	 * Konstruktor.
	 * 
	 * @param value
	 */
	public TokenConstantDouble(final double value) {
		this.value = value;
	}

	/**
	 * Vrijednost.
	 * 
	 * @return
	 */
	public double getValue() {
		return value;
	}

	// override
	@Override
	public String asText() {
		return Double.toString(value);
	}

}
