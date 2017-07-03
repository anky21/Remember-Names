package me.anky.connectid.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import me.anky.connectid.R;
import me.anky.connectid.data.ConnectidConnection;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private ArrayList<ConnectidConnection> mConnections;
    private Context mContext;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // TODO Remove if tracking item clicks is never necessary
    private int mClickedPosition = -1;

    public RecyclerViewAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }

    // Inflates the item layout and returns the holder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.connections_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        mContext = parent.getContext();
        return viewHolder;
    }

    // Binds the data to each item via the holder
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String name = mConnections.get(position).getName();
        String description = mConnections.get(position).getDescription();
        holder.listItemTv.setText(name + " - " + description);

        // TODO Remove if tracking item clicks is never necessary
        if (mClickedPosition == position) {
            Toast.makeText(
                    mContext,
                    "Adapter position " + position + " clicked!",
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    // Returns the total number of items in the list
    @Override
    public int getItemCount() {
        if (mConnections == null) {
            return 0;
        } else {
            return mConnections.size();
        }
    }

    // Stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView listItemTv;

        public ViewHolder(View itemView) {
            super(itemView);
            listItemTv = (TextView) itemView.findViewById(R.id.list_item_tv);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) {
                mClickListener.onItemClick(view, getAdapterPosition());
            }

            // TODO Remove if tracking item clicks is never necessary
            mClickedPosition = getAdapterPosition();
            notifyItemChanged(mClickedPosition);
        }
    }

    // TODO Implement parcelable for ConnectidConnection
    // Retrieves the list item at the clicked position
    public ConnectidConnection getParcelableItem(int position) {
        return mConnections.get(position);
    }

    // Retrieves the entire array list to preserve scroll position during orientation changes
    public ArrayList<ConnectidConnection> getParcelableArrayList() {
        return mConnections;
    }

    // Listens for click events
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // Implemented in MovieListActivity to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    // Updates the array, allowing for uninterrupted scrolling
    public void setConnections(ArrayList<ConnectidConnection> connections) {
        if (mConnections == null) {
            mConnections = new ArrayList<>();
            mConnections = connections;
        } else {
            mConnections.addAll(connections);
            notifyDataSetChanged();
        }
    }

    // Clears the array
    public void resetAdapter() {
        if (mConnections != null) {
            mConnections.clear();
            notifyDataSetChanged();
        }
    }

    // Restores the previous array on device orientation change
    public void refillAdapter(ArrayList<ConnectidConnection> data) {
        if (data != null) {
            mConnections = new ArrayList<>();
            mConnections.addAll(data);
        }
    }
}