package com.sec.android.app.soundalive;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.audiofx.EnvironmentalReverb;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;
import android.os.Build;
import android.os.IBinder;

public class SoundAliveService extends Service {

    private Equalizer mEqualizer;
    private Virtualizer mVirtualizer;
    private EnvironmentalReverb mReverb;

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundServiceNotification();
        applyAudioEffects();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        applyAudioEffects();
        return START_STICKY; // Obliga al sistema a mantener el servicio siempre vivo
    }

    private void applyAudioEffects() {
        SharedPreferences prefs = getSharedPreferences("SoundAlive_Settings", Context.MODE_PRIVATE);

        try {
            // Inicializar efectos en la sesión global de audio (0)
            if (mEqualizer == null) {
                mEqualizer = new Equalizer(0, 0);
                mEqualizer.setEnabled(true);
            }
            if (mVirtualizer == null) {
                mVirtualizer = new Virtualizer(0, 0);
                mVirtualizer.setEnabled(true);
            }
            if (mReverb == null) {
                mReverb = new EnvironmentalReverb(0, 0);
                mReverb.setEnabled(true);
            }

            // 1. Aplicar 7 Bandas
            short minEQ = mEqualizer.getBandLevelRange()[0];
            short maxEQ = mEqualizer.getBandLevelRange()[1];
            short numBands = mEqualizer.getNumberOfBands();

            for (int i = 0; i < 7; i++) {
                int savedLevel = prefs.getInt("band_" + i, (maxEQ - minEQ) / 2);
                short bandIndex = (short) Math.min(i, numBands - 1);
                mEqualizer.setBandLevel(bandIndex, (short) (savedLevel + minEQ));
            }

            // 2. Surround
            boolean isSurround = prefs.getBoolean("surround_on", false);
            if (mVirtualizer.getStrengthSupported()) {
                mVirtualizer.setStrength((short) (isSurround ? 1000 : 0));
            }

            // 3. Concert Hall
            boolean isConcert = prefs.getBoolean("concert_on", false);
            mReverb.setDecayTime(isConcert ? 3000 : 0);

            // 4. Tube Amp Pro
            boolean isTube = prefs.getBoolean("tube_on", false);
            if (isTube) {
                mEqualizer.setBandLevel((short) 0, maxEQ);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startForegroundServiceNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "soundalive_channel",
                    "SoundAlive Audio Engine",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }

            Notification notification = new Notification.Builder(this, "soundalive_channel")
                    .setContentTitle("SoundAlive")
                    .setContentText("Efectos de audio activos")
                    .setSmallIcon(android.R.drawable.ic_media_play)
                    .build();

            startForeground(1, notification);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
