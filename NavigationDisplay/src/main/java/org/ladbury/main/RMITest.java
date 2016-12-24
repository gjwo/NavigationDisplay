package org.ladbury.main;

import inertialNavigation.RemoteInstruments;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;
import dataTypes.TimestampedData3f;
import main.RemoteMain;

/**
 * NavigationDisplay - org.ladbury.main
 * Created by MAWood on 19/12/2016.
 */
public class RMITest extends Thread
{
	private final NavDisplay navDisplay;
    private final RemoteInstruments instruments;
    private final NavClientGUI navClientGUI;

    public static Registry registry;

    
    RMITest(Registry reg) throws RemoteException, NotBoundException
    {
        this.registry = reg;
        instruments = (RemoteInstruments) reg.lookup("Instruments");
        navDisplay = new NavDisplay();
		this.navClientGUI = new NavClientGUI(4);
        NavClientGUI.setNavClientMain(navClientGUI); 
        navClientGUI.init();
        navClientGUI.start();
        this.start();
    }

    @Override
    public void run()
    {
    	navDisplay.initDisplay();
        super.run();
        while(!Thread.interrupted())
            try
            {
            	navDisplay.processTaitBryanAngles(new TimestampedData3f(instruments.getTaitBryanAnglesD()));
                //System.out.println("RMI data: " + instruments.getTaitBryanAnglesD().toString());
            	TimeUnit.MILLISECONDS.sleep(20);
            } catch (InterruptedException | RemoteException e)
            {
            	this.interrupt();
                System.err.println("Failed to access remote system, closing down");
                System.exit(5);
            }
    }
    
    public static void main(String[] args) throws IOException, NotBoundException, InterruptedException
    {
         if (args.length != 1) {
             System.out.println("Usage: java NavClient <hostname>");
             return;
        }
        System.setProperty("java.rmi.server.hostname", args[0]) ;

        Registry reg = LocateRegistry.getRegistry(args[0], Registry.REGISTRY_PORT);
        System.out.println(Arrays.toString(reg.list()));

        RemoteMain main = (RemoteMain)reg.lookup("Main");

        main.start(EnumSet.of(RemoteMain.SubSystemType.INSTRUMENTS, RemoteMain.SubSystemType.DRIVE_ASSEMBLY));

        System.out.println(Arrays.toString(reg.list()));

        new RMITest(reg);
     }
}