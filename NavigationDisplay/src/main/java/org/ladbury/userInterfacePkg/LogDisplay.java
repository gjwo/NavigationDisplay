package org.ladbury.userInterfacePkg;

import org.ladbury.main.RMITest;
import logging.LogEntry;
import logging.RemoteLog;

import java.awt.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;


public class LogDisplay extends Thread
{
	private static LogDisplay logDisplay = null;
	private TextArea textArea;
	private UiFrame parent;
	private int count;
	private ArrayList<LogEntry> remoteEntries;

	
	static public LogDisplay getLogDisplay(){return logDisplay;}
	
	public LogDisplay(UiFrame parent)
	{
		this.parent = parent;
		this. textArea = new TextArea();
        // add log text area
        textArea.setBackground(Color.pink);
        textArea.setColumns(80);
        textArea.setCursor(null);
        textArea.setEditable(false);
        textArea.setFont(UiStyle.NORMAL_FONT);
        textArea.setRows(20);
        textArea.setText("");
		LogDisplay.logDisplay = this;
		count = 0;
        try
        {
            remoteEntries = ((RemoteLog)RMITest.registry.lookup("Log")).getEntries();
        } catch (RemoteException | NotBoundException e)
        {
            e.printStackTrace();
            remoteEntries = new ArrayList<>();
        }
        this.start();
    }
	public TextArea getLogDisplayArea(){return textArea;}
    
    public void displayLog(String str) 
    {
        textArea.append(str );
        parent.repaint();
    }
    
    public void displayLog(int i) 
    {
        textArea.append(Integer.toString(i));
        parent.repaint();
    }
    
    public void displayLog(long l) 
    {
        textArea.append(Long.toString(l));
        parent.repaint();
    }
    
    public void displayLog(double d) 
    {
        textArea.append(Double.toString(d));
        parent.repaint();
    }
    
    public void displayLog(float f) 
    {
        textArea.append(Float.toString(f));
        parent.repaint();
    }
    
    public void displayLog(boolean b) 
    {
        textArea.append(Boolean.toString(b));
        parent.repaint();
    }

    public void newline()
    {
    	displayLog("\n\r");
    }

    @Override
    public void run()
    {
        super.run();

        while(!Thread.interrupted())
        {

            try
            {
                remoteEntries = ((RemoteLog)RMITest.registry.lookup("Log")).getEntries();
            } catch (RemoteException | NotBoundException ignored) {}
            if(remoteEntries.size()-1 == count )
            {
                try
                {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
            } else
            {
                textArea.setText("");
                for(LogEntry entry: remoteEntries)
                {
                    displayLog(entry.toString());
                    newline();
                    count = remoteEntries.size() -1;
                }
            }
        }

    }
}
