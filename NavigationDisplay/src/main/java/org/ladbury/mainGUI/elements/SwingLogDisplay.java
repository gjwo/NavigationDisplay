package org.ladbury.mainGUI.elements;

import logging.LogEntry;
import logging.RemoteLog;
import org.ladbury.userInterfacePkg.UiStyle;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.ArrayList;

/**
 * NavigationDisplay - org.ladbury.userInterfacePkg
 * Created by MAWood on 26/12/2016.
 */
public class SwingLogDisplay extends JTextArea implements Runnable
{
    private final Thread thread;

    private ArrayList<LogEntry> remoteEntries;
    private final ArrayList<LogEntry> localEntries;
    private int count;
    private final Registry registry;

    public SwingLogDisplay(Registry registry)
    {
        super();
        this.registry = registry;
        this.setText("");
        this.setBackground(Color.pink);
        this.setColumns(80);
        this.setCursor(null);
        this.setEditable(false);
        this.setFont(UiStyle.NORMAL_FONT);
        this.setRows(20);
        DefaultCaret caret = (DefaultCaret)this.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        this.count = 0;

        this.localEntries = new ArrayList<>();

        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run()
    {
        while(!Thread.interrupted())
        {

            try
            {
                remoteEntries = ((RemoteLog) registry.lookup("Log")).getEntries();
            } catch (RemoteException | NotBoundException ignored) {}
            if(count < remoteEntries.size())
            {
                for(; count<remoteEntries.size(); count++)
                {
                    localEntries.add(remoteEntries.get(count));
                }
                updateText();
            } else
                try
                {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
        }
    }

    private void updateText()
    {
        String text = "";
        for(LogEntry entry:localEntries)
        {
            text += entry.toString();
            text += System.lineSeparator();
        }
        this.setText(text);
    }

    public void stop()
    {
        thread.interrupt();
    }
}
