package org.ladbury.userInterfacePkg;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import devices.driveAssembly.RemoteDriveAssembly;

class MotorControlFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6863060551972404948L;
	
	private JLabel speedLabel;
	private JLabel angleLabel;
	
	private JButton forwards;
	private JButton backwards;
	private JButton left;
	private JButton right;
	private JButton stop;
	
	private float angle;
	private float speed;
	
	private JPanel[][] grid = new JPanel[3][3];
	
	private RemoteDriveAssembly rda;
	
	public MotorControlFrame() throws HeadlessException 
	{
		super();
		this.setSize(new Dimension(400,400));
		this.setLayout(new GridLayout(3,2));
		this.setTitle("Motor Control");
	    this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setVisible(true);
		
		try {
			Registry reg = LocateRegistry.getRegistry("192.168.1.123");
			rda = (RemoteDriveAssembly) reg.lookup("DriveAssembly");
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
		
		speed = 0;
		angle = 0;
		
		for(int i = 0; i< 9; i++) 
		{
			grid[i%3][Math.floorDiv(i, 3)] = new JPanel();
			this.add(grid[i%3][Math.floorDiv(i, 3)]);
		}
		createLabels();
		createButtons();
		
		
	}
	
	private void createLabels() {
		speedLabel = new JLabel("Speed: 0");
		angleLabel = new JLabel("Angle: 0");
		
		grid[0][0].add(speedLabel); 
		grid[2][0].add(angleLabel); 
	}
	
	private void createButtons() {
		forwards = 	new JButton("Faster");
		backwards = new JButton("Slower");
		left = 		new JButton("Left");
		right = 	new JButton("Right");
		stop = 		new JButton("Stop");

		forwards.addActionListener	(e -> handleForwards());
		backwards.addActionListener	(e -> handleBackwards());
		left.addActionListener		(e -> handleLeft());
		right.addActionListener		(e -> handleRight());
		stop.addActionListener		(e -> handleStop());
		
		grid[1][0].add(forwards);
		grid[0][1].add(left);
		grid[1][2].add(backwards);
		grid[2][1].add(right);
		grid[1][1].add(stop);
	}
	
	private void handleForwards() {
		speed+= 0.1f;
		try {
			rda.setSpeed(speed);
			updateLabels();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	private void handleBackwards() {
		speed-= 0.1f;
		try {
			rda.setSpeed(speed);
			updateLabels();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	private void handleLeft() {
		angle-= 10;
		try {
			rda.setDirection(angle);
			updateLabels();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	private void handleRight() {
		angle+= 10;
		try {
			rda.setDirection(angle);
			updateLabels();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	private void handleStop() {
		try {
			rda.stop();
			updateLabels();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	private void updateLabels()
	{
		try {
			angle = rda.getDirection();
			speed = rda.getSpeed();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		speedLabel.setText("Speed: "+ speed *100);
		angleLabel.setText("Angle: " + angle);
	}

	public static void main(String[] args)
	{
		new MotorControlFrame();
	}

}
