package pimpsten;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Classe que gestiona els missatges que imprimim en pantalla
 * 
 * @author Oscar Saleta
 *
 */
public class MessageManager {

	private int maxX;
	private int maxY;
	private PimpstenInvasion game;

//	private final Font titleFont = new Font("Press Start 2P", Font.BOLD, 43);
//	private final Font scoreFont = new Font("Press Start 2P", Font.PLAIN, 20);
//	private final Font messageFont = new Font("Press Start 2P", Font.PLAIN, 11);

	MessageManager (int maxX, int maxY, PimpstenInvasion game) {
		this.maxX = maxX;
		this.maxY = maxY;
		this.game = game;
	}

	private void blinkingMessage(String m, int positionY, Graphics2D aux) {
		final Font messageFont = new Font(aux.getFont().getFontName(),Font.PLAIN,11);
		if (game.isMessageVisible) {
			//si el missatge anterior era visible
			if (System.currentTimeMillis() - game.lastVisibilitySwapTime < game.visibleTime) {
				//si encara hem d'imprimir un missatge visible
				aux.setColor(Color.WHITE);
				aux.setFont(messageFont);
				aux.drawString(m, (maxX-aux.getFontMetrics().stringWidth(m))/2, positionY);
			} else {
				//hem de deixar de mostrar el missatge
				game.isMessageVisible = false;
				game.lastVisibilitySwapTime = System.currentTimeMillis();
			}
		} else {
			//el missatge anterior no era visible
			if (System.currentTimeMillis() - game.lastVisibilitySwapTime > game.invisibileTime) {
				//hem de fer el missatge visible
				aux.setColor(Color.WHITE);
				aux.setFont(messageFont);
				aux.drawString(m, (maxX-aux.getFontMetrics().stringWidth(m))/2, positionY);
				game.isMessageVisible = true;
				game.lastVisibilitySwapTime = System.currentTimeMillis();
			}
		}
	}

	public void startScreen(Graphics2D g) {
		Graphics2D aux = ((Graphics2D) ((Graphics) g).create());
		
		final Font titleFont = new Font(aux.getFont().getFontName(),Font.BOLD,43);
		final Font messageFont = new Font(aux.getFont().getFontName(),Font.PLAIN,11);
		
		game.gameGM.paintOnlyAsteroids(aux);
		//pintem el missatge que parpalleja
		String m = "Press a button to start or quit";
		blinkingMessage(m, maxY/2+260, aux);
		
		//pintem els altres missatges
		aux.setColor(Color.WHITE);

		String title1 = "PIMPSTEN INVASION";
		aux.setFont(titleFont);
		aux.drawString(title1, (maxX-aux.getFontMetrics().stringWidth(title1))/2, maxY/2-150);
		
		String subtitle = "v1.00-stable";
		aux.setFont(messageFont);
		aux.drawString(subtitle, (maxX-aux.getFontMetrics().stringWidth(subtitle))/2, maxY/2-100);

		aux.dispose();
	}

	public void noTeleport(Graphics2D g) {
		Graphics2D aux = ((Graphics2D) ((Graphics) g).create());
		
		final Font messageFont = new Font(aux.getFont().getFontName(),Font.PLAIN,11);
		
		String m = "Teleport on cooldown";
		aux.setFont(messageFont);
		aux.setColor(Color.WHITE);
		aux.drawString(m,(maxX-aux.getFontMetrics().stringWidth(m))/2,maxY-100);
	}

	public void waitMessage(int score, Graphics2D g) {
		Graphics2D aux = ((Graphics2D) ((Graphics) g).create());
		
		final Font titleFont = game.arcadeFont.deriveFont(43.0f).deriveFont(Font.BOLD);//new Font(aux.getFont().getFontName(),Font.BOLD,43);
		final Font messageFont = game.arcadeFont.deriveFont(11.0f);//new Font(aux.getFont().getFontName(),Font.PLAIN,11);
		
		aux.setColor(Color.WHITE);
		
		String m1 = "GAME OVER";
		aux.setFont(titleFont);
		aux.drawString(m1,(maxX-aux.getFontMetrics().stringWidth(m1))/2,200);
		
		String m2 = "Final score: "+score;
		aux.setFont(messageFont);
		aux.drawString(m2,(maxX-aux.getFontMetrics().stringWidth(m2))/2, 300);
		
		String m3 = "What a shame. You should try again.";
		blinkingMessage(m3, maxY/2+260, aux);
		aux.dispose();
	}

	public void currentScore(int score, int level, Graphics2D g) {
		Graphics2D aux = ((Graphics2D) ((Graphics) g).create());
		
		final Font scoreFont = game.arcadeFont.deriveFont(20.0f);
		
		aux.setColor(Color.WHITE);
		aux.setFont(scoreFont);
		aux.drawString("Score: "+score, 30, 30);
		aux.drawString("Level: "+level,30,60);
	}
	
}
