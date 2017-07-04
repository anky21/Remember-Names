package me.anky.connectid.view;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import me.anky.connectid.R;
import me.anky.connectid.data.ConnectidColumns;

public class ConnectidCursorAdapter extends CursorRecyclerViewAdapter<ConnectidCursorAdapter.ViewHolder> {

    Context mContext;
    ViewHolder mVh;

    public ConnectidCursorAdapter(Context context, Cursor cursor){
        super(context, cursor);
        mContext = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mListItemTv;
        public ViewHolder(View view){
            super(view);
            mListItemTv = (TextView) view.findViewById(R.id.list_item_tv);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.connections_list_item, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        mVh = vh;
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor){
        DatabaseUtils.dumpCursor(cursor);
        String name = cursor.getString(cursor.getColumnIndex(ConnectidColumns.NAME));
        String description = cursor.getString(cursor.getColumnIndex(ConnectidColumns.DESCRIPTION));
        viewHolder.mListItemTv.setText(name + " - " + description);
    }

}
