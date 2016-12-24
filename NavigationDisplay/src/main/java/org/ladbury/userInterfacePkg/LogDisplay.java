package org.ladbury.userInterfacePkg;

import java.awt.Color;
import java.awt.TextArea;


public class LogDisplay
{
	private static LogDisplay logDisplay;
	private TextArea textArea;
	private UiFrame parent;
	
	static public LogDisplay getLogDisplay(){return logDisplay;}
	
	public LogDisplay(UiFrame parent)
	{
		this.parent = parent;
		this. textArea = new TextArea();
        // add log text area
        textArea.setBackground(Color.pink);
        textArea.setColumns(80);
        textArea.setCursor(null);
        textArea.setEditable(false);
        textArea.setFont(UiStyle.NORMAL_FONT);
        textArea.setRows(20);
        textArea.setText("");
		LogDisplay.logDisplay = this;
	}
	public TextArea getLogDisplayArea(){return textArea;}
    
    public void displayLog(String str) 
    {
        textArea.append(str);
        parent.repaint();
    }
    
    public void displayLog(int i) 
    {
        Integer intWrapper = new Integer(i);
        textArea.append(intWrapper.toString());
        parent.repaint();
    }
    
    public void displayLog(long l) 
    {
        Long lWrapper = new Long(l);
        textArea.append(lWrapper.toString());
        parent.repaint();
    }
    
    public void displayLog(double d) 
    {
        Double dWrapper = new Double(d);
        textArea.append(dWrapper.toString());
        parent.repaint();
    }
    
    public void displayLog(float f) 
    {
        Float fWrapper = new Float(f);
        textArea.append(fWrapper.toString());
        parent.repaint();
    }
    
    public void displayLog(boolean b) 
    {
        Boolean bWrapper = new Boolean(b);
        textArea.append(bWrapper.toString());
        parent.repaint();
    }

    public void newline()
    {
    	displayLog("\n\r");
    }
}
