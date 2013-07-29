package hr.fer.zemris.java.webserver;

import hr.fer.zemris.java.custom.scripting.exec.SmartScriptEngine;
import hr.fer.zemris.java.custom.scripting.parser.SmartScriptParser;
import hr.fer.zemris.java.webserver.RequestContext.RCCookie;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SMART HTTP SERVER Poslužitelj koristi HTTP aplikacijski protokol za
 * komunikaciju s klijentima. Podržana isključivo GET metoda i verzije HTTP 1.0
 * i HTTP 1.1. Poslužitelj svakom novom konekcijom stvara sjednicu koja traje
 * određeno definirano vrijeme. Potrebni direktoriji: webroot, log i config.
 * Webroot direktorij matični je dirketorij za datoteke koje se poslužuju
 * korisnicima, log direktorij u kojem se nalazi poslužiteljski dnevnik, a
 * config direktorij s konfiguracijskim datotekama za poslužitelj. Prilikom
 * pokretanja poslužitelja, nužno je navesti putanju do konfiguracijske datoteke
 * poslužitelja. Poslužitelj osim statičkog posluživanja datoteka ima mogućnost
 * dinamičkog generiranja sadržaja (obrada smart skripti i web aplikacije).
 * Početna stranica poslužitelja je index.html.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class SmartHttpServer {

	/**
	 * IP adresa poslužitelja
	 */
	private String address;
	/**
	 * Vrata na kojima poslužitelj sluša zahtjeve.
	 */
	private int port;
	/**
	 * Broj korisničkih dretvi poslužitelja.
	 */
	private int workerThreads;
	/**
	 * Vremensko ograničenje sjednice. Odnosi se na vrijeme provedeno bez
	 * komunikacije.
	 */
	private int sessionTimeout;
	/**
	 * Mapa mime tipova odgovora.
	 */
	private final Map<String, String> mimeTypes = new HashMap<String, String>();
	/**
	 * Poslužiteljska dretva.
	 */
	private final ServerThread serverThread;
	/**
	 * Bazen dretvi: služi za stvaranje korisničkih dretvi.
	 */
	private ExecutorService threadPool;
	/**
	 * Staza do korijenskog direktorija poslužitelja obzirom na korisnike.
	 */
	private Path documentRoot;
	/**
	 * Zastavica treba li ugasiti poslužitelj.
	 */
	private volatile boolean stop = false;
	/**
	 * Mapa poslužiteljskih web aplikacija.
	 */
	private final Map<String, IWebWorker> workersMap = new HashMap<String, IWebWorker>();
	/**
	 * Mapa sjednica. Format: SID<->SessionMapEntry.
	 */
	private final Map<String, SessionMapEntry> sessions = new HashMap<String, SessionMapEntry>();
	/**
	 * Generator pseudoslučajnih brojeva.
	 */
	private final Random sessionRandom = new Random();
	/**
	 * Periodičan pokretač dretve za čišćenje nevaljanih (starih) sjednica.
	 */
	Timer periodicalJobScheduler = new Timer();

	/**
	 * Konstruktor SmartHttpServera. Očekuje se ime (putanja) konfuguracijske
	 * datoteke.
	 * 
	 * @param configFileName
	 *            konfiguracijska datoteka
	 */
	public SmartHttpServer(final String configFileName) {
		loadProperties(configFileName);
		serverThread = new ServerThread();
	}

	/**
	 * Učitavanje parametara iz konfiguracijske datoteke.
	 * 
	 * @param configFileName
	 *            konfiguracijska datoteka
	 */
	private void loadProperties(final String configFileName) {
		final Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(configFileName));
			address = properties.getProperty("server.address");
			port = Integer.parseInt(properties.getProperty("server.port"));
			workerThreads = Integer.parseInt(properties
					.getProperty("server.workerThreads"));
			documentRoot = Paths.get(properties
					.getProperty("server.documentRoot"));
			loadMimeTypes(properties.getProperty("server.mimeConfig"));
			sessionTimeout = Integer.parseInt(properties
					.getProperty("session.timeout"));
			loadWebWorkers(properties.getProperty("server.workers"));
		} catch (final Exception e) {
			Loger.log(Loger.LOG_TYPE.INIT_ERROR, e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * Učitavanje razreda web aplikacija. Datoteka služi da bi se ostvarilo
	 * mapiranje web aplikacije na određenu stazu u URI-ju.
	 * 
	 * @param workerFileName
	 *            datoteka mapiranja web aplikacija
	 */
	private void loadWebWorkers(final String workerFileName) {
		Scanner scanner = null;
		try {
			scanner = new Scanner(new File(workerFileName));
		} catch (final FileNotFoundException e) {
			Loger.log(Loger.LOG_TYPE.INIT_ERROR, e.toString());
			e.printStackTrace();
		}

		String line;
		while (scanner.hasNext()) {
			line = scanner.nextLine();

			if (line.startsWith("#") || line.isEmpty()) {
				continue;
			}

			final String[] pathFqcn = line.split("\\s*=\\s*");
			final String path = pathFqcn[0];
			final String fqcn = pathFqcn[1];

			if (workersMap.containsKey(path)) {
				Loger.log(Loger.LOG_TYPE.INTERNAL_ERROR,
						"Multiple mappings in " + workerFileName);
				throw new IllegalArgumentException(
						"Cannot have multiple mappings on same path.");
			}

			Class<?> referenceToClass;
			try {
				// dinamički, refleksijom, se učitava razred
				referenceToClass = this.getClass().getClassLoader()
						.loadClass(fqcn);
				final Object newObject = referenceToClass.newInstance();
				final IWebWorker iww = (IWebWorker) newObject;
				workersMap.put(path, iww);
			} catch (InstantiationException | IllegalAccessException
					| ClassNotFoundException e) {
				Loger.log(Loger.LOG_TYPE.REFLECTION_ERROR, e.toString());
				e.printStackTrace();
			}

		}

	}

	/**
	 * Učitavanje mime tipova.
	 * 
	 * @param mimeFileName
	 *            datoteka mime tipova
	 */
	private void loadMimeTypes(final String mimeFileName) {
		final Properties mimeProperties = new Properties();
		try {
			mimeProperties.load(new FileInputStream(mimeFileName));
			final Enumeration<Object> mimeKeys = mimeProperties.keys();
			while (mimeKeys.hasMoreElements()) {
				final String key = (String) mimeKeys.nextElement();
				mimeTypes.put(key, (String) mimeProperties.get(key));
			}

		} catch (final Exception e) {
			Loger.log(Loger.LOG_TYPE.INIT_ERROR, e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * Pokretanje poslužitelja: pokreće se poslužiteljska dretva i bazen dretvi
	 * unutar kojeg će se stvarati klijentske dretve. Stvara se i dretva
	 * kontrolera mrtvih sjednica koja se periodički pokreće svakih 5 min te
	 * uklanja zastarjele sjednice.
	 */
	protected synchronized void start() {
		if (!serverThread.isAlive()) {
			stop = false;
			serverThread.start();
			threadPool = Executors.newFixedThreadPool(workerThreads);
			// pozivanje svakih 5 min
			periodicalJobScheduler.schedule(new ExpiredSessionCollector(),
					300000, 300000);

			Loger.log(Loger.LOG_TYPE.INFO, "Server start.");

			System.out.println("SmartHttpServer started. Hello.");
			System.out.println("Server running ok? " + serverThread.isAlive());
		}
	}

	/**
	 * Gašenje poslužitelja: gasi se bazen dretvi, kontroler mrtvih sjednica i
	 * glavna pslužiteljska dretva.
	 */
	protected synchronized void stop() {
		stop = true;
		threadPool.shutdown();
		periodicalJobScheduler.cancel();

		try {
			serverThread.join();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}

		Loger.log(Loger.LOG_TYPE.INFO, "Server stop.");
		System.out.println("SmartHttpServer stopped. Bye.");
	}

	/**
	 * Dretva kontroler isteklih (mrtvih) sjednica. Dretva prolazi kros mapu
	 * sjednica i briše sve sjednice koje su zastarjele.
	 * 
	 * @author Karlo Knezevic, karlo.knezevic@fer.hr
	 * 
	 */
	private class ExpiredSessionCollector extends TimerTask {

		/**
		 * Metoda prolazi korz mapu sjednica i briše sve zastarjele sjednice.
		 */
		@Override
		public void run() {
			for (final Entry<String, SessionMapEntry> entry : sessions
					.entrySet()) {
				// ako je sjednica zastarjela, briši ju
				if (entry.getValue().validUntil < new Date().getTime()) {
					sessions.remove(entry.getKey());
				}
			}
		}
	}

	/**
	 * Poslužiteljska dretva. Prilikom pokretanja otvara se priključnica na
	 * kojoj dretva sluša pristigle zahtjeve. Pristigli zahtjevi slušaju se u
	 * beskonačnoj petlji. Petlja se izvede minimalno jednom u svake 3 sekunde.
	 * Svakim pristiglim zahtjevom stvara se klijentska dretva u kojoj se
	 * opslužuje klijent.
	 * 
	 * @author Karlo Knezevic, karlo.knezevic@fer.hr
	 * 
	 */
	protected class ServerThread extends Thread {

		/**
		 * Stvara se priključnica i dretva u beskonačnoj petlji čeka zahtjeve.
		 * Ukoliko ne pristigne ni jedan zahtjev u 3 sekunde, osvježava se
		 * osluškivanje.
		 */
		@Override
		public void run() {
			ServerSocket ssocket = null;
			try {
				// stvaranje priključnice i povezivanje adrese i porta
				ssocket = new ServerSocket(port);
				ssocket.bind(new InetSocketAddress(address, port));
			} catch (final IOException ignorable) {
			}

			Socket csocket = null;
			while (!stop) {
				try {
					// socket blokiran 3 sekunde
					ssocket.setSoTimeout(3000);
					csocket = ssocket.accept();
				} catch (final IOException e) {
					// iznimka se hvata kada istekne sekunda bez konekcije
					continue;
				}

				// stvori novog radnika i predaj ga bazenu dretvi
				final ClientWorker cw = new ClientWorker(csocket);
				threadPool.submit(cw);
			}
		}

	}

	/**
	 * Razred sjedničke mape. Ključ svake sjednice je SID. Svaki SID sadrži
	 * objekt u kojem su spremljene informacije o sjednici. Informacije o
	 * sjednici: vrijeme valjanosti i mapa korisničkih informacija. Mapa
	 * korisničkih informacija služi za pohranu informacija korisnika ili
	 * pooslužitelja za korisnika (poput privilegija i sl.).
	 * 
	 * @author Karlo Knezevic, karlo.knezevic@fer.hr
	 * 
	 */
	private static class SessionMapEntry {
		/**
		 * Sjednički ID.
		 */
		@SuppressWarnings("unused")
		String sid;
		/**
		 * Vrijeme valjanosti.
		 */
		long validUntil;
		/**
		 * Podatkovna mapa sjednice.
		 */
		Map<String, String> map;
	}

	/**
	 * Klijentska dretva u kojoj se obrađuje klijentski zahtjev. Dretva prima
	 * priključnicu prema klijentu i stvara kontekstni zahtjev kojim se vraća
	 * odgovor. Kontekstni zahtijev prosljeđuje se iz ovog razreda svi web
	 * aplikacijama pohranjenim na ovom poslužitelju. Dretva završava
	 * zatvaranjem klijentske priključnice.
	 * 
	 * @author Karlo Knezevic, karlo.knezevic@fer.hr
	 * 
	 */
	private class ClientWorker implements Runnable {

		/**
		 * Klijentska priključnica.
		 */
		private final Socket csocket;
		/**
		 * Ulazni tok podataka od klijenta.
		 */
		private PushbackInputStream istream;
		/**
		 * Izlazni tok podataka prema klijentu.
		 */
		private OutputStream ostream;
		/**
		 * Verzija HTTP protokola.
		 */
		private String version;
		/**
		 * HTTP metoda: trenutno podržana isključivo GET.
		 */
		private String method;
		/**
		 * Parametri iz URL-a.
		 */
		private final Map<String, String> params = new HashMap<String, String>();
		/**
		 * Parametri poslani putem kolačića ili koji će biti poslani putem
		 * kolačića.
		 */
		private Map<String, String> permParams = new HashMap<String, String>();
		/**
		 * Kolačići za klijenta.
		 */
		private final List<RCCookie> outputCookies = new ArrayList<RequestContext.RCCookie>();
		/**
		 * Sjednički ID.
		 */
		private String SID;
		/**
		 * Kontekstni zahtijev.
		 */
		private RequestContext requestContext;

		/**
		 * Konstruktor. Klijentska dretva prima klijentsku priključnicu kojom će
		 * komunicirati.
		 * 
		 * @param csocket
		 *            klijentska priključnica
		 */
		public ClientWorker(final Socket csocket) {
			this.csocket = csocket;
		}

		/**
		 * GLAVNA METODA KOJA POSLUŽUJE KLIJENTSKE ZAHTJEVE NA POSLUŽITELJU. Za
		 * svaki zahtjev provjerava se zaglavlje i čitaju podaci iz zaglavlja.
		 * Na temelju pročitanih podataka vrši se akcija pokretanja web
		 * aplikacije, izvršavanje smart skripte ili slanja podataka iz datoteka
		 * webroot direktorija. U početku se ustanovljava postoji li sjednica
		 * između poslužitelja i klijenta i ako ne postoji, ista se stvara.
		 */
		@Override
		public void run() {
			try {

				/**
				 * STVORI KONTEKSTNI ZAHTJEV
				 */
				istream = new PushbackInputStream(csocket.getInputStream());
				ostream = csocket.getOutputStream();
				requestContext = new RequestContext(ostream, params,
						permParams, outputCookies);

				/**
				 * DOHVATI LINIJE ZAGLAVLJA
				 */
				final List<String> request = readRequest();

				/**
				 * ZAPIŠI ZAHTIJEV U POSLUŽITELJSKI DNEVNIK
				 */
				Loger.log(Loger.LOG_TYPE.REQUEST, "Address: "
						+ csocket.getInetAddress().toString() + "\r\n"
						+ request.toString());

				/**
				 * AKO JE ZAGLAVLJE PRAZNO, ERROR 400
				 */
				if (request.isEmpty()) {
					setErrorStatus(400);
					return;
				}

				/**
				 * ANALIZA PRVE LINIJE ZAGLAVLJA:
				 * METODA_PUTANJA_VERZIJAPROTOKOLA
				 */
				final String firstLine = request.get(0);
				final String[] methodPathVersion = firstLine.split("\\s+");
				method = methodPathVersion[0];
				String requestedPath = methodPathVersion[1];
				version = methodPathVersion[2];

				/**
				 * AKO PRVA LINIJA NE ODGOVARA SPECIFIKACIJI POSLUŽITELJA, ERROR
				 * 400
				 */
				// format prve linije: METODA\sPUTANJA\sVERZIJApROTOKOLA
				if (!(method.equals("GET") && (version.equals("HTTP/1.0") || version
						.equals("HTTP/1.1")))) {
					setErrorStatus(400);
					return;
				}

				/**
				 * PROVJERA SJEDNICE Ako sjednica postoji, osvježava se vrijeme
				 * trajanja sjednice, u suprotnom se stvara nova sjednica.
				 */
				// *************provjeri sjednicu*************
				sessionCheck(request);
				// *************provjeri sjednicu*************

				/**
				 * AKO JE PUTANJA "/", KLIJENTU SE POSLUŽUJE STRANICA index.html
				 */
				if (requestedPath.equals("/")) {
					requestedPath += "index.html";
				}

				/**
				 * AKO SU PUTEM URI-ja POSLANI PARAMETRI, PARSIRA SE PUTANJA U
				 * 1. LINIJI U slučaju neispravnog formata parametara, ERROR 400
				 */
				final String[] pathArgs = requestedPath.split("\\?");
				final String path = pathArgs[0].substring(1);
				final Path resourcePath = documentRoot.resolve(path)
						.normalize();
				if (pathArgs.length > 1) {
					final String paramString = pathArgs[1];
					// provjeri ispravnost name=value
					if (!parseParameters(paramString)) {
						return;
					}
				}

				/**
				 * PRISTUP: convention-over-approach WEB APLIKACIJE MAPIRAJU SE
				 * VIRTUALNI DIREKTORIJ KOJI SPECIFICIRA POSLUŽITELJ VIRTUALNI
				 * DIREKTORIJ: /ext U slučaju nepostojane web aplikacije, ERROR
				 * 404
				 */
				// pokretanje WebWorker objekta bez bez obzira na mapiranost
				final String URLConvOverConf = "ext/";
				final String webWorkerPackage = "hr.fer.zemris.java.webserver.workers.";
				if (path.startsWith(URLConvOverConf)) {
					final String className = webWorkerPackage
							+ path.substring(URLConvOverConf.length());

					final Object newObject = getObject(className);
					if (newObject == null) {
						setErrorStatus(404);
					} else {
						final IWebWorker iww = (IWebWorker) newObject;
						iww.processRequest(requestContext);
					}
					return;
				}

				/**
				 * AKO JE WEB APLIKACIJA MAPIRANA U KONFIGURACIJSKOJ DATOTECI,
				 * POKREĆE SE
				 */
				// pokretanje WebWorker objekta ukoliko je mapiran
				if (workersMap.containsKey("/" + path)) {
					workersMap.get("/" + path).processRequest(requestContext);
					return;
				}

				/**
				 * U SLUČAJU IZLASKA IZVAN WEBROOT DIRKETORIJA, ERROR 403
				 */
				// ako se želi izaći izvan javnog direktorija
				if (!resourcePath.startsWith(documentRoot)) {
					setErrorStatus(403);
					return;
				}

				/**
				 * AKO TRAŽENI RESURS NE POSTOJI, NIJE OBIČNA DATOTEKA (npr.
				 * link) ILI NIJE ČITLJIVA, ERROR 404
				 */
				if (!(Files.exists(resourcePath)
						&& Files.isRegularFile(resourcePath) && Files
							.isReadable(resourcePath))) {
					setErrorStatus(404);
					return;
				}

				/**
				 * DOHVAĆANJE EKSTENZIJE RESURSA TE ODREĐIVANJE MIME TIPA U
				 * OVISNOSTI O EKSTENZIJI. MIME TIPOVI MAPIRANI SU NA EKSTENZIJE
				 * U KONFIGURACIJSKOJ DATOTECI.
				 */
				final String extension = getResourceExtension(resourcePath);
				String mime = null;
				mime = mimeTypes.get(extension);

				/**
				 * GENERIRANJE ODGOVORA PREMA KLIJENTU.
				 */
				// *************generiranje odgovora*************
				generateResponce(extension, mime, resourcePath);
				// *************generiranje odgovora*************

			} catch (final Exception e) {
				e.printStackTrace();
			} finally {
				try {
					// zatvaranjem priključnice završava komunikacija
					// klijent-poslužitelj
					csocket.close();
				} catch (final IOException e) {
					// upiši u dnevnik ako nešto prođe po zlu
					Loger.log(Loger.LOG_TYPE.CONN_ERROR, e.toString());
					e.printStackTrace();
				}
			}
		}

		/**
		 * Provjera postoji li klijentska sjednica na poslužitelju. Ako sjednica
		 * postoji, osvježava se vrijeme valjanosti sjednice, a ako ne postoji,
		 * stvara se nova sjednica. U slučaju resetiranja poslužitelja, stvaraju
		 * se nove sjednice.
		 * 
		 * @param request
		 *            kontekstni zahtijev
		 */
		private synchronized void sessionCheck(final List<String> request) {
			final String sidCandidate = findCookie(request);
			final String hostAddress = getHostAddress(request.get(1));
			// ako sjednica nije uspostavljena
			if (sidCandidate == null) {
				createNewSession(hostAddress);
			} else {
				// ako sjednički ID postoji
				final SessionMapEntry session = sessions.get(sidCandidate);
				// ako je poslužitelj resetiran, a stigne kolačić sa SIDom,
				// sjednice su resetirane pa treba napraviti novu sjednicu
				if (session == null) {
					createNewSession(hostAddress);
				} else if (session.validUntil < new Date().getTime()) {
					// ako je sjednica istekla
					sessions.remove(sidCandidate);
					createNewSession(hostAddress);
				} else {
					// osvježi novo vrijeme do završetka sjednice
					SID = sidCandidate;
					session.validUntil = new Date().getTime()
							+ (sessionTimeout * 1000);
				}
			}

			// ključno mapiranje za slanje podataka prema klijentu
			permParams = sessions.get(SID).map;
			requestContext.setPersistentParameters(permParams);

		}

		/**
		 * Dohvaćanje poslužiteljske IP adrese iz klijentnog pogleda.
		 * Poslužiteljska IP adresa zapisana je u 2. liniji Host:. Ova metoda
		 * važna je za pravilnu detekciju sjedničkog zahtijeva.
		 * 
		 * @param hostAddressLine
		 *            2. linija klijentnosg zahtjeva
		 * @return poslužiteljska IP adresa iz pogleda klijenta
		 */
		private synchronized String getHostAddress(final String hostAddressLine) {
			final String[] parts = hostAddressLine.split("\\s*:\\s*");
			return parts[1];
		}

		/**
		 * Stvaranje nove sjednice. Generira se 40-znamenkasti ID sjednice koji
		 * se sastoji od brojeva, malih slova i velikih slova. Vrijeme trajanje
		 * sjednice definirano je u konfiguracijskoj datoteci. Max-age postavlja
		 * se na 0 da se sjednica zatvori gašenjem klijenta.
		 * 
		 * @param hostAddress
		 *            IP adresa poslužitelja iz klijentskog pogleda
		 */
		private synchronized void createNewSession(final String hostAddress) {
			createUniqueSID(40);
			final SessionMapEntry session = new SessionMapEntry();
			session.sid = SID;
			session.validUntil = new Date().getTime() + (sessionTimeout * 1000);
			// stvaranje mape prikladne za pristup dretve
			session.map = new ConcurrentHashMap<String, String>();
			sessions.put(SID, session);
			// stvoren je sjednički kolačić (max-age = null)
			outputCookies.add(new RCCookie("sid", SID, null, hostAddress, "/"));
		}

		/**
		 * Stravanje 40-znakovnog identifikatora koji služi za identifikator
		 * sjednice. Korišteni znakovi su brojke, mala slova i velika slova.
		 * Svaki 4. znak je broj.
		 * 
		 * @param SIDLength
		 *            veličina identifikatora
		 */
		private synchronized void createUniqueSID(final int SIDLength) {
			final StringBuilder sb = new StringBuilder(SIDLength);
			for (int i = 0; i < SIDLength; i++) {
				if ((i % 4) == 0) {
					sb.append((char) (sessionRandom.nextInt(10) + '0'));
				} else {
					sb.append((char) (sessionRandom.nextInt(26) + (sessionRandom
							.nextBoolean() ? 'a' : 'A')));
				}
			}
			SID = sb.toString();
		}

		/**
		 * Pronalazak SID-a u kolačiću pristiglom u klijentkom zahtjevu.
		 * 
		 * @param request
		 *            linije zaglavlja
		 * @return SID
		 */
		private synchronized String findCookie(final List<String> request) {

			String sidCandidate = null;
			for (final String line : request) {
				if (!line.startsWith("Cookie:")) {
					continue;
				}
				final String namesAndValues = line.substring("Cookie: "
						.length());
				final String[] cookies = namesAndValues.split(";");
				for (final String cookie : cookies) {
					if (cookie.trim().startsWith("sid")) {
						sidCandidate = cookie.split("=")[1];
						sidCandidate = sidCandidate.substring(1,
								sidCandidate.length() - 1);
					}
				}
				break;
			}
			return sidCandidate;
		}

		/**
		 * Dinamičko učitavanje razreda korištenjem refleksije. Na ovakav način
		 * učitavaju se web aplikacije pohranjene na poslužitelju.
		 * 
		 * @param className
		 *            ime razreda web aplikacije
		 * @return primjerak web aplikacije
		 */
		private synchronized Object getObject(final String className) {
			Class<?> referenceToClass = null;
			try {
				referenceToClass = this.getClass().getClassLoader()
						.loadClass(className);
				final Object newObject = referenceToClass.newInstance();
				return newObject;
			} catch (final Exception e) {
				Loger.log(Loger.LOG_TYPE.REFLECTION_ERROR, e.toString());
				e.printStackTrace();
			}
			return null;
		}

		/**
		 * Generiranje odgovora u slučaju zahtijeva resursa. Mime tip i
		 * ekstenzija resursa određuju tip odgovora.
		 * 
		 * @param extension
		 *            ekstenzija traženog resursa
		 * @param mime
		 *            mime tip resursa
		 * @param resourcePath
		 *            putanja do resursa
		 */
		private synchronized void generateResponce(final String extension,
				final String mime, final Path resourcePath) {

			if (extension.equals("smscr")) {
				// ako je smart skripta
				generateSmscrResponce(resourcePath);
			} else if (mime == null) {
				// ako mime tip nije određen, tada se šalju sirovi podaci
				generateDefaultMimeResponce(resourcePath);
			} else {
				// generiranje odgovora određenog mime tipa
				generateMimeResponce(mime, resourcePath);
			}

		}

		/**
		 * Pokretanje smart skripte.
		 * 
		 * @param resourcePath
		 *            putanja do smart skripte
		 */
		private void generateSmscrResponce(final Path resourcePath) {
			String documentBody = null;
			try {
				// učitavanje skripte
				documentBody = new String(Files.readAllBytes(resourcePath));
			} catch (final IOException e) {
				Loger.log(Loger.LOG_TYPE.READING_CONTENT_ERROR, e.toString());
				e.printStackTrace();
			}
			// izvođenje skripte
			new SmartScriptEngine(
					new SmartScriptParser(documentBody).getDocumentNode(),
					requestContext).execute();
		}

		/**
		 * Čitanje resursa određenog mime tipa.
		 * 
		 * @param mime
		 *            mime tip resursa
		 * @param resourcePath
		 *            putanja do resursa
		 */
		private void generateMimeResponce(final String mime,
				final Path resourcePath) {
			requestContext.setMimeType(mime);
			try {
				// postavljanje veličine resursa
				requestContext.setContentLength(Files.size(resourcePath));
			} catch (final IOException e) {
				e.printStackTrace();
			}

			FileInputStream input = null;
			try {
				input = new FileInputStream(resourcePath.toFile());
			} catch (final FileNotFoundException e) {
				Loger.log(Loger.LOG_TYPE.READING_CONTENT_ERROR, e.toString());
				e.printStackTrace();
			}

			// čitanje datoteke po 1kB
			final byte[] buf = new byte[1024];
			while (true) {
				int len;
				try {
					len = input.read(buf);
					if (len == -1) {
						break;
					}
					final byte[] buff = new byte[len];
					for (int i = 0; i < len; i++) {
						buff[i] = buf[i];
					}
					requestContext.write(buff);
				} catch (final IOException e) {
					Loger.log(Loger.LOG_TYPE.READING_CONTENT_ERROR,
							e.toString());
					e.printStackTrace();
					try {
						input.close();
					} catch (final IOException e1) {
						Loger.log(Loger.LOG_TYPE.CONN_ERROR, e.toString());
						e1.printStackTrace();
					}
				}
			}

		}

		/**
		 * Čitanje resursa neodređenog mime tipa. Slanje sirovih podataka.
		 * 
		 * @param resourcePath
		 *            putanja do resursa
		 */
		private void generateDefaultMimeResponce(final Path resourcePath) {
			final byte[] buf = new byte[1024];
			FileInputStream input = null;
			try {
				input = new FileInputStream(resourcePath.toFile());
			} catch (final FileNotFoundException ignorable) {
			}
			while (true) {
				int len;
				try {
					len = input.read(buf);
					if (len == -1) {
						break;
					}
					ostream.write(buf, 0, len);
				} catch (final IOException e) {
					Loger.log(Loger.LOG_TYPE.READING_CONTENT_ERROR,
							e.toString());
					e.printStackTrace();
					try {
						input.close();
					} catch (final IOException e1) {
						Loger.log(Loger.LOG_TYPE.CONN_ERROR, e.toString());
						e1.printStackTrace();
					}
					return;
				}
			}

			try {
				ostream.flush();
			} catch (final IOException e) {
				e.printStackTrace();
			}

		}

		/**
		 * Dohvaćanje ekstenzije traženog resursa. U slučaju da resurs ne sadrži
		 * ekstanziju, null se vraća.
		 * 
		 * @param resourcePath
		 *            putanja do resursa
		 * @return ekstenzija, bez znaka .
		 */
		private synchronized String getResourceExtension(final Path resourcePath) {
			final String name = resourcePath.toFile().getName();
			final int dotPos = name.lastIndexOf('.');
			if (dotPos < 0) {
				return null;
			}
			final String ext = name.substring(dotPos + 1);
			if (ext.isEmpty()) {
				return null;
			}
			return ext;
		}

		/**
		 * Parsiranje parametara iz URI-ja. Parametri su formata
		 * name1=value1&name2=value2... Ukoliko sintaksa nije valjana, ERROR
		 * 400.
		 * 
		 * @param paramString
		 *            parametri iz URI-ja
		 * @return true ukliko je zapis sintaksno ispravan, false inače
		 */
		private synchronized boolean parseParameters(final String paramString) {
			final String[] pairs = paramString.split("&");
			for (int i = 0, n = pairs.length; i < n; i++) {
				final String[] pair = pairs[i].split("=");

				// ako sintaksa nije ispravna
				if (pair.length != 2) {
					setErrorStatus(400);
					return false;
				}

				params.put(pair[0], pair[1]);
			}
			return true;
		}

		/**
		 * Čitanje zaglavlja korisničkog zahtjeva.
		 * 
		 * @return lista redaka zaglavlja korisničkog zahtjeva
		 */
		private synchronized List<String> readRequest() {
			final Scanner sc = new Scanner(istream);
			final List<String> header = new ArrayList<String>();
			try {
				while (true) {
					final String line = sc.nextLine();
					// čitanje do prvog praznog retka
					if (line.equals("\r\n") || line.isEmpty()) {
						break;
					}
					header.add(line);
				}
			} catch (final NoSuchElementException ignorable) {
			}
			return header;
		}

		/**
		 * Statusni kodovi pogreške. Svaki kod ima opsi pogreške. Opisi su
		 * određeni RFC-om za HTTP protokol. Podržani opisi grešaka: 400, 403 i
		 * 404. Za nepodržan opis, šalje se "Undescribed error".
		 * 
		 * @param code
		 *            statusni kod greške
		 */
		private synchronized void setErrorStatus(final int code) {
			String description = "Error";
			switch (code) {
			case 400:
				description = "Bad Request";
				break;
			case 403:
				description = "Forbidden";
				break;
			case 404:
				description = "Not Found";
				break;
			default:
				description = "Undescribed error";
			}

			requestContext.setStatusCode(code);
			requestContext.setStatusText(description);
			try {
				// generiraj html opis greške
				requestContext.write("<!doctype html><html>"
						+ "<head><title>SmartHttpServer</title></head>"
						+ "<body><h1>Error " + code + "</h1><h2>" + description
						+ "</h2></body>" + "</html>");
			} catch (final IOException e) {
				Loger.log(Loger.LOG_TYPE.CONN_ERROR, e.toString());
				e.printStackTrace();
			}
		}

	}

	/**
	 * Main metoda. Preko konzolne linije prima se putanja do poslužiteljske
	 * konfiguracijske datoteke. Automatski se pokreće poslužitelj i komandna
	 * linija putem koje se poslužitelj može ugasiti.
	 * 
	 * @param args
	 *            putanja do poslužiteljske konfiguracijske datoteke
	 * @throws IOException
	 *             u slučaju problema čitanja konfiguracijske datoteke
	 */
	public static void main(final String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println("Server configuration file expected.");
			Loger.log(Loger.LOG_TYPE.INIT_ERROR, "No config file in args.");
			System.exit(-1);
		}
		final SmartHttpServer server = new SmartHttpServer(args[0]);
		server.start();

		// trenutno jedina podržana naredba konzolne linije: STOP
		System.out.println("Enter \"stop\" to shut down the server.");
		final BufferedReader consoleReader = new BufferedReader(
				new InputStreamReader(System.in, "UTF-8"));
		String line;
		while (true) {
			System.out.print("> ");
			line = consoleReader.readLine();

			if (line == null) {
				continue;
			}

			if (line.isEmpty()) {
				continue;
			}

			if (line.equalsIgnoreCase("stop")) {
				server.stop();
				break;
			} else {
				System.out.println("Unsupported command.");
				continue;
			}
		}
		consoleReader.close();
		// nakon ispisa ove poruke, poslužitelj je potpuno ugašen. Garantira se
		// nepostojanje zombi dretvi nakon gašenja.
		System.out.println("Server main exit.");
	}

}
