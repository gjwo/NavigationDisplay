package org.ladbury.main;
import dataTypes.TimestampedData3f;
import org.jfree.ui.RefineryUtilities;
import org.ladbury.chartingPkg.DynamicLineAndTimeSeriesChart;
import org.ladbury.chartingPkg.InstrumentCompass;

import javax.swing.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
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

	private NavClient(String serverName, NavClientGUI gui, DynamicLineAndTimeSeriesChart dg, InstrumentCompass comp, int debug)
	{
		//this.navClientGUI = gui;
		this.serverName = serverName;
		this.setName("NavClientThread");
		this.debugLevel = debug;
		this.dynamicGraph = dg;
		this.compass = comp;
		
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
        navClient = new NavClient(args[0],ncg,dc,comp,5);
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
	 
	            // send request
	        byte[] buf = new byte[256];
	        InetAddress address = InetAddress.getByName(serverName);
	        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, serverPortNbr);
	        socket.send(packet); //register interest with server
	        boolean stop = false;
	        Pattern msgSplit = Pattern.compile("(^.*?),(.*$)");
	        Matcher msg;
	        String msgType;
	    	DatagramPacket inPacket = new DatagramPacket(buf, buf.length);
	    	socket.setSoTimeout(500); //1/2 second timeout
	    	boolean reply = false;
	    	while(!reply) //keep trying to register until we get a response
	    	{
	    		reply = true;
	    		try
	    		{
	                socket.receive(inPacket);
	    			
	    		}catch (SocketTimeoutException e) {
					if(debugLevel>=5) System.out.println("DEBUG org.ladbury.main attempting to register");
	    			reply = false;
	    			socket.send(packet);
	    		}
	    	}
			if(debugLevel>=5) System.out.println("DEBUG org.ladbury.main registered");
	    	//we lose the first response
	    	socket.setSoTimeout(0); //clear timeout
	        while (!stop)
	        {
	            // get response
	        	inPacket = new DatagramPacket(buf, buf.length);
	            socket.receive(inPacket); //now wait for reply
	
	            // display response
	            String received = new String(inPacket.getData(), 0, packet.getLength());
	            if(debugLevel>=5) System.out.println("DEBUG org.ladbury.main received: "+received);
	            msg = msgSplit.matcher(received);
	            if (msg.matches())
		            {
		            msgType = msg.group(1);
		            switch (msgType)
		            {
		            case "STOP": 	stop = true;
		            				break;
		            case "Angles":	processAnglesMsg(msg.group(2));
		            				break;
		            default:		System.out.println("msg: " + received);
		            }
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
    	this.dynamicGraph.addReading(data);
    	//this.dynamicGraph.actionPerformed(null);
    	this.compass.setHeading(yaw);
    	//this.navClientGUI.addReading(data);
    	//this.navClientGUI.dataUpdated();

    /*	
       final Pattern dataTFFFsplit = Pattern.compile( "([+-]?[0-9]+),"
													+ "([+-]?[0-9]*[.]?[0-9]+),"
													+ "([+-]?[0-9]*[.]?[0-9]+),"
													+ "([+-]?[0-9]*[.]?[0-9]+,)"); //pattern updated after StackOverflow question
													
    	Matcher data = dataTFFFsplit.matcher(s.trim());
    	if(data.matches())
    	{
	    	Long time = Long.parseLong(data.group(1));
	    	float yaw = Float.parseFloat(data.group(2));
	    	float pitch = Float.parseFloat(data.group(3));
	    	float roll = Float.parseFloat(data.group(4));
	    	System.out.format("Angles - [%8d] Yaw: %08.3f Pitch: %08.3f Roll: %08.3f%n",time,yaw, pitch,roll);
    	} else
    	{
    		if(debugLevel>=4) System.out.println("DEBUG processAnglesMsg: "+s);
    	}
    	*/
    }
}