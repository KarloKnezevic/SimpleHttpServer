package hr.fer.zemris.java.custom.collections;

/**
 * Razred modelira kolekciju kao niz objekata, uz pripadajuće atribute i metode.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class ArrayBackedIndexedCollection {

	// trenutna veličina kolekcije
	private int size;

	// alocirana veličina polja za spremanje objekata kolekcije
	private int capacity;

	// niz referenci na objekte čija duljina je određena s capacity
	private Object[] elements;

	/**
	 * Predodređeni konstruktor.
	 */
	public ArrayBackedIndexedCollection() {
		// inicijalno veličina polja postavljena na 16
		capacity = 16;
		size = 0;
		elements = new Object[capacity];
	}

	/**
	 * Konstruktor.
	 * 
	 * @param početna
	 *            veličina kolekcije
	 */
	public ArrayBackedIndexedCollection(final int initialCapacity)
			throws IllegalArgumentException {
		if (initialCapacity < 1) {
			throw new IllegalArgumentException(
					"Not allowed capacity less then 1.");
		}

		capacity = initialCapacity;
		size = 0;
		elements = new Object[capacity];
	}

	/**
	 * Ispitivanje je li kolekcija prazna.
	 * 
	 * @return true ukoliko je kolekcija prazna, false u suprotnom.
	 */
	public boolean isEmpty() {
		if (size > 0) {
			return false;
		}
		return true;
	}

	/**
	 * Veličina kolekcije. Veličina kolekcije predstavlja broj elemenata
	 * kolekcije.
	 * 
	 * @return veličina kolekcije.
	 */
	public int size() {
		return size;
	}

	/**
	 * Dodavanje elementa na prvo slobodno mjesto u kolekciji. Ukoliko je
	 * potrebno, vrši se realokacija kolekcije i kapacitet povećava na dvostruku
	 * vrijednost od prethodne.
	 * 
	 * @param objekt
	 *            .
	 */
	public void add(final Object value) throws IllegalArgumentException {
		if (value == null) {
			throw new IllegalArgumentException("Null element is not allowed.");
		}

		if (size == capacity) {
			// realociraj dvostruko veći niz
			final Object[] newArray = new Object[2 * capacity];
			System.arraycopy(elements, 0, newArray, 0, capacity);
			capacity = 2 * capacity;
			elements = newArray;
		}

		elements[size] = value;
		size++;

	}

	/**
	 * Vraćanje elementa na mjestu index.
	 * 
	 * @param index
	 *            elementa kolekcije
	 * @return elements[index]
	 * @throws IndexOutOfBoundsException
	 *             ukoliko se dohvaća element s nepostojećim indexom.
	 */
	public Object get(final int index) throws IndexOutOfBoundsException {
		if ((index > (size - 1)) || (index < 0)) {
			throw new IndexOutOfBoundsException();
		}
		return elements[index];
	}

	/**
	 * Micanje elementa s mjesta index i posmicanje svih elemenata desno od
	 * obrisanog elementa za jedan ulijevo.
	 * 
	 * @param index
	 *            elementa
	 * @throws IndexOutOfBoundsException
	 *             ukoliko se miče element s nepostojećim indexom.
	 */
	public void remove(final int index) throws IndexOutOfBoundsException {
		if ((index > (size - 1)) || (index < 0)) {
			throw new IndexOutOfBoundsException();
		}

		if (index == (size - 1)) {
			elements[size - 1] = null;
			size--;
		} else {
			for (int i = index + 1; i < size; i++) {
				elements[i - 1] = elements[i];
			}
			elements[size - 1] = null;
			size--;
		}
	}

	/**
	 * Ubacivanje elementa na mjesto index i posmicanje svih elemenata,
	 * uključujući i prvotni element na mjestu index za jedan udesno.
	 * 
	 * @param element
	 * @param index
	 * @throws IndexOutOfBoundsException
	 *             ukoliko položaj elementa adresira nepostojeći index.
	 */
	public void insert(final Object value, final int position)
			throws IndexOutOfBoundsException {
		if ((position > size) || (position < 0)) {
			throw new IndexOutOfBoundsException();
		}

		if (size == capacity) {
			final Object[] newArray = new Object[2 * capacity];
			System.arraycopy(elements, 0, newArray, 0, capacity);
			capacity = 2 * capacity;
			elements = newArray;
		}

		int i;
		for (i = size; i > position; i--) {
			elements[i] = elements[i - 1];
		}
		elements[i] = value;
		size++;

	}

	/**
	 * Metoda vraća index prvog pojavljivanja elementa u kolekciji.
	 * 
	 * @param element
	 * @return index elementa ako je u kolekciji, -1 u suprotnom
	 */
	public int indexOf(final Object value) {
		for (int i = 0; i < size; i++) {
			if (elements[i].equals(value)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Ispitivanje nalazi li se element u kolekciji.
	 * 
	 * @param element
	 * @return true ukoliko je element u kolekciji, false u suprotnom.
	 */
	public boolean contains(final Object value) {
		for (int i = 0; i < size; i++) {
			if (elements[i].equals(value)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Briše sve objekte iz kolekcije.
	 */
	public void clear() {
		for (int i = 0; i < size; i++) {
			elements[i] = null;
		}
		size = 0;
	}
}