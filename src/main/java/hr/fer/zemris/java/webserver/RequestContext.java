package hr.fer.zemris.java.webserver;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Razred RequestContext predstavlja kontekst HTTP zahtjeva te služi kao
 * kontejner informacija komunikacije između klijenta i poslužietlja pomoću HTTP
 * protokola.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class RequestContext {

	/**
	 * Izlazni tok.
	 */
	private final OutputStream outputStream;
	/**
	 * Kodna stranica kodiranja zapisa.
	 */
	private Charset charset;
	/**
	 * Ime kodne stranice.
	 */
	private String encoding = "UTF-8";
	/**
	 * Statusni kod odgovora: 1xx, 2xx, 3xx, 4xx, 5xx.
	 */
	private int statusCode = 200;
	/**
	 * Tekstualni opis statusnog koda.
	 */
	private String statusText = "OK";
	/**
	 * Mime tip: opis tipa podataka koji se šalje.
	 */
	private String mimeType = "text/html";
	/**
	 * Veličina sadržaja (datoteke) čiji sadržaj se prenosi.
	 */
	private long contentLength = -1;
	/**
	 * Parametri primljeni putem URI-ja.
	 */
	private final Map<String, String> parameters;
	/**
	 * Mapa privremenih parametara za evaluaciju skripte.
	 */
	private Map<String, String> temporaryParameters;
	/**
	 * Mapa parametara poslanih putem kolačića.
	 */
	private Map<String, String> persistentParameters;
	/**
	 * Lista kolačića.
	 */
	private final List<RCCookie> outputCookies;
	/**
	 * Zastavica je li generirano zaglavlje. Zaglavlje se generira samo jednom
	 * po zahtjevu.
	 */
	private boolean headerGenerated = false;

	/**
	 * Konstruktor. Izlazni tok ne smije biti null, u slučaju da je null,
	 * RuntimeException se izbacuje.
	 * 
	 * @param outputStream
	 *            izlazni tok
	 * @param parameters
	 *            mapa parametara URI-ja
	 * @param persistentParameters
	 *            mapa parametara kolačića
	 * @param outputCookies
	 *            izlazni kolačići (kolačići koji se šalju)
	 */
	public RequestContext(final OutputStream outputStream,
			final Map<String, String> parameters,
			final Map<String, String> persistentParameters,
			final List<RCCookie> outputCookies) {

		if (outputStream == null) {
			throw new RuntimeException(
					"It is not allowed to ouputStream to be null.");
		}

		this.outputStream = outputStream;
		this.parameters = parameters;
		this.persistentParameters = persistentParameters;
		this.outputCookies = outputCookies;

		temporaryParameters = new HashMap<String, String>();
	}

	/**
	 * Postavljanje imena kodne stranice. Dozvoljeno prije pisanja u izlazni
	 * tok.
	 * 
	 * @param encoding
	 *            ime kodne stranice
	 */
	public void setEncoding(final String encoding) {
		if (headerGenerated) {
			throw new RuntimeException(
					"At this moment it is impossible to change encoding.");
		}
		this.encoding = encoding;
	}

	/**
	 * Postavljanje statusnog koda odgovora. Dozvoljeno prije pisanja u izlazni
	 * tok.
	 * 
	 * @param statusCode
	 *            izlazni kod odgovora
	 */
	public void setStatusCode(final int statusCode) {
		if (headerGenerated) {
			throw new RuntimeException(
					"At this moment it is impossible to change status code.");
		}
		this.statusCode = statusCode;
	}

	/**
	 * Postavljanje statusnog teksta odgovora. Dozvoljeno prije pisanja u
	 * izlazni tok.
	 * 
	 * @param statusText
	 *            statusni tekst odgovora
	 */
	public void setStatusText(final String statusText) {
		if (headerGenerated) {
			throw new RuntimeException(
					"At this moment it is impossible to change status text.");
		}
		this.statusText = statusText;
	}

	/**
	 * Postavljanje mime tipa. Dozvoljeno prije pisanja u izlazni tok.
	 * 
	 * @param mimeType
	 *            mime tip
	 */
	public void setMimeType(final String mimeType) {
		if (headerGenerated) {
			throw new RuntimeException(
					"At this moment it is impossible to change mime type.");
		}
		this.mimeType = mimeType;
	}

	/**
	 * Dodavanje kolačića u listu kolačića. Dozvoljeno prije pisanja u izlazni
	 * tok.
	 * 
	 * @param cookie
	 *            kolačić
	 */
	public void addRCCookie(final RCCookie cookie) {
		if (headerGenerated) {
			throw new RuntimeException(
					"At this moment it is impossible to change cookies.");
		}
		outputCookies.add(cookie);
	}

	/**
	 * Mapa privremenih parametara za evaluaciju skripte.
	 * 
	 * @return mapu privremenih parametara za evaluaciju skripte
	 */
	public Map<String, String> getTemporaryParameters() {
		return temporaryParameters;
	}

	/**
	 * Mapa privremenih parametara za evaluaciju skripte.
	 * 
	 * @param temporaryParameters
	 *            mapa privremenih parametara za evaluaciju skripte
	 */
	public void setTemporaryParameters(
			final Map<String, String> temporaryParameters) {
		this.temporaryParameters = temporaryParameters;
	}

	/**
	 * Mapa parametara poslanih putem kolačića
	 * 
	 * @return mapu parametara poslanih putem kolačića
	 */
	public Map<String, String> getPersistentParameters() {
		return persistentParameters;
	}

	/**
	 * Mapa parametara poslanih putem kolačića
	 * 
	 * @param persistentParameters
	 *            mapa parametara poslanih putem kolačića
	 */
	public void setPersistentParameters(
			final Map<String, String> persistentParameters) {
		this.persistentParameters = persistentParameters;
	}

	/**
	 * Parametri primljeni putem URI-ja.
	 * 
	 * @return parametri primljeni putem URI-ja
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * Parametar primljen putem URI-ja.
	 * 
	 * @param name
	 *            ime parametra
	 * @return vrijednost parametra
	 */
	public String getParameter(final String name) {
		return parameters.get(name);
	}

	/**
	 * Imena parametara dohvaćenih iz URI-ja.
	 * 
	 * @return imena parametara dohvaćenih iz URI-ja
	 */
	public Set<String> getParameterNames() {
		return new HashSet<String>(parameters.keySet());
	}

	/**
	 * Vrijednost parametra poslanog putem kolačića.
	 * 
	 * @param name
	 *            ime kolačića
	 * @return vrijednost kolačića
	 */
	public String getPersistentParameter(final String name) {
		return persistentParameters.get(name);
	}

	/**
	 * Imena svih kolačića.
	 * 
	 * @return imena svih kolačića
	 */
	public Set<String> getPersistentParameterNames() {
		return new HashSet<String>(persistentParameters.keySet());
	}

	/**
	 * Postavljanje/dodavanje imena i vrijednosti kolačića.
	 * 
	 * @param name
	 *            ime kolačića
	 * @param value
	 *            vrijednost kolačića
	 */
	public void setPersistentParameter(final String name, final String value) {
		persistentParameters.put(name, value);
	}

	/**
	 * Brisanje vrijednosti prenesene putem kolačića imena name.
	 * 
	 * @param name
	 *            ime kolačića
	 */
	public void removePersistentParameter(final String name) {
		persistentParameters.remove(name);
	}

	/**
	 * Dohvaćanje privremenog parametra za izvršavanje skripte.
	 * 
	 * @param name
	 *            ime parametra
	 * @return vrijednost parametra
	 */
	public String getTemporaryParameter(final String name) {
		return temporaryParameters.get(name);
	}

	/**
	 * Skup imena privremenih parametara za izvršavanje skripte.
	 * 
	 * @return skup imena privremenih parametara za izvršavanje skripte
	 */
	public Set<String> getTemporaryParameterNames() {
		return new HashSet<String>(temporaryParameters.keySet());
	}

	/**
	 * Postavljanje/dodavanje privremenog parametra.
	 * 
	 * @param name
	 *            ime parametra
	 * @param value
	 *            vrijednost parametra
	 */
	public void setTemporaryParameter(final String name, final String value) {
		temporaryParameters.put(name, value);
	}

	/**
	 * Brisanje privremenog parametra.
	 * 
	 * @param name
	 *            ime privremenog parametra
	 */
	public void removeTemporaryParameter(final String name) {
		temporaryParameters.remove(name);
	}

	/**
	 * Postavljanje veličine sadržaja u bajtovima (datoteke) koja se prenosi.
	 * Dozvoljeno prije pisanja u izlazni tok.
	 * 
	 * @param contentLength
	 *            veličina sadržaja u B
	 */
	public void setContentLength(final long contentLength) {
		if (headerGenerated) {
			throw new RuntimeException(
					"At this moment it is impossible to change content length.");
		}
		this.contentLength = contentLength;
	}

	/**
	 * Pisanje podataka u izlazni tok. Podaci su u binarnom obliku. Konceptualno
	 * predstavlja slanje podataka od poslužitelja prema klijentu.
	 * 
	 * @param data
	 *            binaran niz podataka
	 * @return kontekst zahtjeva
	 * @throws IOException
	 *             u slučaju pogreške prilikom pisanja u izlazni tok
	 */
	public RequestContext write(final byte[] data) throws IOException {
		if (!headerGenerated) {
			outputStream.write(generateHeader());
			headerGenerated = true;
		}
		outputStream.write(data);
		return this;
	}

	/**
	 * Pisanje podataka u izlazni tok. Podaci su predstavljeni kao niz znakova.
	 * Konceptualno predstavlja slanje podataka od poslužitelja prema klijentu.
	 * 
	 * @param text
	 *            podatak
	 * @return kontekst zahtjeva
	 * @throws IOException
	 *             u slučaju pogreške prilikom pisanja u izlazni tok
	 */
	public RequestContext write(final String text) throws IOException {
		if (!headerGenerated) {
			outputStream.write(generateHeader());
			headerGenerated = true;
		}
		outputStream.write(text.getBytes(charset));
		return this;

	}

	/**
	 * Generator zaglavlja. Zaglavlje se prilikom slanja podataka generira samo
	 * jedanput. Format zaglavlja propisan je RFC za HTTP zaglavlje.
	 * 
	 * @return binarno kodirano zaglavlje
	 */
	private byte[] generateHeader() {
		charset = Charset.forName(encoding);
		final StringBuilder header = new StringBuilder(50);

		// 1. line
		header.append("HTTP/1.1 ").append(statusCode).append(" ")
				.append(statusText).append("\r\n");

		// 2. line
		header.append("Content-Type: ").append(mimeType);
		if (mimeType.startsWith("text/")) {
			header.append("; charset=").append(charset);
		}
		header.append("\r\n");

		// ukoliko je postavljena veličina sadržaja
		if (contentLength > -1) {
			header.append("Content-Length: ").append(contentLength)
					.append("\r\n");
		}

		// 3. line
		if (!outputCookies.isEmpty()) {
			for (final RCCookie cookie : outputCookies) {
				header.append("Set-Cookie: ")
						.append(cookie.getRCCookieHeaderDsc()).append("\r\n");
			}
		}

		header.append("\r\n");

		return header.toString().getBytes(StandardCharsets.ISO_8859_1);
	}

	/**
	 * Kolačić konteksta zahtjeva. Kolačić predstavlja malu količinu informacije
	 * koja se putem HTTP zaglavlja prenosti između klijenta i poslužitelja.
	 * 
	 * @author Karlo Knezevic, karlo.knezevic@fer.hr
	 * 
	 */
	public static class RCCookie {

		/**
		 * Ime kolačića.
		 */
		private final String name;
		/**
		 * Vrijednost kolačića.
		 */
		private final String value;
		/**
		 * Domena poslužitelja iz pogleda klijenta (2. redak.
		 * Host:xxx.xxx.xxx.xxx).
		 */
		private final String domain;
		/**
		 * Staza za koju kolačić vrijedi.
		 */
		private final String path;
		/**
		 * Trajanje kolačića kod klijenta/poslužitelja. Nakon maxAge vremena,
		 * klijent odbacuje kolačić. Ukoliko je maxAge null ili 0, tada
		 * poslužitelj prati trajanje kolačića, a nakon gašenja klijenta,
		 * kolačić se briše.
		 */
		private final Integer maxAge;

		/**
		 * Konstruktor kolačića.
		 * 
		 * @param name
		 *            ime kolačića
		 * @param value
		 *            vrijednost kolačića
		 * @param maxAge
		 *            vremensko trajanje kolačića
		 * @param domain
		 *            domena poslužitelja iz pogleda klijenta
		 * @param path
		 *            staza na poslužitelju za koju kolačić vrijedi
		 */
		public RCCookie(final String name, final String value,
				final Integer maxAge, final String domain, final String path) {
			this.name = name;
			this.value = value;
			this.domain = domain;
			this.path = path;
			this.maxAge = maxAge;
		}

		/**
		 * Ime kolačića.
		 * 
		 * @return ime kolačića
		 */
		public String getName() {
			return name;
		}

		/**
		 * Vrijednost kolačića.
		 * 
		 * @return vrijednost kolačića
		 */
		public String getValue() {
			return value;
		}

		/**
		 * Domena poslužitelja iz pogleda klijenta (2. redak.
		 * Host:xxx.xxx.xxx.xxx).
		 * 
		 * @return domena poslužitelja
		 */
		public String getDomain() {
			return domain;
		}

		/**
		 * Staza za koju kolačić vrijedi.
		 * 
		 * @return staza
		 */
		public String getPath() {
			return path;
		}

		/**
		 * Trajanje kolačića kod klijenta/poslužitelja. Nakon maxAge vremena,
		 * klijent odbacuje kolačić. Ukoliko je maxAge null ili 0, tada
		 * poslužitelj prati trajanje kolačića, a nakon gašenja klijenta,
		 * kolačić se briše.
		 * 
		 * @return trajanje kolačića kod klijenta i poslužitelja
		 */
		public Integer getMaxAge() {
			return maxAge;
		}

		/**
		 * Zapis kolačića kao niz znakova prema RFC za HTTP kolačić.
		 * 
		 * @return kolačić kao niz znakova
		 */
		public String getRCCookieHeaderDsc() {
			final StringBuilder cookieDsc = new StringBuilder(50);

			cookieDsc.append(name).append("=").append("\"").append(value)
					.append("\"");

			if (domain != null) {
				cookieDsc.append("; ").append("Domain=").append(domain);
			}

			if (path != null) {
				cookieDsc.append("; ").append("Path=").append(path);
			}

			if (maxAge != null) {
				cookieDsc.append("; ").append("maxAge=").append(maxAge);
			}

			return cookieDsc.toString();
		}

	}

}
