package hr.fer.zemris.java.webserver.workers;

import hr.fer.zemris.java.webserver.IWebWorker;
import hr.fer.zemris.java.webserver.Loger;
import hr.fer.zemris.java.webserver.RequestContext;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Web aplikacija koja ispisuje prigodnu poruku i broj slova u riječi imena
 * name. Ime se proslijeđuje putem URL-a.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class HelloWorker implements IWebWorker {

	/**
	 * Metoda obrađuje zahtjev na način da ispisuje veličinu proslijeđenog imena
	 * putem URL-a ili prikladnu poruku. Svaki puta se ispisuje trenutno vrijeme
	 * na poslužitelju.
	 */
	@Override
	public void processRequest(final RequestContext context) {
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		final Date now = new Date();
		context.setMimeType("text/html");
		final String name = context.getParameter("name");
		try {
			context.write("<html><body>");
			context.write("<h1>Hello!!!</h1>");
			context.write("<p>Now is: " + sdf.format(now) + "</p>");
			if ((name == null) || name.trim().isEmpty()) {
				context.write("<p>You did not send me your name!</p>");
			} else {
				context.write("<p>Your name has " + name.trim().length()
						+ " letters.</p>");
			}
			context.write("</body></html>");
		} catch (final IOException e) {
			// ako nešto prođe po zlu, zapiši to u log
			Loger.log(Loger.LOG_TYPE.WORKER_ERROR, e.toString());
			e.printStackTrace();
		}
	}
}