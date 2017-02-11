package org.ladbury.mainGUI.elements;

import logging.LogEntry;
import logging.RemoteLog;
import logging.SystemLog.LogLevel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultCaret;

import java.awt.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.ArrayList;

/**
 * NavigationDisplay - org.ladbury.userInterfacePkg
 * Created by MAWood on 26/12/2016.
 */
public class SwingLogDisplay extends JPanel implements Runnable, ChangeListener
{
	private static final long serialVersionUID = -3387172438728251380L;

	private final Thread thread;

    private ArrayList<LogEntry> remoteEntries;
    private final ArrayList<LogEntry> localEntries;
    private int count;
    private final Registry registry;

    private final JTextArea textArea;
    private final JSpinner levelSpinner;
    private int viewingLevel;

    public SwingLogDisplay(Registry registry)
    {
        super();
        this.setLayout(new BorderLayout());

        this.count = 0;
        this.viewingLevel = LogLevel.USER_INFORMATION.getLevel();
        this.localEntries = new ArrayList<>();

        this.registry = registry;
        textArea = new JTextArea();
        levelSpinner = new JSpinner();

        configureTextArea();
        configureLevelSelector();

        thread = new Thread(this);
        thread.start();
    }

    private void configureLevelSelector()
    {
        JPanel spinnerPanel = new JPanel();
        SpinnerModel model =
                new SpinnerNumberModel(	this.viewingLevel,  //initial value
                                    	0,  //min
                                    	10, //max
                                    	1); //step
        levelSpinner.setModel(model);
        levelSpinner.addChangeListener(this);
        spinnerPanel.setLayout(new BorderLayout());
        spinnerPanel.add(new JLabel("Viewing Level:"), BorderLayout.WEST);
        spinnerPanel.add(levelSpinner, BorderLayout.EAST);
        JPanel paddingPanel = new JPanel(new BorderLayout());
        paddingPanel.add(spinnerPanel, BorderLayout.WEST);
        this.add(paddingPanel, BorderLayout.SOUTH);
    }

    private void configureTextArea()
    {
        textArea.setText("");
        textArea.setBackground(Color.pink);
        textArea.setColumns(80);
        textArea.setCursor(null);
        textArea.setEditable(false);
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 10));
        textArea.setRows(20);
        DefaultCaret caret = (DefaultCaret)textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane scrollPane = new JScrollPane(textArea);
        this.add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void run()
    {
        while(!Thread.interrupted())
        {

            try
            {
                remoteEntries = ((RemoteLog) registry.lookup("Log")).getEntries();
                if(count < remoteEntries.size())
                {
                    for(; count<remoteEntries.size(); count++)
                    {
                        localEntries.add(remoteEntries.get(count));
                    }
                    updateText();
                } else
                    Thread.sleep(100);
            } catch (RemoteException | NotBoundException | InterruptedException ignored) {}
        }
    }

    private void updateText()
    {

        textArea.setText(filterLog(viewingLevel));
    }

    private String filterLog( int level)
    {
        return localEntries.stream()
                            .filter(le -> le.level.getLevel() <= level)
                            .map(le ->le.toString()+System.lineSeparator())
                            .reduce("",String::concat);
    }

    @SuppressWarnings("unused")
    public void stop()
    {
        thread.interrupt();
    }

    @Override
    public void stateChanged(ChangeEvent e)
    {
        viewingLevel = (int)levelSpinner.getValue();
        updateText();
    }
}
