package org.ladbury.dataTypes;

/**
 * RPITank - sensors
 * Created by matthew on 10/07/16.
 */
public class Data3f extends Data2f{

    protected float z;

    public Data3f(float x, float y, float z) {
    	super(x,y);
        this.z = z;
    }

    public float getZ() {
        return z;
    }

	public void setZ(float z) {
		this.z = z;
	}
    public void normalize(){
		float norm;
		// Normalise measurements
		norm = (float)Math.sqrt(x*x + y*y + z*z);
		if (norm == 0.0f)
			throw new ArithmeticException(); // handle NaN
		norm = 1f / norm;
		x *= norm;
		y *= norm;
		z *= norm;		
	}

	public String toString()
	{
		final String format = "%+08.3f";
		return 	super.toString() + " z: " + String.format(format,z);
	}
	
    public Data3f clone(){return new Data3f(x,y,z);}
    public Data3f multiply(Data3f data){return new Data3f(this.getX()*data.getX(),this.getY()*data.getY(),this.getZ()*data.getZ());}
    public Data3f add(Data3f data){return new Data3f(this.getX()+data.getX(),this.getY()+data.getY(),this.getZ()+data.getZ());}

}
