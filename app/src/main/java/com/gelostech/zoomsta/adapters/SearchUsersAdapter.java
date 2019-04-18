package com.gelostech.zoomsta.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.gelostech.zoomsta.R;
import com.gelostech.zoomsta.activities.SearchActivity;
import com.gelostech.zoomsta.commoners.DatabaseHelper;
import com.gelostech.zoomsta.commoners.OverviewDialog;
import com.gelostech.zoomsta.commoners.ZoomstaUtil;
import com.gelostech.zoomsta.fragments.StoriesOverview;
import com.gelostech.zoomsta.models.UserObject;

import java.util.List;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by tirgei on 10/31/17.
 */

public class SearchUsersAdapter extends RecyclerView.Adapter<SearchUsersAdapter.SearchUsersHolder> {
    private static final String TAG = "SearchUsersAdapter";
    private Activity context;
    private List<UserObject> userObjects;
    private OverviewDialog overviewDialog;
    private FragmentManager fm;
    private int count;
    private DatabaseHelper databaseHelper;
    private int prof;

    public SearchUsersAdapter(Activity context, List<UserObject> list){
        this.userObjects = list;
        this.context = context;

    }

    @Override
    public SearchUsersHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.story_list_object, parent, false);
        databaseHelper = new DatabaseHelper(context);
        return new SearchUsersHolder(view);
    }

    @Override
    public void onBindViewHolder(final SearchUsersHolder holder, final int position) {
        //holder.setIsRecyclable(false);
        final UserObject object = userObjects.get(position);
        fm = ((SearchActivity) context).getSupportFragmentManager();
        final Bitmap[] bitmap = new Bitmap[1];

        Log.d("searched", "Version 2:" + object.getUserName());

        holder.storyObject.setVisibility(View.VISIBLE);

        holder.realName.setText(object.getRealName());
        holder.userName.setText(object.getUserName());
        Glide.with(context).load(object.getImage()).thumbnail(0.1f).into(holder.userIcon);
        Glide.with(context).asBitmap().load(object.getImage()).thumbnail(0.1f).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                bitmap[0] = resource;
                //holder.userIcon.setImageBitmap(resource);
            }
        });

        holder.userIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseHelper.addHistory(object.getUserId(), object.getUserName(), object.getRealName(), bitmap[0]);

                if(object.getUserId().equals(ZoomstaUtil.getStringPreference(context, "userid"))){
                    overviewDialog = new OverviewDialog(context, bitmap[0], object.getUserName(), true);
                    overviewDialog.show();
                } else {
                    overviewDialog = new OverviewDialog(context, object);
                    overviewDialog.show();
                }

                prof = ZoomstaUtil.getIntegerPreference(context, "profCount");

                if(context instanceof SearchActivity){
                    ((SearchActivity)context).fetchHistory();
                }
            }
        });

        holder.storyObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bitmap[0]!=null)
                    databaseHelper.addHistory(object.getUserId(), object.getUserName(), object.getRealName(), bitmap[0]);

                count = ZoomstaUtil.getIntegerPreference(context, "clickCount");

                StoriesOverview overview = new StoriesOverview();
                Bundle args = new Bundle();
                args.putString("username", object.getUserName());
                args.putString("user_id", object.getUserId());
                overview.setArguments(args);
                overview.show(fm, "Story Overview");

                if(context instanceof SearchActivity){
                    ((SearchActivity)context).fetchHistory();
                }

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

    public class SearchUsersHolder extends RecyclerView.ViewHolder{
        private CircleImageView userIcon;
        private TextView userName;
        private TextView realName;
        private RelativeLayout storyObject;

        public SearchUsersHolder(View view){
            super(view);

            userIcon = view.findViewById(R.id.story_icon);
            userName = view.findViewById(R.id.user_name);
            realName = view.findViewById(R.id.real_name);
            storyObject = view.findViewById(R.id.story_object);

        }
    }

}
