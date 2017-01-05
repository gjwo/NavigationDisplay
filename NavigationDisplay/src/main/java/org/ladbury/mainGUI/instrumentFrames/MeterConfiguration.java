package org.ladbury.mainGUI.instrumentFrames;

public enum MeterConfiguration
{
	BATTERY_METER(0,12,11.5,0,15,15,12,11.1,"Volt Meter", "Volts");

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
