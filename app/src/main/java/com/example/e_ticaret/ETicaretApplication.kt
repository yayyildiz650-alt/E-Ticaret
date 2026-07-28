package com.example.e_ticaret

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache

/**
 * Uygulamanın ana sınıfı. Coil kütüphanesini burada yapılandırarak
 * resimlerin profesyonel bir şekilde önbelleğe alınmasını (cache) sağlıyoruz.
 */
class ETicaretApplication : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            // 1. HAFIZA ÖNBELLEĞİ (RAM)
            // Uygulamanın kullandığı RAM'in %25'ini resimlere ayırır.
            // Sayfalar arası geçişte resimlerin anında görünmesini sağlar.
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            // 2. DİSK ÖNBELLEĞİ (TELEFON HAFIZASI)
            // İnternetten inen resimleri telefonun hafızasına kaydeder.
            // Uygulama kapansa bile resimler tekrar inmez, buradan okunur.
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02) // Telefon hafızasının %2'si veya sabit limit:
                    .maxSizeBytes(50L * 1024 * 1024) // Maksimum 50 MB
                    .build()
            }
            // 3. PERFORMANS AYARLARI
            .crossfade(true) // Tüm resim geçişlerini yumuşak yapar
            .respectCacheHeaders(false) // Sunucudan bağımsız olarak biz cache'i yönetelim
            .build()
    }
}
