package org.ladbury.userInterfacePkg;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.ladbury.mainGUI.instrumentFrames.DynamicLineAndTimeSeriesChart;
import org.ladbury.main.NavClientGUI;
import org.ladbury.mainGUI.motorFrames.MotorControlFrame;

public class UiFrame
    extends JFrame {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    // variables
    private JPanel contentPane;
    private final BorderLayout borderLayout1 = new BorderLayout();
    private final JMenuBar jMenuBar1 = new JMenuBar();

    private final JMenu jMenuFile = new JMenu("File");
    private JMenuItem jMenuFileOpen = new JMenuItem("Open");
    private JMenuItem jMenuFileSave = new JMenuItem("Save");
    
    private final JMenuItem jMenuFileMotorControl = new JMenuItem("Motor Control");
    
    private final JMenuItem jMenuFileExit = new JMenuItem("Exit");

    
    private final JMenu jMenuChart = new JMenu("Chart");
    private final JMenuItem jMenuChartLine = new JMenuItem("Line 2D");
//    private JMenuItem jMenuChartHistogram = new JMenuItem("Histogram");
//    private JMenuItem jMenuChartScatter = new JMenuItem("Scatter");   

    private final JMenu jMenuHelp = new JMenu("Help");
    private final JMenuItem jMenuHelpAbout = new JMenuItem("About..");
    private final LogDisplay logDisplay;

    private FileDialog fileDialogue = null;
	private String 	windowTitle = null;
	private DynamicLineAndTimeSeriesChart dynamicGraph;
	public DynamicLineAndTimeSeriesChart getDynamicGraph(){return dynamicGraph;}
	public LogDisplay getLogDisplay(){return logDisplay;}
    //
    // Construct the frame
    //
    public UiFrame(String str) {
        super(str);
        logDisplay  = new LogDisplay(this);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            windowTitle = str;
            jbInit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    //
    // Component initialisation
    //
    private void jbInit()
    {
        // Create a content pane
        contentPane = (JPanel)this.getContentPane();
        contentPane.setLayout(borderLayout1);
        this.setSize(new Dimension(800, 600));
        this.setTitle(windowTitle);
        // add a menu bar
        this.setJMenuBar(jMenuBar1);
        
        // initialise the menus
        createFileMenu();
        createChartMenu();
        createHelpMenu();
 
        contentPane.add(logDisplay.getLogDisplayArea(), BorderLayout.CENTER);
    }

    private void createFileMenu(){
    	
    	jMenuFileMotorControl.addActionListener(this::jMenuFileMotorControl_actionPerformed);
    	jMenuFile.add(jMenuFileMotorControl);
        jMenuFile.add(jMenuFileExit);

        jMenuFileExit.addActionListener(this::jMenuFileExit_actionPerformed);
        jMenuBar1.add(jMenuFile);
    }

    private void createChartMenu(){
        // add sub items and their actions    
    	
        jMenuChartLine.addActionListener(this::jMenuChartLine_actionPerformed);
        jMenuChart.add(jMenuChartLine);
    }
   
    private void createHelpMenu(){
        // add sub items and their actions      
        jMenuHelpAbout.addActionListener(this::jMenuHelpAbout_actionPerformed);
        jMenuHelp.add(jMenuHelpAbout);
        // add the menu to the menu bar
        jMenuBar1.add(jMenuHelp);
    }

    //Overridden so we can exit when window is closed
    public void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            NavClientGUI.getMain().stop();
        	//System.exit(0);
        }
    }

    //
    //File | Open action performed
    //
    public void jMenuFileOpen_actionPerformed(ActionEvent e) {
        //FilenameFilter m_filter = "*.csv";
        // create a file dialogue
        fileDialogue = new FileDialog(this, "Open measurement readings (.csv) file");
        //fileDialogue.setFilenameFilter((FilenameFilter)"*.csv");
        fileDialogue.setDirectory("c:/");
        fileDialogue.setVisible(true);
        // File Dialogue is modal so won't return unless file or cancel
        if(fileDialogue.getFile() != null ){
        	NavClientGUI.getMain().change_state(NavClientGUI.RunState.OPEN_FILE); //trigger processing in org.ladbury.main loop
        }
        // else the user cancelled the dialog, do nothing
    }

    //
    //File | Save action performed
    //
    public void jMenuFileSave_actionPerformed(ActionEvent e) {
        //FilenameFilter m_filter = "*.csv";
        // create a file dialogue
        fileDialogue = new FileDialog(this, "Open measurement readings (.csv) file");
        //fileDialogue.setFilenameFiter((FilenameFilter)"*.csv");
        fileDialogue.setDirectory("c:/");
        fileDialogue.setVisible(true);
        // File Dialogue is modal so won't return unless file or cancel
        if(fileDialogue.getFile() != null ){
        	NavClientGUI.getMain().change_state(NavClientGUI.RunState.SAVE_FILE);  //trigger processing in org.ladbury.main loop
        }
    }
    
    //
    //File | Exit action performed
    //
    private void jMenuFileExit_actionPerformed(ActionEvent e) {
        NavClientGUI.getMain().stop();
    }
    
    private void jMenuFileMotorControl_actionPerformed(ActionEvent e)
    {
    	new MotorControlFrame();
    }
    //
    //Chart Line action performed
    //
    private void jMenuChartLine_actionPerformed(ActionEvent e) {
    	dynamicGraph = new DynamicLineAndTimeSeriesChart();
        dynamicGraph.pack();
        dynamicGraph.setVisible(true);
        NavClientGUI.getMain().change_state(NavClientGUI.RunState.PROCESS_READINGS);  //trigger processing in org.ladbury.main loop

    }
    

      //
      //Help About action performed
      //
      private void jMenuHelpAbout_actionPerformed(ActionEvent e) {
          UiAboutBox dlg = new UiAboutBox(this);
          Dimension dlgSize = dlg.getPreferredSize();
          Dimension frmSize = getSize();
          Point loc = getLocation();
          dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                          (frmSize.height -
                           dlgSize.height) / 2 + loc.y);
          dlg.setModal(true);
          dlg.setVisible(true);
      }

    public FileDialog getFileDialog() {
		return fileDialogue;
	}

	public void setFileDialog(FileDialog m_filediag1) {
		this.fileDialogue = m_filediag1;
		}
}