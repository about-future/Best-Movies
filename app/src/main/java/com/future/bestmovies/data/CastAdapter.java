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
import com.future.bestmovies.utils.ImageUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class CastAdapter extends RecyclerView.Adapter<CastAdapter.CastViewHolder> {
    private final Context mContext;
    private ArrayList<Cast> mCast;

    //No click listener needed yet

    public CastAdapter(Context context, ArrayList<Cast> cast) {
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

        if (!mCast.get(position).getProfilePath().equals("null")) {
            Picasso.with(mContext)
                    .load(ImageUtils.buildImageUrl(
                            mContext,
                            mCast.get(position).getProfilePath(),
                            ImageUtils.CAST))
                    .into(holder.actorProfileImageView);
            description = mCast.get(position).getActorName();
        }
        else {
            holder.actorProfileImageView.setImageResource(R.drawable.ic_person);
            holder.actorProfileImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            description = mContext.getString(R.string.no_profile_picture);
        }

        holder.actorProfileImageView.setContentDescription(description);
        holder.actorNameTextView.setText(mCast.get(position).getActorName());
    }

    @Override
    public int getItemCount() { return mCast.size(); }

    public void swapCast(ArrayList<Cast> newCast) {
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
