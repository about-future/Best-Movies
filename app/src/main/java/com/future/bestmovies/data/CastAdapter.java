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
import com.squareup.picasso.Callback;
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
    public void onBindViewHolder(@NonNull final CastAdapter.CastViewHolder holder, int position) {
        final int pos = position;
        Picasso.with(mContext)
                .load(ImageUtils.buildImageUrl(
                        mContext,
                        mCast.get(position).getProfilePath(),
                        ImageUtils.CAST))
                .error(R.drawable.ic_person)
                .into(holder.actorProfileImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        // TODO: Error here on click and fast return to main activity
                        holder.actorProfileImageView.setContentDescription(
                                mCast.get(pos).getActorName());
                    }

                    @Override
                    public void onError() {
                        holder.actorProfileImageView.setContentDescription(
                                mContext.getString(R.string.no_profile_picture));
                    }
                });

        holder.actorNameTextView.setText(mCast.get(position).getActorName());
        //holder.characterTextView.setText(mCast.get(position).getCharacter());
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
        //final TextView characterTextView;

        CastViewHolder(View itemView) {
            super(itemView);
            actorProfileImageView = itemView.findViewById(R.id.actor_profile_iv);
            actorNameTextView = itemView.findViewById(R.id.actor_name_tv);
            //characterTextView = itemView.findViewById(R.id.character_tv);
        }
    }
}
