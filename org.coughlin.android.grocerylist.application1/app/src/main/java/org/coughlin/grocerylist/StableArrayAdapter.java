/**
 * 
 */
package org.coughlin.grocerylist;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * @author byron
 *
 */
public class StableArrayAdapter extends SimpleCursorAdapter {
	Cursor mIdMap;
	View.OnTouchListener mTouchListener;
	ListView mListview;

	public StableArrayAdapter(Context context, int textViewResourceId,
			 Cursor cursor, String[] from, int[] to, int flags, View.OnTouchListener listener) {
		super(context, textViewResourceId, cursor, from, to, flags);
		mTouchListener = listener;
		mIdMap = cursor;	
	}
	
	@Override 
	public long getItemId(int position) {
		return mIdMap.getPosition();
	}
	
	public void setCursor(Cursor newCursor) {
		mIdMap = newCursor;
	}
	
	@Override
	public boolean hasStableIds() {
		return true;
	}
	
	@Override	
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		if (view != convertView) {
			// Add touch listener to every new view to track swipe motion
			view.setOnTouchListener(mTouchListener);
		}
		return view;	
	}
}
