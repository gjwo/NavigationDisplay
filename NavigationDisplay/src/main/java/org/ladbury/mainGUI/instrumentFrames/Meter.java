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
	
	private static final long serialVersionUID = 1585778226074987267L;
	private final DefaultValueDataset dataset;
	private JFreeChart chart;
	private ChartPanel chartPanel;
	private MeterConfiguration params;
    private Thread thread;
    private RemoteTelemetry telemetry;
 
	/**
     * TelemetryMeter	-	Constructor
     */
    public Meter(MeterConfiguration configuration)
    {
        super(EnumSet.of(SubSystemType.TELEMETRY));
        this.params = configuration;
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
                this.setValue(telemetry.getRealValue(params.name));
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
    	plot.addInterval(new MeterInterval("Normal", new Range(params.scaleNormalStart, params.scaleNormalEnd), Color.lightGray, new BasicStroke(2.0f),  new Color(72, 255, 59, 255)));
    	plot.addInterval(new MeterInterval("Warning", new Range(params.scaleWarningStart, params.scaleWarningEnd), Color.lightGray, new BasicStroke(2.0f), new  Color(253, 255, 45, 255)));
    	plot.addInterval(new MeterInterval("Critical", new Range(params.scaleCriticalStart, params.scaleCriticalEnd), Color.lightGray, new BasicStroke(2.0f), new Color(255, 37, 40, 255)));
    	plot.setDialBackgroundPaint(Color.WHITE);
    	plot.setDialShape(shape);
     	plot.setNeedlePaint(Color.BLACK);
    	plot.setTickLabelFont(new Font("Lucida Sans", Font.PLAIN, 11));
    	plot.setValueFont(new Font("Lucida Sans", Font.PLAIN, 15));
    	plot.setTickLabelPaint(Color.BLACK);
    	plot.setTickPaint(Color.BLACK);
    	plot.setTickLabelFormat(new DecimalFormat("00.00"));
    	plot.setValuePaint(Color.BLACK);
    	plot.setTickSize(Math.abs(params.scaleEnd - params.scaleStart)/10);
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