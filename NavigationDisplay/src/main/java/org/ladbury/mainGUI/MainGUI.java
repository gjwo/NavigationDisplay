package org.ladbury.mainGUI;

import javax.swing.*;
import java.awt.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * NavigationDisplay - org.ladbury.main
 * Created by MAWood on 26/12/2016.
 */
public class MainGUI extends JFrame
{
    public final Registry registry;


    private MainGUI(Registry registry)
    {
        super("Tank Control");
        this.registry = registry;
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

        JMenu system = new JMenu("System");

        JMenuItem exit = new JMenuItem("Exit");
        exit.setToolTipText("Exit the application.");
        exit.addActionListener(a -> System.exit(0));

        system.add(exit);

        menuBar.add(system);

        this.setJMenuBar(menuBar);
    }

    public static void main(String[] args) throws RemoteException
    {
        System.setProperty("java.rmi.server.hostname", args[0]) ;

        Registry reg = LocateRegistry.getRegistry(args[0], Registry.REGISTRY_PORT);
        new MainGUI(reg);
    }
}
