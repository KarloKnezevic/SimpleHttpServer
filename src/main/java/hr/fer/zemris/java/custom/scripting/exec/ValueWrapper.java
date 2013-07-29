package hr.fer.zemris.java.custom.scripting.exec;

/**
 * Razred implementira omotač objekta. Nudi aritmetičke operacije za pohranjeni
 * objekt.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class ValueWrapper {

	// pohranjeni objekt
	private Object value;
	/**
	 * Konstante.
	 */
	// null vrijednost
	private final int nullVaule = 0;
	// int vrijednost
	private final int intValue = 1;
	// double vrijednost
	private final int doubleValue = 2;
	// string vrijednost
	private final int stringValue = 3;

	/**
	 * Konstruktor.
	 * 
	 * @param value
	 */
	public ValueWrapper(final Object value) {
		this.value = value;
	}

	/**
	 * Metoda izvršava zadanu aritmetičku operaciju.
	 * 
	 * @param operator
	 *            +, -, *, /
	 * @param value
	 * @return rezultat
	 */
	private Object arithmeticOperator(final char operator, final Object value) {
		// ako podaci nisu kompatibilni, baci iznimku uz objašnjene
		if (!isCompatible(value)) {
			throw new RuntimeException(
					"Not compatible data. Cannot make arithmetic operation.");
		}
		// odredi povratni tip podatka
		final int returnType = Math
				.max(valueType(this.value), valueType(value));
		// u ovisnosti o operatoru, izvrši operaciju
		switch (operator) {
		case '+':
			this.value = Double.parseDouble(this.value.toString())
					+ Double.parseDouble(value.toString());
			break;
		case '-':
			this.value = Double.parseDouble(this.value.toString())
					- Double.parseDouble(value.toString());
			break;
		case '*':
			this.value = Double.parseDouble(this.value.toString())
					* Double.parseDouble(value.toString());
			break;
		case '/':
			if (Double.parseDouble(value.toString()) == 0) {
				System.out.println("Cannot divide by 0.");
				System.exit(1);
			}
			this.value = Double.parseDouble(this.value.toString())
					/ Double.parseDouble(value.toString());
		}
		// ako je povratni tip int ili null, castaj double u int; odbaci
		// decimalnu točku
		if (((returnType == intValue) || (returnType == nullVaule))
				&& (operator != '/')) {
			final double result = Double.parseDouble(this.value.toString());
			this.value = (int) Math.round(result);
		}
		return this.value;
	}

	/**
	 * Dekrement vrijednost za decvalue iznos.
	 * 
	 * @param decValue
	 */
	public void decrement(final Object decValue) {
		arithmeticOperator('-', decValue);
	}

	/**
	 * Kvocijent vrijednosti za divValue iznos.
	 * 
	 * @param divValue
	 */
	public void divide(final Object divValue) {
		arithmeticOperator('/', divValue);
	}

	/**
	 * Metoda vraća pohranjei objekt.
	 * 
	 * @return pohranjeni objekt
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Inkrement vrijednosti za incValue iznos.
	 * 
	 * @param incValue
	 */
	public void increment(final Object incValue) {
		arithmeticOperator('+', incValue);
	}

	/**
	 * Kompatibilnost podataka za aritmetičku operaciju. Aritmetička operacija
	 * je podržana ukoliko su obe vrijednosti brojevi ili null(0).
	 * 
	 * @param value
	 * @return
	 */
	private boolean isCompatible(final Object value) {
		final int thisValueType = valueType(this.value);
		final int valueType = valueType(value);
		// ako je podatak nepoznatog tipa
		if ((thisValueType == -1) || (valueType == -1)) {
			return false;
		}
		// ako je podatak string
		if ((thisValueType == stringValue) || (valueType == stringValue)) {
			return false;
		}
		return true;
	}

	/**
	 * Umnožak vrijednosti za mulValue iznos.
	 * 
	 * @param mulvalue
	 *            objekt
	 */
	public void multiply(final Object mulValue) {
		arithmeticOperator('*', mulValue);
	}

	/**
	 * Usporedba iznosa vrijednosti s withValue.
	 * 
	 * @param withValue
	 * @return 1 (vrijednost >), -1 (vrijednost <), 0 (vrijednost ==)
	 */
	public int numCompare(final Object withValue) {
		final Object currValue = value;
		final Object difference = arithmeticOperator('-', withValue);
		value = currValue;
		if (Double.parseDouble(difference.toString()) > 0) {
			return 1;
		}
		if (Double.parseDouble(difference.toString()) < 0) {
			return -1;
		}
		return 0;
	}

	/**
	 * Metoda postavlja trenutnu vrijednost.
	 * 
	 * @param value
	 */
	public void setValue(final Object value) {
		this.value = value;
	}

	/**
	 * Analizator stringa. String može sadržavati broj (cjelobrojni ili
	 * decimalni) ili nebroj.
	 * 
	 * @param value
	 * @return 1 (int), 2 (double), 3 (string)
	 */
	private int stringAnalyze(final Object value) {
		// regex za integer
		final String intRegex = "([\\+-]?\\d+)([eE][\\+-]?\\d+)?";
		// regex za double
		final String doubleRegex = "([\\+-]?\\d+(\\.\\d*)?|\\.\\d+)"
				+ "([eE][\\+-]?(\\d+(\\.\\d*)?|\\.\\d+))?";
		// prvo provjeri integer!
		if (value.toString().matches(intRegex)) {
			return intValue;
		} else if (value.toString().matches(doubleRegex)) {
			return doubleValue;
		}
		return stringValue;
	}

	/**
	 * Određivanje tipa objekta.
	 * 
	 * @param value
	 * @return 0 nullvrijednost, 1 int, 2 double, 3 string
	 */
	private int valueType(final Object value) {
		if (value instanceof Integer) {
			return intValue;
		} else if (value instanceof Double) {
			return doubleValue;
		} else if (value instanceof String) {
			// string analiziraj dodatno jer u njemu može biti pohranjen broj
			return stringAnalyze(value);
		} else if (value == null) {
			return nullVaule;
		}
		return -1;
	}
}
