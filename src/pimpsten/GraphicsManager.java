package pimpsten;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;


/**
 * Classe per dibuixar els objectes del joc. S'encarrega tant de pintar el fons
 * com de pintar cada un dels trets, asteroides, naus. També pinta l'explosió
 * que surt quan la nau és destruïda.
 * @author Oscar Saleta
 */
public class GraphicsManager {

	private int maxX, maxY;
	private PimpstenInvasion game;

	GraphicsManager(int maxX, int maxY, PimpstenInvasion game) {
		this.maxX = maxX;
		this.maxY = maxY;
		this.game = game;
	}

	/**
	 * Pintar el fons (negre)
	 * @param g gràfics
	 */
	public void paintBackground(Graphics2D g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, maxX, maxY);
	}
	
	/**
	 * Pintar només asteroides (per la pantalla d'inici)
	 * @param g gràfics
	 */
	public void paintOnlyAsteroids(Graphics2D g) {
		for (int i=0; i<game.asteroids.size(); i++)
			game.asteroids.get(i).paint(g);
	}
	
	/**
	 * Pintar tots els objectes del joc
	 * @param g gràfics
	 */
	public void paintObjects(Graphics2D g) {
		int i;
		if (game.ship.status == true)
			game.ship.paint(g);
		for (i=0; i<game.asteroids.size(); i++)
			game.asteroids.get(i).paint(g);
		for (i=0; i<game.shots.size(); i++)
			game.shots.get(i).paint(g);
		for (i=0; i<game.saucer.size(); i++)
			game.saucer.get(i).paint(g);
		for (i=0; i<game.saucerShots.size(); i++)
			game.saucerShots.get(i).paint(g);
		if (game.teleportMessage)
			game.gameMM.noTeleport(g);
	}
	
	/**
	 * Pintar una explosió creixent on xoca la nau
	 * @param x coordenada x del xoc
	 * @param y coordenada y del xoc
	 * @param scale escala de l'explosió
	 * @param g gràfics
	 */
	public void paintExplosion(int x, int y, int scale, Graphics2D g) {
		try {
			URL url = this.getClass().getResource("resources/graphics/explosion.png");
			BufferedImage img = ImageIO.read(url);
			g.drawImage(img, x-scale*5, y-scale*5, scale*10, scale*10, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
