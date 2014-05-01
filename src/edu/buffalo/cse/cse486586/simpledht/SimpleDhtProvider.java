package edu.buffalo.cse.cse486586.simpledht;


import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SimpleDhtProvider extends ContentProvider {
	
	private static final String AUTH = "edu.buffalo.cse.cse486586.simpledht";
	private static final Uri MESSAGES_URI = Uri.parse("content://"+AUTH+"."+DatabaseHelper.TABLE_NAME);
	private static final String TAG = SimpleDhtProvider.class.getSimpleName();
	private static final int SERVER_PORT = 10000;
	private static ServerTask serverTask;
	private static String node_id;
	private static Cursor myCursor = null;
	private boolean cursorbool = true;
	private boolean localQuery = true;
	private boolean deletebool = true;
	private ChordNode myChordNode;
	volatile SQLiteDatabase db;
	volatile DatabaseHelper dbHelper;
	boolean pass;
	String realOrigin;
	Cursor passCursor;

    @Override
    public boolean onCreate() {
    	node_id = getNodeId();
    	serverTask = new ServerTask();
    	serverTask.start();
    	myChordNode = new ChordNode(node_id);
    	dbHelper = new DatabaseHelper(getContext());
    	pass = true;;
    	realOrigin = null;
    	passCursor = null;
        return false;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
    	db = dbHelper.getWritableDatabase();
    	// only node
    	if (myChordNode.getHashedNodeId().equals(myChordNode.getPredecessorHash())) {
        	if (selection.matches("@") || selection.matches("\\*")) {
        		db.execSQL("DELETE FROM "+ DatabaseHelper.TABLE_NAME);
        		db.close();
        		return 1;
        	}
        	else {
        		db.delete(DatabaseHelper.TABLE_NAME, "key = '"+ selection+"'", selectionArgs);
        		db.close();
        		return 1;
        	}
    	}
    	else {
    		// more than one node
	    	if (selection.matches("@")) {
	    		db.execSQL("DELETE FROM "+ DatabaseHelper.TABLE_NAME);
	    		db.close();
	    		return 1;
	    	}
	    	else if (selection.matches("\\*")) {
	    		db.execSQL("DELETE FROM "+ DatabaseHelper.TABLE_NAME);
	    		if (deletebool)
	    			new DeleteWrapper(myChordNode.getNodeId(),myChordNode.getSuccessor(),selection).sendDelete();
	    		while (deletebool) {
	    			try { Thread.sleep(200); } 
	    			catch (InterruptedException e) { e.printStackTrace();}
	    		}
	    		deletebool = true;
	    		db.close();
	    		return 1;
	    	}
	    	else {
	    		db.delete(DatabaseHelper.TABLE_NAME, "key = '"+selection+"'", selectionArgs);
	    		if (deletebool)
	    			new DeleteWrapper(myChordNode.getNodeId(),myChordNode.getSuccessor(),selection).sendDelete();
	    		while (deletebool) {
	    			try { Thread.sleep(200); } 
	    			catch (InterruptedException e) { e.printStackTrace();}
	    		}
	    		deletebool = true;
	    		db.close();
	    		return 1;
	    	}
    	}
	}
    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
    	String hash = null;
    	hash = values.toString().split(" ")[1];
    	hash = hash.split("=")[1];
    	System.out.println(hash);
    	hash = SHA1.genHash(hash);
    	System.out.println(hash);
    	db = dbHelper.getWritableDatabase();
    	
    	
    	if (hash != null)
    	{
    		// Check if one only node in cluster
    		if (myChordNode.getSuccessorHash().equals(myChordNode.getHashedNodeId())) {
    			db.insert(DatabaseHelper.TABLE_NAME, null, values);
    			Log.v("insert", values.toString());
    		}
    		// Check if you are the first node
    		else if (myChordNode.getPredecessorHash().compareTo(myChordNode.getHashedNodeId()) > 0) {
    			if (hash.compareTo(myChordNode.getPredecessorHash()) > 0
    					|| hash.compareTo(myChordNode.getHashedNodeId()) < 0) {
    				db.insert(DatabaseHelper.TABLE_NAME, null, values);
    				Log.v("insert", values.toString());
    			} 	
    			else if (hash.compareTo(myChordNode.getHashedNodeId()) > 0){
    				new InsertWrapper(myChordNode.getSuccessor(),values).sendInsert();
    			}
    		} // Check to see if last node
    		else if (myChordNode.getSuccessorHash().compareTo(myChordNode.getHashedNodeId()) < 0) {
    			if (hash.compareTo(myChordNode.getHashedNodeId()) > 0 ) {
    				new InsertWrapper(myChordNode.getSuccessor(),values).sendInsert();
    			}
    			else if (hash.compareTo(myChordNode.getPredecessorHash()) > 0 
    						&& hash.compareTo(myChordNode.getHashedNodeId()) < 0){
    				db.insert(DatabaseHelper.TABLE_NAME, null, values);
    				Log.v("insert", values.toString());
    			}
    			else if (hash.compareTo(myChordNode.getPredecessorHash()) < 0){
    				new InsertWrapper(myChordNode.getPredecessor(),values).sendInsert();
    			}
    		} 
    		else {
    			if (hash.compareTo(myChordNode.getHashedNodeId()) < 0 
    					&& hash.compareTo(myChordNode.getPredecessorHash()) > 0) {
    				db.insert(DatabaseHelper.TABLE_NAME, null, values);
    				Log.v("insert", values.toString());
    			}// value belongs here
    			else {
    				if (hash.compareTo(myChordNode.getHashedNodeId()) > 0) {
    					Log.v(TAG, "Sending to Successor....");
    					new InsertWrapper(myChordNode.getSuccessor(),values).sendInsert();
    				}
    				else if (hash.compareTo(myChordNode.getHashedNodeId()) < 0) {
    					Log.v(TAG, "Sending to Predecessor....");
    					new InsertWrapper(myChordNode.getPredecessor(),values).sendInsert();
    				}
    			}
    		} // end check for Inflection
    	}// for all content-value keys 	
    	
    	db.close();
    	getContext().getContentResolver().notifyChange(uri, null);
        return uri;
    }
    

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
    	
    	SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
    	queryBuilder.setTables(DatabaseHelper.TABLE_NAME);
    	db = dbHelper.getReadableDatabase();

    	// Local Dump
    	if (selection.matches("@")) {
    		Log.v("@query", "@");
     	    Cursor cursor = queryBuilder.query(db, null, null, null, null, null, null); 	
            return cursor;
    	}
    	// Global Dump
    	else if (selection.matches("\\*")) {
    		Log.v("*query", "*");
        	Cursor cursor = queryBuilder.query(db, null, null, null, null, null, null);
        	QueryWrapper qWrapper;
       		qWrapper = new QueryWrapper(myChordNode.getNodeId(),myChordNode.getSuccessor(),cursor,selection);
       		new Thread(qWrapper).start();
    		while (cursorbool) {
    			try { Thread.sleep(200); } 
    			catch (InterruptedException e) { e.printStackTrace(); }
    		}
    		cursorbool = true;
    		return myCursor;
    	}
    	// General Query
    	else {
    		System.out.println("Finding key..." + selection);
        	queryBuilder.appendWhere("key="+"'"+selection+"'");
        	Cursor cursor = queryBuilder.query(db, null, null, null, null, null, null); 	
        	if (cursor.getCount() == 0 && localQuery) {
	        	QueryWrapper qWrapper = new QueryWrapper(myChordNode.getNodeId(),myChordNode.getSuccessor(),cursor,selection);
	        	new Thread(qWrapper).start();
	    		while (cursorbool) {
	    			try { Thread.sleep(200);} 
	    			catch (InterruptedException e) { e.printStackTrace(); }
	    		}
	    		cursorbool = true;
	    		return myCursor;
        	}
        	else
        		return cursor;
    	}
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    	// TODO Auto-generated method stub
    	if (selection.equals("successor"))
    		return Integer.parseInt(myChordNode.getSuccessor());
    	else
    		return Integer.parseInt(myChordNode.getPredecessor());
    }

    private String getNodeId() {
        TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        String node = String.valueOf((Integer.parseInt(portStr)));
        return node;
    }
    
    // send and get from querywrapper
    
    
    private class ServerTask extends Thread{
	    
    	private final String TAG = ServerTask.class.getSimpleName();
	    protected Thread runningThread = null;

	    public void run() {
            synchronized(this){
                this.runningThread = Thread.currentThread();
            }
            try {	
            	ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            	Socket sock = null;
            	while (true) {
            		sock = null;
            		sock = serverSocket.accept();
            		new Thread(new MessageProcessor(sock)).run();
            	}
            } catch (IOException e) {
            	Log.e(TAG,"IO error: " + e);
            } 
            return;
        }
     }
   
    
    /*
     * Message processing class
     */
    class MessageProcessor implements Runnable{
    	Socket socket;
    	protected Thread runningThread = null;
    	
    	public MessageProcessor(Socket sock) {
    		this.socket = sock;
    	}

		@Override
		public void run() {
            synchronized(this){
                this.runningThread = Thread.currentThread();
            }
			InputStream is;
			try {
				is = socket.getInputStream();
				ObjectInputStream ois = new ObjectInputStream(is);
				Object recievedMessage = ois.readObject();
				if (recievedMessage instanceof ChordNode) {
					ChordNode rMessage = (ChordNode) recievedMessage;
					Log.e("Message:", "Got Message "+ rMessage.getNodeId());
					processChordNode((ChordNode)recievedMessage);
				}
				else if (recievedMessage instanceof UpdatePredecessor) {
					processUpdatePredecessorMessage((UpdatePredecessor) recievedMessage);
				}
				else if (recievedMessage instanceof UpdateSuccessor) {
					processUpdateSuccessorMessage((UpdateSuccessor) recievedMessage);
				}
				else if (recievedMessage instanceof InsertWrapper) {
					processInsert((InsertWrapper) recievedMessage);
				}
				else if (recievedMessage instanceof DeleteWrapper) {
					Log.e("delete", "Got delete message");
					processDelete((DeleteWrapper) recievedMessage);
				}
				else if (recievedMessage instanceof QueryWrapper) {
					System.out.println("General Query");
					processQuery((QueryWrapper) recievedMessage);
				}
				is = null;
				ois = null;
				recievedMessage = null;
			}  catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
        public void processChordNode(ChordNode chordNodeMessage) {
        // Not my own message
        if (myChordNode.getHashedNodeId().compareTo(chordNodeMessage.getHashedNodeId()) != 0) {
        	// First node join
        	if (myChordNode.getSuccessor().equals(myChordNode.getNodeId())
        			&& myChordNode.getPredecessor().equals(myChordNode.getNodeId())) {
        		Log.e("set succ","1st succ: "+myChordNode.getNodeId()+"->"+chordNodeMessage.getNodeId());
        		myChordNode.setSuccessor(chordNodeMessage.getNodeId());
        		myChordNode.setPredecessor(chordNodeMessage.getNodeId());
        		new UpdatePredecessor(myChordNode.getNodeId()).sendPre(chordNodeMessage.getNodeId());
        		new UpdateSuccessor(myChordNode.getNodeId()).sendSucc(chordNodeMessage.getNodeId());
        	}
        
        	// Remote Node message - Successor Logic
        	else if (myChordNode.getHashedNodeId().compareTo(chordNodeMessage.getHashedNodeId()) < 0) {
        	// Check for Point of Inflection (bighash->smallhash)
        		if (myChordNode.getSuccessorHash().compareTo(myChordNode.getHashedNodeId()) < 0) {
        			Log.e("Inflection ","set succ: "+myChordNode.getNodeId()+"->"+chordNodeMessage.getNodeId());
        			new UpdatePredecessor(myChordNode.getNodeId()).sendPre(chordNodeMessage.getNodeId());
        			new UpdateSuccessor(myChordNode.getSuccessor()).sendSucc(chordNodeMessage.getNodeId());
        			new UpdatePredecessor(chordNodeMessage.getNodeId()).sendPre(myChordNode.getSuccessor());
        			myChordNode.setSuccessor(chordNodeMessage.getNodeId());
        		}
        		// I have a successor, check if this is a better match
        		else {
        			if (chordNodeMessage.getHashedNodeId().compareTo(myChordNode.getSuccessorHash()) < 0 ) {
        				Log.e("set succ","reset succ: "+myChordNode.getNodeId()+"->"+chordNodeMessage.getNodeId());
        				new UpdatePredecessor(myChordNode.getNodeId()).sendPre(chordNodeMessage.getNodeId());
        				new UpdateSuccessor(myChordNode.getSuccessor()).sendSucc(chordNodeMessage.getNodeId());
        				new UpdatePredecessor(chordNodeMessage.getNodeId()).sendPre(myChordNode.getSuccessor());
        				myChordNode.setSuccessor(chordNodeMessage.getNodeId());
        			}
        			else {
        				Log.e("Go to","Go to Successor");
        				myChordNode.goToNextNode(chordNodeMessage,myChordNode.getSuccessor());
        			}
        		}
        	}
        	// Remote Node message - Predecessor Logic
        	else if (myChordNode.getHashedNodeId().compareTo(chordNodeMessage.getHashedNodeId()) > 0){
        		// Check for point of Inflection (smallhash->bighash)
        		if (myChordNode.getPredecessorHash().compareTo(myChordNode.getHashedNodeId()) > 0) {
        			Log.e("Inflection ","set prev: "+myChordNode.getNodeId()+"->"+chordNodeMessage.getNodeId());
        			new UpdatePredecessor(myChordNode.getPredecessor()).sendPre(chordNodeMessage.getNodeId());
        			new UpdateSuccessor(myChordNode.getNodeId()).sendSucc(chordNodeMessage.getNodeId());
        			new UpdateSuccessor(chordNodeMessage.getNodeId()).sendSucc(myChordNode.getPredecessor());
        			myChordNode.setPredecessor(chordNodeMessage.getNodeId());
        		}
        		else {
        			if (chordNodeMessage.getHashedNodeId().compareTo(myChordNode.getPredecessorHash()) > 0) {
        				Log.e("set prev","reset prev: "+myChordNode.getNodeId()+"->"+chordNodeMessage.getNodeId());
        				new UpdateSuccessor(myChordNode.getNodeId()).sendSucc(chordNodeMessage.getNodeId());
        				new UpdatePredecessor(myChordNode.getPredecessor()).sendPre(chordNodeMessage.getNodeId());
        				new UpdateSuccessor(chordNodeMessage.getNodeId()).sendSucc(myChordNode.getPredecessor());
        				myChordNode.setPredecessor(chordNodeMessage.getNodeId());
        			}
        			else {
        				Log.e("Go to","Go to Predecessor");
        				myChordNode.goToPrevNode(chordNodeMessage,myChordNode.getPredecessor());
        			}
        		}
        	}
        }
        return;
     }// end of Chord Ring Logic
        
        public void processUpdatePredecessorMessage(UpdatePredecessor upp) {
        	Log.e("update prev","update prev: "+myChordNode.getNodeId()+"->"+upp.getPredecessor());
        	myChordNode.setPredecessor(upp.getPredecessor());
        	return;
        }
        
        public void processUpdateSuccessorMessage(UpdateSuccessor ups) {
        	Log.e("update succ","update succ: "+myChordNode.getNodeId()+"->"+ups.getSucc());
        	myChordNode.setSuccessor(ups.getSucc());
        	return;
        }
    
        public void processInsert(InsertWrapper insert) {
        	String KEY_FIELD = "key";
        	String VALUE_FIELD = "value";
        	ContentValues content = new ContentValues();
        	System.out.println("got insert");
       		content.put(KEY_FIELD, insert.key);
       		content.put(VALUE_FIELD, insert.value);
       		System.out.println("key: "+ insert.key + " value: " + insert.value);
       		getContext().getContentResolver().insert(MESSAGES_URI,content);
        	return;
        }
    
        public void processDelete(DeleteWrapper delete) {
        	if (delete.ori.compareTo(myChordNode.getNodeId()) == 0) {
        		deletebool = false;
        	}
        	else if (delete.selection.matches("\\*") &&
        			delete.ori.compareTo(myChordNode.getNodeId()) != 0){
        		getContext().getContentResolver().delete(MESSAGES_URI, "@", null);
        		new DeleteWrapper(delete.ori, myChordNode.getSuccessor(),delete.selection).sendDelete();
        	}
        	else {
        			deletebool = false;
        			getContext().getContentResolver().delete(MESSAGES_URI, delete.selection, null);
        			new DeleteWrapper(delete.ori, myChordNode.getSuccessor(),delete.selection).sendDelete();
        	}
        }
   
        public void processQuery(QueryWrapper query) {
        	if (query.origin.equals(myChordNode.getNodeId())) {
        		System.out.println("Got my own message!!");
        		myCursor = Marshall.hashMaptoCursor(query.contentMap);
        		cursorbool = false;
        		System.out.println("Returning cursor for key "+ query.selection);

        	}
        	else {
        		if (query.selection.matches("\\*")) {
        			cursorbool = false;
            		localQuery = false;
            		Cursor cursor = getContext().getContentResolver().query(MESSAGES_URI, null, "@", null, null);
            		cursorbool = true;
            		localQuery = true;
            		query.contentMap = Marshall.addHashMapToCursor(query.contentMap,cursor);
                	QueryWrapper qWrapper = new QueryWrapper(query.origin,myChordNode.getSuccessor(),Marshall.hashMaptoCursor(query.contentMap),query.selection);
                	new Thread(qWrapper).start();
        		} 
        		else {
	        		cursorbool = false;
	        		localQuery = false;
	        		Cursor cursor = getContext().getContentResolver().query(MESSAGES_URI, null, query.selection, null, null);
	        		cursorbool = true;
	        		localQuery = true;
	        		query.contentMap = Marshall.addHashMapToCursor(query.contentMap,cursor);
	            	QueryWrapper qWrapper = new QueryWrapper(query.origin,myChordNode.getSuccessor(),Marshall.hashMaptoCursor(query.contentMap),query.selection);
	            	new Thread(qWrapper).start();
        		}
        	}
        }
    }
}
