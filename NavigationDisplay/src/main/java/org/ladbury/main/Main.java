package org.ladbury.main;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.ladbury.mainGUI.MainGUI;

public class Main
{

	public static void main(String[] args)
	{
        System.setProperty("java.rmi.server.hostname", args[0]) ;

        Registry reg;
		try
		{
			reg = LocateRegistry.getRegistry(args[0], Registry.REGISTRY_PORT);
		} catch (RemoteException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			reg = null;
			System.exit(99);
		}
        new MainGUI(reg);
    }
}