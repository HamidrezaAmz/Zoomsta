package com.gelostech.zoomsta.adapters;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gelostech.zoomsta.R;
import com.gelostech.zoomsta.activities.SearchActivity;
import com.gelostech.zoomsta.commoners.OverviewDialog;
import com.gelostech.zoomsta.commoners.ZoomstaUtil;
import com.gelostech.zoomsta.fragments.StoriesOverview;
import com.gelostech.zoomsta.models.HistoryModel;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by tirgei on 10/29/17.
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<HistoryModel> modelList = new ArrayList<>();
    private Activity context;
    private OverviewDialog overviewDialog;
    private FragmentManager fm;
    private int count;

    public HistoryAdapter(Activity context, List<HistoryModel> models) {
        this.modelList = models;
        this.context = context;
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        fm = ((SearchActivity) context).getSupportFragmentManager();
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HistoryAdapter.HistoryViewHolder holder, int position) {
        //holder.setIsRecyclable(false);
        final HistoryModel object = modelList.get(position);

        holder.storyObject.setVisibility(View.VISIBLE);

        holder.userName.setText(object.getUserName());
        holder.userIcon.setImageBitmap(ZoomstaUtil.getImage(object.getUserPic()));

        holder.storyObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count = ZoomstaUtil.getIntegerPreference(context, "clickCount");
                StoriesOverview overview = new StoriesOverview();
                Bundle args = new Bundle();
                args.putString("username", object.getUserName());
                args.putString("user_id", object.getUserId());
                overview.setArguments(args);
                overview.show(fm, "Story Overview");


            }
        });

    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    public class HistoryViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView userIcon;
        private TextView userName;
        private LinearLayout storyObject;

        public HistoryViewHolder(View view) {
            super(view);

            userIcon = view.findViewById(R.id.image_thumb);
            userName = view.findViewById(R.id.prof_name);
            storyObject = view.findViewById(R.id.history_image);

        }
    }
}
