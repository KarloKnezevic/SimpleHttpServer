package hr.fer.zemris.java.custom.scripting.exec;

import hr.fer.zemris.java.custom.collections.ObjectStack;
import hr.fer.zemris.java.custom.scripting.nodes.DocumentNode;
import hr.fer.zemris.java.custom.scripting.nodes.EchoNode;
import hr.fer.zemris.java.custom.scripting.nodes.ForLoopNode;
import hr.fer.zemris.java.custom.scripting.nodes.INodeVisitor;
import hr.fer.zemris.java.custom.scripting.nodes.TextNode;
import hr.fer.zemris.java.custom.scripting.tokens.Token;
import hr.fer.zemris.java.custom.scripting.tokens.TokenConstantDouble;
import hr.fer.zemris.java.custom.scripting.tokens.TokenConstantInteger;
import hr.fer.zemris.java.custom.scripting.tokens.TokenFunction;
import hr.fer.zemris.java.custom.scripting.tokens.TokenOperator;
import hr.fer.zemris.java.custom.scripting.tokens.TokenString;
import hr.fer.zemris.java.custom.scripting.tokens.TokenVariable;
import hr.fer.zemris.java.webserver.RequestContext;

import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Evaluator smart skripte. Za parsiranu skriptu, prolazi kroz izgrađeno
 * sintaksno stablo i izvršava instrukcije.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class SmartScriptEngine {

	/**
	 * Čvor dokumenta.
	 */
	private final DocumentNode documentNode;
	/**
	 * Zahtjev u kojem su zapisani parametri potrebni za izvršavanje, kao i
	 * izlazni tok za rezultate.
	 */
	private final RequestContext requestContext;
	/**
	 * Multistog potreban za izvođenje.
	 */
	private final ObjectMultistack multistack = new ObjectMultistack();

	/**
	 * Sinus funkcija.
	 * 
	 * @param arg
	 *            argument sinus funkcije
	 * @return vrijednost sinus funkcije
	 */
	private double sin(final Object arg) {
		final double argument = Double.valueOf(arg.toString());
		return Math.sin((argument * Math.PI) / 180);
	}

	/**
	 * Formater decimalnog broja.
	 * 
	 * @param value
	 *            vrijednost za formatiranje
	 * @param format
	 *            format
	 * @return formatirani decimalni broj
	 */
	private String decfmt(final Object value, final Object format) {
		final String decformat = format.toString();
		final DecimalFormat dformat = new DecimalFormat(decformat.substring(0,
				decformat.length() - 1));
		return dformat.format(value);
	}

	/**
	 * Oblikvni obrazac Visitor. Objekt anonimnog razreda koji zna obrađivati
	 * čvorove sintaksnog stabla dokumenta. Najvažniji dio evaluatora i u njemu
	 * se izvršava kod dokumenta.
	 */
	private final INodeVisitor visitor = new INodeVisitor() {

		/**
		 * Tekstualni čvor se samo ispisuje na izlaz.
		 */
		@Override
		public void visitTextNode(final TextNode node) {
			try {
				requestContext.write(node.toString());
			} catch (final IOException ignorable) {
			}

		}

		/**
		 * Čvor for petlje. Dok god je početna vrijednost manja ili jednaka
		 * završnoj vrijednosti, obrađuju se čvorovi for petlje.
		 */
		@Override
		public void visitForLoopNode(final ForLoopNode node) {
			final ValueWrapper start = new ValueWrapper(node
					.getStartExpression().asText());
			final ValueWrapper end = new ValueWrapper(node.getEndExpression()
					.asText());
			final ValueWrapper step = new ValueWrapper(node.getStepExpression()
					.asText());
			final String variableName = node.getVariable().asText();

			multistack.push(variableName, start);
			while (multistack.peek(variableName).numCompare(end.getValue()) < 1) {
				for (int i = 0; i < node.numberOfChildren(); i++) {
					node.getChild(i).accept(visitor);
				}
				multistack.peek(variableName).increment(step.getValue());
			}
			multistack.pop(variableName);
		}

		/**
		 * Echo čvor. Čvor može sadržavati konstante, operatore i funkcije. U
		 * ovisnosti o tipu parametra, pomoću razreda ValueWrapper i stoga,
		 * izvršava se određena operacija.
		 */
		@Override
		public void visitEchoNode(final EchoNode node) {
			final Token[] token = node.getTokens();
			final ObjectStack tempObjectStack = new ObjectStack();

			for (int i = 0; i < token.length; i++) {

				/**
				 * double konstanta
				 */
				if (token[i] instanceof TokenConstantDouble) {

					final TokenConstantDouble tokenDouble = (TokenConstantDouble) token[i];
					tempObjectStack.push(tokenDouble.getValue());
				}

				/**
				 * integer konstanta
				 */
				if (token[i] instanceof TokenConstantInteger) {

					final TokenConstantInteger tokenInteger = (TokenConstantInteger) token[i];
					tempObjectStack.push(tokenInteger.getValue());
				}

				/**
				 * string
				 */
				if (token[i] instanceof TokenString) {

					final TokenString tokenString = (TokenString) token[i];
					final int len = tokenString.getValue().length();
					tempObjectStack.push(tokenString.getValue().substring(1,
							len - 1));

				}

				/**
				 * varijabla
				 */
				if (token[i] instanceof TokenVariable) {
					final TokenVariable tokenVariable = (TokenVariable) token[i];
					final Object valueVariable = multistack.peek(
							tokenVariable.getName()).getValue();
					tempObjectStack.push(valueVariable);
				}

				/**
				 * operator
				 */
				if (token[i] instanceof TokenOperator) {

					final Object o1 = tempObjectStack.pop();
					final Object o2 = tempObjectStack.pop();

					final ValueWrapper result = new ValueWrapper(o1);
					final TokenOperator operator = (TokenOperator) token[i];

					if (operator.getSymbol().equals("+")) {
						result.increment(o2);
					} else if (operator.getSymbol().equals("-")) {
						result.decrement(o2);
					} else if (operator.getSymbol().equals("*")) {
						result.multiply(o2);
					} else if (operator.getSymbol().equals("/")) {
						result.divide(o2);
					}

					tempObjectStack.push(result.getValue());

				}

				/**
				 * funkcija
				 */
				if (token[i] instanceof TokenFunction) {

					final TokenFunction tokenFunction = (TokenFunction) token[i];
					final String functionName = tokenFunction.getName()
							.substring(1);

					/**
					 * sinus
					 */
					if (functionName.equals("sin")) {

						final Object x = tempObjectStack.pop();
						final double r = sin(x);
						tempObjectStack.push(r);

						/**
						 * formater decimalnih brojeva
						 */
					} else if (functionName.equals("decfmt")) {

						final Object f = tempObjectStack.pop();
						final Object x = tempObjectStack.pop();
						final String r = decfmt(x, f);
						tempObjectStack.push(r);

						/**
						 * duplikator vrijednosti
						 */
					} else if (functionName.equals("dup")) {

						final Object x = tempObjectStack.pop();
						tempObjectStack.push(x);
						tempObjectStack.push(x);

					} else if (functionName.equals("setMimeType")) {

						final String x = tempObjectStack.pop().toString();
						requestContext.setMimeType(x);

					} else if (functionName.equals("paramGet")) {

						final String dv = tempObjectStack.pop().toString();
						final String name = tempObjectStack.pop().toString();
						final String value = requestContext.getParameter(name);
						tempObjectStack.push(value == null ? dv : value);

					} else if (functionName.equals("pparamGet")) {

						final String dv = tempObjectStack.pop().toString();
						final String name = tempObjectStack.pop().toString();
						final String value = requestContext
								.getPersistentParameter(name);
						tempObjectStack.push(value == null ? dv : value);

					} else if (functionName.equals("pparamSet")) {

						final String name = tempObjectStack.pop().toString();
						final String value = tempObjectStack.pop().toString();
						requestContext.setPersistentParameter(name, value);

					} else if (functionName.equals("pparamDel")) {

						final String name = tempObjectStack.pop().toString();
						requestContext.removePersistentParameter(name);

					} else if (functionName.equals("tparamGet")) {

						final String dv = tempObjectStack.pop().toString();
						final String name = tempObjectStack.pop().toString();
						final String value = requestContext
								.getTemporaryParameter(name);
						tempObjectStack.push(value == null ? dv : value);

					} else if (functionName.equals("tparamSet")) {

						final String name = tempObjectStack.pop().toString();
						final String value = tempObjectStack.pop().toString();
						requestContext.setTemporaryParameter(name, value);

					} else if (functionName.equals("tparamDel")) {

						final String name = tempObjectStack.pop().toString();
						requestContext.removeTemporaryParameter(name);

					}
				}
			}
			/**
			 * sve vrijednosti koje su ostale na stogu su rezultati i kupe se
			 * obrnutim redoslijedom
			 */
			final ObjectStack reverseStack = new ObjectStack();
			while (!tempObjectStack.isEmpty()) {
				reverseStack.push(tempObjectStack.pop());
			}

			while (!reverseStack.isEmpty()) {
				try {
					requestContext.write(reverseStack.pop().toString());
				} catch (final IOException ignorable) {
				}
			}
		}

		/**
		 * Korijenski čvor dokumenta. Svim čvorovima dokumenta proslijeđuje se
		 * visitor koji zna obrađivati čvor.
		 */
		@Override
		public void visitDocumentNode(final DocumentNode node) {

			for (int i = 0; i < node.numberOfChildren(); i++) {
				node.getChild(i).accept(visitor);
			}

		}
	};

	/**
	 * Konstruktor evaluatora.
	 * 
	 * @param documentNode
	 *            čvor dokumenta
	 * @param requestContext
	 *            kontekst u kojem pišu parametri potrebni za obradu
	 */
	public SmartScriptEngine(final DocumentNode documentNode,
			final RequestContext requestContext) {
		this.documentNode = documentNode;
		this.requestContext = requestContext;
	}

	/**
	 * Evaluacija skripte.
	 */
	public void execute() {
		documentNode.accept(visitor);
	}
}
