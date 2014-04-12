package edu.buffalo.cse.cse486586.simpledht;

import java.util.HashMap;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;

public class Marshall {

	public static HashMap<String,String> cursorToHashMap(Cursor cursor) {
		if (cursor != null) {
			HashMap<String,String> contentMap = new HashMap<String,String>();
		    if (cursor.moveToFirst()) {
		        do {
		            contentMap.put(cursor.getString(0),cursor.getString(1));
		        } while (cursor.moveToNext());
		    } 
		    if (cursor != null && !cursor.isClosed()) {
		        cursor.close();
		    }
			return contentMap;
		}
		return null;
	}
	
	public static synchronized Cursor hashMaptoCursor(HashMap<String,String> contentMap) {
		if (contentMap != null) {
			MatrixCursor matrixCursor = new MatrixCursor(new String[] { "key", "value" });
			for (String key: contentMap.keySet()) {
				matrixCursor.addRow(new Object[] { key, contentMap.get(key) });
			}
			return matrixCursor;
		}
		return null;
	}

	public static synchronized HashMap<String,String> addHashMapToCursor(HashMap<String,String> contentMap, Cursor cursor) {
		if (contentMap != null && cursor != null) {
			MatrixCursor matrixCursor = new MatrixCursor(new String[] { "key", "value" });
			for (String key: contentMap.keySet()) {
				matrixCursor.addRow(new Object[] { key, contentMap.get(key) });
			}
			MergeCursor mergeCursor = new MergeCursor(new Cursor[] { matrixCursor, cursor });
			return Marshall.cursorToHashMap(mergeCursor);
		}
		else if (contentMap != null && cursor == null) {
			return contentMap;
		}
		else if (contentMap == null && cursor != null) {
			return Marshall.cursorToHashMap(cursor);
		}
		return null;
	}
	
	public static synchronized String[] printCursor(Cursor cursor) {
		String[] rows = new String[200];
		int i = 0;
	    if (cursor.moveToFirst()) {
	        do {
	            rows[i++] = cursor.getString(0);
	            rows[i++] = cursor.getString(1);
	        } while (cursor.moveToNext());
	    }
	    if (cursor != null && !cursor.isClosed()) {
	        cursor.close();
	    }
	    return rows;
	}
}
