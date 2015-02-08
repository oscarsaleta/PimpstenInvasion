package pimpsten;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Classe que gestiona input per teclat del joc. Usa
 * flags booleans per saber si una tecla és premuda o
 * no, així permet fer més d'una acció a la vegada.
 * @author Oscar Saleta
 */
public class KeyboardEventManager extends KeyAdapter {

	private PimpstenInvasion gp;
	
	KeyboardEventManager(PimpstenInvasion gp) {
		this.gp = gp;
	}
	
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_UP)
			gp.keyUp = true;
		if (e.getKeyCode() == KeyEvent.VK_DOWN)
			gp.keyDown = true;
		if (e.getKeyCode() == KeyEvent.VK_LEFT)
			gp.keyLeft = true;
		if (e.getKeyCode() == KeyEvent.VK_RIGHT)
			gp.keyRight = true;
		if (e.getKeyCode() == KeyEvent.VK_SPACE)
			gp.keySpace = true;
		if (e.getKeyCode() == KeyEvent.VK_X)
			gp.keyX = true;
	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_UP)
			gp.keyUp = false;
		if (e.getKeyCode() == KeyEvent.VK_DOWN)
			gp.keyDown = false;
		if (e.getKeyCode() == KeyEvent.VK_LEFT)
			gp.keyLeft = false;
		if (e.getKeyCode() == KeyEvent.VK_RIGHT)
			gp.keyRight = false;
		if (e.getKeyCode() == KeyEvent.VK_SPACE)
			gp.keySpace = false;
		if (e.getKeyCode() == KeyEvent.VK_X)
			gp.keyX = false;
	}

}
