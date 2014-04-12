package edu.buffalo.cse.cse486586.simpledht;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;


import android.database.Cursor;
import android.util.Log;
 
public class QueryWrapper implements Serializable, Runnable {
	
	public HashMap<String,String> contentMap;
	public String dest;
	public String origin;
	public String selection;
	private static final String TAG = QueryWrapper.class.getSimpleName();
	
	
	public QueryWrapper(String ori, String dest,Cursor cursor, String selection) {
		this.dest = dest;
		this.origin = ori;
		this.selection = selection;
		this.contentMap = Marshall.cursorToHashMap(cursor);
		
	}
	// For specific query
	@Override
	public void run() {
       //synchronized(this){
            
		try {
        	System.out.println("message query......");
            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
            		Integer.parseInt(dest) * 2);
            /*
             * TODO: Fill in your client code that sends out a message.
             */
            OutputStream os = socket.getOutputStream();
            ObjectOutputStream msgObject = new ObjectOutputStream(os);
            msgObject.writeObject(this);
            socket.close();
            
        }  catch (EOFException e) {
        	Log.e(TAG, "EOFException");
        } catch (UnknownHostException e) {
            Log.e(TAG, "ClientTask UnknownHostException");
        } catch (IOException e) {
            Log.e(TAG, "ClientTask socket IOException" + e);
        } 

        return;
      }
//	}
}	



