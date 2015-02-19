import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.bluetooth.LocalDevice;

/**
 * @Author Nathaniel Doyle
 * 
 * Project's main routine, simply opens up the GUI and creates a bluetooth listening thread and a parser for the input.
 */
public class ProjectMain {

	public static void main(String[] args) {

		MainWindow theWindow = new MainWindow();
		InputParser theParser = new InputParser(theWindow);
		String input;
		//Loop forever until program turned off.
		while (true) {
			// loop while the GUI says to be listening for input.
			while (theWindow.doListen == true) {
				try {
					// display local device address and name in console.
					LocalDevice localDevice = LocalDevice.getLocalDevice();
					System.out.println("Address: "+localDevice.getBluetoothAddress());
					System.out.println("Name: "+localDevice.getFriendlyName());
					SampleSPPServer sampleSPPServer = new SampleSPPServer();
					//starts the server to recieve input
					input = sampleSPPServer.startServer();
					//send the input to the parser.
					theParser.parseInput(input);
					
				} catch (Exception e) {
					// System.out.println("Error.");
				}
			}
		}
		
		//simple console I/O for testing responses.
		/*
		 * System.out.println("Let's test!");
		 * System.out.println("Enter the string you wish to test on");
		 * String temp3 = ""; 
		 * BufferedReader temp2 = new BufferedReader(new InputStreamReader(System.in));
		 * try { temp3 = temp2.readLine(); } catch (IOException e) { }
		 * theParser.parseInput(temp3);
		 */
	}

}
