package chartingPkg;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.JFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.ladbury.meterPkg.Metric;
import org.ladbury.smartpowerPkg.Timestamped;

public class TimeHistogram extends JFrame {

	 /**
	 * 
	 */
	private static final long serialVersionUID = 2351741114969077397L;

	/**
     * Time based Histogram Constructor
     *
     * @param title  the frame title.
     */
    public TimeHistogram(final String frameTitle,Metric mtc, String numberAxisTitle) {
        super(frameTitle);
    	Timestamp t1 = mtc.getEarliest();
    	Timestamp t2 = mtc.getLatest();
    	
        XYBarRenderer.setDefaultBarPainter(new StandardXYBarPainter()); //Stops fancy bar rendering
        final XYItemRenderer renderer1 = new XYBarRenderer();
        
        final DateAxis domainAxis = new DateAxis("Time");
        final ValueAxis rangeAxis = new NumberAxis(numberAxisTitle);     
        final XYDataset data1 = mtc.getJfreeChartData(t1,  t2);
        final XYPlot plot = new XYPlot(data1, domainAxis, rangeAxis, renderer1);
        
        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setShadowVisible(false);
        plot.setDomainPannable(true);
		DateFormat df = new SimpleDateFormat(Timestamped.DATE_AND_DAYFORMAT);
        String chartTitle = frameTitle+" "+ df.format(t1);
        final JFreeChart chart = new JFreeChart(chartTitle, plot);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        chartPanel.setMouseZoomable(true, false);
        setContentPane(chartPanel);
    }
}