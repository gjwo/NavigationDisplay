package org.ladbury.main;

import inertialNavigation.RemoteInstruments;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import dataTypes.TimestampedData3f;
import devices.driveAssembly.RemoteDriveAssembly;
import devices.driveAssembly.RemoteDriveAssemblyImpl;
import main.RemoteMain;
import subsystems.LogDisplayer;
import subsystems.LogEntry;
import subsystems.RemoteLog;
import subsystems.SystemLog;

/**
 * NavigationDisplay - org.ladbury.main
 * Created by MAWood on 19/12/2016.
 */
public class RMITest extends Thread
{
	NavDisplay navDisplay;
    RemoteInstruments instruments;
    Registry reg;
    private NavClientGUI navClientGUI;

    public static String hostname;

    
    RMITest(String hostname) throws RemoteException, NotBoundException
    {
        reg = LocateRegistry.getRegistry(hostname, Registry.REGISTRY_PORT);
        instruments = (RemoteInstruments) reg.lookup("Instruments");
        navDisplay = new NavDisplay();
		this.navClientGUI = new NavClientGUI(4);
        NavClientGUI.setNavClientMain(navClientGUI); 
        navClientGUI.init();
        navClientGUI.start();
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
        hostname = args[0];
        System.setProperty("java.rmi.server.hostname", args[0]) ;
        Registry reg = LocateRegistry.getRegistry(args[0], Registry.REGISTRY_PORT);
        System.out.println(Arrays.toString(reg.list()));

        RemoteLog log = (RemoteLog) reg.lookup("Log");
        //log.registerInterest((LogDisplayer) UnicastRemoteObject.exportObject(new LogHandler(),0));
        //for(int i =0; i< log.getEntryCount(); i++) System.out.println(log.getEntry(i).toString());
        for(LogEntry entry:log.getEntries())System.out.println(entry.toString());

        RemoteMain main = (RemoteMain)reg.lookup("Main");
        EnumSet<RemoteMain.SubSystemType> systems =
                EnumSet.of(RemoteMain.SubSystemType.INSTRUMENTS, RemoteMain.SubSystemType.DRIVE_ASSEMBLY);
        main.start(systems);

        System.out.println(Arrays.toString(reg.list()));

        new RMITest(args[0]).start();
     }
}