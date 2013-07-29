package hr.fer.zemris.java.custom.scripting.nodes;

import hr.fer.zemris.java.custom.scripting.tokens.Token;
import hr.fer.zemris.java.custom.scripting.tokens.TokenVariable;

/**
 * Razred reprezentant FOR taga.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class ForLoopNode extends Node {

	private final TokenVariable variable;
	private final Token startExpression;
	private final Token endExpression;
	private final Token stepExpression;

	/**
	 * Konstruktor.
	 * 
	 * @param variable
	 *            varijabla brojača
	 * @param startExpression
	 *            početna vrijednost brojača
	 * @param endExpression
	 *            konačna vrijednost brojača
	 * @param stepExpression
	 *            korak brojača
	 */
	public ForLoopNode(final TokenVariable variable,
			final Token startExpression, final Token endExpression,
			final Token stepExpression) {
		this.variable = variable;
		this.startExpression = startExpression;
		this.endExpression = endExpression;
		this.stepExpression = stepExpression;
	}

	/**
	 * Varijabla.
	 * 
	 * @return
	 */
	public TokenVariable getVariable() {
		return variable;
	}

	/**
	 * Početna vrijednost.
	 * 
	 * @return
	 */
	public Token getStartExpression() {
		return startExpression;
	}

	/**
	 * Konačna vrijednost.
	 * 
	 * @return
	 */
	public Token getEndExpression() {
		return endExpression;
	}

	/**
	 * Korak brojača.
	 * 
	 * @return
	 */
	public Token getStepExpression() {
		return stepExpression;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		// sb.append("<FOR>");
		if (!(stepExpression == null)) {
			sb.append(variable.asText()).append(" ")
					.append(startExpression.asText()).append(" ")
					.append(endExpression.asText()).append(" ")
					.append(stepExpression.asText());
		} else {
			sb.append(variable.asText()).append(" ")
					.append(startExpression.asText()).append(" ")
					.append(endExpression.asText());
		}
		// sb.append("</FOR>");
		return sb.toString();
	}

	@Override
	public void accept(final INodeVisitor visitor) {
		visitor.visitForLoopNode(this);
	}
}