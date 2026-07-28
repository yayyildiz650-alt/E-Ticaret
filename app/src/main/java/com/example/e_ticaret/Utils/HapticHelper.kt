package com.example.e_ticaret.Utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Uygulama genelinde profesyonel (Apple tarzı) titreşimler sağlayan yardımcı sınıf.
 */
object HapticHelper {

    private fun getVibrator(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    /**
     * İşlem başarılı olduğunda (Sepete ekleme, Favori vb.)
     * Hızlı, tok ve çift bir tık hissi verir.
     */
    fun playSuccess(context: Context) {
        val vibrator = getVibrator(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ için ön tanımlı BAŞARILI efekti
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
        } else {
            // Eski cihazlar için manuel pattern
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 20, 50, 20), -1)
        }
    }

    /**
     * Adet artırma/azaltma veya kaydırma işlemlerinde
     * Çok ince, mekanik bir saat tıkı hissi verir.
     */
    fun playTick(context: Context) {
        val vibrator = getVibrator(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(10)
        }
    }

    /**
     * Silme veya hata gibi uyarı durumlarında
     * Daha belirgin ve tok bir tık hissi verir.
     */
    fun playWarning(context: Context) {
        val vibrator = getVibrator(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(40)
        }
    }

    /**
     * Standart buton tıklamalarında
     * Hafif ve net bir tık hissi verir.
     */
    fun playClick(context: Context) {
        val vibrator = getVibrator(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(15)
        }
    }
}
