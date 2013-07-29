package hr.fer.zemris.java.webserver.workers;

import hr.fer.zemris.java.webserver.IWebWorker;
import hr.fer.zemris.java.webserver.Loger;
import hr.fer.zemris.java.webserver.RequestContext;

import java.io.IOException;
import java.util.Set;

/**
 * Web aplikacija koja ispisuje parametre iz URL-a.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class EchoParams implements IWebWorker {

	/**
	 * Metoda obrađuje zahtjev na način da ispisuje parametre prislijeđene u
	 * URL-u.
	 */
	@Override
	public void processRequest(final RequestContext context) {
		// dohvati sve parametre
		final Set<String> names = context.getParameterNames();
		try {
			// generiraj html i ispiši parametre u listi
			context.write("<!doctype html><html>" + "<head>"
					+ "<title>SmartHttpServer: EchoParams</title>"
					+ "<meta charset=\"UTF-8\">" + "</head>"
					+ "<body><h1>Proslijeđeni parametri</h1>");
			context.write("<dl>");
			if (names.isEmpty()) {
				context.write("<dt>" + "None parameter in URL proceed."
						+ "</dt><dd>" + "Please, try to add any parameter."
						+ "</dd>");
			}
			for (final String name : names) {
				context.write("<dt>" + name + "</dt><dd>"
						+ context.getParameter(name) + "</dd>");
			}
			context.write("</dl></body></html>");
		} catch (final IOException e) {
			// u log zapiši ako nešto pođe po zlu
			Loger.log(Loger.LOG_TYPE.WORKER_ERROR, e.toString());
			e.printStackTrace();
		}

	}

}
