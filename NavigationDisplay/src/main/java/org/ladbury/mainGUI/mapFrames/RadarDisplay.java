package org.ladbury.mainGUI.mapFrames;

import mapping.RemoteRangeScanner;
import org.ladbury.mainGUI.MainGUI;
import org.ladbury.mainGUI.SubSystemDependentJFrame;
import sensors.interfaces.UpdateListener;
import subsystems.SubSystem;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.Instant;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;
import javax.swing.*;

/**
 * RadarChart   -   radar display, polls RangeScanner for updated radar data and displays it
 * Created by GJWood on 31/01/2017.
 */
public class RadarDisplay extends SubSystemDependentJFrame implements Runnable, UpdateListener
{
    private RemoteRangeScanner rangeScanner;
    private Thread thread;
    private volatile  boolean dataReady;
    private Instant lastUpdated;
    private RadarPanel radarPanel;

    /**
     * RadarChart   -   Constructor
     */
    public RadarDisplay()
    {
        super(EnumSet.of(SubSystem.SubSystemType.MAPPING));
        this.setTitle("Radar");
        this.dataReady = false;
        if(!isDependenciesMet()) return;

        try // get the source of data
        {
            this.rangeScanner = (RemoteRangeScanner) MainGUI.registry.lookup("RangeScanner");
            this.lastUpdated = rangeScanner.lastUpdated();
        } catch (RemoteException | NotBoundException e)
        {
            e.printStackTrace();
        }

        thread = new Thread(this);
        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                thread.interrupt();
            }});

        // build the chart and display panel
        final int MAX_RANGE_MM = 1200;
        radarPanel = new RadarPanel(MAX_RANGE_MM, MAX_RANGE_MM);
        setContentPane(radarPanel); //add the panel to the ApplicationFrame
        this.setSize(radarPanel.getPreferredSize());
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // start the display and display the initial chart
        thread.start();
        this.pack();
        this.setVisible(true);
    }

    /**
     * Run  -   main display loop for updating the chart
     */
    @Override
    public void run()
    {
        Instant timeLastUpdated;
        while (!Thread.interrupted())
        {
            try
            {
                if (dataReady)
                {
                    dataReady = false;
                    this.radarPanel.plot(rangeScanner.getRawRanges());
                    repaint();
                    this.setVisible(true);
                }
                try
                {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {break;}
                timeLastUpdated = rangeScanner.lastUpdated();
                if(timeLastUpdated.isAfter(lastUpdated))
                {
                    lastUpdated = timeLastUpdated;
                    dataReady = true;
                }
            } catch (RemoteException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * dataUpdated  -   a new set of data is ready (triggered internally or externally)
     */
    @Override
    public void dataUpdated()
    {
        dataReady = true;
    }
}