package org.ladbury.main;

import java.applet.Applet;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.Paths;
import javax.swing.UIManager;

import org.ladbury.userInterfacePkg.UiFrame;

import dataTypes.CircularArrayRing;
import dataTypes.TimestampedData3f;

/**
 * NavClientGUI.java:	Applet
 * 
 * This Applet processes readings from a navigation sensor
 * 
 * @author GJWood
 * @version 0.1 2016/11/19 MCU-9150
 */
public class NavClientGUI
    extends Applet
    implements Runnable,UpdateListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static enum RunState {
		IDLE, OPEN_FILE, PROCESS_FILE, PROCESS_READINGS, SAVE_FILE, PROCESS_EDGES, PROCESS_EVENTS, STOP
	};   
	
    private boolean		packFrame = false;
    private	String 		displayString = null;
	private Thread 		threadNavGui = null; //Thread object for the applet
	private RunState	state = RunState.IDLE;
    
    // Application Specific data (not persistent)
    private static	NavClientGUI	NavClientMain = null; //This is the root access point for all data in the package, the only static.
    private	UiFrame 			frame = null;
    private	Files 				file = null;
    
    // variables for storing data from Navigation Client
    private volatile boolean	dataReady;
    private volatile CircularArrayRing <TimestampedData3f> navData;
    private int debugLevel = 0;

    // Parameter names.  To change a name of a parameter, you need only make
    // a single change.  Simply modify the value of the parameter string below.
    //--------------------------------------------------------------------------
    private final String PARAM_readingsfile = "readingsfile";

    // NavClientGUI Class Constructor
    //----------------------------------------------------------------------
	public NavClientGUI() {

        frame = new UiFrame("Graham's Navigation client");

        //Pack frames that have useful preferred size info, e.g. from their layout
        //Validate frames that have preset sizes
        if (packFrame) {
            frame.pack();
        }
        else {
            frame.validate();

            // Centre the frame
        }
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        frame.setLocation( (screenSize.width - frameSize.width) / 2,
                          (screenSize.height - frameSize.height) / 2);
        frame.setVisible(true);

        displayString = new String("no line");

        //file = new Files();
        
        this.navData = new CircularArrayRing <TimestampedData3f>(50);
     }
	public NavClientGUI(int debug) {
		this();
		this.debugLevel = debug;
	}

    // APPLET INFO SUPPORT:
    //		The getAppletInfo() method returns a string describing the applet's
    // author, copyright date, or miscellaneous information.
    //--------------------------------------------------------------------------
    public String getAppletInfo() {
        return "Name: NavClientGUI\r\n" +
            "Author: G.J.Wood Copyright 2016\r\n";
    }

    // PARAMETER SUPPORT
    //		The getParameterInfo() method returns an array of strings describing
    // the parameters understood by this applet.
    //
    // NavClientGUI Parameter Information:
    //  { "Name", "Type", "Description" },
    //--------------------------------------------------------------------------
    public String[][] getParameterInfo() {
        String[][] info = {
            {
            PARAM_readingsfile, "String",
            "The name of the input file"}
            ,
        };
        return info;
    }

    // The init() method is called by the AWT when an applet is first loaded or
    // reloaded.  Override this method to perform whatever initialisation your
    // applet needs, such as initialising data structures, loading images or
    // fonts, creating frame windows, setting the layout manager, or adding UI
    // components.
    //--------------------------------------------------------------------------
    public void init() {

        // Place additional initialisation code here
    }

    // Place additional applet clean up code here.  destroy() is called when
    // when you applet is terminating and being unloaded.
    //-------------------------------------------------------------------------
    public void destroy() {
        // Place applet cleanup code here
    }

    // NavClientGUI Paint Handler
    //--------------------------------------------------------------------------
    public void paint(Graphics g) {
        // Place applet paint code here
        g.drawString("Running: " + Math.random(), 10, 20);
        //g.drawString("Filename: " + this.file.inputFilename(), 10, 40);
        //g.drawString("Directory name: " +this.file.inputPathname(), 10, 60);
       // g.drawString("Filestate: " + this.file.state(), 10, 80);
        g.drawString(displayString, 10, 90);
    }

     public void start() {
        if (this.threadNavGui == null) {
            this.threadNavGui = new Thread(this,"GUIThread");
            this.threadNavGui.start();
        }
        //Place additional applet start code here
    }

    public void stop() {
        if (this.threadNavGui != null) {
            this.change_state(RunState.STOP);
            //this.threadSmartPower = null;
        }
    }

    public void run() {
    	int j = 0;
    	//get data
        while (this.get_state() != RunState.STOP) {
            try {
                switch (this.get_state()) {
                    case IDLE:
                        this.frame.displayLog(".");
                        j++;
                        if (j == 79){ this.frame.displayLog("\n\r"); j=0;}
                        repaint();
                        Thread.sleep(5000);
                        break;
                    case OPEN_FILE: //this state triggered by the user opening a file
                        this.frame.displayLog("\n\rRun: Opening file\n\r");
                        repaint();
                        //this.file.setInputFilename(this.frame.getFileDialog().getFile());
                        //this.file.setInputPathname(this.frame.getFileDialog().getDirectory());
                        //if ( !(this.file.inputFilename() == null | this.file.inputPathname() == null)) {
                        //    this.file.openInput();
                            //frame.displayLog("Run: back from open\n");
                        //    this.change_state(RunState.PROCESS_FILE);
                        //} else {
                        	this.change_state(RunState.STOP);
                        //}
                        break;
                    case PROCESS_FILE: 
                        this.frame.displayLog("Run: Processing file\n\r");
                        repaint();
                       	this.change_state(RunState.PROCESS_READINGS);
                        break;
                    case PROCESS_READINGS:
                        this.frame.displayLog("Run: Processing readings\n\r");                	
                        repaint();
                        this.frame.displayLog("Run: Completed processing readings\n\r");                	
                        repaint();
                        this.change_state(RunState.IDLE);
                        //System.gc(); // kick off the garbage collector
                        break;
                    case SAVE_FILE: //this state triggered by user selecting save file
                        this.frame.displayLog("\n\rRun: Saving files\n\r");
                        repaint();
                        //		this.file.setOutputFilename(this.frame.getFileDialog().getFile());
                        //		this.file.setOutputPathname(this.frame.getFileDialog().getDirectory());
                        //		if ( !(this.file.outputFilename() == null | this.file.outputPathname() == null)) {
                        //			this.file.OutputMetricAsCSVFile(MeterType.ONZO, 
                        //					getCurrentMeter().getMetric(currentMetricType));
                        //			this.file.OutputActivityAsCSVFile(	getCurrentMeter().getMetric(currentMetricType).getName(),
                        //        										getData().getActivity());
                        //		}
                         //frame.displayLog("Run: back from open\n");
                        this.change_state(RunState.IDLE);
                        //System.gc(); // kick off the garbage collector
                        break;
                    default:
                        repaint();
                        Thread.sleep(1000);
                }

            }
            catch (InterruptedException e) {
            	this.stop();
            }
        }
        System.exit(0);
    }
    
    //
    // Access Methods
    //
	public static NavClientGUI getMain() {
		return NavClientGUI.getNavClientMain(); //needed to access all other dynamic data without specific access methods
	}
    public synchronized void change_state(RunState new_state) {
    	this.state = new_state;
    	//this.threadSmartPower.interrupt(); // this caused persistence to fail
    }
    public synchronized RunState get_state() {
        return (this.state);
    }
    public void display(String s) {
        this.displayString = new String(s);
        System.out.println(this.displayString);
        repaint();
    }
	public  UiFrame getFrame() {
		return this.frame;
	}
	/*
	protected  FileAccess getFile() {
		return this.file;
	}
	 */

	public static NavClientGUI getNavClientMain() {
		return NavClientMain;
	}

	public static void setNavClientMain(NavClientGUI navClientMain) {
		NavClientMain = navClientMain;
	}

	//Navigation interface methods
	@Override
	public void dataUpdated() {this.dataReady = true;}
	public void addReading(TimestampedData3f reading)
	{
		this.navData.add(reading);
	}
}
