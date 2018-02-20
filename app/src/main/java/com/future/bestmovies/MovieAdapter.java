package com.future.bestmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.future.bestmovies.data.Movie;
import com.squareup.picasso.Picasso;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private final Context mContext;
    private Movie[] mMovies;
    private final GridItemClickListener mOnclickListener;

    public interface GridItemClickListener {
        void onGridItemClick(int clickedItemIndex);
    }

    public MovieAdapter(Context context, Movie[] movies, GridItemClickListener listener) {
        mContext = context;
        mMovies = movies;
        mOnclickListener = listener;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Explicit version
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.movie_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        view.setFocusable(true);

        //Short version
        // View view = LayoutInflater.from(mContext).inflate(R.layout.movie_list_item, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        //holder.bind(position);

        String baseUrl = "http://image.tmdb.org/t/p/w500";
        String imageUrl = mMovies[position].getPosterPath();

        String posterUrl = baseUrl.concat(imageUrl);
        Picasso.with(mContext)
                .load(posterUrl)
                .into(holder.moviePosterImageView);
    }

    @Override
    public int getItemCount() {
        return mMovies.length;
    }

    class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView moviePosterImageView;
        TextView viewHolderIndex;
        TextView listItemNumberView;

        public MovieViewHolder(View itemView) {
            super(itemView);
            moviePosterImageView = (ImageView) itemView.findViewById(R.id.movie_poster_iv);
            itemView.setOnClickListener(this);
        }

        void bind(int listIndex) {
            //listItemNumberView.setText(String.valueOf(listIndex));
            //listItemNumberView.setText(mMovieTitles[listIndex]);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            // Find the id of the movie, based on the clicked position and pass it to the listener
            //mCursor.moveToPosition(adapterPosition);
            //long movieID = mCursor.getLong(MainActivity.INDEX_MOVIE_ID);
            //mOnclickListener.onGridItemClick(movieID);
            mOnclickListener.onGridItemClick(clickedPosition);
        }
    }
}