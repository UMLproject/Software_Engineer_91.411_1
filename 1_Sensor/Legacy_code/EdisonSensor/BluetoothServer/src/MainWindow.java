import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Font;

/**
 * @author Nathaniel Doyle The Main Window/GUI class, responsible for displaying
 *         the current state of the program and turning on/off bluetooth
 *         listening.
 */
public class MainWindow implements ActionListener {

	// Variable names all self-explanatory.
	boolean doListen = false;
	private JFrame window_;
	JPanel panel;
	JLabel calBurnedLabel;
	JButton emergencyButton;
	JButton startListenButton;
	JButton stopListenButton;
	JLabel fallCountLabel;
	JLabel fallCountDisplay;
	JLabel calBurnedDisplay;
	JMenuBar menuBar;
	JMenuItem exitOption;
	Timer counter = new Timer(1000, this);
	private int falls = 0;
	private int actionsTaken = 0;
	private JLabel isEmergencyLabel;
	int callTimer = 30;
	int callCountdown = callTimer;
	// if time for implementation, may store available connections.
	private JList list;

	/*
	 * Main constructor for GUI class, initiates all windows, buttons, labels,
	 * ETC.
	 */
	public MainWindow() {
		window_ = new JFrame("Recieved Info Display");
		window_.setResizable(false);
		window_.setSize(400, 350);
		panel = new JPanel();
		window_.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(null);
		calBurnedLabel = new JLabel("Calories Burned / Actions Taken:");
		calBurnedLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));
		calBurnedLabel.setBounds(10, 11, 255, 25);
		panel.add(calBurnedLabel);

		emergencyButton = new JButton("");
		emergencyButton.setBounds(180, 170, 194, 105);
		emergencyButton.addActionListener(this);
		emergencyButton.setEnabled(false);
		emergencyButton.setBackground(Color.LIGHT_GRAY);
		panel.add(emergencyButton);

		startListenButton = new JButton("Start Listening");
		startListenButton.setBounds(10, 220, 126, 23);
		startListenButton.addActionListener(this);
		panel.add(startListenButton);

		stopListenButton = new JButton("Stop Listening");
		stopListenButton.setBounds(10, 254, 126, 23);
		stopListenButton.addActionListener(this);
		panel.add(stopListenButton);

		isEmergencyLabel = new JLabel("");
		isEmergencyLabel.setBounds(178, 125, 196, 36);
		panel.add(isEmergencyLabel);

		fallCountLabel = new JLabel("Falls:");
		fallCountLabel.setFont(new Font("Times New Roman", Font.PLAIN, 16));
		fallCountLabel.setBounds(10, 47, 59, 23);
		panel.add(fallCountLabel);

		fallCountDisplay = new JLabel("0");
		fallCountDisplay.setFont(new Font("Tahoma", Font.PLAIN, 16));
		fallCountDisplay.setBounds(79, 49, 59, 19);
		panel.add(fallCountDisplay);

		calBurnedDisplay = new JLabel("0");
		calBurnedDisplay.setFont(new Font("Tahoma", Font.PLAIN, 16));
		calBurnedDisplay.setBounds(261, 13, 76, 20);
		panel.add(calBurnedDisplay);

		list = new JList();
		list.setBounds(10, 91, 126, 118);
		panel.add(list);

		window_.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		menuBar = new JMenuBar();
		window_.setJMenuBar(menuBar);
		exitOption = new JMenuItem("Exit");
		menuBar.add(exitOption);
		exitOption.addActionListener(this);
		window_.setLocation(500, 500);
		window_.setVisible(true);
	}

	/*
	 * The action performed method, handles all the button presses that can
	 * occur within the GUI.
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == emergencyButton) {
			emergencyOff();
		} else if (e.getSource() == startListenButton) {
			doListen = true;
		} else if (e.getSource() == stopListenButton) {
			doListen = false;
		} else if (e.getSource() == exitOption) {
			System.exit(0);
		} else if (e.getSource() == counter) {
			if (callCountdown > 0) {
				callCountdown--;
				isEmergencyLabel.setText("Call Triggered in " + callCountdown
						+ " sec.");
			} else {
				counter.stop();
				callCountdown = callTimer;
			}
		}
	}

	/*
	 * Simple public method to increment the fall counter and update the label
	 * with the new quantity.
	 */
	public void incrementFalls() {
		falls++;
		fallCountDisplay.setText("" + falls);
	}

	/*
	 * Simple public method to increment the actions taken counter, and update
	 * the label with the new quantity.
	 */
	public void incrementActionsTaken() {
		actionsTaken++;
		calBurnedDisplay.setText("" + actionsTaken);
	}

	/*
	 * Simple public method to enable the emergency button, turn it red, and
	 * potentially initiate a countdown.
	 */
	public void emergencyOn() {

		// Sound should play here, but ran out of time.
		counter.start();
		emergencyButton.setEnabled(true);
		emergencyButton.setBackground(Color.RED);
		emergencyButton.setText("Call Off Emergency!");
		isEmergencyLabel.setText("Call Triggered in 30 sec.");
	}

	/*
	 * Simple public method to cancel an emergency in progress, re-graying the
	 * button, and disabling the text.
	 */
	public void emergencyOff() {
		emergencyButton.setEnabled(false);
		emergencyButton.setText("");
		emergencyButton.setBackground(Color.LIGHT_GRAY);
		isEmergencyLabel.setText("");
		counter.stop();
		callCountdown = callTimer;
	}
}
