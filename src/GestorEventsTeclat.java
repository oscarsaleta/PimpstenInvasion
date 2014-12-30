import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Classe que gestiona input per teclat del joc
 * 
 * @author Oscar Saleta
 *
 */
public class GestorEventsTeclat extends KeyAdapter {
	private int pressCount=1;

	Joc joc;
	
	GestorEventsTeclat(Joc joc) {
		this.joc = joc;
	}
	
	public void keyPressed(KeyEvent e) {
		if (joc.esperantTecla)
			return;

		if (e.getKeyCode() == KeyEvent.VK_UP)
			joc.teclaAdalt = true;
		if (e.getKeyCode() == KeyEvent.VK_DOWN)
			joc.teclaAbaix = true;
		if (e.getKeyCode() == KeyEvent.VK_LEFT)
			joc.teclaEsquerra = true;
		if (e.getKeyCode() == KeyEvent.VK_RIGHT)
			joc.teclaDreta = true;
		if (e.getKeyCode() == KeyEvent.VK_SPACE)
			joc.teclaEspai = true;
		if (e.getKeyCode() == KeyEvent.VK_X)
			joc.teclaX = true;
	}

	public void keyReleased(KeyEvent e) {
		if (joc.esperantTecla)
			return;

		if (e.getKeyCode() == KeyEvent.VK_UP)
			joc.teclaAdalt = false;
		if (e.getKeyCode() == KeyEvent.VK_DOWN)
			joc.teclaAbaix = false;
		if (e.getKeyCode() == KeyEvent.VK_LEFT)
			joc.teclaEsquerra = false;
		if (e.getKeyCode() == KeyEvent.VK_RIGHT)
			joc.teclaDreta = false;
		if (e.getKeyCode() == KeyEvent.VK_SPACE)
			joc.teclaEspai = false;
		if (e.getKeyCode() == KeyEvent.VK_X)
			joc.teclaX = false;
	}

	public void keyTyped(KeyEvent e) {
		if (joc.esperantTecla) {
			if (pressCount == 1) {
				joc.esperantTecla = false;
				joc.iniciarPartida();
				pressCount=0;
			} else {
				pressCount++;
			}
		}
		if (e.getKeyChar() == 27)
			System.exit(0);
	}
}
