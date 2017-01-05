package org.ladbury.mainGUI.instrumentFrames;

import java.awt.BasicStroke;

/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 *
 * -----------------
 * TelemetryMeter.java
 * -----------------
 * (C) Copyright 2002-2004, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: InstrumentCompass.java,v 1.7 2004/04/26 19:11:53 taqua Exp $
 *
 * Changes
 * -------
 * 04-Aug-2003 : Version 1 (DG);
 *
 */


import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DialShape;
import org.jfree.chart.plot.MeterInterval;
import org.jfree.chart.plot.MeterPlot;
import org.jfree.data.Range;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;
import org.jfree.ui.RectangleInsets;
import org.ladbury.mainGUI.SubSystemDependentJFrame;
import org.ladbury.mainGUI.MainGUI;
import subsystems.SubSystem.SubSystemType;
import telemetry.RemoteTelemetry;

/** The available needle types. */
public class Meter extends SubSystemDependentJFrame implements Runnable
{
	public class MeterParameters
	{

		private double scaleStart;
		private double scaleNormalStart;
	    private double scaleWarningStart;
	    private double scaleCriticalStart;
	    private double scaleEnd;
	    private double scaleNormalEnd;
	    private double scaleWarningEnd;
	    private double scaleCriticalEnd;
	    private String name;
	    private String units; 
	    
	    //Constructor
	    MeterParameters()
	    {
			this.scaleStart = 0;
			this.scaleNormalStart = 12;
		    this.scaleWarningStart = 11.5 ;
		    this.scaleCriticalStart = 0;
		    this.scaleEnd = 15;
		    this.scaleNormalEnd = 15;
		    this.scaleWarningEnd = 12;
		    this.scaleCriticalEnd = 11.1;
		    this.name = "Volt Meter";
		    this.units = "Volts"; 
	    	
	    }
	    
	    public void setScale(double start, double end, boolean CriticalHigh)
	    {
	    	if (CriticalHigh)
	    	{
		    	scaleStart = start;
		    	scaleEnd = end;
		        scaleNormalStart = scaleStart;
		        scaleWarningStart = scaleStart + (scaleEnd-scaleStart)*0.7;
		        scaleCriticalStart = scaleStart + (scaleEnd-scaleStart)*0.9;
		        scaleNormalEnd = scaleWarningStart;
		        scaleWarningEnd = scaleCriticalStart;
		        scaleCriticalEnd = scaleEnd;
	    	} else
	    	{
		    	scaleStart = start;
		    	scaleEnd = end;
		        scaleNormalStart = (scaleEnd-scaleStart)*0.8;
		        scaleWarningStart = scaleStart + (scaleEnd-scaleStart)*0.7;
		        scaleCriticalStart = scaleStart;
		        scaleNormalEnd = scaleEnd;
		        scaleWarningEnd = scaleStart + (scaleEnd-scaleStart)*0.8;
		        scaleCriticalEnd = scaleStart + (scaleEnd-scaleStart)*0.7;

	    	}
	    		
	    }
	    public boolean setCriticalScale(double start, double end)
	    {
	    	if ((start < scaleStart) | (start > scaleEnd) |(end < scaleEnd) |(end > scaleEnd)) return false;
	    	this.scaleCriticalStart = start;
	    	this.scaleCriticalEnd = end;
	    	return true;
	    }
	    
	    public boolean setWarningScale(double start, double end)
	    {
	    	if ((start < scaleStart) | (start > scaleEnd) |(end < scaleEnd) |(end > scaleEnd)) return false;
	    	this.scaleWarningStart = start;
	    	this.scaleWarningEnd = end;
	    	return true;
	    }
	    
	    public boolean setNormalScale(double start, double end)
	    {
	    	if ((start < scaleStart) | (start > scaleEnd) |(end < scaleEnd) |(end > scaleEnd)) return false;
	    	this.scaleNormalStart = start;
	    	this.scaleNormalEnd = end;
	    	return true;
	    }
	    
	    public void setName(String name){ this.name = name;}
	    public void setUnits(String units){this.units = units;}
	
	}
	
	private static final long serialVersionUID = 1585778226074987267L;
	private final DefaultValueDataset dataset;
	private JFreeChart chart;
	private ChartPanel chartPanel;
	private MeterParameters params;
    private Thread thread;
    private RemoteTelemetry telemetry;
 
	/**
     * TelemetryMeter	-	Constructor
     */
    public Meter(SubSystemType subSystemType)
    {
        super(EnumSet.of(subSystemType));
        this.params = new MeterParameters();
        this.setTitle(params.name);
        dataset = new DefaultValueDataset(new Double(0.0)); //Create the dataset (single value)
        chart = createChart(dataset);	//Create the chart
        
        chartPanel = new ChartPanel(chart);// add the chart to a panel...
        chartPanel.setPreferredSize(new java.awt.Dimension(300, 270));
        chartPanel.setEnforceFileExtensions(false);

        setContentPane(chartPanel); //add the panel to the ApplicationFrame
        if(!isDependenciesMet()) return;

        try
        {
            this.telemetry = (RemoteTelemetry) MainGUI.registry.lookup("Telemetry");
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

        this.setSize(300,300);
        this.setVisible(true);
    }

    @Override
    public void run()
    {
        while(!Thread.interrupted())
            try
            {
                this.setValue(telemetry.getVoltage());
                TimeUnit.MILLISECONDS.sleep(20);
            } catch (InterruptedException | RemoteException ignored) {}
    }
    
	/**
     * Creates a chart.
     * 
     * @param dataset  the dataset.
     * @return The chart.
     */
    private JFreeChart createChart(final ValueDataset dataset) 
    {
    	
    	MeterPlot plot = new MeterPlot(dataset);
    	DialShape shape = DialShape.CIRCLE;
    	
    	plot.setUnits(params.units);
    	plot.setRange(new Range(params.scaleStart, params.scaleEnd));
    	plot.addInterval(new MeterInterval("Normal", new Range(params.scaleNormalStart, params.scaleNormalEnd), Color.lightGray, new BasicStroke(2.0f),  new Color(0, 255, 0, 64)));
    	plot.addInterval(new MeterInterval("Warning", new Range(params.scaleWarningStart, params.scaleWarningEnd), Color.lightGray, new BasicStroke(2.0f), new  Color(255, 255, 0, 64)));
    	plot.addInterval(new MeterInterval("Critical", new Range(params.scaleCriticalStart, params.scaleCriticalEnd), Color.lightGray, new BasicStroke(2.0f), new Color(255, 0, 0, 128)));
    	
    	plot.setDialShape(shape);
     	plot.setNeedlePaint(Color.white);
    	plot.setTickLabelFont(new Font("SansSerif", Font.BOLD, 9));
    	plot.setTickLabelFormat(new DecimalFormat("0"));
    	plot.setTickLabelPaint(Color.white);
    	plot.setTickSize(1.0);
    	plot.setTickLabelsVisible(true);
    	plot.setInsets(new RectangleInsets(5, 5, 5, 5));
    	JFreeChart chart = new JFreeChart(	params.name, 
    			 							JFreeChart.DEFAULT_TITLE_FONT, 
    			 							plot, 
    			 							false);
        return chart;
    }
    
    public void setValue(double d) {this.dataset.setValue(d);}

    public JFreeChart getChart() {return chart;}

	public ChartPanel getChartPanel() {return chartPanel;}
}