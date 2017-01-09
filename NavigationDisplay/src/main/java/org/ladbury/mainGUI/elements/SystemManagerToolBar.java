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
import java.util.*;
import java.util.stream.Collectors;

/**
 * NavigationDisplay - org.ladbury.mainGUI
 * Created by MAWood on 26/12/2016.
 */
public class SystemManagerToolBar extends JToolBar
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1974543340064805640L;
	private final RemoteMain remoteMain;
	private final HashMap<SubSystem.SubSystemType,SubSystemController> controllers;

    public SystemManagerToolBar(Registry registry) throws RemoteException, NotBoundException
    {
        super();
        this.controllers = new HashMap<>();
        this.remoteMain = (RemoteMain) registry.lookup("Main");

        this.setRollover(true);

        setupToolBar();
    }

    private void setupToolBar() throws RemoteException
    {
        Set<SubSystem.SubSystemType> systems = remoteMain.getSubSystems();
        for(SubSystem.SubSystemType system:systems)
        {
            controllers.put(system,new SubSystemController(remoteMain,system));
            this.add(controllers.get(system));
        }
    }

    private void updateToolBar() throws RemoteException
    {
        EnumSet<SubSystem.SubSystemType> remoteSystems = remoteMain.getSubSystems();
        Collection<SubSystem.SubSystemType> newSystems = remoteSystems.stream()
                .filter(subSystemType -> !controllers.keySet().contains(subSystemType))
                .collect(Collectors.toList());
        for(SubSystem.SubSystemType system:newSystems)
        {
            controllers.put(system,new SubSystemController(remoteMain,system));
            this.add(controllers.get(system));
        }

    }

    class SubSystemController extends JPanel implements ActionListener, Runnable
    {
        /**
		 * 
		 */
		private static final long serialVersionUID = 231011596168975507L;
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
                            default:    
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
