package com.future.bestmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.future.bestmovies.data.Movie;
import com.future.bestmovies.utils.ImageUtils;
import com.future.bestmovies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.net.URL;

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
                    .load(ImageUtils.buildImageUrlWithImageType(
                            mContext,
                            mMovies[position].getPosterPath(),
                            ImageUtils.POSTER))
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

    void swapMovies(Movie[] newMovies) {
        mMovies = newMovies;
        notifyDataSetChanged();
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