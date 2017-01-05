package org.ladbury.mainGUI.instrumentFrames;

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
 * InstrumentCompass.java
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
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DialShape;
import org.jfree.chart.plot.MeterPlot;
import org.jfree.data.Range;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;
import org.jfree.ui.RectangleInsets;
import org.ladbury.mainGUI.SubSystemDependentJFrame;
import org.ladbury.mainGUI.MainGUI;
import subsystems.SubSystem;
import telemetry.RemoteTelemetry;

/** The available needle types. */
public class TelemetryMeter extends SubSystemDependentJFrame implements Runnable
{

	private static final long serialVersionUID = 1585778226074987267L;
	private final DefaultValueDataset dataset;
	private JFreeChart chart;
	private final ChartPanel chartPanel;

    private Thread thread;
    private RemoteTelemetry telemetry;

	/**
     * InstrumentCompass	-	Constructor
     */
    public TelemetryMeter()
    {
        super(EnumSet.of(SubSystem.SubSystemType.TELEMETRY));
        this.setTitle("Meter");

        dataset = new DefaultValueDataset(new Double(45.0)); //Create the dataset (single value)
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
                //System.out.println("RMI data: " + instruments.getTaitBryanAnglesD().toString());
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
    	
    	DefaultValueDataset data = new DefaultValueDataset(75.0);
    	MeterPlot plot = new MeterPlot(data);
    	DialShape shape = DialShape.CIRCLE;
    	
    	plot.setUnits("Degrees");
    	plot.setRange(new Range(20.0, 140.0));
    	//plot.setNormalRange(new Range(70.0, 100.0));
    	//plot.setWarningRange(new Range(100.0, 120.0));
    	//plot.setCriticalRange(new Range(120.0, 140.0));
    	
    	plot.setDialShape(shape);
     	plot.setNeedlePaint(Color.white);
    	plot.setTickLabelFont(new Font("SansSerif", Font.BOLD, 9));
    	plot.setInsets(new RectangleInsets(5, 5, 5, 5));
    	 JFreeChart chart = new JFreeChart(	"Meter Chart", 
    			 							JFreeChart.DEFAULT_TITLE_FONT, 
    			 							plot, 
    			 							false);
        return chart;
    }
    
    public void setValue(double d) {this.dataset.setValue( d);}

    public JFreeChart getChart() {return chart;}

	public ChartPanel getChartPanel() {return chartPanel;}
}