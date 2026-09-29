package com.sec.android.app.soundalive;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.audiofx.EnvironmentalReverb;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

public class MainActivity extends Activity {

    private Equalizer mEqualizer;
    private Virtualizer mVirtualizer;
    private EnvironmentalReverb mReverb;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        prefs = getSharedPreferences("SoundAlive_Settings", Context.MODE_PRIVATE);

        initAudioEffects();
        setupEqualizerBands();
        setupSwitches();
    }

    private void initAudioEffects() {
        try {
            mEqualizer = new Equalizer(0, 0);
            mEqualizer.setEnabled(true);

            mVirtualizer = new Virtualizer(0, 0);
            mVirtualizer.setEnabled(true);

            mReverb = new EnvironmentalReverb(0, 0);
            mReverb.setEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupEqualizerBands() {
        if (mEqualizer == null) return;

        final short minEQ = mEqualizer.getBandLevelRange()[0];
        final short maxEQ = mEqualizer.getBandLevelRange()[1];
        short numBands = mEqualizer.getNumberOfBands();

        int[] bandIds = {
            R.id.band_60, R.id.band_150, R.id.band_400,
            R.id.band_1k, R.id.band_3k, R.id.band_8k, R.id.band_16k
        };

        for (int i = 0; i < bandIds.length; i++) {
            SeekBar seekBar = findViewById(bandIds[i]);
            if (seekBar == null) continue;

            final short bandIndex = (short) Math.min(i, numBands - 1);
            int savedLevel = prefs.getInt("band_" + i, (maxEQ - minEQ) / 2);

            seekBar.setMax(maxEQ - minEQ);
            seekBar.setProgress(savedLevel);
            mEqualizer.setBandLevel(bandIndex, (short) (savedLevel + minEQ));

            final int index = i;
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                    if (fromUser && mEqualizer != null) {
                        mEqualizer.setBandLevel(bandIndex, (short) (progress + minEQ));
                        prefs.edit().putInt("band_" + index, progress).apply();
                    }
                }
                @Override public void onStartTrackingTouch(SeekBar sb) {}
                @Override public void onStopTrackingTouch(SeekBar sb) {}
            });
        }
    }

    private void setupSwitches() {
        // Switch Surround
        Switch switchSurround = findViewById(R.id.switch_surround);
        if (switchSurround != null) {
            boolean isSurround = prefs.getBoolean("surround_on", false);
            switchSurround.setChecked(isSurround);
            if (mVirtualizer != null && mVirtualizer.getStrengthSupported()) {
                mVirtualizer.setStrength((short) (isSurround ? 1000 : 0));
            }

            switchSurround.setOnCheckedChangeListener((cb, isChecked) -> {
                if (mVirtualizer != null && mVirtualizer.getStrengthSupported()) {
                    mVirtualizer.setStrength((short) (isChecked ? 1000 : 0));
                    prefs.edit().putBoolean("surround_on", isChecked).apply();
                }
            });
        }

        // Switch Tube Amp Pro
        Switch switchTube = findViewById(R.id.switch_tube);
        if (switchTube != null) {
            boolean isTube = prefs.getBoolean("tube_on", false);
            switchTube.setChecked(isTube);

            switchTube.setOnCheckedChangeListener((cb, isChecked) -> {
                prefs.edit().putBoolean("tube_on", isChecked).apply();
                if (mEqualizer != null) {
                    short minEQ = mEqualizer.getBandLevelRange()[0];
                    short maxEQ = mEqualizer.getBandLevelRange()[1];
                    short mid = (short) ((maxEQ - minEQ) / 2 + minEQ);
                    // Calidez analógica simulando tubo
                    mEqualizer.setBandLevel((short) 0, (short) (isChecked ? maxEQ / 2 : mid));
                }
            });
        }

        // Switch Concert Hall
        Switch switchConcert = findViewById(R.id.switch_concert);
        if (switchConcert != null) {
            boolean isConcert = prefs.getBoolean("concert_on", false);
            switchConcert.setChecked(isConcert);
            if (mReverb != null) {
                mReverb.setDecayTime(isConcert ? 3000 : 0);
            }

            switchConcert.setOnCheckedChangeListener((cb, isChecked) -> {
                if (mReverb != null) {
                    mReverb.setDecayTime(isChecked ? 3000 : 0);
                    prefs.edit().putBoolean("concert_on", isChecked).apply();
                }
            });
        }
    }
}
