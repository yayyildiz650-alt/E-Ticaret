# Tereddüt Avcısı (Hesitation Hunter) Stabilizasyonu - FAZ 15: ÖZEL AYARLAR

Kullanıcının talep ettiği özel tetikleme değerlerini (15 saniye, 2 ziyaret, 10K barajı) sisteme mühürleme planı.

## User Review Required

> [!IMPORTANT]
> - **Tetikleme Kuralları:**
>   - **VIP Barajı:** 10.000 TL ve üzeri ürünlerde çalışacak.
>   - **Ziyaret:** Ürün detayına 2. kez girildiğinde kart anında çıkacak.
>   - **Dwell Time:** Sayfada 15 saniye beklendiğinde kart çıkacak.

## Proposed Changes

### [ViewModels]

#### [MODIFY] [UrunEtkilesimViewModel.kt](file:///C:/Users/yayyi/AndroidStudioProjects/ETicaret/app/src/main/java/com/example/e_ticaret/ViewModeller/UrunEtkilesimViewModel.kt)
- `delay(20000)` -> `delay(15000)` (15 Saniye)
- `yeniSayi >= 3` -> `yeniSayi >= 2` (2. Giriş)
- Baraj değerleri: `10000.0` (Level 1 ve Level 2 için)

---

## Verification Plan

### Manual Verification
1. **Ziyaret Testi:** 10.000 TL+ bir ürüne girin, çıkın, tekrar girin. Kartın anında fırladığını görün.
2. **Süre Testi:** Aynı ürüne ilk kez girip 15 saniye bekleyin. Kartın otomatik çıktığını görün.
3. **Senkronizasyon:** Logcat'te `EtkilesimVM` loglarını takip ederek 10K barajının geçildiğini doğrulayın.
