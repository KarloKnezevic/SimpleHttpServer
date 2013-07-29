package hr.fer.zemris.java.custom.collections;

/**
 * Razred modelira stog i operacije sa stogom.
 * @author Karlo
 *
 */

/**
 * Razred modelira stog i operacije rada sa stogom. Stog podržava tip objekta
 * Object (i sve objekte nasljeđene iz navedenog objekta).
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class ObjectStack {
	// korištenje oblikovnog obrasca Adapter
	private final ArrayBackedIndexedCollection adaptee;

	/**
	 * Predodređeni konstruktor.
	 */
	public ObjectStack() {
		adaptee = new ArrayBackedIndexedCollection();
	}

	/**
	 * Provjera je li stog prazan.
	 * 
	 * @return true ukoliko je broj elemenata na stogu 0, false u suprotnom.
	 */
	public boolean isEmpty() {
		if (adaptee.isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * Broj elemenata na stogu.
	 * 
	 * @return prirodan broj elemenata na stogu.
	 */
	public int size() {
		return adaptee.size();
	}

	/**
	 * Dodavanje vrijednosti value na stog.
	 * 
	 * @param objekt
	 */
	public void push(final Object value) {
		adaptee.add(value);
	}

	/**
	 * Vraća zadnji element sa stoga i pritom ga briše sa stoge.
	 * 
	 * @return posljednji element
	 * @throws EmptyStackException
	 *             ukoliko se dohvaća element s praznog stoga
	 */
	public Object pop() throws EmptyStackException {
		if (adaptee.isEmpty()) {
			throw new EmptyStackException();
		}

		final Object element = adaptee.get(adaptee.size() - 1);
		adaptee.remove(adaptee.size() - 1);
		return element;
	}

	/**
	 * Vraća zadnji element sa stoga, ali ga ne briše.
	 * 
	 * @return element s vrha stoga
	 * @throws EmptyStackException
	 *             ukoliko se dohvaća element s praznog stoga
	 */
	public Object peek() throws EmptyStackException {
		if (adaptee.isEmpty()) {
			throw new EmptyStackException();
		}
		return adaptee.get(adaptee.size() - 1);
	}

	/**
	 * Briše sve elemente stoga. Veličina stoga nakon poziva je 0.
	 */
	public void clear() {
		adaptee.clear();
	}
}