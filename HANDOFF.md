# Hueno — bàn giao để tiếp tục ở phiên khác

Viết ngày 12/11/2026, sau commit `f8cc577`. Mục đích: một phiên mới đọc file này là làm tiếp được, bắt đầu bằng việc thử trên điện thoại thật.

Cách dùng: mở phiên mới trong thư mục `Documents/hueno` và nói "đọc HANDOFF.md rồi làm tiếp phần thử trên máy thật".

## 1. Dự án là gì

Hueno là app Android nhận màu bằng camera, nói tên màu bằng từ dễ hiểu ("Dark blue") và cho biết kết quả đáng tin đến đâu. Người dùng chính: người mù màu; thứ hai: người so màu sơn, vải, mỹ phẩm.

| Mục | Giá trị |
|---|---|
| Thư mục | `/Users/balua/Documents/hueno` |
| Remote | `git@github.com:baluadev/Hueno-app.git` (riêng tư) |
| Nhánh | `main`, khớp `origin/main` tại `f8cc577` |
| Package | `com.hueno.coloridentifier` (chưa khóa, chờ tra nhãn hiệu) |
| Nền tảng | Kotlin native, Compose, Android tối thiểu 8.0 (API 26) |
| Ngôn ngữ app | Chỉ tiếng Anh (en-US) |
| Kiếm tiền | Miễn phí + quảng cáo; Pro mua một lần; xem video để có Pro 6 giờ |

Tài liệu trong repo:
- [product-plan.md](product-plan.md): kế hoạch sản phẩm, phạm vi v1 (mục 2), kiếm tiền (mục 3).
- [tech-architecture.md](tech-architecture.md): kiến trúc, bài thử kỹ thuật (mục 5), cấu hình Pro và quảng cáo (mục 7).
- [hueno-aso.md](hueno-aso.md): tên, từ khóa, listing Google Play.
- Ba file nghiên cứu: `color-picker-user-research.md`, `color-picker-competitive-brief.md`, `color-and-palette-deep-dive.md`.

## 2. Cách làm việc đã thống nhất với chủ dự án

- Trả lời bằng tiếng Việt.
- Làm trực tiếp trên nhánh `main` (đã được chọn khi hỏi về nhánh).
- Chỉ commit và đẩy khi được yêu cầu. Thông điệp commit bằng tiếng Anh.
- Không có ngân sách cho phỏng vấn người dùng; đánh giá dựa trên dữ liệu sẵn có.
- Báo cáo trung thực: nói rõ cái gì đã thử, cái gì chưa.

## 3. Build, test, chạy

Java mặc định của máy là bản 18, không dùng để build. Dùng Java 21 đi kèm Android Studio:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

```bash
./gradlew :core-color:test :camera:testDebugUnitTest :data:testDebugUnitTest :monetization:testDebugUnitTest :app:assembleDebug
```

```bash
./gradlew :app:installDebug
```

- `adb` nằm ở `~/Library/Android/sdk/platform-tools/adb` (không có trong PATH).
- Hiện có 47 unit test, tất cả qua.
- Máy ảo đã dùng: `Medium_Phone_API_35` (`emulator-5554`). Trên đó còn dữ liệu thử của app và một ảnh thử `/sdcard/Pictures/hueno_test.png`.
- Phiên bản thư viện: Gradle 8.13, AGP 8.13.2, Kotlin 2.2.20, CameraX 1.6.2, Room 2.8.4, Play Billing 9.1.0, Mobile Ads 25.5.0, UMP 4.0.0.

Lưu ý khi tự động hóa bằng `adb`:
- `uiautomator dump` hay treo nếu gọi dồn dập hoặc khi màn hình đang tắt. Đánh thức máy bằng `input keyevent WAKEUP` rồi `wm dismiss-keyguard` trước.
- `adb shell input text` gõ lặp ký tự vào ô nhập của Compose ("Wall" thành "Wallall").
- Đừng chạm theo tọa độ gần banner quảng cáo: một lần đã mở Chrome.

## 4. Cấu trúc mã

| Module | Vai trò |
|---|---|
| `:core-color` | Kotlin thuần. Toán màu, đặt tên hai tầng (`ColorNamer`), so màu CIEDE2000, hiệu chuẩn, độ tin cậy, làm mượt, `ReadingProcessor` |
| `:camera` | CameraX. `ColorCamera` (lấy mẫu vòng ngắm, khóa AE/AWB, hiệu chuẩn, hồ sơ), `RingSampler`, `CalibrationProfile` |
| `:data` | Room (phiên bản 3). Lịch sử màu, tên riêng, bộ sưu tập, ảnh thu nhỏ, `CaptureCsv` |
| `:monetization` | `ProRules`, `ProBilling`, `AdsController`, lớp gộp `Monetization` |
| `:app` | Giao diện Compose trong `ui/`: camera, result, photo, history, pro, settings, onboarding; `HuenoViewModel`, `HuenoApp` |

Điều hướng là tự viết trong `HuenoApp.kt`: ba tab, còn màn hình chi tiết, Pro và cài đặt được vẽ đè lên tab để phiên camera không bị ngắt.

## 5. Đã xong (chạy được trên máy ảo)

Toàn bộ F1–F13 ở mục 2 của kế hoạch đã có mã:
- Camera nhận màu trực tiếp, ba cỡ vùng ngắm, đèn flash.
- Tên màu hai tầng, độ tin cậy ba mức kèm gợi ý, đọc to.
- Đóng băng và di con trỏ trong ảnh đã đóng băng; chụp kèm ảnh thu nhỏ của vật.
- Hiệu chuẩn bằng vật trắng; hồ sơ hiệu chuẩn theo nơi dùng.
- So hai màu trực tiếp trên camera.
- Chọn màu từ ảnh, zoom bằng nút và bằng hai ngón.
- Lịch sử lưu bằng Room, đặt tên riêng, bộ sưu tập, bộ lọc, xóa, xuất CSV.
- Hướng dẫn lần đầu, icon app, màn hình cài đặt.
- Pro mua một lần, Pro tạm thời 6 giờ bằng video có thưởng, banner, đồng ý quảng cáo.

Giới hạn bản miễn phí (trong `ProRules`): 3 lần so màu mỗi ngày, 20 màu trong lịch sử, 1 hồ sơ hiệu chuẩn, không xuất CSV.

## 6. Vấn đề còn tồn tại

### A. Chưa kiểm chứng, rủi ro cao
1. **App chưa chạy lần nào trên điện thoại thật.** Mọi kết quả đến từ máy ảo với camera giả lập.
2. **Khóa phơi sáng và cân bằng trắng có giữ được không** là câu hỏi quyết định lời hứa "hiệu chuẩn". Chưa có câu trả lời.
3. **Hồ sơ hiệu chuẩn chưa chạy lần nào.** Camera máy ảo không có điều khiển thủ công nên phần lưu, dùng lại, xóa chỉ mới được biên dịch. Đây là mã rủi ro nhất.
4. **Mua Pro và khôi phục giao dịch chưa thử.** Cần app trên Play Console và sản phẩm `pro_lifetime`.
5. **Hộp thoại đồng ý quảng cáo chưa thử.** Đây là luồng gây 1 sao cho đối thủ. Có thể thử bằng thiết lập gỡ lỗi vùng EEA của UMP với mã thiết bị thử.
6. **Bản release chưa build lần nào.** Bật rút gọn mã nhưng chưa có cấu hình ký và chưa kiểm tra quy tắc giữ lớp.

### B. Lỗi hoặc thiếu sót đã biết trong mã
7. **Xoay màn hình làm mất trạng thái camera.** Chưa khóa hướng dọc; xoay máy sẽ khởi động lại camera, mất hiệu chuẩn và ảnh đóng băng. Đề xuất: khóa hướng dọc cho v1.
8. **Hiệu chuẩn có thể sai âm thầm sau khi app ra nền rồi quay lại.** Camera đóng và mở lại, nhưng app vẫn coi hiệu chuẩn còn hiệu lực. Cần thử; nhiều khả năng phải xóa hiệu chuẩn khi app dừng.
9. **Bật hoặc tắt đèn flash sau khi hiệu chuẩn** làm đổi ánh sáng nhưng app không hủy hiệu chuẩn.
10. **Hiệu chuẩn chấp nhận vật không trắng.** Trên máy ảo, bức tường màu be vẫn được nhận làm "trắng". Ngưỡng nằm ở `Calibration.MAX_GAIN_RATIO`.
11. **Vòng ngắm trực tiếp chỉ khớp gần đúng** với vùng thật sự lấy mẫu, do hình xem trước bị cắt còn khung phân tích thì không.
12. **Màu đọc khi đóng băng lấy từ ảnh xem trước**, khác đường xử lý với màu trực tiếp, nên có thể lệch nhẹ. Độ tin cậy dùng lại của khung lúc đóng băng.
13. **Banner nằm sát thanh điều hướng** ở màn hình History, dễ bấm nhầm. Cần xem lại theo chính sách đặt quảng cáo của AdMob.
14. **Banner tải lại mỗi lần mở màn hình chi tiết**, sinh nhiều yêu cầu quảng cáo.
15. **Từ chối quyền camera vĩnh viễn** thì không có lối dẫn sang cài đặt hệ thống.
16. **Không có phản hồi** khi bấm "Restore purchase", khi mua thành công, hay khi giao dịch đang chờ xử lý.
17. **Đọc to im lặng** nếu máy chưa sẵn sàng bộ đọc.
18. **Trạng thái điều hướng không được lưu** khi hệ thống đóng app trong nền.

### C. Chất lượng cần tinh chỉnh bằng dữ liệu thật
19. Ngưỡng chia họ màu trong `ColorNamer` là bản nháp, mới khớp 25 màu mẫu.
20. Bảng tên chi tiết chỉ có khoảng 110 tên màu CSS, nên đôi khi ra tên lạ (tường be ra "Sienna").
21. Ngưỡng độ tin cậy (`ConfidenceRater`) và ngưỡng so màu (`ColorDifference`) chưa chỉnh với camera thật.
22. Lần bấm xem video đầu tiên phụ thuộc thời gian chờ SDK 20 giây; chưa thử lại sau khi nới.

### D. Chưa thử trên máy ảo
23. Ghi file CSV thật, zoom bằng hai ngón, bỏ tên, đổi bộ sưu tập, xóa màu khi đang lọc.
24. Giới hạn 20 màu, banner ẩn khi có Pro, Pro tạm thời hết giờ.
25. TalkBack và chữ lớn. Listing trên cửa hàng đang ghi "Works with TalkBack and large text", phải kiểm trước khi đăng.
26. Xoay ảnh theo EXIF trên Android 8.0–8.1; máy tính bảng; màn hình nhỏ.

### E. Việc ngoài mã, cần chủ dự án
27. Tra nhãn hiệu và tên miền cho "Hueno", rồi khóa package ID.
28. Mã AdMob thật và sản phẩm Pro. App đang dùng mã thử nghiệm công khai của Google, không sinh doanh thu.
29. Email nhận góp ý (`HUENO_FEEDBACK_EMAIL`); đang trống nên nút bị ẩn.
30. Chính sách quyền riêng tư có đường dẫn công khai, và đường dẫn đó trong app. App xin quyền camera và có SDK quảng cáo.
31. Khai Data safety theo SDK quảng cáo; chụp ảnh cửa hàng từ bản build thật; video giới thiệu.

### F. Nhỏ
32. Câu giải thích khi so màu sinh bằng tiếng Anh trong `:core-color`.
33. Chưa nhúng phông Space Grotesk; app dùng phông hệ thống.
34. Lộ trình ngày tháng trong `product-plan.md` chưa cập nhật theo tiến độ thật.

## 7. Kế hoạch thử trên máy thật (việc đầu tiên của phiên sau)

### Chuẩn bị
- Điện thoại Android bật gỡ lỗi USB, cắm vào máy.
- Một tờ giấy trắng, vài vật màu rõ (đỏ, xanh lá, xanh dương, vàng, đen), nếu có thì hai sợi dây đỏ và xanh lá.
- Hai nguồn sáng: ánh sáng ban ngày và một đèn vàng trong nhà.

### Phiên làm việc tự làm được qua `adb`
- Kiểm tra `adb devices`, cài bản debug, cấp quyền camera, mở app.
- Chụp màn hình, đọc dòng số đo gỡ lỗi dưới tên màu: `ring`, `jitter`, `fps`, `first … ms`, `locked`, `profiles`.
- Đọc nhật ký để bắt crash thật của `com.hueno.coloridentifier`.

Phần chĩa máy vào vật và giấy trắng phải do người cầm máy làm.

### Các bài thử và tiêu chí đạt

| # | Bài thử | Cách làm | Đạt khi |
|---|---|---|---|
| 1 | Tốc độ | Mở app nguội, đọc `first … ms` và `fps` | Kết quả đầu dưới 3000 ms; fps từ 15 trở lên |
| 2 | Ổn định | Giữ yên trên một vật màu 5 giây | Tên họ màu không đổi; `jitter` gần 0 |
| 3 | Đặt tên | Chĩa lần lượt vào các vật màu rõ, ánh sáng ban ngày | Đúng họ màu với hầu hết các vật |
| 4 | Khóa có giữ | Hiệu chuẩn trên giấy trắng, lia sang vật màu, quay lại giấy | Giấy vẫn đọc "White"; `locked yes` |
| 5 | Hiệu chuẩn có ích | Dưới đèn vàng, đọc giấy trắng trước và sau hiệu chuẩn | Trước: ngả vàng hoặc xám; sau: "White" |
| 6 | Từ chối vật không trắng | Hiệu chuẩn trên vật màu rõ | App báo không giống giấy trắng |
| 7 | Hồ sơ | Xem `profiles yes/no`. Nếu yes: hiệu chuẩn, lưu, xóa hiệu chuẩn, dùng lại hồ sơ | Giấy trắng đọc "White" sau khi dùng lại, cùng ánh sáng |
| 8 | Ra nền rồi quay lại | Hiệu chuẩn, bấm Home, mở lại app | Ghi lại: giấy còn đọc "White" không (vấn đề 8) |
| 9 | Xoay máy | Hiệu chuẩn rồi xoay ngang | Ghi lại hành vi (vấn đề 7) |
| 10 | Đóng băng | Đóng băng, chạm các điểm khác nhau | Màu đổi theo điểm chạm, không crash |
| 11 | So hai màu | A là dây đỏ, B là dây xanh lá | "Different", câu giải thích đúng |
| 12 | Ảnh | Chọn ảnh thật, chụm hai ngón để zoom | Zoom mượt, vòng ngắm không nhảy |
| 13 | Đọc to | Bấm nút loa | Nghe đúng tên màu |
| 14 | TalkBack | Bật TalkBack, đi qua màn hình camera | Mọi nút có tên; đọc được tên màu |
| 15 | Quảng cáo | Mở app lần hai, vào History; xem video Pro | Banner thử hiện; xem xong được Pro 6 giờ, banner ẩn |

Nếu bài 4 không đạt, lời hứa "hiệu chuẩn" phải thu hẹp trước khi làm gì khác (xem mục 5 của `tech-architecture.md`).

### Sau khi thử
Ưu tiên sửa theo thứ tự: bất cứ crash nào, rồi bài 4 và 5, rồi vấn đề 7–10 ở mục 6, rồi tinh chỉnh ngưỡng (vấn đề 19–21) bằng số đo thu được.

## 8. Việc mã có thể làm mà không cần thiết bị

- Khóa hướng dọc (vấn đề 7).
- Xóa hiệu chuẩn khi app dừng và khi đổi đèn flash (vấn đề 8, 9).
- Siết ngưỡng vật trắng (vấn đề 10), tốt nhất sau khi có số đo thật.
- Lối sang cài đặt hệ thống khi bị từ chối quyền camera (vấn đề 15).
- Phản hồi cho mua, khôi phục, đọc to (vấn đề 16, 17).
- Cấu hình ký và build thử bản release (vấn đề 6).
- Thiết lập gỡ lỗi UMP để thử hộp thoại đồng ý (vấn đề 5).

## 9. Cập nhật sau khi viết bàn giao

### Splash và hiệu ứng mở đầu (12/11/2026)
- Splash hệ thống: nền tối và logo, qua thư viện `core-splashscreen`. Theme khởi động là `Theme.Hueno.Starting`, icon là `drawable/ic_splash.xml`.
- Hiệu ứng mở đầu: `ui/splash/SplashOverlay.kt`, dài khoảng 1,3 giây cộng 0,28 giây mờ dần. Vòng ngắm xoay và tách thành bốn cung, tâm đổi màu như đang dò màu, vòng khép lại, một vòng sáng tỏa ra, rồi tên app hiện lên.
- App bên dưới chỉ được khởi động sau khi logo đã hoàn chỉnh (hằng số `REVEAL`), không khởi động cùng lúc với hiệu ứng. Lý do: khởi động camera và quảng cáo cùng lúc làm hiệu ứng bị giật.
- Tỷ lệ logo trong `ic_splash.xml` (0.70) phải bằng `MARK_SCALE` trong `SplashOverlay.kt`, nếu không logo sẽ nhảy kích thước khi chuyển từ splash hệ thống sang hiệu ứng.
- Tắt hiệu ứng trong cài đặt hệ thống thì app chỉ hiện logo tĩnh một lúc ngắn.

### Vấn đề mới cần xem trên máy thật
35. **App khởi động chặn luồng giao diện.** Trên máy ảo đang quá tải, hiệu ứng đứng lại khoảng 1,5 giây lúc app bắt đầu khởi động. Cần đo trên máy thật; nếu vẫn thấy khựng thì chuyển phần khởi tạo trong `HuenoViewModel` (Billing, Room, quảng cáo) ra khỏi luồng chính.
36. **Splash làm kết quả màu đầu tiên đến muộn hơn khoảng 0,9 giây** so với trước. Cần kiểm lại tiêu chí "thấy màu trong 3 giây" ở bài thử 1; số `first … ms` trên màn hình chỉ tính từ lúc camera bắt đầu, không tính splash.
37. Trên Android dưới 12, splash hệ thống vẽ icon ở kích thước khác, nên có thể có một nhịp nhảy kích thước khi vào hiệu ứng. Chưa thử.

Thêm vào kế hoạch thử: quan sát lần mở app nguội, xem hiệu ứng có mượt không và mất bao lâu từ lúc chạm icon đến lúc thấy tên màu.
