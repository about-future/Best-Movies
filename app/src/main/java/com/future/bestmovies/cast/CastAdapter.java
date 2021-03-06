package com.future.bestmovies.cast;


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


public class CastAdapter extends RecyclerView.Adapter<CastAdapter.CastViewHolder> {
    private final Context mContext;
    private ArrayList<Cast> mCast = new ArrayList<Cast>() {
    };
    private final ListItemClickListener mOnClickListener;

    public interface ListItemClickListener {
        void onListItemClick(Cast castClicked);
    }

    public CastAdapter(Context context, ListItemClickListener listener) {
        mContext = context;
        mOnClickListener = listener;
    }

    @Override
    @NonNull
    public CastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.cast_list_item, parent, false);
        view.setFocusable(false);
        return new CastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CastAdapter.CastViewHolder holder, int position) {
        String castImagePath = mCast.get(position).getProfilePath();

        final String castImageUrl;
        // If we have a valid image path, try loading it from cache or from web with Picasso
        if (!TextUtils.isEmpty(castImagePath)) {
            castImageUrl = ImageUtils.buildImageUrlForCast(castImagePath);

            // Try loading image from device memory or cache
            Picasso.get()
                    .load(castImageUrl)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(holder.actorProfileImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            // Yay!
                        }

                        @Override
                        public void onError(Exception e) {
                            // Try again online, if cache loading failed
                            Picasso.get()
                                    .load(castImageUrl)
                                    .error(R.drawable.ic_person)
                                    .into(holder.actorProfileImageView);
                        }
                    });
        } else {
            // Otherwise, don't bother using Picasso and set no_poster for actorProfileImageView
            holder.actorProfileImageView.setImageResource(R.drawable.ic_person);
        }

        holder.actorNameTextView.setText(mCast.get(position).getActorName());
        holder.characterTextView.setText(mCast.get(position).getCharacter());
    }

    @Override
    public int getItemCount() {
        if (mCast != null)
            return mCast.size();
        else
            return 0;
    }

    public void swapCast(ArrayList<Cast> newCast) {
        mCast = newCast;
        notifyDataSetChanged();
    }

    class CastViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.actor_profile_iv)
        ImageView actorProfileImageView;
        @BindView(R.id.actor_name_tv)
        TextView actorNameTextView;
        @BindView(R.id.character_tv)
        TextView characterTextView;

        CastViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // Find the position of the cast that was clicked and pass the cast object from that
            // position to the listener
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(mCast.get(clickedPosition));
        }
    }
}
