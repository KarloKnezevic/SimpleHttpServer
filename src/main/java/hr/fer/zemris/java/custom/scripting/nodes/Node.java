package hr.fer.zemris.java.custom.scripting.nodes;

import hr.fer.zemris.java.custom.collections.ArrayBackedIndexedCollection;

/**
 * Čvor dokumenta. Čvor u kolekciji čuva čvorove sve djece određenog taga.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public abstract class Node {

	// kolekcija čvorova djece
	private final ArrayBackedIndexedCollection childNodes = new ArrayBackedIndexedCollection();

	/**
	 * Dodavanje čvora djeteta.
	 * 
	 * @param child
	 */
	public void addChildNode(final Node child) {
		childNodes.add(child);
	}

	/**
	 * Broj čvorova kolekcije.
	 * 
	 * @return
	 */
	public int numberOfChildren() {
		return childNodes.size();
	}

	/**
	 * Vraćanje čvora na mjestu index.
	 * 
	 * @param index
	 * @return
	 */
	public Node getChild(final int index) {
		return (Node) childNodes.get(index);
	}

	public abstract void accept(INodeVisitor visitor);

	@Override
	public String toString() {
		return "";
	}
}