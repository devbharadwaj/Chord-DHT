package edu.buffalo.cse.cse486586.simpledht;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.content.ContentValues;
import android.util.Log;


public class DeleteWrapper implements Serializable {
	String ori;
	String dest;
	public String selection = null;
	DeleteWrapper deleteWrapper;
	private static final String TAG = DeleteWrapper.class.getSimpleName();
	
	public DeleteWrapper(String ori, String dest, String selection){
		this.ori = ori;
		this.dest = dest;
		this.selection = selection;
		this.deleteWrapper = this;
	}
	
	public void sendDelete() {
		//new Thread(new ClientTask()).start();
		new ClientTask().run();
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
                msgObject.writeObject(deleteWrapper);
                socket.close();
           
            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException" + e);
            } 

            return;
        }
    }	
}
