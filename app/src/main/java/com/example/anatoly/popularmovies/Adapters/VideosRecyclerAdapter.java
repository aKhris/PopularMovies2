package com.example.anatoly.popularmovies.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.anatoly.popularmovies.R;
import com.example.anatoly.popularmovies.TMDBObjects.Video;

import java.util.List;

public class VideosRecyclerAdapter extends RecyclerView.Adapter<VideosRecyclerAdapter.VideoViewHolder>{
    private List<Video> videos;
    private OnItemClickListener onItemClickListener;

    public VideosRecyclerAdapter(List<Video> videos, OnItemClickListener listener) {
        this.videos = videos;
        this.onItemClickListener = listener;
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final VideoViewHolder holder, int position) {
        Video video = videos.get(position);
        holder.videoNameTextView.setText(video.getName());
        holder.shareImageView.setVisibility(position==0?View.VISIBLE:View.GONE);
        holder.playImageView.setTag(video.getKey());
        holder.shareImageView.setTag(video.getKey());
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.videos_item_layout, parent, false);
        return new VideoViewHolder(view);
    }

    class VideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView   playImageView;
        ImageView   shareImageView;
        TextView    videoNameTextView;

        VideoViewHolder(View itemView) {
            super(itemView);
            playImageView = itemView.findViewById(R.id.iv_video_play);
            shareImageView = itemView.findViewById(R.id.iv_video_share);
            videoNameTextView = itemView.findViewById(R.id.tv_video_name);
            playImageView.setOnClickListener(this);
            shareImageView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            onItemClickListener.itemClicked(getAdapterPosition(), v);
        }
    }

}
