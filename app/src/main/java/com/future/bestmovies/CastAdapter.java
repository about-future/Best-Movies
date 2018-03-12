package com.future.bestmovies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.future.bestmovies.data.Cast;
import com.future.bestmovies.utils.ImageUtils;
import com.squareup.picasso.Picasso;


public class CastAdapter extends RecyclerView.Adapter<CastAdapter.CastViewHolder> {
    private final Context mContext;
    private Cast[] mCast;

    //No click listener needed yet

    public CastAdapter(Context context, Cast[] cast) {
        mContext = context;
        mCast = cast;
    }

    @Override @NonNull
    public CastAdapter.CastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.cast_list_item, parent, false);
        view.setFocusable(false);
        return new CastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CastAdapter.CastViewHolder holder, int position) {
        String description;

        if (!mCast[position].getProfilePath().equals("null")) {
            Picasso.with(mContext)
                    .load(ImageUtils.buildImageUrl(
                            mContext,
                            mCast[position].getProfilePath(),
                            ImageUtils.CAST))
                    .into(holder.actorProfileImageView);
            description = mCast[position].getActorName();
        }
        else {
            holder.actorProfileImageView.setImageResource(R.drawable.ic_person);
            holder.actorProfileImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            description = mContext.getString(R.string.no_profile_picture);
        }

        holder.actorProfileImageView.setContentDescription(description);
        holder.actorNameTextView.setText(mCast[position].getActorName());
    }

    @Override
    public int getItemCount() { return mCast.length; }

    void swapCast(Cast[] newCast) {
        mCast = newCast;
        notifyDataSetChanged();
    }

    class CastViewHolder extends RecyclerView.ViewHolder {
        final ImageView actorProfileImageView;
        final TextView actorNameTextView;

        CastViewHolder(View itemView) {
            super(itemView);
            actorProfileImageView = itemView.findViewById(R.id.actor_profile_iv);
            actorNameTextView = itemView.findViewById(R.id.actor_name_tv);
        }
    }
}
