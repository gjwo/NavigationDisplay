package org.ladbury.main;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.ladbury.mainGUI.MainGUI;

public class Main
{

	public static void main(String[] args)
	{
        if (args.length != 1)
        {
            System.out.println("Usage: java NavClient <hostname>");
            System.out.println("Use IP address if no name available e.g. 192.168.1.123");
            return;
        }
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