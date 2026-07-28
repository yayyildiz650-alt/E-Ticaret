# Tereddüt Avcısı (Hesitation Hunter) Stabilizasyonu - KESİN MÜHÜR (FAZ 14)

Kullanıcının talep ettiği özel tetikleme değerleri (15 saniye, 2 ziyaret, 10K barajı) ve görünürlük sorunları bu faz ile tamamen mühürlenmiştir.

## Yapılan İyileştirmeler

### 1. Kullanıcı Odaklı Tetikleme Değerleri
- **VIP Barajı:** Tüm indirimler için baraj **10.000 TL**'ye çekildi.
- **Ziyaret Sayısı:** Artık 3 değil, **2. girişte** kart anında patlayacak şekilde güncellendi.
- **Bekleme Süresi (Dwell Time):** Artık 20 saniye değil, **15 saniye** sonra kampanya fırlayacak.

### 2. Overlay Görünürlük Garantisi
- **Konumlandırma:** Kampanya kartı `Scaffold` body'si içinde en üst katmana (`Box` overlay) yerleştirildi.
- **Padding Fix:** Kartın alt bardan (`BottomBar`) etkilenmemesi için `innerPadding.calculateBottomPadding() + 16.dp` değeri kullanılarak konumlandırıldı.

### 3. Syntax ve Kod Temizliği
- `Urun_Detay_Screen.kt` dosyasındaki tüm fazla parantezler ve bozuk bloklar sökülüp atıldı. Dosya tertemiz bir yapıya kavuştu.

## Nasıl Test Edilir?

1. **VIP Ürün Seçin:** 10.000 TL ve üzeri bir ürüne girin.
2. **Hızlı Tetikleme:** Sayfadan çıkıp tekrar girin (toplam 2 kez). Kartın anında fırladığını görün.
3. **Bekleme Testi:** Sayfaya ilk kez girdiğinizde 15 saniye bekleyin. Kartın kendiliğinden geldiğini görün.

> [!IMPORTANT]
> Logcat'te `EtkilesimVM` yazarak tetikleme anlarını saniyesi saniyesine izleyebilirsiniz.
