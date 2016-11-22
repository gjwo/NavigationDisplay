package org.ladbury.userInterfacePkg;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Point;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.ladbury.chartingPkg.DynamicLineAndTimeSeriesChart;
import org.ladbury.main.NavClientGUI;

public class UiFrame
    extends JFrame {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    // variables
    private JPanel contentPane;
    private BorderLayout borderLayout1 = new BorderLayout();
    private JMenuBar jMenuBar1 = new JMenuBar();

    private JMenu jMenuFile = new JMenu("File");
    private JMenuItem jMenuFileOpen = new JMenuItem("Open");
    private JMenuItem jMenuFileSave = new JMenuItem("Save");
    private JMenuItem jMenuFileExit = new JMenuItem("Exit");

    
    private JMenu jMenuChart = new JMenu("Chart");
    private JMenuItem jMenuChartLine = new JMenuItem("Line 2D");
//    private JMenuItem jMenuChartHistogram = new JMenuItem("Histogram");
//    private JMenuItem jMenuChartScatter = new JMenuItem("Scatter");   

    private JMenu jMenuHelp = new JMenu("Help");
    private JMenuItem jMenuHelpAbout = new JMenuItem("About..");
    private TextArea textArea1 = new TextArea();

    private FileDialog fileDialogue = null;
	private String 	windowTitle = null;
	private DynamicLineAndTimeSeriesChart dynamicGraph;
	public DynamicLineAndTimeSeriesChart getDynamicGraph(){return dynamicGraph;}
    //
    // Construct the frame
    //
    public UiFrame(String str) {
        super(str);
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
    private void jbInit() throws Exception {
        // Create a content pane
        contentPane = (JPanel)this.getContentPane();
        contentPane.setLayout(borderLayout1);
        this.setSize(new Dimension(800, 600));
        this.setTitle(windowTitle);
        // add a menu bar
        this.setJMenuBar(jMenuBar1);
        
  
        // initialise the menus
        //createFileMenu();
        createChartMenu();
        createHelpMenu();
 
        // add log text area
        textArea1.setBackground(Color.pink);
        textArea1.setColumns(80);
        textArea1.setCursor(null);
        textArea1.setEditable(false);
        textArea1.setFont(UiStyle.NORMAL_FONT);
        textArea1.setRows(20);
        textArea1.setText("Smart Power System Messages\n");

        contentPane.add(textArea1, BorderLayout.CENTER);
    }
/*
    private void createFileMenu(){

        // add sub items and their actions 
        jMenuFile.add(jMenuFileOpen);
        jMenuFileOpen.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(ActionEvent e) {
                jMenuFileOpen_actionPerformed(e);
            }
        });
        jMenuFile.add(jMenuFileSave);
        jMenuFileSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jMenuFileSave_actionPerformed(e);
            }
        });
        jMenuFile.addSeparator();
        jMenuFile.add(jMenuFileExit);
        jMenuFileExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jMenuFileExit_actionPerformed(e);
            }
        });
        // add the menu to the menu bar
        jMenuBar1.add(jMenuFile);
    }
*/
  

    private void createChartMenu(){
        // add sub items and their actions    
    	
        jMenuChartLine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jMenuChartLine_actionPerformed(e);
            }
        });
        jMenuChart.add(jMenuChartLine);
/*        
        jMenuChartHistogram.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jMenuChartHistogram_actionPerformed(e);
            }
        });
        jMenuChart.add(jMenuChartHistogram);
        jMenuChartScatter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //jMenuChartScatter_actionPerformed(e);
            }
        });
        jMenuChart.add(jMenuChartScatter);
       // add the menu to the menu bar
        jMenuBar1.add(jMenuChart);*/
    }
   
    private void createHelpMenu(){
        // add sub items and their actions      
        jMenuHelpAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jMenuHelpAbout_actionPerformed(e);
            }
        });
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
        //fileDialogue.setFilenameFilter((FilenameFilter)"*.csv");
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
    public void jMenuFileExit_actionPerformed(ActionEvent e) {
        NavClientGUI.getMain().stop();
    }
    //
    //Chart Line action performed
    //
    public void jMenuChartLine_actionPerformed(ActionEvent e) {
    	dynamicGraph = new DynamicLineAndTimeSeriesChart("Navigation Data");
        dynamicGraph.pack();
        dynamicGraph.setVisible(true);
        NavClientGUI.getMain().change_state(NavClientGUI.RunState.PROCESS_READINGS);  //trigger processing in org.ladbury.main loop

    }
/*
    //
    //Histogram action performed
    //
    public void jMenuChartHistogram_actionPerformed(ActionEvent e) {
    	
    	Meter m = NavClientGUI.getMain().getData().getMeters().get(0);  
    	ArrayList<TimeHistogram> histograms = new ArrayList<TimeHistogram>(Collections.<TimeHistogram>emptyList());
    	for (int i = 0; i<m.getMetricCount(); i++ ){
    		if (m.getMetric(i).getReadingsCount()>0){
    			histograms.add(new TimeHistogram("Power Histogram",m.getMetric(i),"Power (W)"));
    		}
    	}
		for (int i = 0; i<histograms.size();i++){
			histograms.get(i).setLocation(i*20, i*20);  //cascade windows
			histograms.get(i).pack();
			//RefineryUtilities.centerFrameOnScreen(histograms.get(i));
			histograms.get(i).setVisible(true);	
		}
		
    }
*/    
      public void displayLog(String str) {
        textArea1.append(str);
        repaint();
    }

      //
      //Help About action performed
      //
      public void jMenuHelpAbout_actionPerformed(ActionEvent e) {
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

    //
    // write an integer to the log area
    //
    public void displayLog(int i) {
        Integer intWrapper = new Integer(i);
        textArea1.append(intWrapper.toString());
        repaint();
    }
    public FileDialog getFileDialog() {
		return fileDialogue;
	}

	public void setFileDialog(FileDialog m_filediag1) {
		this.fileDialogue = m_filediag1;
		}
}