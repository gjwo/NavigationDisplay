package org.ladbury.mainGUI;

import main.RemoteMain;
import org.ladbury.mainGUI.MainGUI;
import subsystems.SubSystem;
import subsystems.SubSystemState;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.EnumSet;

/**
 * NavigationDisplay - org.ladbury.chartingPkg
 * Created by MAWood on 26/12/2016.
 */
public abstract class SubSystemDependentJFrame extends JFrame
{
	private static final long serialVersionUID = -6380521236961137492L;
	private boolean dependenciesMet;

    protected SubSystemDependentJFrame(EnumSet<SubSystem.SubSystemType> dependencies)
    {
        try
        {
            RemoteMain rm = (RemoteMain) MainGUI.registry.lookup("Main");
            dependenciesMet = true;

            for(SubSystem.SubSystemType dependency:dependencies)
            {
                if(!isSubSystemRunning(rm,dependency))
                {
                    JOptionPane.showMessageDialog(this, dependency.name() + " sub system is not running");
                    this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
                    dependenciesMet = false;
                    return;
                }
            }
        } catch (RemoteException | NotBoundException e)
        {
            dependenciesMet = false;
        }
    }

    private boolean isSubSystemRunning(RemoteMain rm, SubSystem.SubSystemType system) throws RemoteException
    {
        return rm.getSubSystemState(system) == SubSystemState.RUNNING;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean isDependenciesMet()
    {
        return dependenciesMet;
    }
}
