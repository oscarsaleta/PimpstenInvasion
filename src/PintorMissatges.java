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
public class PintorMissatges {

	private static final Font gameOver = new Font("Press Start 2P", Font.BOLD, 45);
	private static final Font puntuacioActual = new Font("Press Start 2P", Font.PLAIN, 20);
	private static final Font missatges = new Font("Press Start 2P", Font.PLAIN, 11);
	
	public static void pantallaInici(){}
	
	public static void noTeleport(int xMax, int yMax, Graphics2D g) {
		Graphics2D aux = ((Graphics2D) ((Graphics) g).create());
		String m = "Motor d'hipervelocitat en refredament";
		aux.setFont(missatges);
		aux.setColor(Color.white);
		aux.drawString(m,(xMax-aux.getFontMetrics().stringWidth(m))/2,yMax-100);
	}

	public static void espera(boolean nauViva, int puntuacio, int xMax, int yMax, Graphics2D g) {
		Graphics2D aux = ((Graphics2D) ((Graphics) g).create());
		aux.setColor(Color.WHITE);
		aux.setFont(missatges);
		if (!nauViva) {
			String m1 = "GAME OVER";
			String m2 = "Puntuació final: "+puntuacio;
			aux.drawString(m2,(xMax-aux.getFontMetrics().stringWidth(m2))/2, 300);
			aux.setFont(gameOver);
			aux.drawString(m1,(xMax-aux.getFontMetrics().stringWidth(m1))/2,200);
		}
		aux.setFont(missatges);
		String m3 = "Prem una tecla";
		aux.drawString(m3,(xMax-aux.getFontMetrics().stringWidth(m3))/2,350);
		aux.dispose();
	}

	public static void puntuacioActual(int puntuacio, int xMax, int yMax, Graphics2D g) {
		Graphics2D aux = ((Graphics2D) ((Graphics) g).create());
		String s = "Puntuació: "+puntuacio;
		aux.setColor(Color.WHITE);
		aux.setFont(new Font("Press Start 2P",Font.PLAIN,20));
		aux.drawString(s, 30, 30);
	}
}
