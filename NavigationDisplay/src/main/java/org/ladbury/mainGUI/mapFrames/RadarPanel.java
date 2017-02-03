package org.ladbury.mainGUI.mapFrames;

import dataTypes.PolarCoordinatesD;
import dataTypes.TimeStampedPolarCoordD;
import dataTypes.TimestampedData2f;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.Arrays;

/**
 * RadarPanel   -   Displays a set of radar data
 */
public class RadarPanel extends JPanel
{
    private static final int PREF_W = 400;
    private static final int PREF_H = PREF_W;
    private static final Color EDGE_COLOR = Color.black;
    private static final Color RADAR_COLOR = Color.blue;
    private static final Color BACKGROUND_COLOR = Color.lightGray;
    private static final Color OBJECT_COLOR = Color.gray;
    private static final Stroke EDGE_STROKE = new BasicStroke(2f);
    private static final Stroke RADAR_STROKE = new BasicStroke(4f);
    private double scale;
    private double centreX;
    private  double centreY;
    private Path2D path;
    private int panelHight;
    private int panelWidth;
    private int maxRange;
    private final float cutOffThreshold;

     /**
      * RadarPanel          -   Constructor
      * @param maxRange     -   the Range of the radar, for scaling
      */
     public RadarPanel(int maxRange, float cutOffThreshold)
     {
         this.maxRange = maxRange;
         this.centreX = PREF_W/2;
         this.centreY = PREF_H/2;
         this.scale = (Math.min(PREF_W,PREF_H)/2f)/(float)maxRange;
         this.panelHight = PREF_H;
         this.panelWidth = PREF_W;
         this.cutOffThreshold = cutOffThreshold;
         path = new Path2D.Double();
     }

     /**
      * plot            -   Plots the radar data received
      * @param ranges   -   Polar coordinates .X contains range, .Y contains angle in degrees
      */
     public void plot(TimestampedData2f[] ranges)
     {
         float point = 0;
         path = new Path2D.Double();
         for(TimestampedData2f range:ranges)
         {
             if(range == null) continue;
             if(range.getX() > cutOffThreshold) range.setX(maxRange);
             point = Math.max(point, range.getX());
             double dX = (scale * range.getX() * Math.cos(Math.toRadians(range.getY()))) + centreX;
             double dY = (scale * range.getX() * Math.sin(Math.toRadians(range.getY()))) + centreY;
             if (path.getCurrentPoint() == null)
             {
                 path.moveTo(dX, dY);
             } else path.lineTo(dX, dY);
         }
         path.closePath();
     }

    /**
     * plot            -   Plots the radar data received
     * @param polars   -   Polar coordinates in radians
     */
    public void plot(TimeStampedPolarCoordD[] polars)
    {
        double point = 0;
        path = new Path2D.Double();
        for(TimeStampedPolarCoordD polar:polars)
        {
            if(polar == null) continue;
            if(polar.getData().getR() > cutOffThreshold) polar.getData().setR(maxRange); //suspect!
            point = Math.max(point, polar.getData().getR());
            double dX = (scale * polar.getData().getR() * Math.cos(polar.getData().getTheta())) + centreX;
            double dY = (scale * polar.getData().getR() * Math.sin(polar.getData().getTheta())) + centreY;
            if (path.getCurrentPoint() == null)
            {
                path.moveTo(dX, dY);
            } else path.lineTo(dX, dY);
        }
        path.closePath();
    }

    /**
     * plot            -   Plots the radar data received
     * @param polars   -   Polar coordinates in radians
     */
    public void plot(PolarCoordinatesD[] polars)
    {
        double point = 0;
        path = new Path2D.Double();
        for(PolarCoordinatesD polar:polars)
        {
            if(polar.getR() > cutOffThreshold) polar.setR(maxRange);
            point = Math.max(point, polar.getR());
            double dX = (scale * polar.getR() * Math.cos(polar.getTheta())) + centreX;
            double dY = (scale * polar.getR() * Math.sin(polar.getTheta())) + centreY;
            if (path.getCurrentPoint() == null)
            {
                path.moveTo(dX, dY);
            } else path.lineTo(dX, dY);
        }
        path.closePath();
    }

    /**
      * paintComponent  -   draws the radar display
      * @param g        -   graphics plane
      */
     @Override
     protected void paintComponent(Graphics g)
     {
         panelWidth = getWidth();
         panelHight = getHeight();
         centreX = panelWidth/2;
         centreY = panelHight/2;
         float panelMinDim = Math.min(panelWidth,panelHight);
         scale = panelMinDim/2f/(float)maxRange;
         super.paintComponent(g);
         Graphics2D g2 = (Graphics2D) g;
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                 RenderingHints.VALUE_ANTIALIAS_ON);

         // draw the visible objects, leaving the centre as background
         Ellipse2D.Double radarExtent = new Ellipse2D.Double(centreX-panelMinDim/2f,centreY-panelMinDim/2f,panelMinDim,panelMinDim);
         Area visibleObjects = new Area(radarExtent);
         Area boundary = new Area(path);
         visibleObjects.subtract(boundary);
         g2.setPaint(OBJECT_COLOR);
         g2.setBackground(BACKGROUND_COLOR);
         g2.fill(radarExtent);
         g2.setColor(BACKGROUND_COLOR);
         g2.fill(path);
         //colour in the edges
         g2.setColor(EDGE_COLOR);
         g2.setStroke(EDGE_STROKE);
         g2.drawOval((int) centreX -1,(int) centreY -1,2,2); // draw the center point
         g2.draw(radarExtent); //draw a circle at edge of range
         g2.setColor(RADAR_COLOR);
         g2.setStroke(RADAR_STROKE);
         g2.draw(path);

     }

     @Override
     public Dimension getPreferredSize()
     {
         if (isPreferredSizeSet())
         {
             return super.getPreferredSize();
         }
         return new Dimension(PREF_W, PREF_H);
     }
}