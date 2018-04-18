package com.future.bestmovies.movie;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.future.bestmovies.data.FavouritesContract.*;

public class FavouriteActorsAdapter extends RecyclerView.Adapter<FavouriteActorsAdapter.ActorViewHolder> {

    private final Context mContext;
    private Cursor mActorsCursor;
    private final GridItemClickListener mOnClickListener;

    public interface GridItemClickListener {
        void onActorItemClick(int actorId);
    }

    public FavouriteActorsAdapter(Context context, GridItemClickListener listener) {
        mContext = context;
        mOnClickListener = listener;
    }

    @Override
    @NonNull
    public ActorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.favourite_actor_list_item, parent, false);
        view.setFocusable(true);
        return new ActorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ActorViewHolder holder, int position) {
        mActorsCursor.moveToPosition(position);

        int nameColumnIndex = mActorsCursor.getColumnIndex(ActorsEntry.COLUMN_NAME);
        int pictureColumnIndex = mActorsCursor.getColumnIndex(ActorsEntry.COLUMN_PROFILE_PATH);

        holder.actorNameTextView.setText(mActorsCursor.getString(nameColumnIndex));
        final String profilePath = mActorsCursor.getString(pictureColumnIndex);

        // Try loading image from device memory or cache
        Picasso.get()
                .load(ImageUtils.buildImageUrlForRecyclerView(
                        mContext,
                        profilePath))
                //.placeholder(R.drawable.no_picture)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(holder.actorProfileImageView, new Callback() {
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
                                        profilePath))
                                //.placeholder(R.drawable.no_picture)
                                .error(R.drawable.no_picture)
                                .into(holder.actorProfileImageView);
                    }
                });
    }

    @Override
    public int getItemCount() {
        if (mActorsCursor != null)
            return mActorsCursor.getCount();
        else
            return 0;
    }

    public void swapActors(Cursor newActors) {
        mActorsCursor = newActors;
        notifyDataSetChanged();
    }

    class ActorViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.profile_picture_iv)
        ImageView actorProfileImageView;
        @BindView(R.id.person_name_tv)
        TextView actorNameTextView;

        ActorViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // Find the position of the movie that was clicked and pass the movie object from that
            // position to the listener or the movieId
            int adapterPosition = getAdapterPosition();
            mActorsCursor.moveToPosition(adapterPosition);

            int resultId = mActorsCursor.getInt(mActorsCursor.getColumnIndex(ActorsEntry.COLUMN_ACTOR_ID));
            mOnClickListener.onActorItemClick(resultId);
        }
    }
}