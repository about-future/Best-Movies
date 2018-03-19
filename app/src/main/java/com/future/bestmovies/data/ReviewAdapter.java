package com.future.bestmovies.data;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.future.bestmovies.R;

import java.util.ArrayList;


public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>{
    private final Context mContext;
    private ArrayList<Review> mReviews;

    public ReviewAdapter(Context context, ArrayList<Review> reviews) {
        mContext = context;
        mReviews = reviews;
    }

    @NonNull
    @Override
    public ReviewAdapter.ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.review_list_item, parent, false);
        view.setFocusable(false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewAdapter.ReviewViewHolder holder, int position) {
        holder.reviewAuthorTextView.setText(mReviews.get(position).getReviewAuthor());
        holder.reviewContentTextView.setText(mReviews.get(position).getReviewContent());
    }

    @Override
    public int getItemCount() {
        return mReviews.size();
    }

    public void swapReviews(ArrayList<Review> newReviews) {
        mReviews = newReviews;
        notifyDataSetChanged();
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {
        final TextView reviewAuthorTextView;
        final TextView reviewContentTextView;

        ReviewViewHolder(View itemView) {
            super(itemView);
            reviewAuthorTextView = itemView.findViewById(R.id.review_author_tv);
            reviewContentTextView = itemView.findViewById(R.id.review_content_tv);
        }
    }
}
