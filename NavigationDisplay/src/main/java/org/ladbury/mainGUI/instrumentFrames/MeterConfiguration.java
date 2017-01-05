package org.ladbury.mainGUI.instrumentFrames;

public enum MeterConfiguration
{
	BATTERY_METER(10,12,11.1,10,12.6,12.6,12,11.1,"Volt Meter", "Volts"),
	CURRENT_METER(0,0,2000,3300,3700,2000,3300,3700,"Current Meter", "mAmps"),
	POWER_METER(0,0,30,60,100,30,60,100,"Power Meter", "Watts");

	final double scaleStart;
	final double scaleNormalStart;
    final double scaleWarningStart;
    final double scaleCriticalStart;
    final double scaleEnd;
    final double scaleNormalEnd;
    final double scaleWarningEnd;
    final double scaleCriticalEnd;
    final String name;
    final String units; 
    
    //Constructor
    MeterConfiguration(double scaleStart,double scaleNormalStart,double scaleWarningStart,double scaleCriticalStart,
					double scaleEnd,double scaleNormalEnd,double scaleWarningEnd,double scaleCriticalEnd,
					String name,String units)
    {
		this.scaleStart = scaleStart;
		this.scaleNormalStart = scaleNormalStart;
	    this.scaleWarningStart = scaleWarningStart;
	    this.scaleCriticalStart = scaleCriticalStart;
	    this.scaleEnd = scaleEnd;
	    this.scaleNormalEnd = scaleNormalEnd;
	    this.scaleWarningEnd = scaleWarningEnd;
	    this.scaleCriticalEnd = scaleCriticalEnd;
	    this.name = name;
	    this.units = units; 
	    //if ((start < scaleStart) | (start > scaleEnd) |(end < scaleEnd) |(end > scaleEnd)) return false;	
    }
}
