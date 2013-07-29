package hr.fer.zemris.java.custom.scripting.tokens;

/**
 * Token reprezentant FUNKCIJE.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class TokenFunction extends Token {

	// ime
	private final String name;

	/**
	 * Konstruktor.
	 * 
	 * @param name
	 *            ime funkcije
	 */
	public TokenFunction(final String name) {
		this.name = name;
	}

	/**
	 * Ime funckije.
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
