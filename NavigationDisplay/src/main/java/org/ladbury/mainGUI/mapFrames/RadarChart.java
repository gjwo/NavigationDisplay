package org.ladbury.mainGUI.mapFrames;
import dataTypes.TimestampedData1f;
import mapping.RemoteRangeScanner;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.util.TableOrder;
import org.ladbury.mainGUI.SubSystemDependentJFrame;
import org.ladbury.mainGUI.MainGUI;
import sensors.interfaces.UpdateListener;
import subsystems.SubSystem;
import org.jfree.util.Rotation;

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
    private int rangeValuesPerRotation;
    private int displayPoints; // no of categories diplayed on chart
    private double[] plotPoints;
    private DefaultCategoryDataset categoryDataset;
    private float angle;
    private final int displayRatio;


    /**
     * RadarChart   -   Constructor
     */
    public RadarChart()
    {
        super(EnumSet.of(SubSystem.SubSystemType.MAPPING));
        this.setTitle("Radar");
        this.dataReady = false;
        displayRatio = 16;
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
            rangeValuesPerRotation = rangeScanner.getStepsPerRevolution();
        } catch (RemoteException e)
        {
            e.printStackTrace();
        }
        displayPoints = rangeValuesPerRotation / displayRatio;
        JFreeChart radarChart = createRadarChart(createRadarDataset(displayPoints));
        ChartPanel chartPanel = new ChartPanel(radarChart);
        int plotSize = 500;
        chartPanel.setPreferredSize(new java.awt.Dimension(plotSize, plotSize));
        chartPanel.setEnforceFileExtensions(false);
        setContentPane(chartPanel); //add the panel to the ApplicationFrame
        this.setSize(plotSize, plotSize);
        this.pack();
        this.setVisible(true);
    }

    private JFreeChart createRadarChart(CategoryDataset dataset)
    {
        SpiderWebPlot webPlot = new SpiderWebPlot(dataset, TableOrder.BY_ROW);
        webPlot.setToolTipGenerator(new StandardCategoryToolTipGenerator());
        //webPlot.setOutlineVisible(true);
        webPlot.setStartAngle(270); // 0 is at 3 o'clock, we want 12 o'clock
        //webPlot.setWebFilled(true);
        webPlot.setDirection(Rotation.CLOCKWISE);
        return new JFreeChart("Radar",null, webPlot,false);
    }
    private CategoryDataset createRadarDataset(int points)
    {
        // see http://stackoverflow.com/questions/32862913/how-to-draw-a-spiderchart-above-a-existing-jfreechart
        // and http://www.jfree.org/phpBB2/viewtopic.php?f=3&t=28485
        angle = 360f/points;
        categoryDataset = new DefaultCategoryDataset();
        plotPoints = new double[points];
        //only one row in this dataset, could add times at a later point
        for( int i = 0; i<points;i++)
        {
            categoryDataset.addValue(50+i, "Radar", angleLable(i * angle));
        }
        return categoryDataset;
    }
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
                averageRange+= ranges[i].getX();
            }
            averageRange/=displayRatio;
            plotPoints[i] = averageRange;
            //only one row in this dataset, could add times at a later point1
            categoryDataset.setValue(averageRange, "Radar", angleLable(i * angle));
        }
    }

    /**
     * angleLable           -   gets a category lable based on the angle of the radar scan
     * @param totalAngle    -   current total angle in degrees 0-359
     * @return              -   a string built from the angle
     */
    private String angleLable(float totalAngle){return ((Float) ((float) totalAngle)).toString();}

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