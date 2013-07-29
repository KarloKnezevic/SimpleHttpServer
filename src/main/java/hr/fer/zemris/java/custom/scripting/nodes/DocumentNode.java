package hr.fer.zemris.java.custom.scripting.nodes;

/**
 * Glavni čvor dokumenta.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class DocumentNode extends Node {

	@Override
	public void accept(final INodeVisitor visitor) {
		visitor.visitDocumentNode(this);
	}

}
