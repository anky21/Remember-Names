package me.anky.connectid.connections;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.SparseBooleanArray;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.anky.connectid.R;
import me.anky.connectid.data.ConnectidConnection;

public class ConnectionsRecyclerViewAdapter extends
        RecyclerView.Adapter<ConnectionsRecyclerViewAdapter.ViewHolder> {

    private List<ConnectidConnection> connections = new ArrayList<>();
    private List<ConnectidConnection> connectionsOriginal = new ArrayList<>();
    private LayoutInflater inflater;
    private Context context;
    private RecyclerViewClickListener clickListener;
    private MultiSelectListener multiSelectListener;
    private boolean searchMode;
    private String searchKeyword;
    private SparseBooleanArray selectedItems;
    private boolean isMultiSelectEnabled = false;


    public interface RecyclerViewClickListener {
        void onItemClick(View view, int databaseId, int position); // Added position
    }

    public interface MultiSelectListener {
        void onMultiSelectStart();
        void onItemSelectedStateChanged();
        void onMultiSelectEnd();
    }

    public ConnectionsRecyclerViewAdapter(Context context, List<ConnectidConnection> connections,
                                        RecyclerViewClickListener clickListener, MultiSelectListener multiSelectListener) {
        this.inflater = LayoutInflater.from(context);
        this.connections = connections;
        this.clickListener = clickListener;
        this.multiSelectListener = multiSelectListener;
        this.selectedItems = new SparseBooleanArray();
        this.searchKeyword = "";
        this.searchMode = false; // Default searchMode to false
    }

    public void setNewData(boolean searchMode, String searchKeyword) {
        this.searchMode = searchMode;
        this.searchKeyword = searchKeyword;

        notifyDataSetChanged();
    }

    // Inflates the item layout and returns the holder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.connections_list_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);

        context = parent.getContext();
        connectionsOriginal = new ArrayList<>(connections);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ConnectionsRecyclerViewAdapter.ViewHolder holder, final int position) {
        ConnectidConnection currentConnection = connections.get(position);
        String firstName = currentConnection.getFirstName();
        String lastName = currentConnection.getLastName();
        String feature = currentConnection.getFeature();
        String imageName = currentConnection.getImageName();

        ContextWrapper cw = new ContextWrapper(context);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        String path = directory.getAbsolutePath() + "/" + imageName;
        Glide.with(context)
                .load(Uri.fromFile(new File(path)))
                .apply(RequestOptions.circleCropTransform())
                .into(holder.listItemIv);
        String name = firstName + " " + lastName;
        holder.listNameTv.setText(name);
        holder.listFeatureTv.setText(feature);

        // Visual indication of selection
        holder.itemView.setActivated(selectedItems.get(position, false));

        holder.container.setOnClickListener(v -> {
            if (isMultiSelectEnabled) {
                toggleSelection(holder.getAdapterPosition());
                multiSelectListener.onItemSelectedStateChanged();
            } else {
                if (clickListener != null) {
                    clickListener.onItemClick(v, currentConnection.getDatabaseId(), holder.getAdapterPosition());
                }
            }
        });

        holder.container.setOnLongClickListener(v -> {
            if (!isMultiSelectEnabled) {
                isMultiSelectEnabled = true;
                multiSelectListener.onMultiSelectStart();
            }
            toggleSelection(holder.getAdapterPosition());
            multiSelectListener.onItemSelectedStateChanged();
            return true;
        });

        highlightSearchKeyword(holder, name, feature);
    }

    public void toggleSelection(int position) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
        } else {
            selectedItems.put(position, true);
        }
        notifyItemChanged(position);
    }

    public void clearSelections() {
        isMultiSelectEnabled = false;
        selectedItems.clear();
        notifyDataSetChanged(); // Or iterate and notifyItemChanged for previously selected items
        multiSelectListener.onMultiSelectEnd();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(connections.get(selectedItems.keyAt(i)).getDatabaseId());
        }
        return items;
    }

    public boolean isMultiSelectEnabled() {
        return isMultiSelectEnabled;
    }

    public void setMultiSelectEnabled(boolean enabled) {
        this.isMultiSelectEnabled = enabled;
        if (!enabled) {
            selectedItems.clear(); // Ensure selections are cleared when mode is disabled externally
            notifyDataSetChanged();
        }
    }

    private void highlightSearchKeyword(ViewHolder holder, String name, String feature) {

        if (searchMode && holder.listNameTv.getText().toString().toLowerCase().contains(searchKeyword)) {

            Spannable spannable = new SpannableString(name);

            int position = name.toLowerCase().indexOf(searchKeyword);

            spannable.setSpan(new ForegroundColorSpan(Color.BLUE), position, position + searchKeyword.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            holder.listNameTv.setText(spannable, TextView.BufferType.SPANNABLE);
        } else {
            holder.listNameTv.setText(name);
        }

        if (searchMode && holder.listFeatureTv.getText().toString().toLowerCase().contains(searchKeyword)) {

            Spannable spannable = new SpannableString(feature);

            int position = feature.toLowerCase().indexOf(searchKeyword);

            spannable.setSpan(new ForegroundColorSpan(Color.BLUE), position, position + searchKeyword.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            holder.listFeatureTv.setText(spannable, TextView.BufferType.SPANNABLE);
        } else {
            holder.listFeatureTv.setText(feature);
        }
    }

    @Override
    public int getItemCount() {
        if (connections == null) {
            return 0;
        } else {
            return connections.size();
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.list_item_iv)
        ImageView listItemIv;
        @BindView(R.id.list_name_tv)
        TextView listNameTv;
        @BindView(R.id.list_feature_tv)
        TextView listFeatureTv;
        @BindView(R.id.list_item_container)
        LinearLayout container;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    // Updates the array, allowing for uninterrupted scrolling
    public void setConnections(List<ConnectidConnection> connections) {
        if (connections == null) {
            connections = new ArrayList<>();
            this.connections = connections;
        } else {
            this.connections.clear();
            this.connections.addAll(connections);
            notifyDataSetChanged();
        }
    }

    public void filter() {
        connections.clear();
        if (searchKeyword.isEmpty()) {
            connections.addAll(connectionsOriginal);
        } else {
            searchKeyword = searchKeyword.toLowerCase();
            for (ConnectidConnection item : connectionsOriginal) {
                if ((item.getAppearance() != null && item.getAppearance().toLowerCase().contains(searchKeyword))
                        || (item.getFeature() != null && item.getFeature().toLowerCase().contains(searchKeyword))
                        || (item.getDescription() != null && item.getDescription().toLowerCase().contains(searchKeyword))
                        || (item.getMeetVenue() != null && item.getMeetVenue().toLowerCase().contains(searchKeyword))
                        || (item.getTags() != null && item.getTags().contains(searchKeyword)) // Assuming tags is a simple string for now
                        || (item.getFirstName() != null && item.getFirstName().toLowerCase().contains(searchKeyword))
                        || (item.getLastName() != null && item.getLastName().toLowerCase().contains(searchKeyword))) {
                    connections.add(item);
                }
            }
        }
        // If in multi-select mode, preserve selections (this might need more sophisticated handling
        // if filtering is allowed during multi-select, e.g., by re-evaluating selectedItems
        // against the filtered list. For now, filter() might implicitly clear visual selections
        // on items that are filtered out, which is acceptable for a first pass).
        notifyDataSetChanged();
    }
}
