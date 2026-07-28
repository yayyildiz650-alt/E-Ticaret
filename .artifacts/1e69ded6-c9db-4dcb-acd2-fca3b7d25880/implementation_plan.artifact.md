# Ürün Dağılımı ve Görsel İyileştirme Planı

Kullanıcının geri bildirimi doğrultusunda, JSON dosyasındaki ürünlerin (özellikle yeni eklenen 130 ürünün) doğru kategorilere dağıtılması ve görseli olmayan ürünlerin kullanıcı deneyimini bozmaması için uygulama mantığında iyileştirmeler yapılacaktır.

## Kullanıcı İncelemesi Gerekenler

- **Kategori Eşleşmeleri**: Ürünlerin hangi anahtar kelimelerle hangi kategorilere atanacağı belirlenmiştir. Örneğin; "akıllı telefon" geçen ürünler `smartphones` kategorisine atanacaktır.
- **Görsel Yedekleme**: Görseli olmayan ürünler için varsayılan bir "Resim Yok" ikonu gösterilecektir.

## Önerilen Değişiklikler

### [Yardımcı Araçlar (Utils)]

#### [NEW] [ProductUtils.kt](file:///C:/Users/yayyi/AndroidStudioProjects/ETicaret/app/src/main/java/com/example/e_ticaret/Utils/ProductUtils.kt)
- Ürünlerin `category` alanlarını, `categories.json` içindeki **slug** değerleriyle (örn: `groceries`, `smartphones`) eşleştiren gelişmiş bir mantık kurulacak.

### [Görünüm Modelleri (ViewModels)]

#### [MODIFY] [UrunlerViewModel.kt](file:///C:/Users/yayyi/AndroidStudioProjects/ETicaret/app/src/main/java/com/example/e_ticaret/ViewModeller/UrunlerViewModel.kt)
- Ana sayfada gösterilen tüm ürünler `ProductUtils` üzerinden geçirilerek kategorileri düzeltilecek.

#### [MODIFY] [FilterViewModel.kt](file:///C:/Users/yayyi/AndroidStudioProjects/ETicaret/app/src/main/java/com/example/e_ticaret/ViewModeller/FilterViewModel.kt)
- Kategori filtreleme mantığı, düzeltilmiş kategoriler üzerinden çalışacak şekilde güncellenecek.

### [UI Bileşenleri (UI Components)]

#### [MODIFY] [AnaEkran-Afiş.kt](file:///C:/Users/yayyi/AndroidStudioProjects/ETicaret/app/src/main/java/com/example/e_ticaret/AnaEkran-Afiş.kt)
- `UrunKarti` içindeki `AsyncImage` bileşenine, görsel yüklenemediğinde veya boş olduğunda gösterilecek bir **placeholder** ve **error** durumu eklenecek.

## Doğrulama Planı

### Kategorizasyon Testi
- "Market" kategorisine girildiğinde, eskiden "Genel"de kalan gıda ürünlerinin burada göründüğü doğrulanacak.
- "Mobilya" kategorisinde `furniture` slug'ına sahip ürünlerin listelendiği kontrol edilecek.

### Görsel Testi
- Görseli olmayan ürünlerin kartlarında boşluk yerine "Resim Yok" ikonunun çıktığı görülecek.
