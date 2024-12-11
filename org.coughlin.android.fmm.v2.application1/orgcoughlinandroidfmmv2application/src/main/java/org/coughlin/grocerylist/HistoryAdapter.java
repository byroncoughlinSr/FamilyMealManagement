/**
 * 
 */
package org.coughlin.grocerylist;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author byron
 *
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
	private List<ProductHistoryDetail> historyList;
	private final HistoryViewModel viewModel;
	View.OnTouchListener mTouchListener;
	public static final String MONTH_DAY_YEAR = "mm-dd-yyyy";
	public static final String YEAR_MONTH_DAY = "yyyy-mm-dd";
	private Product product;
	private HistoryRepository historyRepository;

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(YEAR_MONTH_DAY, Locale.US);
	public static final SimpleDateFormat VIEW_DATE_FORMAT = new SimpleDateFormat(MONTH_DAY_YEAR, Locale.US);
	public static class HistoryViewHolder extends RecyclerView.ViewHolder {
		private final TextView historyNameTextView;
		public HistoryViewHolder(@NonNull View itemView) {
			super(itemView);
			historyNameTextView = itemView.findViewById(R.id.historyItem);
		}
		public TextView getTextView() {
			return historyNameTextView;
		}
	}
	public HistoryAdapter(@NonNull List<ProductHistoryDetail> historyList,
						  @NonNull HistoryViewModel viewModel) {
		this.historyList = historyList;
		this.viewModel = viewModel;
	}
	@NonNull
	@Override
	public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.historylist_item, parent, false);
		return new HistoryViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
		// Get the current ProductHistory object
		ProductHistoryDetail historyItem = historyList.get(position);

		// Format the text to include the product name and history date
		//String displayText = String.format("%s - %s", productName, historyItem.getHisDate());

		// Set the formatted text to the TextView
		//holder.getTextView().setText(displayText);
	}

	public void updateHistorylist(List<ProductHistoryDetail> newProducts) {
		this.historyList = newProducts;
		notifyDataSetChanged(); // Notify RecyclerView to refresh
	}

	@Override
	public int getItemCount() {
		return historyList != null ? historyList.size() : 0;
	}

}

