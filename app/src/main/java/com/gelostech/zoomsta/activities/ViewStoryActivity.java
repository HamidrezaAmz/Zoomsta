package com.gelostech.zoomsta.activities;

import android.os.Bundle;
import android.util.Log;

import com.gelostech.zoomsta.R;
import com.gelostech.zoomsta.adapters.StoryViewPagerAdapter;
import com.gelostech.zoomsta.commoners.DataHolder;
import com.gelostech.zoomsta.commoners.StoryListener;
import com.gelostech.zoomsta.commoners.ZoomstaUtil;
import com.gelostech.zoomsta.models.StoryModel;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

public class ViewStoryActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, StoryListener {
    private ViewPager viewPager;
    private StoryViewPagerAdapter adapter;
    private ArrayList<StoryModel> modelList;
    private int position = 0, totalPages;
    private List<String> urls;
    private Boolean isFromNet;
    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_story);

        viewPager = findViewById(R.id.story_view_pager);
        modelList = new ArrayList<>();
        urls = new ArrayList<>();

        loadStories();
        adapter = new StoryViewPagerAdapter(getSupportFragmentManager(), modelList, isFromNet, urls);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(this);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setCurrentItem(position);


    }

    private void loadStories() {
        isFromNet = getIntent().getBooleanExtra("isFromNet", false);
        Boolean isLarge = getIntent().getBooleanExtra("isLarge", false);
        if (!isFromNet) {
            if (isLarge) {
                Log.d("data", "Receiving large data...");
                modelList = (ArrayList<StoryModel>) DataHolder.getData();

            } else {
                modelList = getIntent().getParcelableArrayListExtra("storylist");
                Log.d("data", "Data received is small");

            }
            position = getIntent().getIntExtra("pos", 0);
            totalPages = modelList.size();
        } else {
            urls = getIntent().getStringArrayListExtra("urls");
            position = getIntent().getIntExtra("pos", 0);
            totalPages = urls.size();
        }

        //adapter.notifyDataSetChanged();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        this.position = position;
        count++;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.enter_signin, R.anim.exit_main);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void deletePage(Boolean delete) {
        if (delete)
            adapter.deletePage(viewPager.getCurrentItem());

        if (viewPager.getChildCount() == 0) {
            ZoomstaUtil.setBooleanPreference(this, "refreshSaved", true);
            finish();
        }
    }
}
