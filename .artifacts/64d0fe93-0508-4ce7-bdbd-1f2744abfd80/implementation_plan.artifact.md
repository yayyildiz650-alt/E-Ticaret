# "Tereddüt Avcısı" - VIP Final Stand & %100 Atomik Çözüm (İTO-SAVAR)

Bu plan, kampanya sistemindeki tüm senkronizasyon, fiyat formatlama ve Level 2 geçiş hatalarını **Singleton Pattern** ve **Strict Await Coroutines** kullanarak kökten çözer.

## Yapılacak Radikal Düzenlemeler

### 1. Sepet Veri Mührü (Garantili Senkronizasyon)

#### [MODIFY] [Sepet_ViewModel.kt](file:///C:/Users/yayyi/AndroidStudioProjects/ETicaret/app/src/main/java/com/example/e_ticaret/ViewModeller/Sepet_ViewModel.kt)
- **Direct Database Authority:** `sepetListesi` artık `Flow` yerine veritabanından her saniye en güncel veriyi zorla çeken bir `StateFlow` yapısına (stateIn) kavuşturulacak.
- **Atomic Operation:** `sepeteEkle` fonksiyonu bittiği an, sistem veritabanı akışını (Flow) manuel olarak kamçılayıp tüm UI'ları güncellenmeye zorlayacak.

### 2. Level 2 Geçişi (Kesin ve Işık Hızında)

#### [MODIFY] [UrunEtkilesimViewModel.kt](file:///C:/Users/yayyi/AndroidStudioProjects/ETicaret/app/src/main/java/com/example/e_ticaret/ViewModeller/UrunEtkilesimViewModel.kt)
- **Immediate State Purge:** Level 1 indirimi mühürlendiği an, tüm "teklif" state'leri anlık temizlenecek.
- **Sequential Trigger:** Level 2 teklifi, Level 1 kapandıktan tam 500ms sonra (UI animasyonunun bitmesi beklenerek) anında tetiklenecek.

### 3. UI Güvenlik Duvarı ve VIP Barajı

#### [MODIFY] [Urun_Detay_Screen.kt](file:///C:/Users/yayyi/AndroidStudioProjects/ETicaret/app/src/main/java/com/example/e_ticaret/Urun_Detay_Screen.kt)
- **Atomic Button Action:** "Fırsatı Yakala" butonu artık şu disiplinle çalışacak:
    1.  UI State: `teklifGoster = false` (Immediate).
    2.  DB Save: Level 1 mühürle (Await).
    3.  Basket Update: İndirimli fiyatı sepete mühürle (Await).
    4.  Delay: 500ms bekle.
    5.  Trigger Level 2: Beklemeden yeni kartı patlat.
- **Price Mismatch Guard:** 50.000 TL barajı kuruş hassasiyetiyle kontrol edilecek.

## Doğrulama Planı
1.  **VIP Kontrol:** 50.000 TL altı ürünlerde kampanya çıkmadığını doğrula.
2.  **L1 -> Basket:** %5 indirimi yakala, sepete git, fiyatın **kuruşu kuruşuna** yansıdığını gör.
3.  **L2 Transition:** Detay sayfasına dön, Level 2 (%20) kartının anında geldiğini gör.
