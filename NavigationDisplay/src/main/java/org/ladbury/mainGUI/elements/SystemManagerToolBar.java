package org.ladbury.mainGUI.elements;

import main.RemoteMain;
import subsystems.SubSystem;
import subsystems.SubSystemState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.EnumSet;
import java.util.Set;

/**
 * NavigationDisplay - org.ladbury.mainGUI
 * Created by MAWood on 26/12/2016.
 */
public class SystemManagerToolBar extends JToolBar
{
    private final Registry registry;
    public SystemManagerToolBar(Registry registry) throws RemoteException, NotBoundException
    {
        super();
        this.registry = registry;

        this.setRollover(true);

        setupToolBar();
    }

    private void setupToolBar() throws RemoteException, NotBoundException
    {
        RemoteMain remoteMain = (RemoteMain) registry.lookup("Main");
        Set<SubSystem.SubSystemType> systems = remoteMain.getSubSystems();
        for(SubSystem.SubSystemType system:systems) this.add(new SubSystemController(remoteMain,system));
    }

    class SubSystemController extends JPanel implements ActionListener, Runnable
    {
        private final RemoteMain remoteMain;
        private final SubSystem.SubSystemType subSystem;
        private final JButton button;

        SubSystemController(RemoteMain remoteMain, SubSystem.SubSystemType subSystem)
        {
            this.remoteMain = remoteMain;
            this.subSystem = subSystem;

            this.setLayout(new BorderLayout());
            this.add(new Label(subSystem.name()), BorderLayout.NORTH);
            button = new JButton("blank");
            button.setMinimumSize(button.getPreferredSize());
            button.addActionListener(this);
            this.add(button,BorderLayout.SOUTH);
            updateState();
            new Thread(this).start();
        }

        private void updateState()
        {
            try
            {
                button.setText(remoteMain.getSubSystemState(subSystem).name());
            } catch (RemoteException ignored) {ignored.printStackTrace();}
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                SubSystemState state = remoteMain.getSubSystemState(subSystem);
                button.setEnabled(false);
                new Thread(() ->
                {
                    try
                    {
                        switch (state)
                        {
                            case RUNNING:
                                remoteMain.shutdown(EnumSet.of(subSystem));
                                break;
                            case IDLE:
                                remoteMain.start(EnumSet.of(subSystem));
                                break;
                        }
                    } catch (RemoteException ignored) {}
                    updateState();
                    button.setEnabled(true);
                }).start();

            } catch (RemoteException ignored) {}
        }

        @Override
        public void run()
        {
            while(!Thread.interrupted())
            {
                try
                {
                    Thread.sleep(100);
                } catch (InterruptedException ignored){}
                updateState();
            }
        }
    }
}
