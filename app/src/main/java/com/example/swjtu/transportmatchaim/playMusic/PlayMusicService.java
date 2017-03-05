package com.example.swjtu.transportmatchaim.playMusic;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

/**
 * Created by tangpeng on 2017/3/4.
 */

public class PlayMusicService extends Service {
    private static final String TAG = "PlayMusicService";

    MediaPlayer mediaPlayer;

    private AudioManager audioManager;

    private int currMusicVolume;
    private int maxMusicVolume;

    private MyBinder myBinder = new MyBinder();

    public class MyBinder extends Binder {
        public void startMP3Play() {
            for (int i = currMusicVolume; i <= maxMusicVolume; i++) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FX_FOCUS_NAVIGATION_UP);
            }
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        }

        public void stopMP3Play() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.reset();
                initMP3Play();
            }
        }

        public void pauseMP3Play() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        }
    }

    private void initMP3Play() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        currMusicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxMusicVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        try {
            AssetFileDescriptor descriptor = getAssets().openFd("alert_voice.mp3");
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(descriptor.getFileDescriptor());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "onCreate: IOException ", e);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind: ");
        return myBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: ");
        initMP3Play();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
