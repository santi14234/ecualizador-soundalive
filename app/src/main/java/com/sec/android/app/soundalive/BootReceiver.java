package com.sec.android.app.soundalive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferences prefs = context.getSharedPreferences("SoundAlive_Settings", Context.MODE_PRIVATE);

            try {
                // Re-aplicar Bass Boost al encender el teléfono
                BassBoost bassBoost = new BassBoost(0, 0);
                bassBoost.setEnabled(true);
                int bassLevel = prefs.getInt("bass_level", 0);
                if (bassBoost.getStrengthSupported()) {
                    bassBoost.setStrength((short) bassLevel);
                }

                // Re-aplicar Efecto Envolvente (Surround 3D)
                Virtualizer virtualizer = new Virtualizer(0, 0);
                virtualizer.setEnabled(true);
                int surroundLevel = prefs.getInt("surround_level", 0);
                if (virtualizer.getStrengthSupported()) {
                    virtualizer.setStrength((short) surroundLevel);
                }

                // Re-aplicar Ecualizador
                Equalizer equalizer = new Equalizer(0, 0);
                equalizer.setEnabled(true);
                short minEQLevel = equalizer.getBandLevelRange()[0];
                short maxEQLevel = equalizer.getBandLevelRange()[1];
                int savedBand62 = prefs.getInt("band_62", (maxEQLevel - minEQLevel) / 2);
                equalizer.setBandLevel((short) 0, (short) (savedBand62 + minEQLevel));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
