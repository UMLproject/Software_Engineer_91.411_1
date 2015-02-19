import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

/**
 * @author Nathaniel Doyle
 * Manages communications between the GUI and the main window.
 */
public class InputParser implements ActionListener {

	MainWindow theWindow;
	int elapsedTime = 0;
	Timer temp;

	/*
	 * Constructor for input parsing class.
	 */
	public InputParser(MainWindow toBeTheWindow) {
		theWindow = toBeTheWindow;
	}

	/*
	 * Interprets the strings that the program recieves as input.
	 */
	public void parseInput(String input) {

		if (input.equals("ACTIVITY")) {
			theWindow.incrementActionsTaken();
		}
		else if (input.equals("FALL")) {
			theWindow.incrementFalls();
			startFallTimer();
		}
		else if (input.equals("TAP")) {
			theWindow.emergencyOff();
			elapsedTime = 0;
			temp.stop();
		}
		else if (input.equals("TAPX2")) {
			theWindow.emergencyOff();
			elapsedTime = 0;
			temp.stop();
		}
		else {
			System.out.println("Invalid Input String.");
		}
	}

	public void startFallTimer() {
		temp = new Timer(1000, this);
		temp.start();
		while (elapsedTime < 5) {
			System.out.print(elapsedTime);
		}
		theWindow.emergencyOn();
		temp.stop();
		elapsedTime = 0;
	}

	public void actionPerformed(ActionEvent e) {
		elapsedTime++;
	}
}
