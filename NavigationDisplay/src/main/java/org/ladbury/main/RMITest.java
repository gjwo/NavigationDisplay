package org.ladbury.main;

import inertialNavigation.RemoteInstruments;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import javax.swing.UIManager;

import dataTypes.TimestampedData3f;

/**
 * NavigationDisplay - org.ladbury.main
 * Created by MAWood on 19/12/2016.
 */
public class RMITest extends Thread
{
	NavDisplay navDisplay;
    RemoteInstruments instruments;
    
    RMITest(String hostname) throws RemoteException, NotBoundException
    {
        System.setProperty("java.rmi.server.hostname", hostname) ;
        Registry reg = LocateRegistry.getRegistry(hostname, Registry.REGISTRY_PORT);

        instruments = (RemoteInstruments) reg.lookup("Instruments");
        navDisplay = new NavDisplay();
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
}