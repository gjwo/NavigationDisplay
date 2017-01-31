package org.ladbury.mainGUI.mapFrames;

import dataTypes.TimestampedData2f;
import mapping.RemoteRangeScanner;
import org.ladbury.mainGUI.MainGUI;
import org.ladbury.mainGUI.SubSystemDependentJFrame;
import sensors.interfaces.UpdateListener;
import subsystems.SubSystem;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.Instant;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import javax.swing.*;
import java.awt.geom.Area;

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
        int MAX_RANGE_MM = 1500;
        radarPanel = new RadarPanel(MAX_RANGE_MM);
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
                TimeUnit.MILLISECONDS.sleep(500);
                timeLastUpdated = rangeScanner.lastUpdated();
                if(timeLastUpdated.isAfter(lastUpdated))
                {
                    lastUpdated = timeLastUpdated;
                    dataUpdated();
                }
            } catch (InterruptedException | RemoteException ignored) {}
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

/**
 * RadarPanel   -   Displays a set of radar data
 */
class RadarPanel extends JPanel
 {
     //adapted from http://stackoverflow.com/questions/31036718/drawing-four-leaf-rose-in-java
     private static final int PREF_W = 400;
     private static final int PREF_H = PREF_W;
     private final double SCALE;
     private static final double DELTA_X = PREF_W/2;
     @SuppressWarnings("SuspiciousNameCombination")
     private static final double DELTA_Y = DELTA_X;
     private static final Color EDGE_COLOR = Color.black;
     private static final Color RADAR_COLOR = Color.blue;
     private static final Color BACKGROUND_COLOR = Color.lightGray;
     private static final Color OBJECT_COLOR = Color.darkGray;
     private static final Stroke EDGE_STROKE = new BasicStroke(2f);
     private static final Stroke RADAR_STROKE = new BasicStroke(4f);
     private final Path2D path = new Path2D.Double();

     /**
      * RadarPanel          -   Constructor
      * @param maxRange     -   the Range of the radar, for scaling
      */
     public RadarPanel(int maxRange)
     {
         SCALE = DELTA_X/ maxRange;
     }

     /**
      * plot            -   Plots the radar data received
      * @param ranges   -   Polar coordinates .X contains range, .Y contains angle in degrees
      */
     public void plot(TimestampedData2f[] ranges)
     {
         for (int i = 0; i <ranges.length; i++)
         {
             double dX = SCALE * ranges[i].getX() * Math.cos(Math.toRadians(ranges[i].getY())) + DELTA_X;
             double dY = SCALE * ranges[i].getX() * Math.sin(Math.toRadians(ranges[i].getY())) + DELTA_Y;
             if (i == 0)
             {
                 path.moveTo(dX, dY);
             } else
             {
                 path.lineTo(dX, dY);
             }
         }
         path.closePath();
     }

     /**
      * paintComponent  -   draws the radar display
      * @param g        -   graphics plane
      */
     @Override
     protected void paintComponent(Graphics g)
     {
         super.paintComponent(g);
         Graphics2D g2 = (Graphics2D) g;
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                 RenderingHints.VALUE_ANTIALIAS_ON);

         // draw the visible objects, leaving the centre as background
         Ellipse2D.Double radarExtent = new Ellipse2D.Double(0,0,PREF_W,PREF_W);
         Area visibleObjects = new Area(radarExtent);
         Area boundary = new Area(path);
         visibleObjects.subtract(boundary);
         g2.setPaint(OBJECT_COLOR);
         g2.setBackground(BACKGROUND_COLOR);
         g2.fill(radarExtent);
         g2.setColor(BACKGROUND_COLOR);
         g2.fill(path);
         //colour in the edges
         g2.setColor(EDGE_COLOR);
         g2.setStroke(EDGE_STROKE);
         g2.drawOval((int)DELTA_X-1,(int)DELTA_Y-1,2,2); // draw the center point
         g2.draw(radarExtent); //draw a circle at edge of range
         g2.setColor(RADAR_COLOR);
         g2.setStroke(RADAR_STROKE);
         g2.draw(path);

     }

     @Override
     public Dimension getPreferredSize()
     {
         if (isPreferredSizeSet())
         {
             return super.getPreferredSize();
         }
         return new Dimension(PREF_W, PREF_H);
     }
 }