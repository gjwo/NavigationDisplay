package org.ladbury.mainGUI.motorFrames;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.WindowEvent;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import javax.swing.*;

import devices.driveAssembly.RemoteDriveAssembly;
import main.RemoteMain;
import org.ladbury.mainGUI.MainGUI;
import subsystems.SubSystem;
import subsystems.SubSystemState;

public class MotorControlFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6863060551972404948L;
	
	private JLabel speedLabel;
	private JLabel angleLabel;

    private float angle;
	private float speed;
	
	private final JPanel[][] grid = new JPanel[3][3];
	
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
			RemoteMain rm = (RemoteMain) MainGUI.registry.lookup("Main");
			if(rm.getSubSystemState(SubSystem.SubSystemType.DRIVE_ASSEMBLY) != SubSystemState.RUNNING)
			{
				JOptionPane.showMessageDialog(this, "Drive assembly sub system is not running");
                this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
				return;
			}
			rda = (RemoteDriveAssembly) MainGUI.registry.lookup("DriveAssembly");
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
        JButton forwards = new JButton("Faster");
        JButton backwards = new JButton("Slower");
        JButton left = new JButton("Left");
        JButton right = new JButton("Right");
        JButton stop = new JButton("Stop");

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
		speed = 0;

		try {
			rda.setSpeed(speed);
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
