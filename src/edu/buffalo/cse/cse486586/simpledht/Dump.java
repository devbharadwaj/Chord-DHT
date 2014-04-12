package edu.buffalo.cse.cse486586.simpledht;
import android.content.ContentResolver;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;


public class Dump implements OnClickListener {

	private TextView tv;
	private ContentResolver contentResolver;
	private static final String AUTH = "edu.buffalo.cse.cse486586.simpledht";
	private static final Uri MESSAGES_URI = Uri.parse("content://"+AUTH+"."+DatabaseHelper.TABLE_NAME);
	
	public Dump(TextView tv, ContentResolver contentResolver) {
		this.tv = tv;
		this.contentResolver = contentResolver;
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		contentResolver.delete(MESSAGES_URI, "key1", null);
	}

}
