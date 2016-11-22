package org.ladbury.dataTypes;

/**
 * RPIsensors.dataTypes
 * Created by G.J.Wood 6/11/2016.
 */
public class Data2s extends Data1s
{
    protected short y;

    public Data2s(short x,short y)
    {
        super(x);
        this.y = y;
    }

    public short getY()
    {
        return y;
    }

    public void setY(short y)
    {
        this.y = y;
    }

    public void scale(short xScale,short yScale)
    {
        super.scale(xScale);
        y *= yScale;
    }

    public void offset(short xOffset,short yOffset)
    {
        super.offset(xOffset);
        y += yOffset;
    }

    public String toString()
    {
        final String format = "%+04n";
        return 	super.toString() + " y: " + String.format(format,y);
    }
    public Data2s clone()
    {
        return new Data2s(x,y);
    }
}
