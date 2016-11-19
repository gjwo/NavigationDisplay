package main;

import java.applet.Applet;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.Paths;


import javax.swing.UIManager;

import userInterfacePkg.UiFrame;

/**
 * NavClientGUI.java:	Applet
 * 
 * This Applet processes readings from a domestic energy monitor in order to better
 * understand domestic power consumption by turning raw readings into a more understandable form.
 * Ultimately the readings are associated with devices defined by the user so that the behaviour
 * that causes power consumption in the home can be understood and modified if desired.
 * 
 * The intention is to recognise devices and their usage patterns to give a comprehensive understanding
 * with minimal manual intervention. Given the lack of uniqueness of device power signatures, some
 * intervention will almost always be required to work out what is happening
 * 
 * @author GJWood
 * @version 1.1 2012/11/29 Incorporating handling of Owl meter
 * @version 1.2 2013/11/19 Incorporating handling of Onzo meter
 */
public class NavClientGUI
    extends Applet
    implements Runnable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static enum RunState {
		IDLE, OPEN_FILE, PROCESS_FILE, PROCESS_READINGS, SAVE_FILE, PROCESS_EDGES, PROCESS_EVENTS, STOP
	};   
	private static final String PARAM_measurementfile = "measurement file";
	
    private boolean		packFrame = false;
    private boolean 	fStandAlone = true;    //	fStandAlone will be set to true if applet is run stand alone
    private	String 		displayString = null;
	private Thread 		threadNavGui = null; //Thread object for the applet
	private RunState	state = RunState.IDLE;
    
    // Application Specific data (not persistent)
    private static	NavClientGUI	NavClientMain = null; //This is the root access point for all data in the package, the only static.
    private	UiFrame 			frame = null;
    private	Files 			file = null;
//    private Meter				meter = null;
    
    //Application Specific data (persistent)
    
    // PARAMETER SUPPORT:
    //Parameters allow an HTML author to pass information to the applet;
    // the HTML author specifies them using the <PARAM> tag within the <APPLET>
    // tag.  The following variables are used to store the values of the
    // parameters.
    //--------------------------------------------------------------------------
    // Members for applet parameters
    // <type>       <MemberVar>    = <Default Value>
    //--------------------------------------------------------------------------
    private String readingsFile = "";

    // Parameter names.  To change a name of a parameter, you need only make
    // a single change.  Simply modify the value of the parameter string below.
    //--------------------------------------------------------------------------
    private final String PARAM_readingsfile = "readingsfile";

    // STANDALONE APPLICATION SUPPORT
    // The GetParameter() method is a replacement for the getParameter() method
    // defined by Applet. This method returns the value of the specified parameter;
    // unlike the original getParameter() method, this method works when the applet
    // is run as a stand alone application, as well as when run within an HTML page.
    // This method is called by GetParameters().
    //---------------------------------------------------------------------------
    String GetParameter(String strName, String args[]) {
        if (args == null) {
            // Running within an HTML page, so call original getParameter().
            //-------------------------------------------------------------------
            return getParameter(strName);
        }

        // Running as stand alone application, so parameter values are obtained from
        // the command line. The user specifies them as follows:
        //
        //	JView NavClientGUI param1=<val> param2=<"val with spaces"> ...
        //-----------------------------------------------------------------------
        int i;
        String strArg = strName + "=";
        String strValue = null;
        int nLength = strArg.length();

        try {
            for (i = 0; i < args.length; i++) {
                String strParam = args[i].substring(0, nLength);

                if (strArg.equalsIgnoreCase(strParam)) {
                    // Found matching parameter on command line, so extract its value.
                    // If in double quotes, remove the quotes.
                    //---------------------------------------------------------------
                    strValue = args[i].substring(nLength);
                    if (strValue.startsWith("\"")) {
                        strValue = strValue.substring(1);
                        if (strValue.endsWith("\"")) {
                            strValue = strValue.substring(0,
                                strValue.length() - 1);
                        }
                    }
                    break;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return strValue;
    }

    // STANDALONE APPLICATION SUPPORT
    // 	The GetParameters() method retrieves the values of each of the applet's
    // parameters and stores them in variables. This method works both when the
    // applet is run as a standalone application and when it's run within an HTML
    // page.  When the applet is run as a standalone application, this method is
    // called by the main() method, which passes it the command-line arguments.
    // When the applet is run within an HTML page, this method is called by the
    // init() method with args == null.
    //---------------------------------------------------------------------------
    void GetParameters(String args[]) {
        // Query values of all Parameters
        //--------------------------------------------------------------
        String param;

        // measurement file : Parameter description
        //--------------------------------------------------------------
        param = GetParameter(PARAM_measurementfile, args);
         if (param != null) {
             readingsFile = param;
             readingsFile = readingsFile+""; //Suppress warning
           
        }
    }

    // STANDALONE APPLICATION SUPPORT
    // 	The main() method acts as the applet's entry point when it is run
    // as a standalone application. It is ignored if the applet is run from
    // within an HTML page.
    //----------------------------------------------------------------------
    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        NavClientGUI applet_SmartPower = new NavClientGUI();
        NavClientGUI.NavClientMain = applet_SmartPower;
        //frame.add("Center", applet_SmartPower);
        applet_SmartPower.fStandAlone = true;
        applet_SmartPower.GetParameters(args);
        applet_SmartPower.init();
        applet_SmartPower.start();
    }

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
        
        // create persistent objects, data loaded in init()
     }

    // APPLET INFO SUPPORT:
    //		The getAppletInfo() method returns a string describing the applet's
    // author, copyright date, or miscellaneous information.
    //--------------------------------------------------------------------------
    public String getAppletInfo() {
        return "Name: NavClientGUI\r\n" +
            "Author: G.J.Wood Copyright 2012,2013\r\n" +
            "Created with Eclipse Indigo,Juno & Neon";
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
        if (!fStandAlone) {
            GetParameters(null);
        }
        	//set up data
        //
        // If you use a ResourceWizard-generated "control creator" class to
        // arrange controls in your applet, you may want to call its
        // CreateControls() method from within this method. Remove the following
        // call to resize() before adding the call to CreateControls();
        // CreateControls() does its own resizing.
        //----------------------------------------------------------------------
        //resize(320, 240);

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

    //		The start() method is called when the page containing the applet
    // first appears on the screen. The AppletWizard's initial implementation
    // of this method starts execution of the applet's thread.
    //--------------------------------------------------------------------------
    public void start() {
        if (this.threadNavGui == null) {
            this.threadNavGui = new Thread(this,"GUIThread");
            this.threadNavGui.start();
        }
        //Place additional applet start code here
    }

    //		The stop() method is called when the page containing the applet is
    // no longer on the screen. The AppletWizard's initial implementation of
    // this method stops execution of the applet's thread.
    //--------------------------------------------------------------------------
    public void stop() {
        if (this.threadNavGui != null) {
            this.change_state(RunState.STOP);
            //this.threadSmartPower = null;
        }
    }

    // THREAD SUPPORT
    //		The run() method is called when the applet's thread is started. If
    // your applet performs any ongoing activities without waiting for user
    // input, the code for implementing that behaviour typically goes here. For
    // example, for an applet that performs animation, the run() method controls
    // the display of images.
    //--------------------------------------------------------------------------
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
                    case PROCESS_EDGES:
                        this.frame.displayLog("Run: Processing edges\n\r");                	
                    	this.change_state(RunState.IDLE);
                    	//System.gc(); // kick off the garbage collector
                    	break;
                    case PROCESS_EVENTS:
                        this.frame.displayLog("Run: Processing Events\n\r");
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
                // Place exception-handling code here in case an
                //       InterruptedException is thrown by Thread.sleep(),
                //		 meaning that another thread has interrupted this one
                //this.frame.displayLog("!");
                //e.printStackTrace();
            	//System.out.println(e.toString());
            }
        }
        System.exit(0);
    }
    
    //
    // Access Methods
    //
	public static NavClientGUI getMain() {
		return NavClientGUI.NavClientMain; //needed to access all other dynamic data without specific access methods
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
	}
*/
}
