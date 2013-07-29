package hr.fer.zemris.java.custom.scripting.tokens;

/**
 * Token reprezentant OPERATORA.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class TokenOperator extends Token {

	// simbol
	private final String symbol;

	/**
	 * Konstruktor.
	 * 
	 * @param symbol
	 *            simbol operatora
	 */
	public TokenOperator(final String symbol) {
		this.symbol = symbol;
	}

	/**
	 * Simbol operatora.
	 * 
	 * @return
	 */
	public String getSymbol() {
		return symbol;
	}

	// override
	@Override
	public String asText() {
		return symbol;
	}
}
