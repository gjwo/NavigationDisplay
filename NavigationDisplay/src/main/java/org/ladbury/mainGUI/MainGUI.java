package org.ladbury.mainGUI;

import javax.swing.*;
import java.awt.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.ladbury.mainGUI.elements.SwingLogDisplay;
import org.ladbury.mainGUI.elements.SystemManagerToolBar;
import org.ladbury.mainGUI.elements.UiAboutBox;
import org.ladbury.mainGUI.instrumentFrames.CubeFrame;
import org.ladbury.mainGUI.instrumentFrames.DynamicLineAndTimeSeriesChart;
import org.ladbury.mainGUI.instrumentFrames.InstrumentCompass;
import org.ladbury.mainGUI.motorFrames.MotorControlFrame;

/**
 * NavigationDisplay - org.ladbury.main
 * Created by MAWood on 26/12/2016.
 */
public class MainGUI extends JFrame
{
	private static final long serialVersionUID = -6694309420011224182L;
	public static Registry registry;


    public MainGUI(Registry registry)
    {
        super("Tank Control");
        MainGUI.registry = registry;
        this.setSize(1200,800);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setLayout(new BorderLayout());

        setupMenuBar();
        setupToolBar();
        addLog();
        this.setVisible(true);
    }


    private void addLog()
    {
        this.add(new JScrollPane(new SwingLogDisplay(registry)));
    }

    private void setupToolBar()
    {
        try
        {
            this.add(new SystemManagerToolBar(registry), BorderLayout.SOUTH);
        } catch (RemoteException | NotBoundException e)
        {
            e.printStackTrace();
        }
    }
    private void setupMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();

        // System menu
        JMenu systemMenu = new JMenu("System");

        JMenuItem exit = new JMenuItem("Exit");
        exit.setToolTipText("Exit the application.");
        exit.addActionListener(a -> System.exit(0));

        systemMenu.add(exit);

        // Graph menu
        JMenu graphMenu = new JMenu("Graphing");

        JMenuItem imuGraph = new JMenuItem("IMU Graph");
        imuGraph.addActionListener(a -> new DynamicLineAndTimeSeriesChart());
        graphMenu.add(imuGraph);

        JMenuItem cube = new JMenuItem("Orientation Cube");
        cube.addActionListener(a -> new CubeFrame());
        graphMenu.add(cube);

        JMenuItem compass = new JMenuItem("Compass");
        compass.addActionListener(a -> new InstrumentCompass());
        graphMenu.add(compass);

        // Control menu
        JMenu controlMenu = new JMenu("Control");

        JMenuItem daControl = new JMenuItem("Drive Assembly Control");
        daControl.addActionListener(a -> new MotorControlFrame());
        controlMenu.add(daControl);

        // Help menu
        JMenu helpMenu = new JMenu("Help)");
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(a -> new UiAboutBox(this));
        helpMenu.add(about);
        
        menuBar.add(systemMenu);
        menuBar.add(graphMenu);
        menuBar.add(controlMenu);
        menuBar.add(helpMenu);

        this.setJMenuBar(menuBar);
        
    }

    public static void main(String[] args) throws RemoteException
    {
        System.setProperty("java.rmi.server.hostname", args[0]) ;

        Registry reg = LocateRegistry.getRegistry(args[0], Registry.REGISTRY_PORT);
        new MainGUI(reg);
    }
}
