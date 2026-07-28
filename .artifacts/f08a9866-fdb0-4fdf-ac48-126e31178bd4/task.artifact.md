# Dwell Time & FOMO Sistemi Görev Listesi

- [x] **Room & Data Layer Geliştirmeleri**
    - [x] `SepetDao.kt`: `sepeteKesinEkle` metodunda ağırlıklı ortalama mantığını mühürle.
- [x] **ViewModel Logic Geliştirmeleri**
    - [x] `Sepet_ViewModel.kt`: `adetGuncelle` metodunda fiyat koruma formülünü (Ağırlıklı Ortalama) kusursuzlaştır.
    - [x] `UrunEtkilesimViewModel.kt`: Level 1 -> Level 2 geçişini otomatikleştir ve sayaç senkronizasyonunu optimize et.
- [x] **UI Layer Geliştirmeleri**
    - [x] `Urun_Detay_Screen.kt`: Kampanya kartı overlay'ini ve `AnimatedContent` geçişlerini ayarla.
    - [x] `Urun_Detay_Screen.kt`: Sepete ekleme ve adet artırma butonlarını kampanya onayı ile entegre et.
- [x] **Doğrulama ve Test**
    - [x] 10.000 TL VIP barajı kontrolü.
    - [x] 15sn / 2. giriş tetikleme kontrolü.
    - [x] Sepette ağırlıklı ortalama fiyat kontrolü.
    - [x] Level 2 anlık tetikleme kontrolü.
