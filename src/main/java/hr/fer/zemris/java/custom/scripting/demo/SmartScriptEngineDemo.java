package hr.fer.zemris.java.custom.scripting.demo;

import hr.fer.zemris.java.custom.scripting.exec.SmartScriptEngine;
import hr.fer.zemris.java.custom.scripting.parser.SmartScriptParser;
import hr.fer.zemris.java.webserver.RequestContext;
import hr.fer.zemris.java.webserver.RequestContext.RCCookie;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Demo za evaluator smart skripti. Demo se sastoji od 4 primjera u koji
 * pokazuju pravilan rad evaluatora.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class SmartScriptEngineDemo {

	/**
	 * Main metoda.
	 * 
	 * @param args
	 *            nema argumenata
	 * @throws Exception
	 *             u slučaju problema s čitanjem skripte
	 */
	public static void main(final String[] args) throws Exception {

		/**
		 * OSNOVNI.SMSCR
		 */
		String documentBody = new String(Files.readAllBytes(Paths
				.get("webroot/scripts/osnovni.smscr")));
		Map<String, String> parameters = new HashMap<String, String>();
		Map<String, String> persistentParameters = new HashMap<String, String>();
		List<RCCookie> cookies = new ArrayList<RequestContext.RCCookie>();
		// create engine and execute it
		new SmartScriptEngine(
				new SmartScriptParser(documentBody).getDocumentNode(),
				new RequestContext(System.out, parameters,
						persistentParameters, cookies)).execute();

		System.out.println("=============================================");

		/**
		 * ZBRAJANJE.SMSCR
		 */
		documentBody = new String(Files.readAllBytes(Paths
				.get("webroot/scripts/zbrajanje.smscr")));
		parameters = new HashMap<String, String>();
		persistentParameters = new HashMap<String, String>();
		cookies = new ArrayList<RequestContext.RCCookie>();
		parameters.put("a", "4");
		parameters.put("b", "2");
		// create engine and execute it
		new SmartScriptEngine(
				new SmartScriptParser(documentBody).getDocumentNode(),
				new RequestContext(System.out, parameters,
						persistentParameters, cookies)).execute();

		System.out.println("\n=============================================");

		/**
		 * BROJPOZIVA.SMSCR
		 */
		documentBody = new String(Files.readAllBytes(Paths
				.get("webroot/scripts/brojPoziva.smscr")));
		parameters = new HashMap<String, String>();
		persistentParameters = new HashMap<String, String>();
		cookies = new ArrayList<RequestContext.RCCookie>();
		persistentParameters.put("brojPoziva", "3");
		final RequestContext rc = new RequestContext(System.out, parameters,
				persistentParameters, cookies);
		// create engine and execute it
		new SmartScriptEngine(
				new SmartScriptParser(documentBody).getDocumentNode(), rc)
				.execute();
		System.out.print("\nVrijednost u mapi: "
				+ rc.getPersistentParameter("brojPoziva"));

		System.out.println("\n=============================================");

		/**
		 * FIBONACCI.SMSCR
		 */
		documentBody = new String(Files.readAllBytes(Paths
				.get("webroot/scripts/fibonacci.smscr")));
		parameters = new HashMap<String, String>();
		persistentParameters = new HashMap<String, String>();
		cookies = new ArrayList<RequestContext.RCCookie>();
		// create engine and execute it
		new SmartScriptEngine(
				new SmartScriptParser(documentBody).getDocumentNode(),
				new RequestContext(System.out, parameters,
						persistentParameters, cookies)).execute();
	}
}
