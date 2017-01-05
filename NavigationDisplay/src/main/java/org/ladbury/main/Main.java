package org.ladbury.main;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.TimeUnit;

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
        System.setProperty("java.rmi.server.hostname", args[0]);

        Registry reg = null;
        while(reg == null)
        {
            reg = attemptToConnect(args[0]);
        }
        new MainGUI(reg);
    }

    private static Registry attemptToConnect(String address)
    {
        Registry reg = null;
        try
        {
            reg = LocateRegistry.getRegistry(address, Registry.REGISTRY_PORT);
            reg.lookup("Main");
            reg.lookup("Log");
        } catch (Exception e)
        {
            reg = null;
            try
            {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException ignored) {}
        }
        return reg;
    }
}