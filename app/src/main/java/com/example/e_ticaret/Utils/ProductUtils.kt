package com.example.e_ticaret.Utils

object ProductUtils {
    /**
     * Ürünün başlığına, açıklamasına veya mevcut kategorisine bakarak 
     * categories.json içindeki 17 ana kategoriden birine (slug) atar.
     */
    fun determineCategory(title: String?, description: String?, currentCategory: String?): String {
        // Mevcut kategoriyi kontrol et (Eğer zaten eşleşen bir slug ise koru)
        val category = currentCategory?.lowercase() ?: ""
        val text = ((title ?: "") + (description ?: "")).lowercase()
        
        return when {
            // Akıllı Telefonlar
            category.contains("smartphone") || text.contains("telefon") || text.contains("iphone") || text.contains("samsung") -> "smartphones"
            
            // Dizüstü Bilgisayarlar
            category.contains("laptop") || text.contains("dizüstü") || text.contains("bilgisayar") || text.contains("notebook") -> "laptops"
            
            // Tabletler
            category.contains("tablet") || text.contains("ipad") -> "tablets"
            
            // Elektronik (Genel)
            category.contains("electronics") || text.contains("kulaklık") || text.contains("hoparlör") || text.contains("şarj") -> "electronics"
            
            // Telefon Aksesuarları
            category.contains("mobile-accessories") || text.contains("kılıf") || text.contains("ekran koruyucu") || text.contains("powerbank") -> "mobile-accessories"
            
            // Mutfak Gereçleri
            category.contains("kitchen") || text.contains("tava") || text.contains("tencere") || text.contains("bardak") || text.contains("mutfak") || text.contains("çatal") -> "kitchen-accessories"
            
            // Ev Dekorasyonu
            category.contains("decoration") || text.contains("tablo") || text.contains("vazo") || text.contains("ayna") || text.contains("aydınlatma") -> "home-decoration"
            
            // Mobilya
            category.contains("furniture") || text.contains("kanepe") || text.contains("koltuk") || text.contains("masa") || text.contains("yatak") || text.contains("dolap") -> "furniture"
            
            // Erkek Saat
            category.contains("watches") || (text.contains("saat") && (text.contains("erkek") || text.contains("kol"))) -> "mens-watches"
            
            // Spor Aksesuarları
            category.contains("sports") || text.contains("spor") || text.contains("dumbell") || text.contains("mat") || text.contains("topu") -> "sports-accessories"
            
            // Güneş Gözlüğü
            category.contains("sunglasses") || text.contains("gözlük") || text.contains("güneş") -> "sunglasses"
            
            // Erkek Ayakkabı
            (category.contains("shoes") && category.contains("men")) || (text.contains("ayakkabı") && text.contains("erkek")) -> "mens-shoes"
            
            // Erkek Gömlek
            category.contains("shirts") || text.contains("gömlek") -> "mens-shirts"
            
            // Giyim (Genel)
            category.contains("apparel") || text.contains("tişört") || text.contains("pantolon") || text.contains("mont") || text.contains("giyim") || text.contains("çorap") -> "apparel"
            
            // Motosiklet
            category.contains("motorcycle") || text.contains("motosiklet") || text.contains("kask") -> "motorcycle"
            
            // Araçlar
            category.contains("vehicle") || text.contains("araba") || text.contains("araç") || text.contains("lastik") -> "vehicle"
            
            // Market (Groceries) - Geri kalan her şey
            category.contains("groceries") || text.contains("süt") || text.contains("ekmek") || text.contains("peynir") || text.contains("yağ") || text.contains("makarna") || text.contains("et") || text.contains("tavuk") || text.contains("meyve") || text.contains("sebze") || text.contains("kozmetik") || text.contains("bakım") -> "groceries"
            
            else -> "groceries"
        }
    }

    /**
     * Fiyat stringini (₺120.000,00 veya ₺120,000.00) Double formatına hatasız çevirir.
     * Binlik ayraçları ve para birimi sembollerini akıllıca temizler.
     */
    fun cleanPrice(priceString: String?): Double {
        if (priceString.isNullOrBlank()) return 0.0
        
        // 1. Sayı ve ayraçlar dışındaki her şeyi temizle
        var clean = priceString.replace(Regex("[^0-9,.]"), "").trim()
        if (clean.isEmpty()) return 0.0

        // 2. Ondalık ve Binlik ayracı tespiti
        val lastComma = clean.lastIndexOf(',')
        val lastDot = clean.lastIndexOf('.')

        return try {
            if (lastComma > lastDot) {
                // TR Formatı: 1.234.567,89 -> Binlik noktalarını sil, virgülü noktaya çevir
                clean.replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
            } else if (lastDot > lastComma) {
                // US Formatı: 1,234,567.89 -> Binlik virgüllerini sil
                clean.replace(",", "").toDoubleOrNull() ?: 0.0
            } else if (lastDot != -1) {
                // Sadece nokta var: 120.000 veya 123.45
                // Eğer noktadan sonra tam 3 rakam varsa bu muhtemelen bir binlik ayracıdır (TR)
                val digitsAfter = clean.length - lastDot - 1
                if (digitsAfter == 3) {
                    clean.replace(".", "").toDoubleOrNull() ?: 0.0
                } else {
                    clean.toDoubleOrNull() ?: 0.0
                }
            } else {
                // Sadece virgül var veya hiç ayraç yok
                clean.replace(",", ".").toDoubleOrNull() ?: 0.0
            }
        } catch (e: Exception) {
            0.0
        }
    }
}
