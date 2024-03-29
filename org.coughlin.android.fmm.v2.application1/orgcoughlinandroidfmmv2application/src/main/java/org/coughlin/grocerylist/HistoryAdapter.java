/**
 * 
 */
package org.coughlin.grocerylist;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * @author byron
 *
 */
public class HistoryAdapter extends SimpleCursorAdapter {
	View.OnTouchListener mTouchListener;
	public static final String MONTH_DAY_YEAR = "mm-dd-yyyy";
	public static final String YEAR_MONTH_DAY = "yyyy-mm-dd";
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(YEAR_MONTH_DAY, Locale.US);
	public static final SimpleDateFormat VIEW_DATE_FORMAT = new SimpleDateFormat(MONTH_DAY_YEAR, Locale.US);

	public HistoryAdapter(Context context, int textViewResourceId, Cursor cursor, String[] from, int[] to, int flags, View.OnTouchListener listener) {
		super(context, textViewResourceId, cursor, from, to, flags);
		mTouchListener = listener;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void bindView(final View view, Context arg1, Cursor cursor) {

		 final String date = cursor.getString(cursor.getColumnIndexOrThrow(Historylist.HIS_DATE));
		 final String product = cursor.getString(cursor.getColumnIndexOrThrow(FamilyMealContracts.Products.PRO_NAME));
		 displayProduct(view, product);
		 displayDate(view, date);		
	}

	@Override
	public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
		return LayoutInflater.from(context).inflate(R.layout.historylist_item, parent, false);
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
	
	 /**
     * Formats and displays the date
     * 
     * @param view view
     * @param strDate start date
     */
    private void displayDate(final View view, final String strDate) {
    	java.util.Date formattedDate;
    	String fd = null;
		try {
			formattedDate = DATE_FORMAT.parse(strDate);
			assert formattedDate != null;
			fd = VIEW_DATE_FORMAT.format(formattedDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        final TextView t2 = view.findViewById(R.id.dateTxt);
        t2.setText(fd);
    }
    
    private void displayProduct(final View view, final String strProduct) {
    	final TextView t1 = view.findViewById(R.id.productTxt);
    	t1.setText(strProduct);    	
    }

}
