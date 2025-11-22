package me.anky.connectid.tags;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
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
    private List<ConnectionTag> tagsOriginal = new ArrayList<>();
    private LayoutInflater inflater;
    private Context context;
    private RecyclerViewClickListener clickListener;
    private boolean searchMode;
    private String searchKeyword;

    public interface RecyclerViewClickListener {
        void onItemClick(View view, int position);
    }

    public TagsRecyclerViewAdapter(Context context, List<ConnectionTag> tags, RecyclerViewClickListener clickListener) {
        this.inflater = LayoutInflater.from(context);
        this.tags = tags;
        this.tagsOriginal = new ArrayList<>(tags);
        this.clickListener = clickListener;
        this.searchKeyword = "";
        this.searchMode = false;
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
        int count = 0;
        if (ids != null ) {
            List<String> idsList = new ArrayList(Arrays.asList(ids.split(", ")));
            count =idsList.size();
        }

        holder.tagItemTv.setText(String.format(context.getString(R.string.tag_with_count), tag, count));
    }

    @Override
    public int getItemCount() {
        if (tags == null) {
            return 0;
        } else {
            return tags.size();
        }
    }

    public void setNewData(boolean searchMode, String searchKeyword) {
        this.searchMode = searchMode;
        this.searchKeyword = searchKeyword;
        notifyDataSetChanged();
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
            this.tags = new ArrayList<>();
            this.tagsOriginal = new ArrayList<>();
        } else {
            this.tags.clear();
            this.tags.addAll(tags);

            this.tagsOriginal.clear();
            this.tagsOriginal.addAll(tags);

            notifyDataSetChanged();
        }
    }

    public void filter() {
        tags.clear();
        if (searchKeyword == null || searchKeyword.isEmpty()) {
            tags.addAll(tagsOriginal);
        } else {
            String lower = searchKeyword.toLowerCase();
            for (ConnectionTag item : tagsOriginal) {
                if (item.getTag() != null && item.getTag().toLowerCase().contains(lower)) {
                    tags.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }
}
