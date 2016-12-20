package org.ladbury.main;
import com.sun.j3d.utils.applet.MainFrame;
import dataTypes.TimestampedData3f;
import inertialNavigation.RemoteInstruments;
import messages.Message;
import messages.Message.CommandType;
import messages.Message.ErrorMsgType;
import messages.Message.MessageType;
import messages.Message.ParameterType;

import org.jfree.ui.RefineryUtilities;
import org.ladbury.chartingPkg.CubeFrame;
import org.ladbury.chartingPkg.DynamicLineAndTimeSeriesChart;
import org.ladbury.chartingPkg.InstrumentCompass;

import javax.swing.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class NavClient extends Thread implements Runnable
{
	private static final int serverPortNbr = 9876;
	private InetAddress serverAddress = null;
    private int debugLevel;
    private NavClientGUI navClientGUI;
    private final String serverName;
    private boolean stop;
    private final int BUFFER_SIZE = 1024;
    private DatagramSocket socket = null;
    private long msgsIn;
    private long msgsOut;
    private NavDisplay navDisplay;

	private NavClient(String serverName, NavClientGUI gui,  int debug)
	{
		//this.navClientGUI = gui;
		this.serverName = serverName;
		this.setName("NavClientThread");
		this.debugLevel = debug;
		this.stop = false;
		this.msgsIn = 0;
		this.msgsOut = 0;
		this.navDisplay = new NavDisplay();
	}
	
    public static void main(String[] args) throws IOException, NotBoundException
	{

    	NavClient navClient;
        NavClientGUI ncg = null;

        if (args.length != 1) {
             System.out.println("Usage: java NavClient <hostname>");
             return;
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        navClient = new NavClient(args[0],ncg,1);
        navClient.start();

        new RMITest("192.168.1.127").start();

        //ncg = new NavClientGUI(4);
        //NavClientGUI.setNavClientMain(ncg); 
        //ncg.init();
        //ncg.start();
     }
    
	@Override
	public void run() {
	    try
	    {
	    	navDisplay.initDisplay();
	        socket = new DatagramSocket();
	    	//dynamicGraph = new DynamicLineAndTimeSeriesChart("Navigation Data");
	        serverAddress = InetAddress.getByName(serverName);	        
	        byte[] buf = new byte[BUFFER_SIZE];
	    	DatagramPacket inPacket = new DatagramPacket(buf, buf.length);

	        if (!registerWithServer())
	        	{
	        		System.err.println("Failed to register with Server");
	        		System.exit(5);
	        	}	        
	        requestStreamedData(ParameterType.TAIT_BRYAN);		
	        while (!stop)
	        {
	            // get and handle responses
	        	socket.receive(inPacket);
	        	msgsIn++;
	            if (!handleMessage(inPacket))
	            {
	            	System.err.println("Bad Message, stopping");
	            	stop = true;
	            }
	        }
	        if (debugLevel >=1)System.out.println("Stopping receiving data");
	        socket.close();
	    }
	    catch (Exception e)
	    {
	    	e.printStackTrace();
	    }
	}

	private boolean registerWithServer() throws IOException
	{
		if(debugLevel>=5) System.out.println("registerWithServer");
		//build registration request
        Message reqMsg = new Message();
        reqMsg.setMsgType(MessageType.CLIENT_REG_REQ);
        reqMsg.setErrorMsgType(ErrorMsgType.SUCCESS);
        reqMsg.setCommandType(CommandType.EXECUTE);
        
        // serialise and add to packet
    	byte[] ba = reqMsg.serializeMsg();
        DatagramPacket packet = new DatagramPacket(ba, ba.length, serverAddress, serverPortNbr);
        
        // prepare holder for response
        byte[] buf = new byte[BUFFER_SIZE];
    	DatagramPacket inPacket = new DatagramPacket(buf, buf.length);
    	try {
    		// server may not be handling requests yet & message may be lost so set a timeout
			socket.setSoTimeout(1000); //milliseconds
		} catch (SocketException e1) {
			e1.printStackTrace();
		} 
    	boolean reply = false;
    	
    	while(!reply) //keep trying to register until we get a response
    	{
    		socket.send(packet); // send or resend on timeout
			this.msgsOut++;
    		try
    		{
    			socket.receive(inPacket);
    			this.msgsIn++;
        		reply = true; // prepare to exit loop
    		}catch (SocketTimeoutException  e) {
				if(debugLevel>=5) System.out.println("DEBUG main attempting to register");
    			reply = false; // try again, resend message
    		}
    	}
    	//got a reply
    	socket.setSoTimeout(0); //clear timeout
		if(debugLevel>=5) System.out.println("End registerWithServer");
    	return handleMessage(inPacket);
	}
	private void requestStreamedData(ParameterType p)
	{
		if (debugLevel >=1)System.out.println("Requesting stream: " + p.name());
        Message reqMsg = new Message();
        reqMsg.setMsgType(MessageType.STREAM_REQ);
        reqMsg.setParameterType(p);
        reqMsg.setErrorMsgType(ErrorMsgType.SUCCESS);
        reqMsg.setCommandType(CommandType.EXECUTE);
        sendMessage(reqMsg);
	}
	
	private void sendMessage(Message msg)
	{
    	byte[] ba = msg.serializeMsg();
        DatagramPacket packet = new DatagramPacket(ba, ba.length, serverAddress, serverPortNbr);
		try {
			socket.send(packet);
			this.msgsOut++;
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
    private boolean handleMessage(DatagramPacket packet)
    {
		if(debugLevel>=5) System.out.println("handleMessage");
    	int receivedBytes = 0;
    	Message respMsg = Message.deSerializeMsg(packet.getData());
    	if(respMsg == null)
    	{
    		System.err.println("null message received");
    		System.exit(5);
    	}
    	if (debugLevel >=4) if (msgsIn <=7) System.out.println("Received msg ("+receivedBytes+"bytes): "+ respMsg.toString());
    	ErrorMsgType error = respMsg.getErrorMsgType();
        boolean success = true;
        switch (respMsg.getMsgType())
        {
        case PING_RESP: 
        	if (error == ErrorMsgType.SUCCESS)
        	{
        		Instant time = respMsg.getTime();
        		LocalDateTime ldt = LocalDateTime.ofInstant(time, ZoneId.systemDefault());
        		if (debugLevel >=4)System.out.println("Ping Response at : "+ ldt.toString());
        	}
        	else 
        	{
        		System.err.println("Ping Response failed, error: "+ error.name());
        		success = false;
        	}
         	break;
        case CLIENT_REG_RESP: 
            if (error == ErrorMsgType.SUCCESS)	
            { 	//register client and start thread
        		Instant time = respMsg.getTime();
        		LocalDateTime ldt = LocalDateTime.ofInstant(time, ZoneId.systemDefault());
        		if (debugLevel >=1)System.out.println("Client registered at : "+ ldt.toString());
            }
        	else
        	{
        		System.err.println("Client registration failed, error: "+ error.name());
        		success = false;
        	}
         	break;
        case GET_PARAM_RESP: 
        	if (error == ErrorMsgType.SUCCESS)
        	{
        		if (debugLevel >=4)System.out.println("Get parameter Response: "+respMsg.getParameterType().toString()+ 
        				" value: "+ respMsg.getNavAngles().toString()); //other cases of param
        	}
        	else
        	{
        		System.err.println("Get Parameter failed, error: "+ error.name());
        		success = false;
        	}
       	break;
        case SET_PARAM_RESP:
        	if (error == ErrorMsgType.SUCCESS)
        	{
        		if (debugLevel >=4)System.out.println("Set parameter Response: "+respMsg.getParameterType().toString()+ 
        				" value: "+ respMsg.getNavAngles().toString()); //other cases of param
        	}
        	else
        	{
        		System.err.println("Set Parameter failed, error: "+ error.name());
        		success = false;
        	}
        	break;
        case STREAM_RESP:
        	if (error == ErrorMsgType.SUCCESS)
        	{
        		if (respMsg.getParameterType() == ParameterType.TAIT_BRYAN)
        		{
        			navDisplay.processTaitBryanAngles(respMsg.getNavAngles().clone());
        		}
        		else
        		{
            		System.err.println("Get stream Response unexpected: "+respMsg.getParameterType().toString()+ 
            				" value: "+ respMsg.getNavAngles().toString()); //other cases of param
            		success = false;
        		}

        	}
        	else
        		{
        			System.err.println("Stream Request failed, error: "+ error.name());
        			success = false;
        		}
        	break;
        case CONTROL_REQ: 
        	if (respMsg.getCommandType() == CommandType.STOP)
        	{
        		if (debugLevel >=4)System.out.println("Stop received from server");
        		stop = true;
        	}
        	else 
        		{
        		System.err.println("Unexpected command: "+ respMsg.getCommandType().toString()); 
        		success = false;
        		}
			break;
        case CONTROL_RESP: 
        	if (error == ErrorMsgType.SUCCESS)
        	{
        		if (debugLevel >=4)System.out.println("control Response: "+respMsg.getParameterType().toString()+ 
        				" value: "+ respMsg.getCommandType().toString()); 
        	}
        	else 
        		{
        		System.err.println("Set Parameter failed, error: "+ error.name()); 
        		success = false;
        		}
			break;
        case MSG_ERROR:
		default:	success = false;
        }
		if(debugLevel>=5) System.out.println("End handleMessage "+ success );
        return success;
    }

}