package hr.fer.zemris.java.custom.scripting.demo;

import hr.fer.zemris.java.custom.scripting.nodes.DocumentNode;
import hr.fer.zemris.java.custom.scripting.nodes.EchoNode;
import hr.fer.zemris.java.custom.scripting.nodes.ForLoopNode;
import hr.fer.zemris.java.custom.scripting.nodes.INodeVisitor;
import hr.fer.zemris.java.custom.scripting.nodes.TextNode;
import hr.fer.zemris.java.custom.scripting.parser.SmartScriptParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Razred koji, koristeći oblikovni obrazac Visitor, ispisuje sintaksno stablo
 * smart skripte.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class TreeWriter {

	/**
	 * Main metoda.
	 * 
	 * @param args
	 *            putanja do smart skripte
	 * @throws IOException
	 *             u slučaju problema čitanja smart skripte
	 */
	public static void main(final String[] args) throws IOException {

		if ((args.length != 1) || !Files.exists(Paths.get(args[0]))) {
			System.err.println("To many arguments provided or invalid path.");
			System.exit(-1);
		}

		final String docBody = new String(
				Files.readAllBytes(Paths.get(args[0])));
		final SmartScriptParser p = new SmartScriptParser(docBody);
		final WriterVisitor visitor = new WriterVisitor();
		p.getDocumentNode().accept(visitor);

	}

	/**
	 * Razred posjetitelj koji za što raditi s nekim čvorom sintaksnog stabla
	 * smart skripte.
	 * 
	 * @author Karlo Knezevic, karlo.knezevic@fer.hr
	 * 
	 */
	private static class WriterVisitor implements INodeVisitor {

		/**
		 * Tekst čvor se samo ispisuje.
		 */
		@Override
		public void visitTextNode(final TextNode node) {
			System.out.print(node.toString());
		}

		/**
		 * U For čvoru se nad svakim podčvorom poziva metoda koja prihvaća
		 * posjetitelja.
		 */
		@Override
		public void visitForLoopNode(final ForLoopNode node) {
			System.out.print(node.toString());
			for (int i = 0; i < node.numberOfChildren(); i++) {
				node.getChild(i).accept(this);
			}
		}

		/**
		 * Echo čvor se samo ispisuje.
		 */
		@Override
		public void visitEchoNode(final EchoNode node) {
			System.out.print(node.toString());
		}

		/**
		 * Nad djecom korijenskog čvora dokumenta vrši se predaja posjetitelja.
		 */
		@Override
		public void visitDocumentNode(final DocumentNode node) {
			for (int i = 0; i < node.numberOfChildren(); i++) {
				node.getChild(i).accept(this);
			}
		}

	}

}
