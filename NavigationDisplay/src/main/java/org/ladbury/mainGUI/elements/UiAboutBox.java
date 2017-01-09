package org.ladbury.mainGUI.elements;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * <p>Title: NavClientGUI</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: </p>
 * @author G.J.Wood
 * @version 0.1
 */

public class UiAboutBox
    extends JDialog
    implements ActionListener {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String product = "Navigation Client";
	private static final String version = "1.1";
	private static final String copyright = "Copyright G.J.Wood/M.A.Wood (c) 2016";
	private static final String comments = "Displays navigation data from a sensor on a separate computer";
    
	private final JPanel panel1 = new JPanel();
    private final JPanel panel2 = new JPanel();
    private final JPanel insetsPanel1 = new JPanel();
    private final JPanel insetsPanel3 = new JPanel();
    private final JButton button1 = new JButton();
    JLabel imageLabel = new JLabel();
    private final JLabel label1 = new JLabel();
    private final JLabel label2 = new JLabel();
    private final JLabel label3 = new JLabel();
    private final JLabel label4 = new JLabel();
    private final BorderLayout borderLayout1 = new BorderLayout();
    private final BorderLayout borderLayout2 = new BorderLayout();
    private final FlowLayout flowLayout1 = new FlowLayout();
    private final GridLayout gridLayout1 = new GridLayout();

    public UiAboutBox(Frame parent) {
        super(parent);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            jbInit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        pack();
    }

    /**Component initialisation*/
    private void jbInit()
    {
    	//imageLabel.setIcon(new ImageIcon(UiAboutBox.class.getResource("[Your Image]")));
        this.setTitle("About");
        setResizable(false);
        panel1.setLayout(borderLayout1);
        panel2.setLayout(borderLayout2);
        insetsPanel1.setLayout(flowLayout1);
        gridLayout1.setRows(4);
        gridLayout1.setColumns(1);
        label1.setText(product);
        label2.setText(version);
        label3.setText(copyright);
        label4.setText(comments);
        insetsPanel3.setLayout(gridLayout1);
        insetsPanel3.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        button1.setText("Ok");
        button1.addActionListener(this);
        this.getContentPane().add(panel1, null);
        insetsPanel3.add(label1, null);
        insetsPanel3.add(label2, null);
        insetsPanel3.add(label3, null);
        insetsPanel3.add(label4, null);
        panel2.add(insetsPanel3, BorderLayout.CENTER);
        insetsPanel1.add(button1, null);
        panel1.add(insetsPanel1, BorderLayout.SOUTH);
        panel1.add(panel2, BorderLayout.NORTH);
    }

    /**Overridden so we can exit when window is closed*/
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            cancel();
        }
        super.processWindowEvent(e);
    }

    /**Close the dialog*/
    private void cancel() {
        dispose();
    }

    /**Close the dialog on a button event*/
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == button1) {
            cancel();
        }
    }
}