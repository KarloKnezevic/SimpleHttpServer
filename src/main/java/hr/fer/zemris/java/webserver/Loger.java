package hr.fer.zemris.java.webserver;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Razred za zapis informacija tijekom rada poslužitelja. Razred bilježi
 * nekoliko različitih vrsti zapisa.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class Loger {

	/**
	 * Ime poslužiteljskog dnevnika.
	 */
	private static String logFileName = "log/SmartHttpServerLog.txt";
	/**
	 * Dnevnik.
	 */
	private static PrintWriter log;
	/**
	 * Vrijeme.
	 */
	private static Date time;
	/**
	 * Format zapisa vremena u dnevniku.
	 */
	private static SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	/**
	 * Tipovi zapisa u dnevnik.
	 * 
	 * @author Karlo Knezevic, karlo.knezevic@fer.hr
	 * 
	 */
	public static enum LOG_TYPE {
		INFO, READING_CONTENT_ERROR, REFLECTION_ERROR, CONN_ERROR, WORKER_ERROR, INIT_ERROR, INTERNAL_ERROR, REQUEST
	}

	/**
	 * Metoda za zapisivanje poruke u poslužiteljski dnevnik. Metoda je
	 * sinkronizirajuća, odnosno, garanitra se da će samo jedna dretva vršiti
	 * zapisivane u jednom trenutku.
	 * 
	 * @param type
	 *            tip zapisa
	 * @param msg
	 *            poruka
	 */
	public synchronized static void log(final LOG_TYPE type, final String msg) {
		try {
			log = new PrintWriter(new BufferedWriter(new FileWriter(
					logFileName, true)));
		} catch (final IOException e) {
			e.printStackTrace();
		}

		time = new Date();
		log.println(sdf.format(time) + " " + type);
		log.println("Description:");
		log.println(msg);
		log.println("-------------------------------------------");

		log.close();
	}

}
