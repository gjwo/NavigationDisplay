package org.ladbury.main;

import org.jfree.ui.RefineryUtilities;
import org.ladbury.chartingPkg.CubeFrame;
import org.ladbury.chartingPkg.DynamicLineAndTimeSeriesChart;
import org.ladbury.chartingPkg.InstrumentCompass;

import com.sun.j3d.utils.applet.MainFrame;

import dataTypes.TimestampedData3f;

public class NavDisplay {
    private final InstrumentCompass compass;
    private final DynamicLineAndTimeSeriesChart dynamicGraph;
    private final CubeFrame cube;
    

	public NavDisplay() {
		/*
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
            e.printStackTrace();
        }*/
		this.dynamicGraph = new DynamicLineAndTimeSeriesChart("Navigation Data");
		dynamicGraph.setLocation(10, 350);
		this.compass = new InstrumentCompass("Compass");
		compass.setLocation(300, 10);
		this.cube = new CubeFrame();
		new MainFrame(cube, 256, 256).setLocation(10, 10);;	

	}

	public void initDisplay()
	{
        dynamicGraph.pack();
        dynamicGraph.setVisible(true);
        compass.pack();
        //RefineryUtilities.centerFrameOnScreen(compass);
        compass.setVisible(true);	        

	}
    public void processTaitBryanAngles(TimestampedData3f data)
    {
    	this.dynamicGraph.addReading(data);
    	this.compass.setHeading(data.getX());
    	this.cube.myRotationBehavior.setAngles(data);
    }

}
