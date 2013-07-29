package hr.fer.zemris.java.custom.scripting.exec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Razred implemenitra kolekciju sličnu set. Za jedan ključ postoji više
 * vrijednosti koje se pohranjuju na stog.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class ObjectMultistack {

	/**
	 * Unutarnji razred. Ostvaruje skup stogova.
	 * 
	 * @author Karlo Knezevic, karlo.knezevic@fer.hr
	 * 
	 */
	private class MultistackEntry {

		// spremište stogova; stog realiziran listom; za svaki ključ postoji
		// jedna lista
		private final Map<String, List<ValueWrapper>> multistacks;

		/**
		 * Konstruktor.
		 */
		public MultistackEntry() {
			multistacks = new HashMap<String, List<ValueWrapper>>();
		}

		/**
		 * S liste ključa vraća vrijednost elementa pohranjenog na i-tom mjestu.
		 * 
		 * @param key
		 *            ključ
		 * @param index
		 *            i
		 * @return
		 */
		private ValueWrapper getElement(final String key, final int index) {
			return multistacks.get(key).get(index);
		}

		/**
		 * Je li lista ključa prazan?
		 * 
		 * @return
		 */
		private boolean isEmpty() {
			return multistacks.isEmpty();
		}

		/**
		 * Ukoliko ključ postoji, dodaje se element u listu. Ako ključ ne
		 * postoji, stvara se nova lista i dodaje se element.
		 * 
		 * @param key
		 *            ključ
		 * @param value
		 *            vrijednost
		 */
		private void put(final String key, final ValueWrapper value) {
			if (multistacks.get(key) == null) {
				final List<ValueWrapper> stack = new ArrayList<ValueWrapper>();
				multistacks.put(key, stack);
			}
			multistacks.get(key).add(value);
		}

		/**
		 * Iz liste ključa miče element na mjestu i.
		 * 
		 * @param key
		 *            ključ
		 * @param index
		 *            i
		 */
		private void remove(final String key, final int index) {
			multistacks.get(key).remove(index);
		}

		/**
		 * Veličina liste ključa.
		 * 
		 * @param key
		 *            ključ
		 * @return
		 */
		private int stackSize(final String key) {
			return multistacks.get(key).size();
		}
	}

	// skup stogova
	private final MultistackEntry stack;

	/**
	 * Konstruktor. Inicijalizacija stogova.
	 */
	public ObjectMultistack() {
		stack = new MultistackEntry();
	}

	/**
	 * Je li stog ključa prazan?
	 * 
	 * @param name
	 *            ključ
	 * @return true ako je prazan, false inače
	 */
	public boolean isEmpty(final String name) {
		return stack.isEmpty();
	}

	/**
	 * Čitanje zadnjeg elementa sa stoga za zadani ključ.
	 * 
	 * @param name
	 *            ključ
	 * @return vraća vrijednost elementa s vrha stoga
	 */
	public ValueWrapper peek(final String name) {
		final int stackSize = stack.stackSize(name);
		ValueWrapper value = null;
		try {
			value = stack.getElement(name, stackSize - 1);
		} catch (final Exception e) {
			System.out.println("Cannot peek empty stack.");
			System.exit(1);
		}
		return value;
	}

	/**
	 * Uzimanje zadnjeg elementa sa stoga za zadani ključ.
	 * 
	 * @param name
	 *            ključ
	 * @return element s vrha stoga i briše element sa stoga
	 */
	public ValueWrapper pop(final String name) {
		final int stackSize = stack.stackSize(name);
		ValueWrapper value = null;
		try {
			value = stack.getElement(name, stackSize - 1);
			stack.remove(name, stackSize - 1);
		} catch (final Exception e) {
			System.out.println("Cannot pop empty stack.");
			System.exit(1);
		}
		return value;
	}

	/**
	 * Dodavanje vrijednosti u skup.
	 * 
	 * @param name
	 *            -> ključ
	 * @param valueWrapper
	 *            -> vrijednost
	 */
	public void push(final String name, final ValueWrapper valueWrapper) {
		stack.put(name, valueWrapper);
	}
}
