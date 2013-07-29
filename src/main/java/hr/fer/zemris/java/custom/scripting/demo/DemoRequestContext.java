package hr.fer.zemris.java.custom.scripting.demo;

import hr.fer.zemris.java.webserver.RequestContext;
import hr.fer.zemris.java.webserver.RequestContext.RCCookie;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Demo konteksta zahtijeva. Probni ispis sadržaja zahtijeva.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class DemoRequestContext {

	/**
	 * Main metoda.
	 * 
	 * @param args
	 *            nema argumenata
	 * @throws IOException
	 *             u slučaju problema sa zapisivanjem u datoteku
	 */
	public static void main(final String[] args) throws IOException {

		// 1. primjer
		demo1("webroot/primjeri/primjer1.txt", "ISO-8859-2");
		// 2. primjer
		demo1("webroot/primjeri/primjer2.txt", "UTF-8");
		// 3. primjer
		demo2("webroot/primjeri/primjer3.txt", "UTF-8");
	}

	/**
	 * Metoda demo2.
	 * 
	 * @param filePath
	 *            staza do datoteke za zapis
	 * @param encoding
	 *            kodna stranica za pisanje
	 * @throws IOException
	 *             u slučaju problema sa zapisivanjem u datoteku
	 */
	private static void demo1(final String filePath, final String encoding)
			throws IOException {
		final OutputStream os = Files.newOutputStream(Paths.get(filePath));
		final RequestContext rc = new RequestContext(os,
				new HashMap<String, String>(), new HashMap<String, String>(),
				new ArrayList<RequestContext.RCCookie>());
		rc.setEncoding(encoding);
		rc.setMimeType("text/plain");
		rc.setStatusCode(205);
		rc.setStatusText("Idemo dalje");
		// Only at this point will header be created and written...
		rc.write("Čevapčići i Šiščevapčići.");
		os.close();
	}

	/**
	 * Metoda demo3.
	 * 
	 * @param filePath
	 *            staza do datoteke za zapis
	 * @param encoding
	 *            kodna stranica za pisanje
	 * @throws IOException
	 *             u slučaju problema sa zapisivanjem u datoteku
	 */
	private static void demo2(final String filePath, final String encoding)
			throws IOException {
		final OutputStream os = Files.newOutputStream(Paths.get(filePath));
		final RequestContext rc = new RequestContext(os,
				new HashMap<String, String>(), new HashMap<String, String>(),
				new ArrayList<RequestContext.RCCookie>());
		rc.setEncoding(encoding);
		rc.setMimeType("text/plain");
		rc.setStatusCode(205);
		rc.setStatusText("Idemo dalje");
		rc.addRCCookie(new RCCookie("korisnik", "perica", 3600, "127.0.0.1",
				"/"));
		rc.addRCCookie(new RCCookie("zgrada", "B4", null, null, "/"));
		// Only at this point will header be created and written...
		rc.write("Čevapčići i Šiščevapčići.");
		os.close();
	}
}