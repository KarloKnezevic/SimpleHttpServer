package hr.fer.zemris.java.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import hr.fer.zemris.java.webserver.RequestContext;
import hr.fer.zemris.java.webserver.RequestContext.RCCookie;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Testovi za kontekstni zahtjev.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class RequestContextTest {

	/**
	 * Ne smije se predati null kao outputstream.
	 */
	@Test(expected = RuntimeException.class)
	public void constructorExceptionTest() {
		new RequestContext(null, new HashMap<String, String>(),
				new HashMap<String, String>(),
				new ArrayList<RequestContext.RCCookie>());
	}

	/**
	 * Nema mijenjanja encodinga nakon zapisivanja u rc.
	 * 
	 * @throws IOException
	 *             u slučaju problema s pisanjem
	 */
	@Test(expected = RuntimeException.class)
	public void noEncodingChangeAfterWriting() throws IOException {
		final RequestContext rc = getNewRC();
		rc.write("test");
		rc.setEncoding("UTF-8");
	}

	/**
	 * Nema mijenjanja statusnog koda nakon zapisivanja u rc.
	 * 
	 * @throws IOException
	 *             u slučaju problema s pisanjem
	 */
	@Test(expected = RuntimeException.class)
	public void noStatusCodeChangeAfterWriting() throws IOException {
		final RequestContext rc = getNewRC();
		rc.write("test");
		rc.setStatusCode(200);
	}

	/**
	 * Nema mijenjanja veličine resursa nakon zapisivanja u rc.
	 * 
	 * @throws IOException
	 *             u slučaju problema s pisanjem
	 */
	@Test(expected = RuntimeException.class)
	public void noContentLengthChangeAfterWriting() throws IOException {
		final RequestContext rc = getNewRC();
		rc.write("test");
		rc.setContentLength(12345);
	}

	/**
	 * Nema mijenjanja statusnog teksta nakon zapisivanja u rc.
	 * 
	 * @throws IOException
	 *             u slučaju problema s pisanjem
	 */
	@Test(expected = RuntimeException.class)
	public void noStattusTextChangeAfterWriting() throws IOException {
		final RequestContext rc = getNewRC();
		rc.write("test");
		rc.setStatusText("OK");
	}

	/**
	 * Nema mijenjanja mime tipa nakon zapisivanja u rc.
	 * 
	 * @throws IOException
	 *             u slučaju problema s pisanjem
	 */
	@Test(expected = RuntimeException.class)
	public void noMimeTypeChangeAfterWriting() throws IOException {
		final RequestContext rc = getNewRC();
		rc.write("test");
		rc.setMimeType("text/html");
	}

	/**
	 * Nema dodavanja kolačića nakon zapisivanja u rc.
	 * 
	 * @throws IOException
	 *             u slučaju problema s pisanjem
	 */
	@Test(expected = RuntimeException.class)
	public void noRCCookieAddingAfterWriting() throws IOException {
		final RequestContext rc = getNewRC();
		rc.write("test");
		rc.addRCCookie(new RCCookie("name", "value", 0, "127.0.0.1", "/"));
	}

	/**
	 * Testiranje dodavanja mape privremenih parametara.
	 */
	@Test
	public void tempParamsTest() {
		final RequestContext rc = getNewRC();
		final Map<String, String> tmpParams = new HashMap<>();
		rc.setTemporaryParameters(tmpParams);
		final Map<String, String> tmpParamsCpy = rc.getTemporaryParameters();
		assertTrue(tmpParams == tmpParamsCpy);
	}

	/**
	 * Testiranje dodavanja privremenog parametra.
	 */
	@Test
	public void tempParamTest() {
		final RequestContext rc = getNewRC();
		rc.setTemporaryParameter("param", "test");
		final String val = rc.getTemporaryParameter("param");
		assertTrue("test".equals(val));
	}

	/**
	 * Testiranje perzistentnih parametara.
	 */
	@Test
	public void persParamsTest() {
		final RequestContext rc = getNewRC();
		final Map<String, String> pParams = new HashMap<>();
		rc.setPersistentParameters(pParams);
		final Map<String, String> pParamsCpy = rc.getPersistentParameters();
		assertTrue(pParams == pParamsCpy);
	}

	/**
	 * Testiranje dohvaćanja parametara.
	 */
	@Test
	public void getParamsTest() {
		final RequestContext rc = getNewFilledRC();
		assertNotNull(rc.getParameters());
	}

	/**
	 * Testiranje dohvaćanja jednog parametra.
	 */
	@Test
	public void getParamTest() {
		final RequestContext rc = getNewFilledRC();
		final String s = rc.getParameter("p");
		assertTrue(s.equals("P"));
	}

	/**
	 * Testiranje dohvaćanja imena parametara.
	 */
	@Test
	public void getParamsNamesTest() {
		final RequestContext rc = getNewFilledRC();
		assertNotNull(rc.getParameterNames());
	}

	/**
	 * Testiranje dohvaćanja perzistentnih parametara.
	 */
	@Test
	public void getPParamTest() {
		final RequestContext rc = getNewFilledRC();
		final String s = rc.getPersistentParameter("pp");
		assertTrue(s.equals("PP"));
	}

	/**
	 * Testiranje dohvaćanja imena perzistentnih parametara.
	 */
	@Test
	public void getPParamsNamesTest() {
		final RequestContext rc = getNewFilledRC();
		assertNotNull(rc.getPersistentParameterNames());
	}

	/**
	 * Testiranje postavljanja i brisanja perzistentnih parametara.
	 */
	@Test
	public void setPParamsNamesTest() {
		final RequestContext rc = getNewFilledRC();
		rc.setPersistentParameter("name", "value");
		assertTrue(rc.getPersistentParameter("name").equals("value"));
		rc.removePersistentParameter("name");
		assertNull(rc.getPersistentParameter("name"));
	}

	/**
	 * Testiranje postavljanja i brisanja privremenih parametara.
	 */
	@Test
	public void TParamsNamesTest() {
		final RequestContext rc = getNewFilledRC();
		rc.setTemporaryParameter("name", "value");
		assertTrue(rc.getTemporaryParameter("name").equals("value"));
		rc.removeTemporaryParameter("name");
		assertNull(rc.getTemporaryParameter("name"));
		assertNotNull(rc.getTemporaryParameterNames());
	}

	/**
	 * Testiranje dohvata informacija o kolačiću.
	 */
	public void rcCookieTest() {
		final RCCookie cookie = new RCCookie("c", "C", 0, "127.0.0.1", "/");
		assertEquals("c", cookie.getName());
		assertEquals("C", cookie.getValue());
		assertEquals(0, cookie.getMaxAge().intValue());
		assertEquals("127.0.0.1", cookie.getDomain());
		assertEquals("/", cookie.getPath());
	}

	/**
	 * Izrada praznog kontekstnog zahtjeva.
	 * 
	 * @return kontekstni zahtjev
	 */
	private RequestContext getNewRC() {
		return new RequestContext(new ByteArrayOutputStream(),
				new HashMap<String, String>(), new HashMap<String, String>(),
				new ArrayList<RequestContext.RCCookie>());
	}

	/**
	 * Izrada popunjenog korisničkog zahtjeva
	 * 
	 * @return kontekstni zahtjev
	 */
	private RequestContext getNewFilledRC() {
		final Map<String, String> parameters = new HashMap<String, String>();
		final Map<String, String> persistentParameters = new HashMap<String, String>();
		final List<RCCookie> outputCookies = new ArrayList<RequestContext.RCCookie>();

		parameters.put("p", "P");
		persistentParameters.put("pp", "PP");
		outputCookies.add(new RCCookie("c", "C", 0, "127.0.0.1", "/"));

		return new RequestContext(new ByteArrayOutputStream(), parameters,
				persistentParameters, outputCookies);
	}

}
