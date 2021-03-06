package com.gelostech.zoomsta.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.gelostech.zoomsta.R;
import com.gelostech.zoomsta.commoners.StoryListener;
import com.gelostech.zoomsta.commoners.InstaUtils;
import com.gelostech.zoomsta.commoners.ZoomstaUtil;
import com.gelostech.zoomsta.models.StoryModel;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.ybq.android.spinkit.SpinKitView;
import com.universalvideoview.UniversalMediaController;
import com.universalvideoview.UniversalVideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class StoryFragment extends Fragment {
    private PhotoView imageView;
    private ImageButton save, share, delete;
    private Bitmap bitmap;
    private RelativeLayout buttons;
    private StoryModel storyItem;
    private String url;
    private SpinKitView wave;
    private StoryListener storyListener;
    private FrameLayout video;
    private UniversalVideoView videoView;
    private UniversalMediaController mediaController;
    private ImageButton play;
    private static Boolean isFromNet;
    private String vidUrl;
    private File cachedFile;

    public StoryFragment() {
        // Required empty public constructor
    }

    public static StoryFragment newInstance(Boolean isFromNet){
        StoryFragment storyFragment = new StoryFragment();
        isFromNet(isFromNet);

        return storyFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof StoryListener)
            storyListener = (StoryListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_story, container, false);

        imageView = view.findViewById(R.id.story_imageview);
        save = view.findViewById(R.id.story_button_save);
        share = view.findViewById(R.id.story_button_share);
        delete = view.findViewById(R.id.story_button_delete);
        buttons = view.findViewById(R.id.story_button_options);
        wave = view.findViewById(R.id.loading_saved_item);
        video = view.findViewById(R.id.video_layout);
        videoView = view.findViewById(R.id.video_view);
        mediaController = view.findViewById(R.id.media_controller);
        videoView.setMediaController(mediaController);
        play = view.findViewById(R.id.story_video);

        setupVideo();
        setStory();

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(buttons.isShown())
                    buttons.setVisibility(View.GONE);
                else
                    buttons.setVisibility(View.VISIBLE);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(storyItem.getFilePath());
                onDeleteClick(file);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(url.endsWith(".jpg") && bitmap != null){
                    saveImage(bitmap);
                } else {
                    new saveVideo(vidUrl).execute(new String[0]);
                }
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFromNet){
                    if(storyItem.getType() == 0){
                        shareLocalImage(storyItem.getFilePath());
                    } else {
                        shareLocalVideo(storyItem.getFilePath());
                    }
                } else {
                    if(url.endsWith(".jpg")){
                        String bitmapPath = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), bitmap,"title", null);
                        Uri bitmapUri = Uri.parse(bitmapPath);

                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("image/png");
                        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
                        startActivity(Intent.createChooser(intent, "Share via..."));
                    } else {
                        new ShareVid().execute(new String[0]);
                    }
                }
            }
        });

        int count = ZoomstaUtil.getIntegerPreference(getActivity(), "itemCount");
        count++;
        if(count < 3376)
            ZoomstaUtil.setIntegerPreference(getActivity(), count, "itemCount");
        else
            ZoomstaUtil.setIntegerPreference(getActivity(), 1, "itemCount");

        return view;
    }

    public void setStoryList(StoryModel story){
        this.storyItem = story;
    }

    public void getStories(String url){
        this.url = url;
    }

    private void setStory(){

        if(!isFromNet){
            if(storyItem.getSaved()){

                Glide.with(getActivity()).load(storyItem.getFilePath()).thumbnail(0.5f).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if(wave.isShown())
                            wave.setVisibility(View.GONE);

                        return false;
                    }
                }).into(imageView);

                if(storyItem.getType() == 1) {
                    video.setVisibility(View.VISIBLE);
                    play.setVisibility(View.VISIBLE);
                    videoView.setVideoURI(Uri.parse(storyItem.getFilePath()));
                    play.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            play.setVisibility(View.GONE);
                            imageView.setVisibility(View.GONE);
                            videoView.start();
                        }
                    });

                }

                delete.setVisibility(View.VISIBLE);

            }
        } else {
            Glide.with(getActivity()).asBitmap().load(url).thumbnail(0.5f).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                    bitmap = resource;
                    imageView.setImageBitmap(bitmap);
                }
            });

            if(!url.endsWith(".jpg")) {
                vidUrl = InstaUtils.vids.get(url);
                video.setVisibility(View.VISIBLE);
                play.setVisibility(View.VISIBLE);
                videoView.setVideoURI(Uri.parse(vidUrl));
                Log.d("playing_vid", "" + vidUrl);
                play.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        play.setVisibility(View.GONE);
                        imageView.setVisibility(View.GONE);
                        videoView.start();
                    }
                });

            }

            save.setVisibility(View.VISIBLE);
        }

    }

    private void setupVideo(){
        videoView.setVideoViewCallback(new UniversalVideoView.VideoViewCallback() {
            @Override
            public void onScaleChange(boolean isFullscreen) {

            }

            @Override
            public void onPause(MediaPlayer mediaPlayer) {
                if(!play.isShown())
                    play.setVisibility(View.VISIBLE);

                if(!buttons.isShown())
                    buttons.setVisibility(View.VISIBLE);

                if(mediaController.isShowing())
                    mediaController.setVisibility(View.GONE);
            }

            @Override
            public void onStart(MediaPlayer mediaPlayer) {
                if(play.isShown())
                    play.setVisibility(View.GONE);

                if(buttons.isShown())
                    buttons.setVisibility(View.VISIBLE);

                if(!mediaController.isShowing())
                    mediaController.setVisibility(View.VISIBLE);
            }


            @Override
            public void onBufferingStart(MediaPlayer mediaPlayer) {

            }

            @Override
            public void onBufferingEnd(MediaPlayer mediaPlayer) {
                if(!buttons.isShown())
                    buttons.setVisibility(View.VISIBLE);
            }



        });
    }

    private void saveImage(Bitmap bitmap){
        File file = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "Zoomsta");
        if(!file.exists())
            file.mkdirs();

        String fileName = "Zoomsta-" + System.currentTimeMillis() + ".jpg";

        File newImage = new File(file, fileName);
        if(newImage.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(newImage);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            Toast.makeText(getActivity(), "Saving image...", Toast.LENGTH_SHORT).show();

            if (Build.VERSION.SDK_INT >= 19) {
                MediaScannerConnection.scanFile(getActivity(), new String[]{newImage.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                    }
                });
            } else {
                getActivity().sendBroadcast(new Intent("android.intent.action.MEDIA_MOUNTED", Uri.fromFile(newImage)));
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onDeleteClick(final File f) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("Delete");
        alert.setMessage("Are you sure you want to delete?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                f.delete();
                if (Build.VERSION.SDK_INT >= 19) {
                    MediaScannerConnection.scanFile(getActivity(), new String[]{f.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });
                } else {
                    getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_REMOVED, Uri.fromFile(f)));
                }

                dialog.dismiss();
                storyListener.deletePage(true);

            }
        });

        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    private void shareLocalImage(String path){
        Uri bitmapUri = Uri.parse(path);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
        startActivity(Intent.createChooser(intent, "Share via..."));
    }

    private void shareLocalVideo(String path){
        Uri uri = Uri.parse(path);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, "Share via..."));
    }

    private static void isFromNet(Boolean val){
        isFromNet = val;
    }

    @Override
    public void onPause() {
        super.onPause();
        if(videoView.isPlaying())
            videoView.stopPlayback();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(!isFromNet){
            if(storyItem.getType() == 1 && !videoView.isPlaying())
                play.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(videoView.isPlaying())
            videoView.stopPlayback();
        

    }


    private class saveVideo extends AsyncTask<String, Integer, List<String>> {
        File newVid;
        String url;
        ProgressDialog progressDialog;

        private saveVideo(String url) {
            this.url = url;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Downloading video...");
            progressDialog.setProgressStyle(1);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMax(100);
            progressDialog.show();
            //Toast.makeText(getActivity(), "Downloading video....", Toast.LENGTH_SHORT).show();
        }

        protected List<String> doInBackground(String... args) {
            String fileName = "Zoomsta-" + System.currentTimeMillis() + ".mp4";

            File file = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "Zoomsta");
            if(!file.exists()) file.mkdirs();

            newVid = new File(file, fileName);
            if(!newVid.exists())
            try {
                newVid.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.connect();
                int lenghtOfFile = connection.getContentLength();
                FileOutputStream out = new FileOutputStream(newVid);
                InputStream is = connection.getInputStream();
                byte[] buffer = new byte[1024];
                long total = 0;
                while (true) {
                    int len1 = is.read(buffer);
                    total += (long) len1;
                    publishProgress(new Integer[]{Integer.valueOf((int) ((100 * total) / ((long) lenghtOfFile)))});
                    if (len1 == -1) {
                        break;
                    }
                    out.write(buffer, 0, len1);
                }
                out.close();
                is.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0].intValue());
        }

        protected void onPostExecute(List<String> list) {
            progressDialog.dismiss();
            try {
                Toast.makeText(getActivity(), "Video saved", Toast.LENGTH_SHORT).show();
                if (Build.VERSION.SDK_INT >= 19) {
                    MediaScannerConnection.scanFile(getActivity(), new String[]{newVid.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });
                } else {
                    getActivity().sendBroadcast(new Intent("android.intent.action.MEDIA_MOUNTED", Uri.fromFile(newVid)));
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Error downloading video. Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public Uri getLocalBitmapUri() {

        if (Environment.getExternalStorageState().equals("mounted")) {
            this.cachedFile = new File(getActivity().getExternalCacheDir() + "/.data");
        } else {
            this.cachedFile = new File(getActivity().getCacheDir() + "/.data");
        }

        if (!this.cachedFile.exists()) {
            this.cachedFile.mkdir();
        }
        File file = new File(this.cachedFile.getAbsolutePath() + "/" + String.valueOf(System.currentTimeMillis()) + ".mp4");
        file.getParentFile().mkdirs();
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            InputStream is = (InputStream) new URL(vidUrl).getContent();
            byte[] buffer = new byte[1024];
            while (true) {
                int len1 = is.read(buffer);
                if (len1 == -1) {
                    out.close();
                    is.close();
                    return Uri.fromFile(file);
                }
                out.write(buffer, 0, len1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private class ShareVid extends AsyncTask<String, String, List<String>> {
        ProgressDialog pd;
        Uri uri;

        private ShareVid() {
        }

        protected void onPreExecute() {
            this.pd = new ProgressDialog(getActivity());
            this.pd.setMessage("Sharing the video");
            this.pd.setCanceledOnTouchOutside(false);
            this.pd.show();
            super.onPreExecute();
        }

        protected List<String> doInBackground(String... args) {
            try {
                this.uri = getLocalBitmapUri();
            } catch (Exception e) {
            }
            return null;
        }

        protected void onPostExecute(List<String> list) {
            this.pd.dismiss();
            try {
                Intent shareIntent = new Intent("android.intent.action.SEND");
                shareIntent.setType("video/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM, this.uri);
                getActivity().startActivity(Intent.createChooser(shareIntent, "Share video"));
            } catch (Exception e) {
            }
        }
    }

}
