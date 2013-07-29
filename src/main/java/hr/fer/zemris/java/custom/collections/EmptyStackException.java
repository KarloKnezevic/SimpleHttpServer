package hr.fer.zemris.java.custom.collections;

/**
 * Iznimka prilikom dohvaćanja elementa s praznog stoga.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class EmptyStackException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EmptyStackException() {
		super("Cannot pop from an empty stack.");
	}

}