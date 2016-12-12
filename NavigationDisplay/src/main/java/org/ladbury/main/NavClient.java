package org.ladbury.main;
import com.sun.j3d.utils.applet.MainFrame;
import dataTypes.TimestampedData3f;
import inertialNavigation.Client;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NavClient extends Thread implements Runnable
{
	private static final int serverPortNbr = 9876;
    private int debugLevel;
    private NavClientGUI navClientGUI;
    private final InstrumentCompass compass;
    private final DynamicLineAndTimeSeriesChart dynamicGraph;
    private final String serverName;
    private final CubeFrame cube;
    private boolean stop;
    private final int BUFFER_SIZE = 1024;

	private NavClient(String serverName, NavClientGUI gui, DynamicLineAndTimeSeriesChart dg, InstrumentCompass comp, int debug)
	{
		//this.navClientGUI = gui;
		this.serverName = serverName;
		this.setName("NavClientThread");
		this.debugLevel = debug;
		this.dynamicGraph = dg;
		this.compass = comp;
		this.cube = new CubeFrame();
		this.stop = false;
		new MainFrame(cube, 256, 256);
		
	}
    public static void main(String[] args) throws IOException {

    	NavClient navClient;
        NavClientGUI ncg = null;
        DynamicLineAndTimeSeriesChart dc;
        InstrumentCompass comp;
   	    	 
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

        //ncg = new NavClientGUI(4);
        dc = new DynamicLineAndTimeSeriesChart("Navigation Data");
        comp = new InstrumentCompass("Compass");
        //NavClientGUI.setNavClientMain(ncg); 
        navClient = new NavClient(args[0],ncg,dc,comp,1);
        //ncg.init();
        //ncg.start();
        navClient.start();
        
        
     }
    
	@Override
	public void run() {
	    try
	    {
	        DatagramSocket socket = new DatagramSocket();
	    	//dynamicGraph = new DynamicLineAndTimeSeriesChart("Navigation Data");
	        dynamicGraph.pack();
	        dynamicGraph.setVisible(true);
	        compass.pack();
	        RefineryUtilities.centerFrameOnScreen(compass);
	        compass.setVisible(true);
	        
	        InetAddress address = InetAddress.getByName(serverName);
	        byte[] buf = new byte[BUFFER_SIZE];
	    	DatagramPacket inPacket = new DatagramPacket(buf, buf.length);

	        if (!registerWithServer(socket, address))
	        	{
	        		System.err.println("Failed to register with Server");
	        		System.exit(5);
	        	}
	        // request streamed data
	        System.out.print("Requesting stream" );
	        Message reqMsg = new Message();
	        reqMsg.setMsgType(MessageType.STREAM_REQ);
	        reqMsg.setParameterType(ParameterType.TAIT_BRYAN);
	    	byte[] ba = reqMsg.serializeMsg();
	        DatagramPacket packet = new DatagramPacket(ba, ba.length, address, serverPortNbr);
			socket.send(packet);       
	        while (!stop)
	        {
	            // get response
	        	inPacket = new DatagramPacket(buf, buf.length);
	            socket.receive(inPacket); //now wait for reply
	            if (!handleMessage(inPacket))
	            {
	            	System.err.println("Bad Message, stopping");
	            	stop = true;
	            }
	        }
	        System.out.println("Stopping receiving data");
	        socket.close();
	    }
	    catch (Exception e)
	    {
	    	e.printStackTrace();
	    }
	}
	private boolean registerWithServer(DatagramSocket socket,InetAddress address) throws IOException
	{
        Message reqMsg = new Message();
        reqMsg.setMsgType(MessageType.CLIENT_REG_REQ);
    	byte[] ba = reqMsg.serializeMsg();
        DatagramPacket packet = new DatagramPacket(ba, ba.length, address, serverPortNbr);
               
        byte[] buf = new byte[BUFFER_SIZE];
    	DatagramPacket inPacket = new DatagramPacket(buf, buf.length);
    	try {
			socket.setSoTimeout(1000); //milliseconds
		} catch (SocketException e1) {
			e1.printStackTrace();
		} //1/2 second timeout
    	boolean reply = false;
    	while(!reply) //keep trying to register until we get a response
    	{
    		socket.send(packet); // send or resend on timeout
    		try
    		{
    			socket.receive(inPacket);
        		reply = true;   			
    		}catch (SocketTimeoutException  e) {
				if(debugLevel>=5) System.out.println("DEBUG main attempting to register");
    			reply = false;
    		}
    	}
    	//got a reply
		if(debugLevel>=5) System.out.println("DEBUG main registered"); 
    	socket.setSoTimeout(0); //clear timeout
    	return handleMessage(inPacket);
	}
	
    private boolean handleMessage(DatagramPacket packet)
    {
    	int receivedBytes = 0;
		receivedBytes = packet.getLength(); //actual length of data
		byte[] trimmedData = new byte[receivedBytes];
		for(int i = 0; i < receivedBytes; i++)
		{
			trimmedData[i] = packet.getData()[i];
		}
    	System.out.println("Handle Data: "+receivedBytes+" " +Arrays.toString(trimmedData));
    	Message respMsg = Message.deSerializeMsg(trimmedData);
    	if(respMsg == null)
    	{
    		System.out.println("null message recieved");
    		System.exit(5);
    	}
    	System.out.println("Received msg: "+ respMsg.toString());
    	ErrorMsgType error = respMsg.getErrorMsgType();
    	System.out.println(respMsg.toString());
        boolean success = true;
        switch (respMsg.getMsgType())
        {
        case PING_RESP: 
        	if (error == ErrorMsgType.SUCCESS)
        	{
        		Instant time = respMsg.getTime();
        		LocalDateTime ldt = LocalDateTime.ofInstant(time, ZoneId.systemDefault());
        		System.out.println("Ping Response at : "+ ldt.toString());
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
        		System.out.println("Client registered at : "+ ldt.toString());
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
        		System.out.println("Get parameter Response: "+respMsg.getParameterType().toString()+ 
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
        		System.out.println("Set parameter Response: "+respMsg.getParameterType().toString()+ 
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
        		if (respMsg.getParameterType()== ParameterType.TAIT_BRYAN)
        		{
        			processTaitBryanAngles(respMsg.getNavAngles());
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
        		System.out.println("Stop received from server");
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
        		System.out.println("control Response: "+respMsg.getParameterType().toString()+ 
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
        return success;
    }

    private void processAnglesMsg(String s)
    {
    	String[] split = s.split(",");
    	Long time = Long.parseLong(split[0]);
    	float yaw = Float.parseFloat(split[1]);
    	float pitch = Float.parseFloat(split[2]);
    	float roll = Float.parseFloat(split[3]);
    	long milliSeconds = TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS);
    	if(debugLevel>=4) System.out.format("Angles - [%8d ms] Yaw: %08.3f Pitch: %08.3f Roll: %08.3f%n",milliSeconds,yaw, pitch,roll);
    	TimestampedData3f data = new TimestampedData3f(yaw,pitch,roll,time);
    	processTaitBryanAngles(data);
     }
    private void processTaitBryanAngles(TimestampedData3f data)
    {
    	this.dynamicGraph.addReading(data);
    	this.compass.setHeading(data.getX());
    	this.cube.myRotationBehavior.setAngles(data);
    }
}