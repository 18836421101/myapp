package com.example.shiyan_bottom;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import java.util.Objects;

public class video_player extends AppCompatActivity {
    VideoView mVideoNet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        mVideoNet=findViewById(R.id.video_net);
        Bundle bundle = getIntent().getExtras();
        String name = bundle.getString("name");
        //System.out.println(getPackageName());
        initNetVideo(name);



    }

    //播放网络视频
    private void initNetVideo(String name) {
        //设置有进度条可以拖动快进
        MediaController localMediaController = new MediaController(this);
        mVideoNet.setMediaController(localMediaController);
        if(Objects.equals(name, "你好")){
            String uri = "android.resource://"+getPackageName()+"/"+R.raw.nihao;
            mVideoNet.setVideoURI(Uri.parse(uri));
            localMediaController.setMediaPlayer(mVideoNet);
            mVideoNet.setMediaController(localMediaController);
            //soundPool.play(soundId[1], 1, 1, 1, 0, 1);
        }if(Objects.equals(name, "不用谢")){
            String uri = "android.resource://"+getPackageName()+"/"+R.raw.buyongxie;
            mVideoNet.setVideoURI(Uri.parse(uri));
            localMediaController.setMediaPlayer(mVideoNet);
            mVideoNet.setMediaController(localMediaController);
        }if(Objects.equals(name, "请坐")){
            String uri = "android.resource://"+getPackageName()+"/"+R.raw.qingzuo;
            mVideoNet.setVideoURI(Uri.parse(uri));
            localMediaController.setMediaPlayer(mVideoNet);
            mVideoNet.setMediaController(localMediaController);
        }if(Objects.equals(name, "多少钱")){
            String uri = "android.resource://"+getPackageName()+"/"+R.raw.duoshaoqian;
            mVideoNet.setVideoURI(Uri.parse(uri));
            localMediaController.setMediaPlayer(mVideoNet);
            mVideoNet.setMediaController(localMediaController);
            //soundPool.play(soundId[0], 1, 1, 1, 0, 1);
        }
    }


}