package com.sec.android.app.soundalive;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;
import android.os.Bundle;
import android.widget.SeekBar;

public class MainActivity extends Activity {

    private Equalizer mEqualizer;
    private BassBoost mBassBoost;
    private Virtualizer mVirtualizer;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        prefs = getSharedPreferences("SoundAlive_Settings", Context.MODE_PRIVATE);

        initAudioEffects();
        setupBassBoost();
        setupVirtualizer();
        setupEqualizerBands();
    }

    private void initAudioEffects() {
        try {
            // Conecta con la sesión de audio global (0) del sistema
            mEqualizer = new Equalizer(0, 0);
            mEqualizer.setEnabled(true);

            mBassBoost = new BassBoost(0, 0);
            mBassBoost.setEnabled(true);

            mVirtualizer = new Virtualizer(0, 0);
            mVirtualizer.setEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupBassBoost() {
        SeekBar seekBass = findViewById(R.id.seek_bass);
        if (seekBass != null && mBassBoost != null) {
            int savedBass = prefs.getInt("bass_level", 0);
            seekBass.setProgress(savedBass);
            
            if (mBassBoost.getStrengthSupported()) {
                mBassBoost.setStrength((short) savedBass);
            }

            seekBass.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser && mBassBoost != null && mBassBoost.getStrengthSupported()) {
                        mBassBoost.setStrength((short) progress);
                        prefs.edit().putInt("bass_level", progress).apply();
                    }
                }

                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }
    }

    private void setupVirtualizer() {
        SeekBar seekSurround = findViewById(R.id.seek_surround);
        if (seekSurround != null && mVirtualizer != null) {
            int savedSurround = prefs.getInt("surround_level", 0);
            seekSurround.setProgress(savedSurround);
            
            if (mVirtualizer.getStrengthSupported()) {
                mVirtualizer.setStrength((short) savedSurround);
            }

            seekSurround.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser && mVirtualizer != null && mVirtualizer.getStrengthSupported()) {
                        mVirtualizer.setStrength((short) progress);
                        prefs.edit().putInt("surround_level", progress).apply();
                    }
                }

                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }
    }

    private void setupEqualizerBands() {
        if (mEqualizer == null) return;

        final short minEQLevel = mEqualizer.getBandLevelRange()[0];
        final short maxEQLevel = mEqualizer.getBandLevelRange()[1];

        // Control de banda de ejemplo (62Hz)
        SeekBar band62 = findViewById(R.id.band_62hz);
        if (band62 != null) {
            int savedBand = prefs.getInt("band_62", (maxEQLevel - minEQLevel) / 2);
            band62.setMax(maxEQLevel - minEQLevel);
            band62.setProgress(savedBand);
            mEqualizer.setBandLevel((short) 0, (short) (savedBand + minEQLevel));

            band62.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser && mEqualizer != null) {
                        mEqualizer.setBandLevel((short) 0, (short) (progress + minEQLevel));
                        prefs.edit().putInt("band_62", progress).apply();
                    }
                }

                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Mantiene los efectos activos en segundo plano tras cerrar la interfaz
    }
}
