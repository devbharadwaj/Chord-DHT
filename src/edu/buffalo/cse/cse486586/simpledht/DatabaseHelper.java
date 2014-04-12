package edu.buffalo.cse.cse486586.simpledht;

import android.content.Context;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "DHT";
	public static final int DATABASE_VERSION = 1;
	public static final String TABLE_NAME = "provider";
	public static final String KEY = "key";
	public static final String VALUE = "value";
	
	private final String createTable = "create table " + TABLE_NAME + "("
			+ KEY + " TEXT PRIMARY KEY, "
			+ VALUE + " TEXT); ";
			
	public DatabaseHelper(Context context) {
		    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
/*	
	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, DATABASE_NAME, factory, version);
	}
*/
	
	@Override
	public void onCreate(SQLiteDatabase arg0) {
		arg0.execSQL(createTable);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

}
