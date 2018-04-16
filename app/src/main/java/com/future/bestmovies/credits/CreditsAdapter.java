package com.future.bestmovies.credits;

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
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreditsAdapter extends RecyclerView.Adapter<CreditsAdapter.CreditsViewHolder> {
    private final Context mContext;
    private ArrayList<Credits> mCredits = new ArrayList<Credits>() {
    };
    private final ListItemClickListener mOnClickListener;

    public interface ListItemClickListener {
        void onListItemClick(Credits creditsClicked);
    }

    public CreditsAdapter(Context context, ListItemClickListener listener) {
        mContext = context;
        mOnClickListener = listener;
    }

    @NonNull
    @Override
    public CreditsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.credits_list_item, parent, false);
        view.setFocusable(false);
        return new CreditsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CreditsViewHolder holder, int position) {
        final int currentPosition = position;

        // Try loading image from device memory or cache
        Picasso.get()
                .load(ImageUtils.buildImageUrl(
                        mContext,
                        mCredits.get(position).getPosterPath(),
                        ImageUtils.POSTER))
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(holder.creditPosterImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        // Yay!
                    }

                    @Override
                    public void onError(Exception e) {
                        // Try again online, if cache loading failed
                        Picasso.get()
                                .load(ImageUtils.buildImageUrl(
                                        mContext,
                                        mCredits.get(currentPosition).getPosterPath(),
                                        ImageUtils.POSTER))
                                .error(R.drawable.no_poster)
                                .into(holder.creditPosterImageView);
                    }
                });

        holder.creditTitleTextView.setText(mCredits.get(position).getTitle());

        String releaseYear;
        if (mCredits.get(position).getReleaseDate() != null && mCredits.get(position).getReleaseDate().length() > 0) {
            releaseYear = mCredits.get(position).getReleaseDate().substring(0, 4);
        } else {
            releaseYear = mContext.getText(R.string.credit_date_unknown).toString();
        }
        holder.creditReleaseDateTextView.setText(TextUtils.concat("(", releaseYear, ")"));

        if (mCredits.get(position).getCharacter() != null && mCredits.get(position).getCharacter().length() > 0)
            holder.creditCharacterTextView.setText(TextUtils.concat(
                    mContext.getText(R.string.credit_as_character).toString(),
                    mCredits.get(position).getCharacter()));
    }

    @Override
    public int getItemCount() {
        return mCredits.size();
    }

    public void swapCredits(ArrayList<Credits> newCredits) {
        Collections.sort(newCredits, new Comparator<Credits>() {
            @Override
            public int compare(Credits o1, Credits o2) {
                if (o2.getReleaseDate() == null && o1.getReleaseDate() == null) {
                    return 0;
                } else if (o2.getReleaseDate() != null && o1.getReleaseDate() == null) {
                    return 1;
                } else if (o2.getReleaseDate() == null && o1.getReleaseDate() != null) {
                    return -1;
                } else {
                    return o2.getReleaseDate().compareTo(o1.getReleaseDate());
                }
            }
        });

        mCredits = newCredits;
        notifyDataSetChanged();
    }

    class CreditsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.credit_poster_iv)
        ImageView creditPosterImageView;
        @BindView(R.id.credit_title_tv)
        TextView creditTitleTextView;
        @BindView(R.id.credit_release_date_tv)
        TextView creditReleaseDateTextView;
        @BindView(R.id.credit_character_tv)
        TextView creditCharacterTextView;

        CreditsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // Find the position of the credit(movie) that was clicked and pass the credit object from that
            // position to the listener
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(mCredits.get(clickedPosition));
        }
    }
}
