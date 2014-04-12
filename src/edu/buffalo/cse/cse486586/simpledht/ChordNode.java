package edu.buffalo.cse.cse486586.simpledht;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;

public class ChordNode implements Serializable{
	
	private String node_id;
	private String hashed_node_id;
	
	private String successor = null;
	private String successor_hash_id = null;

	private String predecessor = null;
	private String predecessor_hash_id = null;
	
	private String destinationNode = "5554";
	private ChordNode chordNode;
	private ChordNode remoteNode = null;
	
	private static final String TAG = ChordNode.class.getSimpleName();
	
	public ChordNode(String node) {
		node_id = node;
		hashed_node_id = SHA1.genHash(node_id);
		successor = node;
		successor_hash_id = hashed_node_id;
		predecessor = node;
		predecessor_hash_id = hashed_node_id;
		chordNode = this;
		Log.e("ChordNode","ChordNode: "+node_id+" : "+hashed_node_id);
		new Thread(new ClientTask()).start();
	}
	
	/*
	 * Setter and Getter methods for successor and predecessor
	 */
	
	public String getSuccessor() {
		return successor;
	}
	public String getPredecessor() {
		return predecessor;
	}
	public void setSuccessor(String succ) {
		successor = succ;
		successor_hash_id = SHA1.genHash(succ);
		Log.e("successor:hash",successor+ ":"+successor_hash_id);
	}
	public void setPredecessor(String pre) {
		predecessor = pre;
		predecessor_hash_id = SHA1.genHash(pre);
		Log.e("predecessor:hash",predecessor+":"+predecessor_hash_id);
	}
	public String getNodeId() {
		return node_id;
	}
	public void setNodeId(String node) {
		node_id = node;
		Log.e("node_id",node_id);
	}
	public String getSuccessorHash() {
		return successor_hash_id;
	}
	public String getPredecessorHash() {
		return predecessor_hash_id;
	}
	public String getHashedNodeId() {
		return hashed_node_id;
	}
	public void setHashedNodeId(String hash) {
		hashed_node_id = SHA1.genHash(hash);
	}
	/*
	 * Forward Chord Node message to next or previous node
	 */
	public void goToNextNode(ChordNode remNode, String dest) {
		remoteNode = remNode;
		destinationNode = dest;
		new Thread(new ClientTask()).start();
	}
	
	public void goToPrevNode(ChordNode remNode, String dest) {
		remoteNode = remNode;
		destinationNode = dest;
		new Thread(new ClientTask()).start();
	}
    /*
     * Client Class
     */

    private class ClientTask implements Runnable{
    	
    	@Override
        public void run() {
            try {
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                		Integer.parseInt(destinationNode) * 2);
                /*
                 * TODO: Fill in your client code that sends out a message.
                 */
                OutputStream os = socket.getOutputStream();
                ObjectOutputStream msgObject = new ObjectOutputStream(os);
                if (remoteNode == null) {
                	msgObject.writeObject(chordNode);
                }
                else {
                	msgObject.writeObject(remoteNode);
                	remoteNode = null;
                }
                socket.close();
            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException" + e);
            } finally {
            	destinationNode = "5554";
            }

            return;
        }
    }	
    
}
