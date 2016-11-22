package org.ladbury.dataTypes;

/**
 * TimestampedData2f - 2 dimensional time stamped floating point data structure
 * Created by MAWood on 18/07/2016, modified by G.J.Wood 10/11/2016
 */
public class TimestampedData2f extends Data2f
{
    public static final long NANOS_PER_SEC = 1000000000;
    public static final float NANOS_PER_SECF = 1000000000f;
    protected final long nanoTime;

    /**
     * TimestampedData2f	- Constructor from 2 scalars and a time
     * @param x
     * @param y
     * @param nanoTime
     */
    public TimestampedData2f(float x, float y, long nanoTime)
    {
        super(x, y);
        this.nanoTime = nanoTime;
    }

    /**
     * TimestampedData2f	- Constructor from 2 floats, system time is added
     * @param x
     * @param y
     */
    public TimestampedData2f(float x, float y)
    {
        this(x, y, System.nanoTime());
    }

    /**
      * TimestampedData2f	- Constructor Data2f, system time is added
     * @param data
     */
    public TimestampedData2f(Data2f data)
    {
        this(data.getX(),data.getY());
    }
    
    /**
     * getTime - get the timestamp
     * @return
     */
    public	long getTime()
    {
    	return nanoTime;
    }
  
    /**
     * unStamp	- return the data without the timestamp
     * @return	base data
     */
    public Data2f unStamp()
    {
        return (new Data2f(this.getX(),this.getY()));
    }

    /**
     * toString - return a formatted string representation for printing
     */
    public String toString()
    {
        String format = "%08.3f";
        return 	" t: " + String.format(format,((float)nanoTime)/NANOS_PER_SECF) +
                " " + super.toString();
    }
    
    /**
     * clone	- return a new instance with the same timestamp and values
     */
     public TimestampedData2f clone()
    {
        return new TimestampedData2f(x,y,nanoTime);
    }
}
