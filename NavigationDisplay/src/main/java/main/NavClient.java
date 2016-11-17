package main;
import java.io.*;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NavClient {
	private static final int serverPortNbr = 9876;
    private static final Pattern dataTFFFsplit = Pattern.compile(	"[+-]([0-9]+),"
    																+ "[+-]([0-9]*[.])?[0-9]+,"
    																+ "[+-]([0-9]*[.])?[0-9]+,"
    																+ "[+-]([0-9]*[.])?[0-9]+,");
    private static final int debugLevel = 4;

	public NavClient() {
		// TODO Auto-generated constructor stub
	}
    public static void main(String[] args) throws IOException {
    	 
        if (args.length != 1) {
             System.out.println("Usage: java NavClient <hostname>");
             return;
        }
        DatagramSocket socket = new DatagramSocket();
 
            // send request
        byte[] buf = new byte[256];
        InetAddress address = InetAddress.getByName(args[0]);
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
    		       	// resend
    				reply = false;
    				socket.send(packet);
    		}
    	}
    	//we lose the first response
    	socket.setSoTimeout(0); //clear timeout
        while (!stop)
        {
            // get response
        	inPacket = new DatagramPacket(buf, buf.length);
            socket.receive(inPacket); //now wait for reply

            // display response
            String received = new String(inPacket.getData(), 0, packet.getLength());
            if(debugLevel<=5) System.out.println("DEBUG main received: "+received);
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
        System.out.println("Stopping");
        socket.close();
    }
    private static void processAnglesMsg(String s)
    {
    	Matcher data = dataTFFFsplit.matcher(s);
    	if(data.matches())
    	{
	    	Long time = Long.parseLong(data.group(1));
	    	float yaw = Float.parseFloat(data.group(2));
	    	float pitch = Float.parseFloat(data.group(3));
	    	float roll = Float.parseFloat(data.group(4));
	    	System.out.format("Angles - [%8d] Yaw: %08.3f Pitch: %08.3f Roll: %08.3f%n",time,yaw, pitch,roll);
    	} else
    	{
    		if(debugLevel<=4) System.out.format("DEBUG processAnglesMsg: "+s);
    	}
    }
}