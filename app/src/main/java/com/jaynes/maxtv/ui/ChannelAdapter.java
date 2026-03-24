package com.jaynes.maxtv.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.jaynes.maxtv.R;
import com.jaynes.maxtv.models.Channel;
import java.util.List;

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ViewHolder> {

    public interface OnChannelClickListener {
        void onChannelClick(Channel channel);
    }

    private final List<Channel> channels;
    private final OnChannelClickListener listener;

    public ChannelAdapter(List<Channel> channels, OnChannelClickListener listener) {
        this.channels = channels;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_channel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Channel channel = channels.get(position);
        holder.bind(channel, listener);
    }

    @Override
    public int getItemCount() { return channels.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivLogo;
        TextView tvName, tvCategory, tvBadge;
        View premiumOverlay;

        ViewHolder(View itemView) {
            super(itemView);
            ivLogo         = itemView.findViewById(R.id.ivLogo);
            tvName         = itemView.findViewById(R.id.tvName);
            tvCategory     = itemView.findViewById(R.id.tvCategory);
            tvBadge        = itemView.findViewById(R.id.tvBadge);
            premiumOverlay = itemView.findViewById(R.id.premiumOverlay);
        }

        void bind(Channel channel, OnChannelClickListener listener) {
            tvName.setText(channel.name);
            tvCategory.setText(channel.category);

            // Badge
            if (channel.isFree) {
                tvBadge.setText("BURE");
                tvBadge.setBackgroundColor(0xFF00CC77);
                tvBadge.setVisibility(View.VISIBLE);
            } else if (channel.hasDrm) {
                tvBadge.setText("HD");
                tvBadge.setBackgroundColor(0xFFE8001D);
                tvBadge.setVisibility(View.VISIBLE);
            } else {
                tvBadge.setVisibility(View.GONE);
            }

            // Logo
            if (channel.image != null && !channel.image.isEmpty()) {
                Glide.with(itemView.getContext())
                    .load(channel.image)
                    .placeholder(R.drawable.ic_channel_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(ivLogo);
            } else {
                ivLogo.setImageResource(R.drawable.ic_channel_placeholder);
            }

            // Click
            itemView.setOnClickListener(v -> listener.onChannelClick(channel));
        }
    }
}
