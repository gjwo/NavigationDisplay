package org.ladbury.chartingPkg;

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

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CompassPlot;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/** The available needle types. */
public class InstrumentCompass extends ApplicationFrame {
    enum NEEDLE_TYPES
    {
    	ARROW1(0,"Arrow1"),
    	LINE(1,"Line"),
    	LONG(2,"long"),
    	PIN(3,"Pin"),
    	PLUM(4,"Plum"),
    	POINTER(5,"Pointer"),
    	SHIP(6,"Ship"),
    	WIND(7,"Wind"),
    	ARROW2(8,"Arrow2");
    	final int value;
    	final String lable;
    	NEEDLE_TYPES(int v, String l ){value = v; lable = l;}
    }

	private static final long serialVersionUID = 1585778226074987267L;
	private DefaultValueDataset dataset;
	private JFreeChart chart;
	private final ChartPanel chartPanel;
	private CompassPlot plot;

	/**
     * InstrumentCompass	-	Constructor
     *
     * @param title  the frame title.
     */
    public InstrumentCompass(final String title)
    {

        super(title);

        dataset = new DefaultValueDataset(new Double(45.0)); //Create the dataset (single value)
        chart = createChart(dataset);	//Create the chart
        
        chartPanel = new ChartPanel(chart);// add the chart to a panel...
        chartPanel.setPreferredSize(new java.awt.Dimension(300, 270));
        chartPanel.setEnforceFileExtensions(false);

        setContentPane(chartPanel); //add the panel to the ApplicationFrame
    }
    
	/**
     * Creates a chart.
     * 
     * @param dataset  the dataset.
     * @return The chart.
     */
    private JFreeChart createChart(final ValueDataset dataset) 
    {
        plot = new CompassPlot(dataset);
        plot.setSeriesNeedle(NEEDLE_TYPES.POINTER.value);
        plot.setSeriesPaint(0, Color.red);
        plot.setSeriesOutlinePaint(0, Color.red);
        chart = new JFreeChart(plot);
        return chart;
    }
    
    public void setHeading(float heading)
    {
    	this.dataset.setValue(new Double(heading));
    }

    public JFreeChart getChart() {return chart;}

	public ChartPanel getChartPanel() {return chartPanel;}

    /*****************************************************************************
    / * JFREECHART DEVELOPER GUIDE                                               *
    / * The JFreeChart Developer Guide, written by David Gilbert, is available   *
    / * to purchase from Object Refinery Limited:                                *
    / *                                                                          *
    / * http://www.object-refinery.com/jfreechart/guide.html                     *
    / *                                                                          *
    / * Sales are used to provide funding for the JFreeChart project - please    * 
    / * support us so that we can continue developing free software.             *
    / ****************************************************************************
    
    /**
     * Starting point for the demonstration application.
     *
     * @param args  ignored.
     */
    public static void main(final String[] args) {
    	// incorporated in calling class, called only for demo
        final InstrumentCompass compass = new InstrumentCompass("Compass");
        compass.pack();
        compass.setVisible(true);
        compass.setLocation(300,10);
    }
}