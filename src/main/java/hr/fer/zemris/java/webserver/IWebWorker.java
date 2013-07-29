package hr.fer.zemris.java.webserver;

/**
 * Sučelje razreda koji predstavljaju web aplikaciju. Ovo sučelje školski je
 * primjer sučelja HttpServletRequest koje nudi sličnu funkcionalnost.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public interface IWebWorker {

	/**
	 * Kontekst zahtjeva su sve informacije pristigle u zaglavlju paketa prema
	 * poslužitelju. Ovu metodu poziva poslužitelj te automatski priprema
	 * zaglavlje odgovora. Prilikom implementiranja ove metode čitaju se podaci
	 * u zahtjevu, upisuje se podaci zaglavlja te na kraju, upisuju se podaci na
	 * izlazni tok. Preporuča se postaviti prikladan mime tip.
	 * 
	 * @param context
	 *            kontekst zahtjeva
	 */
	public void processRequest(RequestContext context);

}
