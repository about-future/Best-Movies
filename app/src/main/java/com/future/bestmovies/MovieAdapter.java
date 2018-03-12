package com.future.bestmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.future.bestmovies.data.Movie;
import com.future.bestmovies.utils.ImageUtils;
import com.squareup.picasso.Picasso;


public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private final Context mContext;
    private Movie[] mMovies;
    private final GridItemClickListener mOnclickListener;

    public interface GridItemClickListener {
        void onGridItemClick(Movie movieClicked);
    }

    public MovieAdapter(Context context, Movie[] movies, GridItemClickListener listener) {
        mContext = context;
        mMovies = movies;
        mOnclickListener = listener;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.movie_list_item, parent, false);
        view.setFocusable(true);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        String description;

        if (!mMovies[position].getPosterPath().equals("null")) {
            Picasso.with(mContext)
                    .load(ImageUtils.buildImageUrlForRecyclerView(
                            mContext,
                            mMovies[position].getPosterPath()))
                    .into(holder.moviePosterImageView);
            description = mMovies[position].
                    getMovieTitle().
                    concat(mContext.getString(R.string.poster));

        } else {
            holder.moviePosterImageView.setImageResource(R.drawable.ic_local_movies);
            holder.moviePosterImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            description = mContext.getString(R.string.no_poster);
        }

        holder.moviePosterImageView.setContentDescription(description);
    }

    @Override
    public int getItemCount() {
        return mMovies.length;
    }

    // This method swaps the old movie result with the newly loaded ones
    void swapMovies(Movie[] newMovies) {
        mMovies = newMovies;
        notifyDataSetChanged();
    }

    // Merge the existing movie with the new Movies, creating a new Movie array
    void mergeMovies(Movie[] newMovies) {
        // To generate a merged Array with the correct length, we have to add the existing
        // length array to the new one.
        Movie[] mergedMovies = new Movie[mMovies.length + newMovies.length];

        // To provide content for our merged array, we copy the entire content of our existing array
        // into the mergedMovies array and than add the content of the newMovies array to the
        // mergedArray.
        System.arraycopy(mMovies, 0, mergedMovies, 0, mMovies.length);
        System.arraycopy(newMovies, 0, mergedMovies, mMovies.length, newMovies.length);

        // After that, we simply swap the arrays and notify the adapter about the change
        swapMovies(mergedMovies);
    }

    class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView moviePosterImageView;

        MovieViewHolder(View itemView) {
            super(itemView);
            moviePosterImageView = itemView.findViewById(R.id.movie_poster_iv);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // Find the position of the movie that was clicked and pass the movie object from that
            // position to the listener
            int clickedPosition = getAdapterPosition();
            mOnclickListener.onGridItemClick(mMovies[clickedPosition]);
        }
    }
}