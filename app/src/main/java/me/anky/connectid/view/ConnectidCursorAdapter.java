package me.anky.connectid.view;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import me.anky.connectid.R;
import me.anky.connectid.data.ConnectidColumns;

public class ConnectidCursorAdapter
        extends CursorRecyclerViewAdapter<ConnectidCursorAdapter.ViewHolder> {

//    public interface OnItemClickListener {
//        public void onItemClicked(View view);
//    }
//
//    private OnItemClickListener mListener;
//
//    public void setClickListener(OnItemClickListener listener) {
//        mListener = listener;
//    }

    Context mContext;
    ViewHolder mVh;

    public ConnectidCursorAdapter(Context context, Cursor cursor){
        super(context, cursor);
        mContext = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public TextView mListItemTv;
        public ViewHolder(View view){
            super(view);
            mListItemTv = (TextView) view.findViewById(R.id.list_item_tv);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            Log.i("TESTING", "Clicked database item id: " + mListItemTv.getTag());
            Log.i("TESTING", mListItemTv.getText().toString());
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
        int id = cursor.getInt(cursor.getColumnIndex(ConnectidColumns._ID));
        String name = cursor.getString(cursor.getColumnIndex(ConnectidColumns.NAME));
        String description = cursor.getString(cursor.getColumnIndex(ConnectidColumns.DESCRIPTION));

        viewHolder.mListItemTv.setTag(id);
        viewHolder.mListItemTv.setText(name + " - " + description);
    }

}
