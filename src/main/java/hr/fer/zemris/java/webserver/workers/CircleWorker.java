package hr.fer.zemris.java.webserver.workers;

import hr.fer.zemris.java.webserver.IWebWorker;
import hr.fer.zemris.java.webserver.Loger;
import hr.fer.zemris.java.webserver.RequestContext;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Web aplikcija koja crta žuti krug i u sredini ispisuje prikladan tekst.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class CircleWorker implements IWebWorker {

	/**
	 * Metoda obrađuje zahtjev na način da na izlaz proslijeđuje sliku png
	 * formata veličine 200x200.
	 */
	@Override
	public void processRequest(final RequestContext context) {
		final BufferedImage bim = new BufferedImage(200, 200,
				BufferedImage.TYPE_3BYTE_BGR);

		final Graphics2D g2d = bim.createGraphics();

		g2d.setColor(Color.YELLOW);
		g2d.fillOval(0, 0, bim.getWidth(), bim.getHeight());

		g2d.setColor(Color.RED);
		final String text = "SmartHttpServer";
		g2d.drawString(text, bim.getWidth() / 4, bim.getHeight() / 2);

		g2d.dispose();

		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		context.setMimeType("image/png");
		try {
			ImageIO.write(bim, "png", bos);
			context.write(bos.toByteArray());
		} catch (final IOException e) {
			// ako nešto pođe po zlu, zapiši u log
			Loger.log(Loger.LOG_TYPE.WORKER_ERROR, e.toString());
			e.printStackTrace();
		}

	}

}
