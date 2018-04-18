package com.future.bestmovies.movie;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.future.bestmovies.R;
import com.future.bestmovies.data.FavouritesContract.MovieDetailsEntry;
import com.future.bestmovies.utils.ImageUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;


public class FavouriteMoviesAdapter extends RecyclerView.Adapter<FavouriteMoviesAdapter.MovieViewHolder> {

    private final Context mContext;
    private Cursor mMoviesCursor;
    private final GridItemClickListener mOnClickListener;

    public interface GridItemClickListener {
        void onMovieItemClick(int movieId);
    }

    public FavouriteMoviesAdapter(Context context, GridItemClickListener listener) {
        mContext = context;
        mOnClickListener = listener;
    }

    @Override
    @NonNull
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.movie_list_item, parent, false);
        view.setFocusable(true);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MovieViewHolder holder, int position) {
        mMoviesCursor.moveToPosition(position);

        int posterColumnIndex = mMoviesCursor.getColumnIndex(MovieDetailsEntry.COLUMN_POSTER_PATH);
        final String posterPath = mMoviesCursor.getString(posterColumnIndex);

        // Try loading image from device memory or cache
        Picasso.get()
                .load(ImageUtils.buildImageUrlForRecyclerView(
                        mContext,
                        posterPath))
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(holder.moviePosterImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        // Celebrate: Yay!!!
                    }

                    @Override
                    public void onError(Exception e) {
                        // Try again online, if loading from device memory or cache failed
                        Picasso.get()
                                .load(ImageUtils.buildImageUrlForRecyclerView(
                                        mContext,
                                        posterPath))
                                .error(R.drawable.no_poster)
                                .into(holder.moviePosterImageView);
                    }
                });
    }

    @Override
    public int getItemCount() {
        if (mMoviesCursor != null)
            return mMoviesCursor.getCount();
        else
            return 0;
    }

    // This method swaps the old movie result with the newly loaded ones and notify the change
    public void swapMovies(Cursor newMovies) {
        mMoviesCursor = newMovies;
        notifyDataSetChanged();
    }

    class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.movie_poster_iv)
        ImageView moviePosterImageView;

        MovieViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // Find the position of the movie that was clicked and pass the movie object from that
            // position to the listener or the movieId
            int adapterPosition = getAdapterPosition();
            mMoviesCursor.moveToPosition(adapterPosition);

            int movieId = mMoviesCursor.getInt(mMoviesCursor.getColumnIndex(MovieDetailsEntry.COLUMN_MOVIE_ID));
            mOnClickListener.onMovieItemClick(movieId);
        }
    }
}
