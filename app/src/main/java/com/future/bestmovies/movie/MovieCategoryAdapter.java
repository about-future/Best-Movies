package com.future.bestmovies.movie;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.future.bestmovies.R;
import com.future.bestmovies.data.FavouritesContract;
import com.future.bestmovies.data.FavouritesContract.MovieDetailsEntry;
import com.future.bestmovies.utils.ImageUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.future.bestmovies.data.FavouritesContract.*;


public class MovieCategoryAdapter extends RecyclerView.Adapter<MovieCategoryAdapter.MovieViewHolder> {

    private final Context mContext;
    private ArrayList<Movie> mMovies = new ArrayList<Movie>(){};
    private Cursor mMoviesCursor;
    private final GridItemClickListener mOnClickListener;

    public interface GridItemClickListener {
        void onGridItemClick(Movie movieClicked, int movieId);
    }

    public MovieCategoryAdapter(Context context, GridItemClickListener listener) {
        mContext = context;
        mOnClickListener = listener;
    }

    @Override @NonNull
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.movie_list_item, parent, false);
        view.setFocusable(true);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MovieViewHolder holder, int position) {
        String posterPath;

        if (mMovies != null && mMoviesCursor == null) {
            posterPath = mMovies.get(position).getPosterPath();
        } else {
            mMoviesCursor.moveToPosition(position);

            int posterColumnIndex;

            try {
                posterColumnIndex = mMoviesCursor.getColumnIndex(MovieDetailsEntry.COLUMN_POSTER_PATH);
                posterPath = mMoviesCursor.getString(posterColumnIndex);
            } catch (IllegalStateException e) {
                Log.v("NO MOVIE POSTER", e.toString());
                try {
                    posterColumnIndex = mMoviesCursor.getColumnIndex(ActorsEntry.COLUMN_PROFILE_PATH);
                    posterPath = mMoviesCursor.getString(posterColumnIndex);
                } catch (IllegalStateException f) {
                    Log.v("NO ACTOR POSTER", f.toString());
                    posterColumnIndex = 0;
                    posterPath = "ups";
                }
            }
        }

        // Try loading image from device memory or cache
        final String finalPosterPath = posterPath;
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
                                        finalPosterPath))
                                .error(R.drawable.no_poster)
                                .into(holder.moviePosterImageView);
                    }
                });
    }

    @Override
    public int getItemCount() {
        if (mMovies != null && mMoviesCursor == null)
            return mMovies.size();
        else if (mMoviesCursor != null)
            return mMoviesCursor.getCount();
        else return 0;
    }

    // This method swaps the old movie result with the newly loaded ones and notify the change
    public void swapMovies(ArrayList<Movie> newMovies) {
        mMovies = newMovies;
        notifyDataSetChanged();
    }

    // Add to the existing movie list the new movies and notify the change
    public void addMovies(ArrayList<Movie> newMovies) {
        mMovies.addAll(newMovies);
        notifyDataSetChanged();
    }

    public void swapCursor(Cursor newCursor) {
        mMoviesCursor = newCursor;
        notifyDataSetChanged();
    }

    class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.movie_poster_iv) ImageView moviePosterImageView;

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
            if (mMovies != null && mMoviesCursor == null)
                mOnClickListener.onGridItemClick(mMovies.get(adapterPosition), 0);
            else {
                mMoviesCursor.moveToPosition(adapterPosition);
                int resultId;
                try {
                    resultId = mMoviesCursor.getInt(mMoviesCursor.getColumnIndex(MovieDetailsEntry.COLUMN_MOVIE_ID));
                } catch (IllegalStateException e) {
                    Log.v("NO MOVIE ID", e.toString());
                    try {
                        resultId = mMoviesCursor.getInt(mMoviesCursor.getColumnIndex(ActorsEntry.COLUMN_ACTOR_ID));
                    } catch (IllegalStateException f) {
                        Log.v("NO ACTOR ID", f.toString());
                        resultId = 0;
                    }
                }
                mOnClickListener.onGridItemClick(null, resultId);
            }
        }
    }
}