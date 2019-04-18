package com.gelostech.zoomsta.adapters;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gelostech.zoomsta.R;
import com.gelostech.zoomsta.activities.MainActivity;
import com.gelostech.zoomsta.commoners.OverviewDialog;
import com.gelostech.zoomsta.commoners.ZoomstaUtil;
import com.gelostech.zoomsta.fragments.StoriesOverview;
import com.gelostech.zoomsta.models.UserObject;

import java.util.List;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by tirgei on 11/1/17.
 */

public class FaveListAdapter extends RecyclerView.Adapter<FaveListAdapter.FaveListHolder> {

    private static final String TAG = "FaveListAdapter";
    private Activity context;
    private List<UserObject> userObjects;
    private OverviewDialog overviewDialog;
    private FragmentManager fm;
    private int count, prof;

    public FaveListAdapter(Activity context, List<UserObject> list){
        this.userObjects = list;
        this.context = context;
    }

    @Override
    public FaveListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fave_user_item, parent, false);
        return new FaveListHolder(view);
    }

    @Override
    public void onBindViewHolder(FaveListHolder holder, int position) {
        final UserObject object = userObjects.get(position);
        fm = ((MainActivity) context).getSupportFragmentManager();

        holder.storyObject.setVisibility(View.VISIBLE);

        holder.realName.setText(object.getRealName());
        holder.userName.setText(object.getUserName());
        Glide.with(context).load(object.getImage()).thumbnail(0.2f).into(holder.userIcon);

        holder.userIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overviewDialog = new OverviewDialog(context, object);
                overviewDialog.show();

                prof = ZoomstaUtil.getIntegerPreference(context, "profCount");
            }
        });

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
    public int getItemCount() {
        return userObjects.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public class FaveListHolder extends RecyclerView.ViewHolder{
        private CircleImageView userIcon;
        private TextView userName;
        private TextView realName;
        private RelativeLayout storyObject;

        public FaveListHolder(View view){
            super(view);

            userIcon = view.findViewById(R.id.fave_story_icon);
            userName = view.findViewById(R.id.fave_user_name);
            realName = view.findViewById(R.id.fave_real_name);
            storyObject = view.findViewById(R.id.fave_user_object);

        }
    }

}