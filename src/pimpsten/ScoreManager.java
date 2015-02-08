package pimpsten;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StreamTokenizer;

import javax.swing.JOptionPane;

/**
 * Classe que controla la puntuació màxima del
 * joc. Llegeix la puntuació d'un fitxer, i si
 * aquesta és superada a una partida, es demana
 * al jugador el seu nom per desar la nova
 * puntuació al fitxer.
 * @author Oscar Saleta
 */
public class ScoreManager {

	private File f;
	private BufferedWriter bw;
	private StreamTokenizer st;

	private double topScore;
	private String topName;

	ScoreManager() {
		try {
			f = new File("score.txt");
			/* si el fitxer score.txt encara no existeix
			 * el crearem */ 
			if (!f.exists()) {
				f.createNewFile();
				topScore = 0;
			}
		} catch (Exception e) {	}

	}

	/**
	 * Getter per la puntuació màxima
	 * @return puntuació màxima
	 */
	public double getTopScore() {
		readScore();
		return topScore;
	}
	
	/**
	 * Getter pel nom del jugador que ha fet la 
	 * puntuació màxima
	 * @return nom del jugador
	 */
	public String getTopName() {
		readScore();
		return topName;
	}
	
	/**
	 * Mirem si hi ha un top score o bé encara
	 * no se n'ha desat cap
	 * @return true si hi ha puntuació, false
	 * en cas contrari
	 */
	public boolean existsScore() {
		readScore();
		if (topScore == 0)
			return false;
		return true;
	}
	
	/**
	 * Llegir del fitxer de puntuacions la puntuació
	 * màxima i el nom del jugador. Com només hi ha una
	 * línia en l'arxiu a cada moment, sabem que el
	 * número serà la puntuació i la string serà
	 * el nom. Usem StreamTokenizer per distingir-los.
	 */
	private void readScore() {
		try {
			st = new StreamTokenizer(new BufferedReader(new FileReader(new File("score.txt"))));
			while (st.ttype != StreamTokenizer.TT_EOF) {
				st.nextToken();
				if (st.ttype == StreamTokenizer.TT_WORD)
					topName = st.sval;
				else
					topScore = st.nval;
			}
		} catch (IOException e) {}
	}
	
	/**
	 * Escriure al fitxer una nova puntuació i el
	 * nom del jugador. Usa un input dialog de
	 * JOptionPane per llegir la string del nom.
	 * @param score puntuació nova
	 */
	public void writeScore(double score) {
		String name = JOptionPane.showInputDialog("TOP SCORE! What's your name?");
		try {
			bw = new BufferedWriter(new FileWriter(new File("score.txt")));
			bw.write(score+" "+name);
			bw.close();
		} catch (IOException e) {}
	}

}
