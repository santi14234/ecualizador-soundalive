package com.sec.android.app.soundalive;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.Switch;

public class MainActivity extends Activity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        prefs = getSharedPreferences("SoundAlive_Settings", Context.MODE_PRIVATE);

        // Iniciar servicio en segundo plano
        startAudioService();

        setupUIControls();
    }

    private void startAudioService() {
        Intent intent = new Intent(this, SoundAliveService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void setupUIControls() {
        int[] bandIds = {
            R.id.band_60, R.id.band_150, R.id.band_400,
            R.id.band_1k, R.id.band_3k, R.id.band_8k, R.id.band_16k
        };

        for (int i = 0; i < bandIds.length; i++) {
            SeekBar seekBar = findViewById(bandIds[i]);
            if (seekBar == null) continue;

            int savedLevel = prefs.getInt("band_" + i, 50);
            seekBar.setMax(100);
            seekBar.setProgress(savedLevel);

            final int index = i;
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                    if (fromUser) {
                        prefs.edit().putInt("band_" + index, progress).apply();
                        startAudioService(); // Notifica al servicio para actualizar el audio
                    }
                }
                @Override public void onStartTrackingTouch(SeekBar sb) {}
                @Override public void onStopTrackingTouch(SeekBar sb) {}
            });
        }

        // Switch Surround
        Switch switchSurround = findViewById(R.id.switch_surround);
        if (switchSurround != null) {
            switchSurround.setChecked(prefs.getBoolean("surround_on", false));
            switchSurround.setOnCheckedChangeListener((cb, isChecked) -> {
                prefs.edit().putBoolean("surround_on", isChecked).apply();
                startAudioService();
            });
        }

        // Switch Tube Amp Pro
        Switch switchTube = findViewById(R.id.switch_tube);
        if (switchTube != null) {
            switchTube.setChecked(prefs.getBoolean("tube_on", false));
            switchTube.setOnCheckedChangeListener((cb, isChecked) -> {
                prefs.edit().putBoolean("tube_on", isChecked).apply();
                startAudioService();
            });
        }

        // Switch Concert Hall
        Switch switchConcert = findViewById(R.id.switch_concert);
        if (switchConcert != null) {
            switchConcert.setChecked(prefs.getBoolean("concert_on", false));
            switchConcert.setOnCheckedChangeListener((cb, isChecked) -> {
                prefs.edit().putBoolean("concert_on", isChecked).apply();
                startAudioService();
            });
        }
    }
}
