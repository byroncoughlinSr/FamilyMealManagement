/**
 * 
 */
package org.coughlin.grocerylist;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author byron
 *
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
	private List<ProductHistory> historyList;
	private final HistoryViewModel viewModel;
	View.OnTouchListener mTouchListener;
	public static final String MONTH_DAY_YEAR = "mm-dd-yyyy";
	public static final String YEAR_MONTH_DAY = "yyyy-mm-dd";
	private LiveData<Product> product;

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
	public HistoryAdapter(@NonNull List<ProductHistory> historyList,
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
		ProductHistory historyItem = historyList.get(position);

		// Fetch the LiveData<Product> for the given product ID
		LiveData<Product> productLiveData = viewModel.getProductNameById(historyItem.getProId());

		// Observe the LiveData to handle updates
		productLiveData.observeForever(new Observer<Product>() {
			@Override
			public void onChanged(Product product) {
				String productName = (product != null && product.getName() != null)
						? product.getName()
						: "Unknown Product";

				String hisDate = Objects.requireNonNullElse(historyItem.getHisDate(), "Unknown Date");
				String displayText = String.format("%s - %s", productName, hisDate);

				holder.getTextView().setText(displayText);

				// Remove observer to avoid memory leaks
				productLiveData.removeObserver(this);
			}
		});
	}

	public void updateHistorylist(List<ProductHistory> newProducts) {
		this.historyList.clear();
		this.historyList.addAll(newProducts);
		notifyDataSetChanged(); // Notify RecyclerView to refresh
	}

	@Override
	public int getItemCount() {
		return historyList != null ? historyList.size() : 0;
	}

}

