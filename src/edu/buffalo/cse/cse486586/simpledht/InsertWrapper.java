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

public class InsertWrapper implements Serializable {
	
	String dest;
	public String key = null;
	public String value = null;
	InsertWrapper insertWrapper;
	private static final String TAG = InsertWrapper.class.getSimpleName();
	
	public InsertWrapper(String dest, ContentValues contentValues){
		this.dest = dest;
		this.key = contentValues.toString().split(" ")[1];
		this.key = this.key.split("=")[1];
		this.value = contentValues.toString().split(" ")[0];
		this.value = this.value.split("=")[1];
		
		this.insertWrapper = this;
	}
	
	public void sendInsert() {
		//new Thread(new ClientTask()).start();
		new ClientTask().run();
	}
	
	private class ClientTask implements Runnable{
    	
        public void run() {
            try {
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                		Integer.parseInt(dest) * 2);
                /*
                 * TODO: Fill in your client code that sends out a message.
                 */
                OutputStream os = socket.getOutputStream();
                ObjectOutputStream msgObject = new ObjectOutputStream(os);
                msgObject.writeObject(insertWrapper);
                socket.close();
                //Thread.sleep(100);
            }  catch (EOFException e) {
            	Log.e(TAG, "EOFException");
            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException" + e);
            } //catch (InterruptedException e) {
		//		e.printStackTrace();
	//		} 

            return;
        }
    }	
}
