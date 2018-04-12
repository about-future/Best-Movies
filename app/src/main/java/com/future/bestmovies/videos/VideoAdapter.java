package com.future.bestmovies.videos;


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
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {
    private final Context mContext;
    private ArrayList<Video> mVideos = new ArrayList<Video>(){};
    private final ListItemClickListener mOnclickListener;

    public interface ListItemClickListener {
        void onListItemClick(Video videoClicked);
    }

    public VideoAdapter(Context context, ListItemClickListener listener) {
        mContext = context;
        mOnclickListener = listener;
    }

    @Override
    @NonNull
    public VideoAdapter.VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.video_list_item, parent, false);
        view.setFocusable(true);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final VideoAdapter.VideoViewHolder holder, final int position) {
        //final int currentPosition = position;
        // Try loading image from device memory or cache
        Picasso.get()
                .load(ImageUtils.buildVideoThumbnailUrl(
                        mContext,
                        mVideos.get(position).getVideoKey()))
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(holder.videoThumbnailImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        // Yay again!
                    }

                    @Override
                    public void onError(Exception e) {
                        // Try again online, if cache loading failed
                        Picasso.get()
                                .load(ImageUtils.buildVideoThumbnailUrl(
                                        mContext,
                                        mVideos.get(position).getVideoKey()))
                                .error(R.drawable.ic_image)
                                .into(holder.videoThumbnailImageView);
                    }
                });

        holder.videoNameTextView.setText(mVideos.get(position).getVideoName());
        holder.videoTypeTextView.setText(mVideos.get(position).getVideoType());
    }

    @Override
    public int getItemCount() {
        return mVideos.size();
    }

    // This method swaps the old movie result with the newly loaded ones and notify the change
    public void swapVideos(ArrayList<Video> newVideos) {
        mVideos = newVideos;
        notifyDataSetChanged();
    }

    class VideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.video_thumbnail_iv) ImageView videoThumbnailImageView;
        @BindView(R.id.video_name_tv) TextView videoNameTextView;
        @BindView(R.id.video_type_tv) TextView videoTypeTextView;

        VideoViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // Find the position of the video that was clicked and pass the video object from that
            // position to the listener
            int clickedPosition = getAdapterPosition();
            mOnclickListener.onListItemClick(mVideos.get(clickedPosition));
        }
    }
}