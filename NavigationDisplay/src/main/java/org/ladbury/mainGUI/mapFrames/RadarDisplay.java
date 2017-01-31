package org.ladbury.mainGUI.mapFrames;

import dataTypes.TimestampedData1f;
import mapping.RemoteRangeScanner;
import org.ladbury.mainGUI.MainGUI;
import org.ladbury.mainGUI.SubSystemDependentJFrame;
import sensors.interfaces.UpdateListener;
import subsystems.SubSystem;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.JPanel;

/**
 * RadarChart   -   radar display for tank
 * Created by GJWood on 31/01/2017.
 */
public class RadarDisplay extends SubSystemDependentJFrame implements Runnable, UpdateListener
{
    private RemoteRangeScanner rangeScanner;
    private Thread thread;
    private volatile  boolean dataReady;
    private int rangeValuesPerRotation;
    private int displayPoints;
    private double[] plotPoints;
    private double[] angles;
    private final int displayRatio;
    private Instant lastUpdated;
    private final float MAX_RANGE_MM = 1500f;
    private RadarPanel radarPanel;


    /**
     * RadarChart   -   Constructor
     */
    public RadarDisplay()
    {
        super(EnumSet.of(SubSystem.SubSystemType.MAPPING));
        this.setTitle("Radar");
        this.dataReady = false;
        displayRatio = 16;
        if(!isDependenciesMet()) return;

        try // get the source of data
        {
            this.rangeScanner = (RemoteRangeScanner) MainGUI.registry.lookup("RangeScanner");
            this.lastUpdated = rangeScanner.lastUpdated();
            this.rangeValuesPerRotation = rangeScanner.getStepsPerRevolution();
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

        // unchanging display parameters
        displayPoints = rangeValuesPerRotation / displayRatio;
        double angle = 360f / displayPoints;
        for( int i = 0; i<displayPoints;i++)
        {
            this.angles[i] = Math.toRadians(i * angle);
        }

        // build the chart and display panel
        radarPanel = new RadarPanel(displayPoints);
        setContentPane(radarPanel); //add the panel to the ApplicationFrame
        this.setSize(radarPanel.getPreferredSize());

        // start the display and display the initial chart
        thread.start();
        this.pack();
        this.setVisible(true);
    }

    /**
     * setRadar         -   Updates the radar display values
     * @param ranges    -   an array of new values
     */
    private void setRadar(TimestampedData1f[] ranges)
    {
        float averageRange;
        if (rangeValuesPerRotation <= 0) return;
        if (displayPoints<=0) return;
        for (int i = 0; i<ranges.length; i=i+displayRatio)
        {
            averageRange=0;
            for(int j = 0; j<displayRatio; j++)
            {
                averageRange+= ranges[j+i].getX();
            }
            averageRange/=displayRatio;
            this.plotPoints[i/displayRatio] = averageRange;
            radarPanel.plot(this.angles,this.plotPoints);
        }
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
                    this.setRadar(rangeScanner.getRawRanges());
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
 class RadarPanel extends JPanel
 {
     //adapted from http://stackoverflow.com/questions/31036718/drawing-four-leaf-rose-in-java
     private static final int PREF_W = 400;
     private static final int PREF_H = PREF_W;
     private static final double SCALE = 1.0;
     private static final double DELTA_X = PREF_W/2;
     private static final double DELTA_Y = DELTA_X;
     private static final Color RADAR_COLOR = Color.blue;
     private static final Stroke RADAR_STROKE = new BasicStroke(4f);
     private final Path2D path = new Path2D.Double();
     private final int DISPLAY_POINTS;

     public RadarPanel(int displayPoints)
     {
         DISPLAY_POINTS = displayPoints;
     }

     public void plot(double[] angles, double[] ranges)
     {
         if (angles.length != DISPLAY_POINTS )
         {
             System.err.println("Radar Display points invalid");
             return;
         }
         for (int i = 0; i <DISPLAY_POINTS; i++)
         {
             double dX = SCALE * ranges[i] * Math.cos(angles[i]) + DELTA_X;
             double dY = SCALE * ranges[i] * Math.sin(angles[i]) + DELTA_Y;
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

     @Override
     protected void paintComponent(Graphics g)
     {
         super.paintComponent(g);
         Graphics2D g2 = (Graphics2D) g;
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                 RenderingHints.VALUE_ANTIALIAS_ON);
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