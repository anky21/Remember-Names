package me.anky.connectid.tags;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.anky.connectid.R;
import me.anky.connectid.data.ConnectionTag;

/**
 * Created by Anky An on 5/11/2017.
 * anky25@gmail.com
 */

public class TagsRecyclerViewAdapter extends RecyclerView.Adapter<TagsRecyclerViewAdapter.ViewHolder> {
    private List<ConnectionTag> tags = new ArrayList<>();
    private LayoutInflater inflater;
    private Context context;
    private RecyclerViewClickListener clickListener;

    public interface RecyclerViewClickListener {
        void onItemClick(View view, int position);
    }

    public TagsRecyclerViewAdapter(Context context, List<ConnectionTag> tags, RecyclerViewClickListener clickListener) {
        this.inflater = LayoutInflater.from(context);
        this.tags = tags;
        this.clickListener = clickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.tags_list_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);

        context = parent.getContext();
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onItemClick(v, viewHolder.getAdapterPosition());
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(TagsRecyclerViewAdapter.ViewHolder holder, int position) {
        String tag = tags.get(position).getTag();
        String ids = tags.get(position).getConnection_ids();
        List<String> idsList = new ArrayList(Arrays.asList(ids.split(", ")));

        holder.tagItemTv.setText(String.format(context.getString(R.string.tag_with_count), tag, idsList.size()));
    }

    @Override
    public int getItemCount() {
        if (tags == null) {
            return 0;
        } else {
            return tags.size();
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tag_item_tv)
        TextView tagItemTv;

        public ViewHolder(View itemView){
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void setTags(List<ConnectionTag> tags) {
        if (tags == null) {
            tags = new ArrayList<>();
            this.tags = tags;
        } else {
            this.tags.clear();
            this.tags.addAll(tags);
            notifyDataSetChanged();
        }
    }
}
