package hr.fer.zemris.java.custom.scripting.tokens;

/**
 * Token reprezentant VARIJABLE.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class TokenVariable extends Token {

	// ime
	private final String name;

	/**
	 * Konstruktor.
	 * 
	 * @param name
	 *            ime varijable
	 */
	public TokenVariable(final String name) {
		this.name = name;
	}

	/**
	 * Ime varijable.
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	// override
	@Override
	public String asText() {
		return name;
	}
}