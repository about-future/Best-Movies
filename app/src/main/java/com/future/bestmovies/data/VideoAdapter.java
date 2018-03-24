package com.future.bestmovies.data;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {
    private final Context mContext;
    private ArrayList<Video> mVideos;
    private final ListItemClickListener mOnclickListener;

    public interface ListItemClickListener {
        void onListItemClick(Video videoClicked);
    }

    public VideoAdapter(Context context, ArrayList<Video> videos, ListItemClickListener listener) {
        mContext = context;
        mVideos = videos;
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
    public void onBindViewHolder(@NonNull VideoAdapter.VideoViewHolder holder, int position) {
        Picasso.with(mContext)
                .load(ImageUtils.buildVideoThumbnailUrl(mVideos.get(position).getVideoKey()))
                .into(holder.videoThumbnailImageView);
        Log.v ("videoThumbnail " + position, ImageUtils.buildVideoThumbnailUrl(mVideos.get(position).getVideoKey()));

        holder.videoNameTextView.setText(mVideos.get(position).getVideoName());
        Log.v ("videoName ", mVideos.get(position).getVideoName());
        holder.videoTypeTextView.setText(mVideos.get(position).getVideoType());
        Log.v ("videoType ", mVideos.get(position).getVideoType());
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
        final ImageView videoThumbnailImageView;
        final TextView videoNameTextView;
        final TextView videoTypeTextView;

        VideoViewHolder(View itemView) {
            super(itemView);
            videoThumbnailImageView = itemView.findViewById(R.id.video_thumbnail_iv);
            videoNameTextView = itemView.findViewById(R.id.video_name_tv);
            videoTypeTextView = itemView.findViewById(R.id.video_type_tv);
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
