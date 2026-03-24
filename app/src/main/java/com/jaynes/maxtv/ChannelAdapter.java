package com.jaynes.maxtv;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * ChannelAdapter — RecyclerView adapter kwa channels grid
 */
public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ViewHolder> {

    public interface OnChannelClick {
        void onClick(Channel channel);
    }

    private final Context         context;
    private final List<Channel>   channels = new ArrayList<>();
    private final OnChannelClick  listener;

    public ChannelAdapter(Context context, OnChannelClick listener) {
        this.context  = context;
        this.listener = listener;
    }

    public void setChannels(List<Channel> list) {
        channels.clear();
        channels.addAll(list);
        notifyDataSetChanged();
    }

    public void filterByCategory(String category, List<Channel> allChannels) {
        channels.clear();
        if (category == null || category.equals("ZOTE")) {
            channels.addAll(allChannels);
        } else {
            for (Channel ch : allChannels) {
                if (category.equals(ch.category)) channels.add(ch);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_channel, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Channel ch = channels.get(position);

        h.tvName.setText(ch.name);
        h.tvCategory.setText(ch.category);

        // Badge: FREE / LIVE / DRM
        if (ch.isFree) {
            h.tvBadge.setText("FREE");
            h.tvBadge.setBackgroundColor(Color.parseColor("#00C853"));
            h.tvBadge.setVisibility(View.VISIBLE);
        } else if (ch.hasDrm) {
            h.tvBadge.setText("HD");
            h.tvBadge.setBackgroundColor(Color.parseColor("#E8001D"));
            h.tvBadge.setVisibility(View.VISIBLE);
        } else {
            h.tvBadge.setVisibility(View.GONE);
        }

        // Load image with Glide
        if (!ch.image.isEmpty()) {
            Glide.with(context)
                 .load(ch.image)
                 .placeholder(R.drawable.ic_channel_placeholder)
                 .error(R.drawable.ic_channel_placeholder)
                 .transition(DrawableTransitionOptions.withCrossFade(200))
                 .centerCrop()
                 .into(h.ivLogo);
        } else {
            h.ivLogo.setImageResource(R.drawable.ic_channel_placeholder);
        }

        h.itemView.setOnClickListener(v -> listener.onClick(ch));
    }

    @Override public int getItemCount() { return channels.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivLogo;
        TextView  tvName, tvCategory, tvBadge;

        ViewHolder(View v) {
            super(v);
            ivLogo     = v.findViewById(R.id.iv_logo);
            tvName     = v.findViewById(R.id.tv_name);
            tvCategory = v.findViewById(R.id.tv_category);
            tvBadge    = v.findViewById(R.id.tv_badge);
        }
    }
}
