package me.anky.connectid.connections;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
    private List<ConnectidConnection> connectionsOriginal;
    private LayoutInflater inflater;
    private Context context;
    private RecyclerViewClickListener clickListener;
    private boolean searchMode;
    private String searchKeyword;

    public interface RecyclerViewClickListener {
        void onItemClick(View view, int position);
    }

    public ConnectionsRecyclerViewAdapter(Context context, List<ConnectidConnection> connections, boolean searchMode, RecyclerViewClickListener clickListener) {
        this.inflater = LayoutInflater.from(context);
        this.connections = connections;
        this.searchMode = searchMode;
        this.clickListener = clickListener;
        this.searchKeyword = "";
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

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onItemClick(v, viewHolder.getAdapterPosition());
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ConnectionsRecyclerViewAdapter.ViewHolder holder, int position) {

        String firstName = connections.get(position).getFirstName();
        String lastName = connections.get(position).getLastName();
        String feature = connections.get(position).getFeature();
        String imageName = connections.get(position).getImageName();

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

        highlightSearchKeyword(holder, name, feature);
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
                        || (item.getTags() != null && item.getTags().contains(searchKeyword))
                        || (item.getFirstName() != null && item.getFirstName().toLowerCase().contains(searchKeyword))
                        || (item.getLastName() != null && item.getLastName().toLowerCase().contains(searchKeyword))) {
                    connections.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }
}
