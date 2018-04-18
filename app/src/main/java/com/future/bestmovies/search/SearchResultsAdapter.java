package com.future.bestmovies.search;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.future.bestmovies.R;
import com.future.bestmovies.utils.ImageUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ResultsViewHolder> {
    private final Context mContext;
    private ArrayList<SearchResult> mResults = new ArrayList<SearchResult>() {
    };
    private final ListItemClickListener mOnClickListener;

    public interface ListItemClickListener {
        void onListItemClick(SearchResult resultClicked);
    }

    public SearchResultsAdapter(Context context, ListItemClickListener listener) {
        mContext = context;
        mOnClickListener = listener;
    }

    @NonNull
    @Override
    public SearchResultsAdapter.ResultsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.search_list_item, parent, false);
        view.setFocusable(false);
        return new ResultsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SearchResultsAdapter.ResultsViewHolder holder, int position) {
        String imagePath;

        switch (mResults.get(position).getType()) {
            case "movie":
                imagePath = mResults.get(position).getPosterPath();
                holder.resultTitleTextView.setText(mResults.get(position).getMovieTitle());
                holder.resultTypeTextView.setText(mContext.getString(R.string.search_result_movie));
                break;
            case "person":
                imagePath = mResults.get(position).getProfilePath();
                holder.resultTitleTextView.setText(mResults.get(position).getPersonName());
                holder.resultTypeTextView.setText(mContext.getString(R.string.search_result_person));
                break;
            default:
                imagePath = mResults.get(position).getPosterPath();
                holder.resultTitleTextView.setText(mResults.get(position).getShowTitle());
                holder.resultTypeTextView.setText(mContext.getString(R.string.search_result_tv_show));
        }

        final String imageUrl;
        // If we have a valid image path, try loading it from cache or from web with Picasso
        if (!TextUtils.isEmpty(imagePath)) {
            imageUrl = ImageUtils.buildImageUrlForSearchResults(imagePath);

            // Try loading image from device memory or cache
            Picasso.get()
                    .load(imageUrl)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.no_poster)
                    .into(holder.resultPosterImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            // Yay!
                        }

                        @Override
                        public void onError(Exception e) {
                            // Try again online, if cache loading failed
                            Picasso.get()
                                    .load(imageUrl)
                                    .placeholder(R.drawable.no_poster)
                                    .error(R.drawable.no_poster)
                                    .into(holder.resultPosterImageView);
                        }
                    });
        } else {
            // Otherwise, don't bother using Picasso and set no_poster for resultPosterImageView
            holder.resultPosterImageView.setImageResource(R.drawable.no_poster);
        }
    }

    @Override
    public int getItemCount() {
        if (mResults != null)
            return mResults.size();
        else
            return 0;
    }

    public void swapResults(ArrayList<SearchResult> newResult) {
        mResults = newResult;
        notifyDataSetChanged();
    }

    // Add to the existing results list the new results and notify the change
    public void addResults(ArrayList<SearchResult> newResults) {
        mResults.addAll(newResults);
        notifyDataSetChanged();
    }

    class ResultsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.search_result_iv)
        ImageView resultPosterImageView;
        @BindView(R.id.search_result_tv)
        TextView resultTitleTextView;
        @BindView(R.id.search_result_label)
        TextView resultTypeTextView;

        ResultsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(mResults.get(clickedPosition));
        }
    }
}
