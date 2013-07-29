package hr.fer.zemris.java.custom.scripting.nodes;

import hr.fer.zemris.java.custom.scripting.tokens.Token;

/**
 * Razred reprezentant ECHO taga.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class EchoNode extends Node {

	private final Token[] tokens;

	/**
	 * Konsturktor.
	 * 
	 * @param tokens
	 */
	public EchoNode(final Token[] tokens) {
		this.tokens = new Token[tokens.length];
		System.arraycopy(tokens, 0, this.tokens, 0, tokens.length);
	}

	/**
	 * Vraćanje svih tokena echo taga.
	 * 
	 * @return tokeni echo taga
	 */
	public Token[] getTokens() {
		final Token[] tokenCopy = new Token[tokens.length];
		System.arraycopy(tokens, 0, tokenCopy, 0, tokens.length);
		return tokenCopy;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		// sb.append("<ECHO>");
		for (int i = 0; i < tokens.length; i++) {
			sb.append(tokens[i].asText() + " ");
		}
		// sb.append("</ECHO>");
		return sb.toString();
	}

	@Override
	public void accept(final INodeVisitor visitor) {
		visitor.visitEchoNode(this);
	}

}