package org.ladbury.main;

import inertialNavigation.RemoteInstruments;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * NavigationDisplay - org.ladbury.main
 * Created by MAWood on 19/12/2016.
 */
public class RMITest extends Thread
{
    RemoteInstruments instruments;
    RMITest(String hostname) throws RemoteException, NotBoundException
    {
        System.setProperty("java.rmi.server.hostname", hostname) ;
        Registry reg = LocateRegistry.getRegistry(hostname, Registry.REGISTRY_PORT);

        instruments = (RemoteInstruments) reg.lookup("Instruments");
    }

    @Override
    public void run()
    {
        super.run();
        while(!Thread.interrupted())
        {

            try
            {
                System.out.println("RMI data: " + instruments.getTaitBryanAnglesD().toString());
                Thread.sleep(100);
            } catch (InterruptedException | RemoteException e)
            {
                e.printStackTrace();
            }
        }
    }
}
