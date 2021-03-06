package com.gelostech.zoomsta.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.gelostech.zoomsta.R;
import com.gelostech.zoomsta.activities.ViewStoryActivity;
import com.gelostech.zoomsta.commoners.SquareLayout;
import com.gelostech.zoomsta.commoners.ZoomstaUtil;
import com.gelostech.zoomsta.models.StoryModel;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by tirgei on 11/4/17.
 */

public class StoriesOverViewAdapter extends RecyclerView.Adapter<StoriesOverViewAdapter.StoriesOverViewHolder> {
    private List<String> modelList;
    private List<StoryModel> storyModels;
    private Context context;
    private int count;

    public StoriesOverViewAdapter(Context context, List<String> models, List<StoryModel> storyModels) {
        this.context = context;
        this.modelList = models;
        this.storyModels = storyModels;
    }

    @Override
    public StoriesOverViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stories_overview_object, parent, false);
        return new StoriesOverViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final StoriesOverViewHolder holder, final int position) {
        holder.setIsRecyclable(false);
        final String model = modelList.get(position);

        Glide.with(context).load(model).thumbnail(0.2f).into(holder.imageView);
        holder.layout.setVisibility(View.VISIBLE);
        if (!model.endsWith(".jpg"))
            holder.isVideo.setVisibility(View.VISIBLE);

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count = ZoomstaUtil.getIntegerPreference(context, "itemCount");
                Intent intent = new Intent(context, ViewStoryActivity.class);
                intent.putExtra("isFromNet", true);
                intent.putStringArrayListExtra("urls", (ArrayList<String>) modelList);
                intent.putExtra("pos", position);
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(R.anim.enter_main, R.anim.exit_splash);
            }
        });


    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public class StoriesOverViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private ImageView isVideo;
        private SquareLayout layout;

        public StoriesOverViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.overview_media_holder);
            isVideo = itemView.findViewById(R.id.overview_is_video);
            this.layout = itemView.findViewById(R.id.select_stories_overview_item);
        }
    }


}
