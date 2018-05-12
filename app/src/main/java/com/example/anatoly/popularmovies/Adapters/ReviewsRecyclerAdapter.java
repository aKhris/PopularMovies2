package com.example.anatoly.popularmovies.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.anatoly.popularmovies.R;
import com.example.anatoly.popularmovies.TMDBObjects.Review;

import java.util.List;

public class ReviewsRecyclerAdapter extends RecyclerView.Adapter<ReviewsRecyclerAdapter.ReviewViewHolder>{
    private List<Review> reviews;
    private OnItemClickListener onItemClickListener;

    public ReviewsRecyclerAdapter(List<Review> reviews, OnItemClickListener listener) {
        this.reviews = reviews;
        this.onItemClickListener = listener;
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.contentTextView.setText(review.getContent());
        holder.authorTextView.setText(review.getAuthor());
        holder.reviewIcon.setTag(review.getUrl());
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reviews_item_layout, parent, false);
        return new ReviewViewHolder(view);
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView   reviewIcon;
        TextView    authorTextView;
        TextView    contentTextView;

        ReviewViewHolder(View itemView) {
            super(itemView);
            authorTextView = itemView.findViewById(R.id.tv_review_author);
            contentTextView = itemView.findViewById(R.id.tv_review_content);
            reviewIcon = itemView.findViewById(R.id.iv_review);
            reviewIcon.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemClickListener.itemClicked(getAdapterPosition(), v);
        }
    }

}
