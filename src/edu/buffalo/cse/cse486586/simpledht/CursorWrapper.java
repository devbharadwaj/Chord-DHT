package edu.buffalo.cse.cse486586.simpledht;

import java.io.Serializable;
import java.util.HashMap;

import android.database.Cursor;

public class CursorWrapper implements Serializable {
	
	HashMap<String,String> cursorMap;
	
	public CursorWrapper() {
		cursorMap = new HashMap<String, String>();
	}
	// For "*" query
	public void getAll(ChordNode chordNode, Cursor cursor) {
		
	}

}
