package me.anky.connectid.connections;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.anky.connectid.R;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.details.DetailsActivity;

public class ConnectionsRecyclerViewAdapter extends
        RecyclerView.Adapter<ConnectionsRecyclerViewAdapter.ViewHolder>  {

    private List<ConnectidConnection> connections;
    private LayoutInflater inflater;
    private Context context;
    private ItemClickListener clickListener;

    // Track user clicks
    private int clickedPosition = -1;

    public ConnectionsRecyclerViewAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
        connections = new ArrayList<>();
    }

    // Inflates the item layout and returns the holder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.connections_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        context = parent.getContext();

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ConnectionsRecyclerViewAdapter.ViewHolder holder, int position) {

        String name = connections.get(position).getName();
        String description = connections.get(position).getDescription();

        holder.listItemTv.setText(name + " - " + description);

        if (clickedPosition == position) {
            Log.i("MVP view", "position " + clickedPosition + " clicked");
            Intent intent = new Intent(holder.listItemTv.getContext(), DetailsActivity.class);
            intent.putExtra("ID", position);
            intent.putExtra("DETAILS", holder.listItemTv.getText().toString());
            holder.listItemTv.getContext().startActivity(intent);
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

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView listItemTv;

        public ViewHolder(View itemView) {
            super(itemView);
            listItemTv = (TextView) itemView.findViewById(R.id.list_item_tv);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) {
                clickListener.onItemClick(view, getAdapterPosition());
            }

            clickedPosition = getAdapterPosition();
            notifyItemChanged(clickedPosition);
        }
    }

    // Retrieves the ConnectidConnection object at the clicked position
    // TODO Implement parcelable in ConnectidConnection
    public ConnectidConnection getParcelableItem(int position) {
        return connections.get(position);
    }


    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    // Updates the array, allowing for uninterrupted scrolling
    public void setConnections(List<ConnectidConnection> connections) {
        if (connections == null) {
            connections = new ArrayList<>();
            this.connections = connections;
        } else {
            this.connections.addAll(connections);
            notifyDataSetChanged();
        }
    }


    public void resetAdapter() {
        if (connections != null) {
            connections.clear();
            notifyDataSetChanged();
        }
    }

    // Restores the previous array on device orientation change
    public void refillAdapter(List<ConnectidConnection> oldConnections) {
        if (oldConnections != null) {
            connections = new ArrayList<>();
            connections.addAll(oldConnections);
        }
    }
}
