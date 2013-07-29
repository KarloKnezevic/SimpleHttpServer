package hr.fer.zemris.java.custom.scripting.parser;

import hr.fer.zemris.java.custom.collections.ArrayBackedIndexedCollection;
import hr.fer.zemris.java.custom.collections.ObjectStack;
import hr.fer.zemris.java.custom.scripting.nodes.DocumentNode;
import hr.fer.zemris.java.custom.scripting.nodes.EchoNode;
import hr.fer.zemris.java.custom.scripting.nodes.ForLoopNode;
import hr.fer.zemris.java.custom.scripting.nodes.Node;
import hr.fer.zemris.java.custom.scripting.nodes.TextNode;
import hr.fer.zemris.java.custom.scripting.tokens.Token;
import hr.fer.zemris.java.custom.scripting.tokens.TokenConstantDouble;
import hr.fer.zemris.java.custom.scripting.tokens.TokenConstantInteger;
import hr.fer.zemris.java.custom.scripting.tokens.TokenFunction;
import hr.fer.zemris.java.custom.scripting.tokens.TokenOperator;
import hr.fer.zemris.java.custom.scripting.tokens.TokenString;
import hr.fer.zemris.java.custom.scripting.tokens.TokenVariable;

/**
 * Obrađivanje ulaznog niza znakova koji predstavlja ulazni dokument. Na temelju
 * tagova i pravila jezika izgrađuje se stablasta struktura koja odgovara
 * strukturi dokumenta.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class SmartScriptParser {

	// korijen
	private DocumentNode document = null;

	/**
	 * Konstruktor. Dovedeni docBody prosljeđuje se na obradu.
	 * 
	 * @param docBody
	 *            tijelo dokumenta
	 */
	public SmartScriptParser(final String docBody) {
		actualParser(docBody);
	}

	/**
	 * Parsiranje dokumenta i stvaranje stabla. Parsiranje obavlja i uz pomoć
	 * objekta tipa ObjectStack. Metoda provjerava i ispravnost položaja zagrada
	 * za tagove.
	 * 
	 * @param doc
	 *            tijelo dokumenta tipa String na temelju kojeg želimo stvoriti
	 *            stablo
	 * @throws SmartScriptParserException
	 *             ukoliko se ne može pristupiti dokumentu ili se u dokumentu
	 *             pojavljuju sintaktičke greške.
	 */
	private void actualParser(final String doc)
			throws SmartScriptParserException {

		if (doc == null) {
			throw new SmartScriptParserException("Cannot read input!");
		}
		document = new DocumentNode();
		final ObjectStack localStack = new ObjectStack();
		localStack.push(document);

		int i = 0;
		int j = 0;
		int pos = 0;

		do {
			final StringBuilder pom1 = new StringBuilder();
			do {
				j = doc.indexOf("[$", pos);
				pos = j + 1;
			} while ((j != -1) && (j != 0) && (doc.charAt(j - 1) == '\\'));
			if (j == -1) {
				j = doc.length();
				if (j == 0) {
					break;
				}
			}

			for (; i < j; i++) {
				if (doc.charAt(i) == '\\') {
					if (doc.charAt(i + 1) == '[') {
						pom1.append('[');
						i++;
					} else if (doc.charAt(i + 1) == '\\') {
						pom1.append('\\');
						i++;
					} else {
						throw new SmartScriptParserException(
								"Invalid escape sequence in text!");
					}
				} else {
					pom1.append(doc.charAt(i));
				}
			}

			if (pom1.length() > 0) {
				final Node anode = (Node) localStack.peek();
				anode.addChildNode(new TextNode(pom1.toString()));
			}
			if (i >= (doc.length() - 1)) {
				break;
			}
			j = doc.indexOf("$]", i + 1);
			if (j == -1) {
				throw new SmartScriptParserException(
						"Error: closing parentheses required!");
			}
			j++;
			String helper = doc.substring(i + 2, j - 1);
			helper = helper.trim();
			String[] tokenz;
			if (helper.charAt(0) == '=') {
				helper = helper.charAt(0) + " " + helper.substring(1) + " ";
				final ArrayBackedIndexedCollection help1 = new ArrayBackedIndexedCollection();
				final StringBuilder bui = new StringBuilder();
				boolean tri = false;
				for (int z = 0; z < helper.length(); z++) {
					if (helper.charAt(z) == '\"') {
						tri = !tri;
						bui.append(helper.charAt(z));
					} else if (!tri && (helper.charAt(z) == ' ')) {
						if (bui.length() > 0) {
							help1.add(bui.toString());
						}
						bui.delete(0, bui.length());
					} else if (!tri && (helper.charAt(z) != ' ')) {
						bui.append(helper.charAt(z));
					} else if (tri) {
						bui.append(helper.charAt(z));
					}

				}

				final String[] trans = new String[help1.size()];
				for (int z = 0; z < help1.size(); z++) {
					trans[z] = (String) help1.get(z);
				}
				tokenz = trans;
			} else {
				tokenz = helper.split(" +");
			}
			switch (tokenz[0]) {

			case "FOR":
				if ((tokenz.length < 4) || (tokenz.length > 5)) {
					throw new SmartScriptParserException(
							"Too few or too many arguments in FOR tag!");
				}
				TokenVariable var;
				final Token[] varf = new Token[3];
				varf[2] = null;
				if (isVariable(tokenz[1])) {
					var = new TokenVariable(tokenz[1]);
				} else {
					throw new SmartScriptParserException(
							"Error in FOR tag: invalid variable name!");
				}
				for (int k = 2; k < tokenz.length; k++) {
					if (isDouble(tokenz[k])) {
						varf[k - 2] = new TokenConstantDouble(
								Double.parseDouble(tokenz[k]));
					} else if (isInt(tokenz[k])) {
						varf[k - 2] = new TokenConstantInteger(
								Integer.parseInt(tokenz[k]));
					} else if (isVariable(tokenz[k])) {
						varf[k - 2] = new TokenVariable(tokenz[k]);
					} else {
						throw new SmartScriptParserException(
								"Unexpected expression type in FOR tag!");
					}
				}
				final ForLoopNode forn = new ForLoopNode(var, varf[0], varf[1],
						varf[2]);
				final Node anode = (Node) localStack.peek();
				anode.addChildNode(forn);
				localStack.push(forn);
				break;

			case "END":
				localStack.pop();
				if (localStack.isEmpty()) {
					throw new SmartScriptParserException(
							"Error: number of END tags is greater then expected!");
				}
				break;

			case "=":
				final Token[] etoken = new Token[tokenz.length - 1];
				for (int k = 1; k < tokenz.length; k++) {
					if (isVariable(tokenz[k])) {
						etoken[k - 1] = new TokenVariable(tokenz[k]);
					} else if (isOperator(tokenz[k])) {
						etoken[k - 1] = new TokenOperator(tokenz[k]);
					} else if (isFunction(tokenz[k])) {
						etoken[k - 1] = new TokenFunction(tokenz[k]);
					} else if (isString(tokenz[k])) {
						etoken[k - 1] = new TokenString(modifyString(tokenz[k]));
					} else if (isInt(tokenz[k])) {
						etoken[k - 1] = new TokenConstantInteger(
								Integer.parseInt(tokenz[k]));
					} else if (isDouble(tokenz[k])) {
						etoken[k - 1] = new TokenConstantDouble(
								Double.parseDouble(tokenz[k]));
					} else {
						throw new SmartScriptParserException(
								"Unkown token in ECHO tag!");
					}
				}
				final EchoNode enod = new EchoNode(etoken);
				final Node bnode = (Node) localStack.peek();
				bnode.addChildNode(enod);
				break;
			default:
				throw new SmartScriptParserException("Unknown tag!");
			}
			j++;
			i = j;
			pos = j;
		} while (i < doc.length());
		if (localStack.size() > 1) {
			throw new SmartScriptParserException(
					"Insufficent number of END tags!");
		}
	}

	/**
	 * Ispitivanje je li moguće određeni podatak tipa String pretvoriti u
	 * cjelobrojni broj.
	 * 
	 * @param s
	 *            tekst za konverziju.
	 * @return true ukoliko je moguća konverzija u cjelobrojni broj, false
	 *         inače.
	 */
	private boolean isInt(final String s) {
		int counter = 0;
		if (s.charAt(0) == '-') {
			counter++;
		}
		for (int i = 0; i < s.length(); i++) {
			if (Character.isDigit(s.charAt(i))) {
				counter++;
			}
		}
		if (counter == s.length()) {
			return true;
		}
		return false;
	}

	/**
	 * Ispitivanje je li moguće određeni podatak tipa String pretvoriti u realni
	 * broj.
	 * 
	 * @param s
	 *            tekst za konverziju.
	 * @return true ukoliko je moguća konverzija u realni broj, false inače.
	 */
	private boolean isDouble(final String s) {
		int counter = 0;
		if (s.charAt(0) == '-') {
			counter++;
		}
		boolean doubflag = false;
		for (int i = 0; i < s.length(); i++) {
			if (Character.isDigit(s.charAt(i))) {
				counter++;
			} else if (s.charAt(i) == '.') {
				doubflag = true;
			}
		}
		if ((counter == (s.length() - 1)) && doubflag) {
			return true;
		}
		return false;
	}

	/**
	 * Ispitivanje može li određeni podatak tipa String biti ime varijable.
	 * 
	 * @param s
	 *            tekst prikladan kao ime varijable
	 * @return true ukoliko tekst može biti ime varijable, false inače.
	 */
	private boolean isVariable(final String s) {
		if (Character.isLetter(s.charAt(0)) == false) {
			return false;
		}
		for (int i = 0; i < s.length(); i++) {
			if ((Character.isLetterOrDigit(s.charAt(i)) || (s.charAt(i) == '_')) == false) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Ispitivanje može li određeni podatak tipa String biti operator (+, -, *,
	 * /)
	 * 
	 * @param s
	 *            tekst za ispitivanje
	 * @return true ukoliko tekst može biti operator, false inače.
	 */
	private boolean isOperator(final String s) {
		if (s.length() > 1) {
			return false;
		}
		switch (s.charAt(0)) {
		case '+':
		case '-':
		case '*':
		case '/':
			return true;
		default:
			return false;
		}
	}

	/**
	 * Ispitivanje može li određeni podatak tipa String biti ime funkcije.
	 * 
	 * @param s
	 *            tekst za ispitivanje
	 * @return true ukoliko tekst može biti ime funkcije, false inače.
	 */
	private boolean isFunction(final String s) {
		if (s.length() <= 1) {
			return false;
		}
		if (s.charAt(0) != '@') {
			return false;
		}
		if (Character.isLetter(s.charAt(1)) == false) {
			return false;
		}
		for (int i = 2; i < s.length(); i++) {
			if ((Character.isLetterOrDigit(s.charAt(i)) || (s.charAt(i) == '_')) == false) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Ispitivanje može li određeni podatak tipa String biti string token.
	 * 
	 * @param s
	 *            tekst za ispitivanje
	 * @return true ukoliko tekst može biti string token, false inače.
	 */
	private boolean isString(final String s) {
		if ((s.charAt(0) == '\"') && (s.charAt(s.length() - 1) == '\"')) {
			return true;
		}
		return false;
	}

	/**
	 * Metoda koja uređuje dobiveni tekst po pravilima definiranima za string
	 * tokene.
	 * 
	 * @param s
	 *            tekst kojeg se treba urediti
	 * @return tekst uređen po pravilima definiranima za string tokene.
	 */
	private String modifyString(final String s) {
		final StringBuilder pom = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			switch (s.charAt(i)) {
			case '\\':
				if (i == (s.length() - 1)) {
					pom.append(s.charAt(i));
					break;
				}
				switch (s.charAt(i + 1)) {
				case 'n':
					pom.append('\n');
					break;
				case 't':
					pom.append('\t');
					break;
				case 'r':
					pom.append('\r');
					break;
				case '\"':
					pom.append('\"');
					break;
				case '\\':
					pom.append('\\');
					break;
				default:
					break;
				}
				i++;
				break;
			default:
				pom.append(s.charAt(i));
			}
		}
		return pom.toString();
	}

	/**
	 * Dohvaćanje dokumenta u obliku stabla.
	 * 
	 * @return parsirana skripta.
	 */
	public DocumentNode getDocumentNode() {
		return document;
	}
}
