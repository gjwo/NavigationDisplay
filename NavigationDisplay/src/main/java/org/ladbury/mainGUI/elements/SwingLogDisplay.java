package org.ladbury.mainGUI.elements;

import logging.LogEntry;
import logging.RemoteLog;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultCaret;

import org.ladbury.mainGUI.UiStyle;

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

        this.registry = registry;
        textArea = new JTextArea();
        levelSpinner = new JSpinner();


        configureTextArea();
        configureLevelSelector();

        this.count = 0;
        this.viewingLevel = 2;
        this.localEntries = new ArrayList<>();

        thread = new Thread(this);
        thread.start();
    }

    private void configureLevelSelector()
    {
        JPanel spinnerPanel = new JPanel();
        SpinnerModel model =
                new SpinnerNumberModel(2,  //initial value
                                    1,  //min
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
        textArea.setFont(UiStyle.NORMAL_FONT);
        textArea.setRows(20);
        DefaultCaret caret = (DefaultCaret)textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        this.add(textArea, BorderLayout.CENTER);
    }

    @Override
    public void run()
    {
        while(!Thread.interrupted())
        {

            try
            {
                remoteEntries = ((RemoteLog) registry.lookup("Log")).getEntries();
            } catch (RemoteException | NotBoundException ignored) {}
            if(count < remoteEntries.size())
            {
                for(; count<remoteEntries.size(); count++)
                {
                    localEntries.add(remoteEntries.get(count));
                }
                updateText();
            } else
                try
                {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
        }
    }

    private void updateText()
    {
        String text = "";
        for(LogEntry entry:localEntries)
        {
            if(entry.level.getLevel() > viewingLevel) continue;
            text += entry.toString();
            text += System.lineSeparator();
        }
        textArea.setText(text);
    }

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
