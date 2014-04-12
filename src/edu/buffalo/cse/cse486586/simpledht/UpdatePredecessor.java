package edu.buffalo.cse.cse486586.simpledht;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;

public class UpdatePredecessor implements Serializable {

	String pre;
	String dest;
	UpdatePredecessor updatePredecessor;
	private static final String TAG = ChordNode.class.getSimpleName();
	
	public UpdatePredecessor(String pre) {
		this.pre = pre;
		this.updatePredecessor = this;
	}
	public String getPredecessor() {
		return pre;
	}
	public void sendPre(String dest) {
		this.dest = dest;
		new Thread(new ClientTask()).start();
	}
	
    private class ClientTask implements Runnable{
    	
    	@Override
        public void run() {
            try {
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                		Integer.parseInt(dest) * 2);
                /*
                 * TODO: Fill in your client code that sends out a message.
                 */
                OutputStream os = socket.getOutputStream();
                ObjectOutputStream msgObject = new ObjectOutputStream(os);
                msgObject.writeObject(updatePredecessor);
                socket.close();
            } catch (EOFException e) {
            	Log.e(TAG, "EOFException");
            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException" + e);
            } 

            return;
        }
    }	

}
