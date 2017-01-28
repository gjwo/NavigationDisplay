package org.ladbury.mainGUI.mapFrames;
import dataTypes.TimestampedData1f;
import mapping.RemoteRangeScanner;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.ladbury.mainGUI.SubSystemDependentJFrame;
import org.ladbury.mainGUI.MainGUI;
import sensors.interfaces.UpdateListener;
import subsystems.SubSystem;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

/**
 * RadarChart   -   radar display for tank
 * Created by GJWood on 28/01/2017.
 */
public class RadarChart extends SubSystemDependentJFrame implements Runnable, UpdateListener
{
    private RemoteRangeScanner rangeScanner;
    private Thread thread;
    private volatile  boolean dataReady;
    private int points_on_circle; //categories
    private double[] plotPoints;
    private DefaultCategoryDataset categoryDataset;
    private float angle;


    public RadarChart()
    {
        super(EnumSet.of(SubSystem.SubSystemType.MAPPING));
        this.setTitle("Radar");
        this.dataReady = false;
        if(!isDependenciesMet()) return;

        try
        {
            this.rangeScanner = (RemoteRangeScanner) MainGUI.registry.lookup("RangeScanner");
        } catch (RemoteException | NotBoundException e)
        {
            e.printStackTrace();
        }

        thread = new Thread(this);
        thread.start();
        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                thread.interrupt();
            }});

        try
        {
            points_on_circle = rangeScanner.getStepsPerRevolution();
        } catch (RemoteException e)
        {
            e.printStackTrace();
        }
        JFreeChart radarChart = createRadarChart(createRadarDataset(points_on_circle));
        ChartPanel chartPanel = new ChartPanel(radarChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(300, 270));
        chartPanel.setEnforceFileExtensions(false);
        setContentPane(chartPanel); //add the panel to the ApplicationFrame
        this.setSize(500,500);
        this.pack();
        this.setVisible(true);
    }

    private JFreeChart createRadarChart(DefaultCategoryDataset dataset)
    {
        SpiderWebPlot webPlot = new SpiderWebPlot(dataset);
        webPlot.setOutlineVisible(true);
        webPlot.setStartAngle(270); // 0 is at 3 o'clock, we want 12 o'clock
        webPlot.setWebFilled(true);
        return new JFreeChart("Radar",null, webPlot,false);
    }
    private DefaultCategoryDataset createRadarDataset(int points)
    {
        angle = 360f/points;
        categoryDataset = new DefaultCategoryDataset();
        plotPoints = new double[points];
        for( int i = 0; i<points_on_circle;i++)
        {
            categoryDataset.addValue(50, "", ((Float)((float)i*angle)).toString());
        }
        return categoryDataset;
    }
    private void setRadar(TimestampedData1f[] ranges)
    {
        if (points_on_circle <= 0) return;
        for (int i = 0; i<points_on_circle; i++)
        {
            plotPoints[i] = (double) ranges[i].getX();
            categoryDataset.setValue(plotPoints[i],"",((Float)((float)i*angle)).toString());
        }
    }

    @Override
    public void run()
    {
        while (!Thread.interrupted())
        {
            try
            {
                if (dataReady)
                {
                    dataReady = false;
                    this.setRadar(rangeScanner.getRawRanges());
                    this.setVisible(true);
                }
                // add wait for data ready call
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException | RemoteException ignored) {}
        }
    }
    @Override
    public void dataUpdated()
    {
        dataReady = true;
    }
}