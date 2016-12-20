package org.ladbury.main;

import inertialNavigation.RemoteInstruments;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import dataTypes.TimestampedData3f;
import devices.driveAssembly.RemoteDriveAssembly;

/**
 * NavigationDisplay - org.ladbury.main
 * Created by MAWood on 19/12/2016.
 */
public class RMITest extends Thread
{
	NavDisplay navDisplay;
    RemoteInstruments instruments;
    Registry reg;
    
    RMITest(String hostname) throws RemoteException, NotBoundException
    {
        System.setProperty("java.rmi.server.hostname", hostname) ;
        reg = LocateRegistry.getRegistry(hostname, Registry.REGISTRY_PORT);
        instruments = (RemoteInstruments) reg.lookup("Instruments");
        navDisplay = new NavDisplay();
        testDriveAssembly();
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
    
    public static void main(String[] args) throws IOException, NotBoundException
	{
         if (args.length != 1) {
             System.out.println("Usage: java NavClient <hostname>");
             return;
        }
        new RMITest(args[0]).start();
     }
    private void testDriveAssembly()
    {
    	RemoteDriveAssembly rda;
		try {
			System.out.println(Arrays.toString(reg.list()));
			rda = (RemoteDriveAssembly)reg.lookup("DriveAssembly");
	    	rda.setSpeed(1f);
	    	TimeUnit.SECONDS.sleep(1);
	    	rda.stop();
	    	} catch (RemoteException | NotBoundException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	

    }
}