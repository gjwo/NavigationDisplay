package org.ladbury.main;

import org.ladbury.mainGUI.instrumentFrames.CubeFrame;
import org.ladbury.mainGUI.instrumentFrames.DynamicLineAndTimeSeriesChart;
import org.ladbury.mainGUI.instrumentFrames.InstrumentCompass;

import dataTypes.TimestampedData3f;

class NavDisplay {
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
		this.dynamicGraph = new DynamicLineAndTimeSeriesChart();
		dynamicGraph.setLocation(10, 350);
		this.compass = new InstrumentCompass();
		compass.setLocation(300, 10);
		this.cube = new CubeFrame();
		//new MainFrame(cube, 256, 256).setLocation(10, 10);;

	}

	public void initDisplay()
	{
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
