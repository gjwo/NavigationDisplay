package org.ladbury.mainGUI.instrumentFrames;

import dataTypes.TimestampedData3f;
import inertialNavigation.RemoteInstruments;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;
import org.ladbury.mainGUI.SubSystemDependentJFrame;
import org.ladbury.mainGUI.MainGUI;
import subsystems.SubSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

/**
 * An example to show how we can create a dynamic chart.
 */
public class DynamicLineAndTimeSeriesChart extends SubSystemDependentJFrame implements Runnable
{

    /**
     *
     */
    private static final long serialVersionUID = -3069370784580469812L;

    /** The time series data. */
    private final TimeSeries yawSeries;
    private final TimeSeries pitchSeries;
    private final TimeSeries rollSeries;
    private long startTime;

    private Thread thread;
    private RemoteInstruments instruments;

    /**
     * Constructs a new dynamic chart application.
     */
    public DynamicLineAndTimeSeriesChart() {

        super(EnumSet.of(SubSystem.SubSystemType.INSTRUMENTS));
        this.setTitle("Navigation Data");
        this.setLocation(10,300);
        this.yawSeries = new TimeSeries("Yaw");
        this.pitchSeries = new TimeSeries("Pitch");
        this.rollSeries = new TimeSeries("Roll");

        if(!isDependenciesMet()) return;

        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        try
        {
            this.instruments = (RemoteInstruments) MainGUI.registry.lookup("Instruments");
        } catch (RemoteException | NotBoundException e)
        {
            e.printStackTrace();
        }

        final JFreeChart chart = createChart();

        chart.setBackgroundPaint(Color.LIGHT_GRAY);						//Sets background colour of chart       
        final JPanel content = new JPanel(new BorderLayout());			//Created JPanel to show graph on screen
        final ChartPanel chartPanel = new ChartPanel(chart); 			//Created a ChartPanel for chart area
        content.add(chartPanel);										//Added chartPanel to org.ladbury.main panel
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 500)); 	//Sets the size of whole window (JPanel)
        setContentPane(content);         								//Puts the whole content on a Frame
        startTime = System.currentTimeMillis();

        this.pack();
        this.setVisible(true);
        thread = new Thread(this);
        thread.start();
        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                thread.interrupt();
            }});
    }

    /**
     * addReading	-	Add a new reading to the circular array
     * @param reading new data to be plotted
     */
    public void addReading(TimestampedData3f reading)
    {
        plotNav(reading);
    }

    private XYDataset createDataset(final TimeSeries series) {
        return new TimeSeriesCollection(series);
    }


    private void setupAxis(XYPlot plot)
    {
        final ValueAxis xAxis = plot.getDomainAxis();
        xAxis.setAutoRange(true);

        // Domain axis would show data of 60 seconds for a time
        xAxis.setFixedAutoRange(10000.0); // 60 seconds
        xAxis.setVerticalTickLabels(true);

        final ValueAxis yAxis = plot.getRangeAxis();
        yAxis.setRange(-190.0, 370.0);

        final XYItemRenderer renderer = plot.getRenderer();
        renderer.setSeriesPaint(0, Color.RED);

        final NumberAxis yAxis1 = (NumberAxis) plot.getRangeAxis();
        yAxis1.setTickLabelPaint(Color.RED);
    }

    private void timeSeries1(final XYPlot plot) {
        final XYDataset firstDataset = this.createDataset(yawSeries);
        plot.setDataset(0, firstDataset); // the second dataset (datasets are zero-based numbering)
        plot.mapDatasetToDomainAxis(0, 0); // same axis, different dataset
        plot.mapDatasetToRangeAxis(0, 0); // same axis, different dataset

        final XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setSeriesPaint(0, Color.RED);
        plot.setRenderer(0, renderer);
    }

    private void timeSeries2(final XYPlot plot) {
        final XYDataset secondDataset = this.createDataset(pitchSeries);
        plot.setDataset(1, secondDataset); // the second dataset (datasets are zero-based numbering)
        plot.mapDatasetToDomainAxis(1, 0); // same axis, different dataset
        plot.mapDatasetToRangeAxis(1, 0); // same axis, different dataset

        final XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setSeriesPaint(0, Color.BLUE);
        plot.setRenderer(1, renderer);
    }

    private void timeSeries3(final XYPlot plot) {
        final XYDataset thirdDataset = this.createDataset(rollSeries);
        plot.setDataset(2, thirdDataset); // the third dataset (datasets are zero-based numbering)
        plot.mapDatasetToDomainAxis(2, 0); // same axis, different dataset
        plot.mapDatasetToRangeAxis(2, 0); // same axis, different dataset

        final XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setSeriesPaint(0, Color.GREEN);
        plot.setRenderer(2, renderer);
    }
    /**
     * Creates a chart with 3 series
     *
     * @return A sample chart.
     */
    private JFreeChart createChart() {
    	/*
    	 * 	createTimeSeriesChart(java.lang.String title, 
    	 * java.lang.String timeAxisLabel, 
    	 * java.lang.String valueAxisLabel, 
    	 * XYDataset dataset, 
    	 * boolean legend, 
    	 * boolean tooltips, 
    	 * boolean urls)
    	 * Creates and returns a time series chart.
    	 */
        final XYDataset dataset = this.createDataset(yawSeries);
        final JFreeChart result = ChartFactory.createTimeSeriesChart(
                "Navigation Data",
                "Time",
                "Angle (degrees)",
                dataset,
                true,
                true,
                false
        );

        final XYPlot plot = result.getXYPlot();

        plot.setBackgroundPaint(new Color(0xffffe0));
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.lightGray);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.lightGray);

        setupAxis(plot);

        timeSeries1(plot);
        timeSeries2(plot);
        timeSeries3(plot);

        return result;
    }

    /**
     * actionPerformed	-	prompt that some data is ready
     *
     * @param e  the action event.
     */
   /* public void actionPerformed(final ActionEvent e) {
    	plotNav( this.navData.get(0)); //get the last reading from the circular array
    }*/

    public void plotNav(TimestampedData3f reading) {
        final Millisecond thisMilliSec = new Millisecond(new Date(startTime + reading.getTime()/1000000L)); // construct epoch relative time using a fixed time.
        this.yawSeries.addOrUpdate(thisMilliSec, reading.getX());
        this.pitchSeries.addOrUpdate(thisMilliSec, reading.getY());
        this.rollSeries.addOrUpdate(thisMilliSec, reading.getZ());
    }

    @Override
    public void run()
    {
        while(!Thread.interrupted())
            try
            {
                this.addReading(new TimestampedData3f(instruments.getTaitBryanAnglesD()));
                //System.out.println("RMI data: " + instruments.getTaitBryanAnglesD().toString());
                TimeUnit.MILLISECONDS.sleep(20);
            } catch (InterruptedException | RemoteException ignored) {}
    }
}