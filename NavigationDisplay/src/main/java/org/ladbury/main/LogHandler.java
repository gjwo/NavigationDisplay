package org.ladbury.main;

import org.ladbury.userInterfacePkg.LogDisplay;
import subsystems.LogDisplayer;
import subsystems.SystemLog;

import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;

/**
 * NavigationDisplay - org.ladbury.main
 * Created by MAWood on 24/12/2016.
 */
public class LogHandler implements LogDisplayer, Serializable
{
    public LogHandler()
    {
    }

    @Override
    public void showEntry(String entry) throws RemoteException
    {
        System.out.println(entry);
        LogDisplay ld = LogDisplay.getLogDisplay();
        if(ld != null) ld.displayLog(entry);
    }
}
