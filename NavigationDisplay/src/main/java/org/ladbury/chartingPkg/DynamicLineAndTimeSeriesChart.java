package org.ladbury.chartingPkg;

import dataTypes.TimestampedData3f;
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
import org.jfree.ui.ApplicationFrame;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.TimeUnit;

/**
 * An example to show how we can create a dynamic chart.
*/
public class DynamicLineAndTimeSeriesChart extends ApplicationFrame// implements ActionListener, UpdateListener
{

    /**
	 * 
	 */
	private static final long serialVersionUID = -3069370784580469812L;

	/** The time series data. */
	private final TimeSeries yawSeries;
	private final TimeSeries pitchSeries;
	private final TimeSeries rollSeries;
    //private volatile CircularArrayRing <TimestampedData3f> navData;
    //private volatile boolean dataReady;
    private Millisecond lastMilliSec;

    /**
     * Constructs a new dynamic chart application.
     *
     * @param title  the frame title.
     */
    public DynamicLineAndTimeSeriesChart(final String title) {

        super(title);
        this.yawSeries = new TimeSeries("Yaw");
        this.pitchSeries = new TimeSeries("Pitch");
        this.rollSeries = new TimeSeries("Roll");

        final JFreeChart chart = createChart();
        
        chart.setBackgroundPaint(Color.LIGHT_GRAY);						//Sets background colour of chart       
        final JPanel content = new JPanel(new BorderLayout());			//Created JPanel to show graph on screen
        final ChartPanel chartPanel = new ChartPanel(chart); 			//Created a ChartPanel for chart area
        content.add(chartPanel);										//Added chartPanel to org.ladbury.main panel
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 500)); 	//Sets the size of whole window (JPanel)
        setContentPane(content);         								//Puts the whole content on a Frame
        
        //this.navData = new CircularArrayRing<>(60000); //set up the buffer for data
        this.lastMilliSec = new Millisecond();
    }
	//Navigation interface methods
	//@Override
	//public void dataUpdated() {this.dataReady = true;}

	/**
	 * addReading	-	Add a new reading to the circular array
	 * @param reading new data to be plotted
	 */
	public void addReading(TimestampedData3f reading)
	{
        plotNav(reading);
		//this.navData.add(reading);
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

        this.setupAxis(plot);

        this.timeSeries1(plot);
        this.timeSeries2(plot);
        this.timeSeries3(plot);

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
    
    @SuppressWarnings("JavadocReference")
    public void plotNav(TimestampedData3f reading) {
    	// plot a new point
        final Minute thisMin = new Minute();
        long milliSecs =  TimeUnit.MILLISECONDS.convert(reading.getTime(), TimeUnit.NANOSECONDS);
        int secs = (int)TimeUnit.SECONDS.convert(milliSecs, TimeUnit.MILLISECONDS);
        milliSecs = milliSecs-(1000*secs);
        final Second thisSec = new Second(secs ,thisMin);
        final Millisecond thisMilliSec = new Millisecond((int)milliSecs,thisSec);
        //System.out.println("Current Time: " + thisMin.toString() + " Secs:  "+ secs+  " Millis: "+ milliSecs+ " Current Value : "+reading.getX());
        this.lastMilliSec = thisMilliSec;
        this.yawSeries.addOrUpdate(thisMilliSec, reading.getX());
        this.pitchSeries.addOrUpdate(thisMilliSec, reading.getY());
        this.rollSeries.addOrUpdate(thisMilliSec, reading.getZ());
    }
}  