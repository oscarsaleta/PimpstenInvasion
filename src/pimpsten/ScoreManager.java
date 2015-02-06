package pimpsten;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StreamTokenizer;

import javax.swing.JOptionPane;

public class ScoreManager {

	private File f;
	private BufferedWriter bw;
	private StreamTokenizer st;

	private double topScore;
	private String topName;

	ScoreManager() {
		try {
			f = new File("score.txt");
			if (!f.exists()) {
				f.createNewFile();
				topScore = 0;
			}
		} catch (Exception e) {	}

	}

	public double getTopScore() {
		readScore();
		return topScore;
	}
	
	public String getTopName() {
		readScore();
		return topName;
	}
	
	public boolean existsScore() {
		readScore();
		if (topScore == 0)
			return false;
		return true;
	}
	
	public void readScore() {
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
	
	public void writeScore(double score) {
		String name = JOptionPane.showInputDialog("TOP SCORE! What's your name?");
		try {
			bw = new BufferedWriter(new FileWriter(new File("score.txt")));
			bw.write(score+" "+name);
			bw.close();
		} catch (IOException e) {}
	}

}
