# اپلیکیشن اندروید – سامانه آمار و حقوق پوینده الکترونیک

**نسخه:** 1.0.0  
**Package:** `ir.pooyande.amar`  
**Min SDK:** 24 | **Target SDK:** 34

---

## ساخت APK با GitHub Actions (روش پیشنهادی)

### مرحله ۱ – ساخت ریپازیتوری

1. در GitHub یک ریپو جدید بسازید (مثلاً `amar-android`).
2. محتوای پوشه `android_app` را به عنوان ریشه ریپو آپلود/پوش کنید.

```bash
cd android_app
git init
git add .
git commit -m "Initial commit - Amar Pooyande Android app"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/amar-android.git
git push -u origin main
```

### مرحله ۲ – دانلود APK

1. به تب **Actions** در ریپو بروید.
2. workflow با نام **Build APK** به‌صورت خودکار اجرا می‌شود.
3. روی آخرین اجرای موفق کلیک کنید.
4. در بخش **Artifacts** فایل **Amar-Pooyande-APK** را دانلود کنید.
5. ZIP را باز کنید → داخلش `app-debug.apk` است.

> برای اجرای دستی: Actions → Build APK → **Run workflow**

### مرحله ۳ – انتشار نسخه (اختیاری)

با ساخت تگ، Release خودکار با APK ساخته می‌شود:

```bash
git tag v1.0.0
git push origin v1.0.0
```

سپس در تب **Releases** فایل APK قابل دانلود خواهد بود.

---

## پیش‌نیاز سمت سرور

1. فایل `mobile_api.php` را در ریشه سایت آپلود کنید:
   ```
   https://amar.pooyande.ir/mobile_api.php
   ```

2. یک‌بار این SQL را اجرا کنید:
   ```sql
   ALTER TABLE users 
   ADD COLUMN api_token VARCHAR(64) NULL DEFAULT NULL;
   CREATE INDEX idx_api_token ON users (api_token);
   ```

---

## قابلیت‌ها

| قابلیت | توضیح |
|--------|--------|
| ورود امن | توکن API |
| نقش‌های کاربر | انتخاب نقش فعال |
| جستجوی زنده | شماره کلاسه |
| تخصیص پرونده | نقش تفکیک و مراحل بعد |
| ثبت/اصلاح اوراق | برای نقش‌های دارای requires_page_count |

---

## تغییر آدرس سرور

`app/src/main/java/ir/pooyande/amar/util/Constants.kt`

```kotlin
const val BASE_URL = "https://amar.pooyande.ir/"
```

---

## ساخت محلی (اختیاری)

```bash
./gradlew assembleDebug
# خروجی: app/build/outputs/apk/debug/app-debug.apk
```

یا با Android Studio: **Build → Build APK(s)**
"# UPDATE-APP-MOBILE" 
