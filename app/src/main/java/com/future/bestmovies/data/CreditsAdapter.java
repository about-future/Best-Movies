package com.future.bestmovies.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.future.bestmovies.R;
import com.future.bestmovies.utils.ImageUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

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
    public void onBindViewHolder(@NonNull CreditsViewHolder holder, int position) {
        Picasso.with(mContext)
                .load(ImageUtils.buildImageUrl(
                        mContext,
                        mCredits.get(position).getPosterPath(),
                        ImageUtils.POSTER))
                .error(R.drawable.no_poster)
                .into(holder.creditPosterImageView);

        holder.creditTitleTextView.setText(mCredits.get(position).getTitle());

        String releaseYear;
        if (mCredits.get(position).getReleaseDate().length() > 4 && !TextUtils.equals(
                mCredits.get(position).getReleaseDate(),
                mContext.getText(R.string.credit_date_unknown))) {
            releaseYear = mCredits.get(position).getReleaseDate().substring(0, 4);
        } else {
            releaseYear = mContext.getText(R.string.credit_date_unknown).toString();
        }
        holder.creditReleaseDateTextView.setText(TextUtils.concat("(", releaseYear, ")"));

        if (!mCredits.get(position).getCharacter().isEmpty())
            holder.creditCharacterTextView.setText(TextUtils.concat(
                    mContext.getText(R.string.credit_as_character).toString(),
                    mCredits.get(position).getCharacter()));
    }

    @Override
    public int getItemCount() {
        return mCredits.size();
    }

    public void swapCredits(ArrayList<Credits> newCredits) {
        mCredits = newCredits;
        notifyDataSetChanged();
    }

    class CreditsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView creditPosterImageView;
        final TextView creditTitleTextView;
        final TextView creditReleaseDateTextView;
        final TextView creditCharacterTextView;

        CreditsViewHolder(View itemView) {
            super(itemView);
            creditPosterImageView = itemView.findViewById(R.id.credit_poster_iv);
            creditTitleTextView = itemView.findViewById(R.id.credit_title_tv);
            creditReleaseDateTextView = itemView.findViewById(R.id.credit_release_date_tv);
            creditCharacterTextView = itemView.findViewById(R.id.credit_character_tv);
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
