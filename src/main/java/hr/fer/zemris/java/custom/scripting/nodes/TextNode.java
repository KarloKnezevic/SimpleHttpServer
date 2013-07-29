package hr.fer.zemris.java.custom.scripting.nodes;

/**
 * Razred za text.
 * @author Karlo
 *
 */

/**
 * Razred reprezentant TEXT taga.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class TextNode extends Node {

	private final String text;

	/**
	 * Konstruktor.
	 * 
	 * @param text
	 */
	public TextNode(final String text) {
		this.text = text;
	}

	/**
	 * Tekst.
	 * 
	 * @return
	 */
	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		// sb.append("<TEXT>");
		sb.append(text);
		// sb.append("</TEXT>");
		return sb.toString();
	}

	@Override
	public void accept(final INodeVisitor visitor) {
		visitor.visitTextNode(this);
	}

}