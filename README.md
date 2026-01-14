# Sprint Poker - Planning Poker Uygulaması

Spring Boot tabanlı, WebSocket kullanan, gerçek zamanlı Sprint Poker (Planning Poker) uygulaması.

## Özellikler

- 🎯 Fibonacci serisi kartları (0, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, ?, ☕)
- 🔄 WebSocket ile gerçek zamanlı iletişim
- 👥 Çoklu oyuncu desteği
- 📊 Oylama sonuçlarını görüntüleme
- 🎨 Modern ve şık tasarım
- 💾 In-memory storage (veritabanı gerektirmez)

## Gereksinimler

- Java 17 veya üzeri
- Maven 3.6+

## Kurulum ve Çalıştırma

1. Projeyi klonlayın veya indirin
2. Terminalde proje dizinine gidin
3. Uygulamayı çalıştırın:

```bash
mvn spring-boot:run
```

4. Tarayıcınızda `http://localhost:8080` adresine gidin

## Kullanım

1. **Oda Oluşturma:**
   - İsminizi girin
   - "Oda Oluştur" butonuna tıklayın
   - Oda adını girin

2. **Odaya Katılma:**
   - İsminizi girin
   - "Odaya Katıl" butonuna tıklayın
   - Oda ID'sini girin

3. **Oylama:**
   - Host olarak story'yi girin
   - Kartlarınızdan birini seçin
   - Tüm oyuncular oy verdikten sonra "Sonuçları Göster" butonuna tıklayın
   - Yeni bir oylama için "Yeni Oylama" butonuna tıklayın

## Teknolojiler

- Spring Boot 3.2.0
- WebSocket (STOMP)
- Thymeleaf
- HTML5, CSS3, JavaScript
- Lombok

## Lisans

Bu proje eğitim amaçlıdır.
