# 🚀 Yeni Nesil Full-Stack E-Ticaret Platformu (AI Destekli & Offline-First)

Modern, yüksek performanslı ve kullanıcı deneyimini merkeze alan kapsamlı bir **Full-Stack E-Ticaret** uygulamasıdır. Proje, standart bir alışveriş uygulamasının ötesine geçerek **Gemini AI destekli akıllı asistan**, **dinamik indirim kartları** ve internet bağlantısından bağımsız çalışabilen **"Offline-First" (Önce Yerel Veritabanı)** mimarisiyle donatılmıştır. 

Uygulama, ön yüzde **Kotlin & MVVM** mimarisiyle kusursuz ve reaktif bir UI sunarken, arka planda **Spring Boot** ve **PostgreSQL** ile güçlü, ölçeklenebilir bir istemci-sunucu (Client-Server) senkronizasyonu sağlar.

---

## 📸 Ekran Görüntüleri

Projenin modern arayüzünü ve zengin özelliklerini aşağıda inceleyebilirsiniz:

<table>
  <tr>
    <td align="center"><img src="https://github.com/user-attachments/assets/0583d1ff-cb7c-400b-aae4-a718f6d4f5cc" width="250"/><br/><b>🌟 Ana Ekran & Vitrin</b></td>
    <td align="center"><img src="https://github.com/user-attachments/assets/3d8622ec-6441-46ce-9c74-1a4a7a040b33" width="250"/><br/><b>🤖  Detay Ekranı</b></td>
    <td align="center"><img src="https://github.com/user-attachments/assets/f0cfb388-a7c2-483c-8331-70cb4f7a8a03" width="250"/><br/><b>💳  AI Destekli Asistan</b></td>
  </tr>
  <tr>
    <td align="center"><img src="https://github.com/user-attachments/assets/dcecbac2-5679-4210-b25a-e759aec727a3" width="250"/><br/><b>🛒  Detaylı Kategoriler </b></td>
    <td align="center"><img src="https://github.com/user-attachments/assets/f21ebe25-9dbf-401d-aecf-b3329d590edb" width="250"/><br/><b>❤️ Favoriler (Dual-Write)</b></td>
    <td align="center"><img src="https://github.com/user-attachments/assets/a9ebbcd6-2415-4963-8bf0-9a73465a9d83" width="250"/><br/><b>📂 Akıllı Sepet Yönetimi </b></td>
  </tr>
</table>

---

## ✨ Öne Çıkan İnovatif Özellikler

### 🤖 Gemini AI Destekli Alışveriş Asistanı
Uygulama, klasik kelime tabanlı arama motorlarının sınırlarını aşarak **Yapay Zeka (Gemini API)** ve **Kamera Entegrasyonu** kullanır. Kullanıcı cihaz kamerasıyla bir ürünü tarattığında sistem şu adımları izler:
* Özel prompt formatlama ile görüntü işlenir.
* Gemini API üzerinden görseldeki ürünün tipi, rengi ve kategorisi saniyeler içinde analiz edilir.
* Backend'de bulunan geniş ürün kataloğu ile eşleştirme mantığı (catalog matching logic) kurularak kullanıcıya en benzer/uygun ürünler sunulur.

### ⚡ Offline-First ve Reaktif Arayüz (Sıfır Gecikme)
İnternet bağlantısı yavaş veya kesik olsa dahi uygulama kusursuz çalışmaya devam eder. Sepete veya favorilere ekleme gibi kullanıcı etkileşimleri şu şekilde yönetilir:
* İşlem önce **Room Database (SQLite)** üzerindeki DAO interfaceleri aracılığıyla yerel veritabanına yazılır.
* **StateFlow** ve **ViewModel** veri akışları sayesinde arayüz (UI) **0 ms gecikme** ile anında güncellenir.
* Kullanıcı, internetin gidip geldiğini hissetmeden akıcı bir alışveriş deneyimi yaşar.

### 🔄 Çift Yönlü (Dual-Write) Arka Plan Senkronizasyonu
Yerel cihazda yapılan veri değişiklikleri, kullanıcıyı bekletmeden arka planda sunucuya iletilir:
* **Kotlin Coroutines** üzerinden asenkron execution başlatılır.
* **Retrofit** aracılığıyla Spring Boot sunucusundaki REST endpoint'lerine (`POST`, `DELETE`) istek fırlatılır.
* Uygulama her başlatıldığında sunucudaki en güncel veriler (`GET`) çekilerek yerel Room veritabanı ile eşitlenir. Bu sayede farklı cihazlardan hesaba girildiğinde dahi tam tutarlılık sağlanır.

### 🎟️ Kişiselleştirilmiş İndirim Kartları Sistemi
Kullanıcı sadakatini ve satışa dönüşüm oranını (conversion rate) artırmak amacıyla tasarlanmış özel bir gamification (oyunlaştırma) sistemidir. UI üzerinde şık bir tasarımla sunulan indirim kartları, kullanıcının sepetindeki ürünlere entegre olarak anında fiyat hesaplaması ve düşüşü sağlar.

---

## 🛠️ Kullanılan Teknolojiler ve Mimari Yaklaşım

### 📱 İstemci Katmanı (Android / Frontend)
* **Dil:** Kotlin
* **Mimari Desen:** MVVM (Model-View-ViewModel) ve Clean Architecture prensipleri
* **Asenkron Akış Yönetimi:** Kotlin Coroutines & StateFlow (Reaktif UI)
* **Ağ Katmanı:** Retrofit2, OkHttp (REST API haberleşmesi için)
* **Yerel Veritabanı:** Room Database (Offline-first yaklaşımı için)
* **Yapay Zeka:** Gemini AI API Entegrasyonu ve Camera capture yetenekleri
* **Görünüm Bağlama:** ViewBinding 

### 🖥️ Sunucu Katmanı (Spring Boot / Backend)
* **Framework:** Spring Boot (Kotlin / Java)
* **Veritabanı:** PostgreSQL (Kalıcı ve ilişkisel veri yönetimi)
* **ORM:** Spring Data JPA / Hibernate (JPA Entity ve Repository konfigürasyonları)
* **API Mimarisi:** REST Controller mimarisi ile uç nokta (endpoint) yönetimi

---

## ⚙️ Hızlı Kurulum

Proje, hem sunucu hem de istemci tarafının uyum içinde çalışması için tasarlanmıştır.

1. **Backend (Spring Boot) Başlatma:**
   Projedeki `BackendApplication` sınıfını çalıştırarak Tomcat sunucusunu `8080` portunda ayağa kaldırın. Proje içerisindeki REST Controller'lar ve JPA Repositories otomatik olarak derlenecektir.
   
2. **Android İstemcisini Başlatma:**
   Android Studio üzerinden projeyi açın. Eğer Spring Boot sunucusu yerel bilgisayarınızda çalışıyorsa, emülatörün sunucuya erişebilmesi için `ApiService` içerisindeki Base URL'in `http://10.0.2.2:8080/` olarak ayarlandığından emin olun ve projeyi derleyin.

 
