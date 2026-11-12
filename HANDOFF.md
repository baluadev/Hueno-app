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

### Sửa mã và lần thử đầu trên máy thật (12/11/2026, chưa commit)

Thiết bị: Samsung Galaxy Z Flip4 (`SM-F721B`, mã `adb` `R5CTC0XWBTY`), Android 16 (API 36). Máy có hai màn hình, nên chụp màn hình phải chỉ định màn hình chính: `adb exec-out screencap -p -d 4630946784334733698`.

Thư mục làm việc mới là `/Users/tampham/Documents/github/hueno-app`. File `local.properties` (đã nằm trong `.gitignore`) trỏ tới `~/Library/Android/sdk`.

Mã đã sửa:
- Khóa hướng dọc trong manifest (vấn đề 7).
- Xóa hiệu chuẩn khi app ra nền (`ON_STOP`) và khi bật hoặc tắt đèn flash, kèm câu báo cho người dùng (vấn đề 8, 9). `ColorCamera.clearCalibration(reason)`, hai giá trị mới `CameraIssue.CALIBRATION_CLEARED_*`. Đèn flash trong giao diện cũng tự tắt khi app ra nền.
- Bị từ chối quyền camera vĩnh viễn thì nút đổi thành "Open settings", mở trang cài đặt của app; quay lại app sẽ tự nhận quyền (vấn đề 15).
- `BillingEvent` trong `:monetization`: báo mua xong, đang chờ thanh toán, mua lỗi, khôi phục được, không có gì để khôi phục, không kết nối được Play. Hiện trên màn hình Pro (vùng TalkBack tự đọc), hoặc bằng toast nếu màn hình Pro đã đóng (vấn đề 16).
- `Speaker` giữ lại câu được yêu cầu khi bộ đọc chưa sẵn sàng và đọc ngay khi sẵn sàng; báo bằng toast nếu máy không đọc được tiếng Anh (vấn đề 17).
- Ba lỗi giao diện thấy trên máy thật: tiêu đề "Camera access" màu đen trên nền tối; nhãn "Good reading · Calibrated" bị nút S/M/L che; nút "Clear calibration" bị xuống dòng và mất chữ. Đã sửa cả ba.

Kết quả thử trên Flip4 (qua `adb`, chĩa vào bức tường trong phòng, chưa có giấy trắng):
| Bài | Kết quả |
|---|---|
| 1 Tốc độ | Activity khởi động 636 ms; `first` 456–565 ms; 30 fps. Đạt. |
| Hồ sơ | `profiles yes`. Lưu hồ sơ "Desk", mở lại app, dùng hồ sơ: tường đọc White #F2F1F1, `locked yes`, khớp lúc vừa hiệu chuẩn (#F1F2F2). Đạt. |
| 6 Vật không trắng | Lần đầu không đạt: tường xám nhạt (#B6B3AE) được nhận làm trắng. Sau khi siết ngưỡng (xem dưới): cốc xanh nhạt, điều khiển có chữ, gỗ đều bị từ chối; tường xám vẫn được nhận (không thể phân biệt bằng số đo). |
| 8 Ra nền | Sau sửa: hiệu chuẩn bị xóa, hiện câu báo, `locked no`. |
| 9 Xoay | Ép hệ thống xoay ngang, app vẫn ở `ROTATION_0`. |
| Flash | Bật đèn flash xóa hiệu chuẩn và hiện câu báo. |
| 13 Đọc to | `com.google.android.tts` phát âm thanh loại `SPEECH`. Chưa nghe bằng tai. |
| Quyền camera | Từ chối hẳn (đặt bằng `pm`), sau đó "Open settings", rồi cấp quyền và quay lại: camera tự bật. |
| Khôi phục | "No Pro purchase was found for this Google account." Nút mua báo không có sản phẩm, đúng vì chưa có `pro_lifetime` trên Play Console. |
| Crash | Không có crash nào trong cả phiên. |

Chưa thử: bài 2–5, 10–12, 14, 15 (cần người cầm máy, giấy trắng, đèn vàng); mua thật; hộp thoại đồng ý quảng cáo. Ở lần mở đầu, hộp thoại quyền lần thứ hai đổi bố cục nên `adb` chạm nhầm nút cho phép; khi tự động hóa, hãy đọc tọa độ bằng `uiautomator dump`, đừng dùng tọa độ cũ.

### Siết ngưỡng vật trắng (12/11/2026, chưa commit)

Mỗi lần bấm Calibrate, `ColorCamera` ghi một dòng `calibrate ref r= g= b= ratio= ringLum= sceneLum= spread= clipped=` vào logcat (thẻ `ColorCamera`). Đọc bằng `adb logcat -d | grep "calibrate ref"`.

Số đo trên Flip4, ánh sáng trong phòng (ratio = kênh mạnh nhất / kênh yếu nhất, độ loang = spread / ringLum):
| Vật | ratio | ringLum | độ loang |
|---|---|---|---|
| Giấy trắng, cân bằng trắng đã tự chỉnh trên giấy | 1,01–1,13 | 0,42–0,53 | 0,01–0,02 |
| Giấy trắng, gần đèn vàng (ngả vàng rõ trên màn hình) | 1,03–1,37 | 0,59–0,80 | 0,01 |
| Giấy trắng, sát đèn vàng (cháy sáng, kênh đỏ = 1,0) | ≥ 1,31 | 0,92 | 0,02 |
| Tường xám nhạt | 1,08 | 0,52 | 0,02 |
| Cốc xanh nhạt, cân bằng trắng tự chỉnh trên cốc | 1,85–1,90 | 0,26–0,30 | 0,06–0,15 |
| Điều khiển trắng có chữ | 1,16 | 0,59 | 0,24 |
| Gỗ | 13,6 | 0,28 | 0,15 |

Đã sửa:
- `Calibration.MAX_GAIN_RATIO` từ 3,0 xuống 1,6, gần điểm giữa giấy dưới đèn vàng (1,37) và cốc xanh (1,85).
- Thêm `MAX_RELATIVE_SPREAD = 0,08`: từ chối vòng ngắm có chữ, vân, mép giấy hoặc nhiều vật.
- **Lỗi "Calibrate again"**: trước đây app hiệu chuẩn lại trong khi vẫn giữ khóa phơi sáng và cân bằng trắng của lần trước, nên cùng tờ giấy đo ra ratio 1,45 thay vì 1,1. Giờ `ColorCamera.calibrate()` mở khóa, chờ khoảng 30 khung (1 giây) cho camera tự chỉnh, rồi mới khóa và đo. Hiệu chuẩn cũ bị bỏ ngay khi bấm.
- Câu hướng dẫn hiệu chuẩn nói rõ phải dùng giấy trắng thật, vì tường xám hoặc màu kem cũng trông trắng với camera. Câu báo lỗi nhắc không để chữ, mép giấy hay bóng trong vòng ngắm.
- Unit test mới dựng từ số đo thật: tổng cộng 49 test, tất cả qua.

Giới hạn còn lại:
- **Tường xám, tường màu kem vẫn được nhận làm trắng.** Camera tự cân bằng trắng và tự chỉnh sáng nên chúng giống hệt giấy; không ngưỡng nào tách được. Chỉ có thể dặn người dùng.
- Lý do từ chối được tách riêng (`CalibrationProblem`): quá sáng, quá tối, không đều, có màu. Mỗi lý do có câu hướng dẫn riêng. Lý do: giấy sát đèn vàng bị cháy sáng, trước đây app báo "không giống giấy trắng" nên người dùng không biết phải dời đèn ra.
- Đã đo dưới đèn vàng của chủ dự án: giấy ngả vàng rõ ra 1,37 và được chấp nhận. Một bóng vàng hơn (khoảng 2700K, đặt rất gần) có thể vượt 1,6; nếu người dùng báo không hiệu chuẩn được dưới đèn ngủ thì xem lại con số này. Hai test cũ dùng mẫu "giấy ấm" ratio 1,56–1,6 đã được đổi sang 1,33.
- Câu báo "quá sáng" và "quá tối" mới chỉ kiểm bằng unit test, chưa hiện lên trên máy thật.

### Các bài thử còn lại trên Flip4 (12/11/2026 buổi tối, đèn trần, chưa commit)

Chủ dự án cầm máy, tôi điều khiển qua `adb`. Script hỗ trợ (để ở `/tmp/hueno`, không nằm trong repo): chụp màn hình chính, chạm theo chữ trên màn hình, đọc tên màu, bấm Calibrate và đọc nhật ký.

| Bài | Kết quả |
|---|---|
| 2 Ổn định | Đạt. Đứng yên trên vật thì tên không đổi, jitter ≤ 0,002. |
| 3 Đặt tên (chưa hiệu chuẩn) | Xanh lá, xanh dương, vàng, đen đúng họ màu. Quả bông đỏ thẫm (#4E0E08) bị gọi là "Dark brown", **đã sửa** (xem dưới). Đồ chơi vàng chanh ra "Dark yellow": đúng họ nhưng tối hơn mắt thấy. |
| 4 Khóa có giữ | **Đạt.** Ghi mỗi giây trong 16 giây sau khi hiệu chuẩn: thời gian phơi sáng, ISO, hệ số cân bằng trắng không đổi; `CONTROL_AWB_STATE` và `CONTROL_AE_STATE` đều ở trạng thái LOCKED. Lia sang vật khác rồi quay lại, giấy vẫn đọc "White". Sau hiệu chuẩn, quả bông đọc "Red". |
| 10 Đóng băng | Đạt. Chạm các điểm khác nhau thì màu đổi theo, không crash. |
| 11 So hai màu | Đạt về logic: đỏ với cốc xám xanh ra "Different" kèm câu giải thích đúng. Chữ "Different" bị bẻ giữa từ, **đã sửa** bố cục. |
| 12 Ảnh | Đạt. Kéo vòng ngắm thì đọc đúng theo vùng; nút zoom giữ vòng ngắm đúng điểm. Zoom bằng hai ngón do chủ dự án thử, chưa có ghi nhận. |
| 14 TalkBack | Không bật TalkBack trên máy cá nhân. Kiểm cây trợ năng ở ba tab: mọi nút bấm của app đều có tên. Chỉ banner AdMob (WebView của bên thứ ba) không có tên. Chưa nghe TalkBack đọc thực tế. |
| 15 Quảng cáo | Đạt. Banner thử hiện ở History. Xem video thử thì có Pro 6 giờ và banner ẩn. Máy đang có Pro tạm thời đến khoảng 2:30 sáng 13/11. |

Lỗi tìm thấy và đã sửa trong lượt này:
- **Hiệu chuẩn thất bại vẫn để camera khóa.** Phơi sáng và cân bằng trắng bị giữ theo vật vừa bị từ chối, trong khi app không có hiệu chuẩn nào. Mọi màu đọc sau đó đều sai mà không có dấu hiệu gì. Giờ hiệu chuẩn thất bại sẽ mở khóa.
- **Đỏ đậm bị gọi là nâu.** `ColorNamer` xếp màu có sắc độ 12–38° vào nâu khi L < 0,50 và C < 0,13. Màu đỏ càng tối thì C càng nhỏ, nên giờ dùng tỷ lệ C/L < 0,21: các màu nâu ở mức 0,13–0,17, đỏ tối ở mức 0,24–0,41. Đã thêm 5 mẫu vào bộ tên chuẩn.
- Câu "Calibration was cleared…" tự ẩn sau 8 giây thay vì hiện mãi.
- Khung so màu: dòng kết quả nằm riêng và trải hết chiều ngang.
- Ghi nhật ký `lock raw …` mỗi giây khi đang khóa (phơi sáng, ISO, hệ số cân bằng trắng, trạng thái khóa), để kiểm khóa trên các máy khác.

Sửa thêm theo yêu cầu (cùng tối 12/11):
- Nút đóng bảng hiệu chuẩn chuyển thành nút X ("Close calibration") cạnh tiêu đề, nên luôn nhìn thấy dù có bao nhiêu hồ sơ đã lưu. Đã kiểm trên Flip4.
- So màu: khi kết luận là "Close" mà hai màu nằm hai bên ranh giới họ màu, câu giải thích nói "Both are close to gray. B leans a little toward blue…" thay vì "A is gray. B is blue". Có unit test. Đã kiểm trên Flip4: di vòng ngắm trên thân cốc ra "Close" kèm "Both are close to blue. B leans a little toward gray."; lúc vòng ngắm lệch hẳn ra nền thì ra "Different" kèm "A is blue. B is gray…", đúng như mong đợi.

Ghi nhận, chưa sửa:
- Ánh sáng hỗn hợp: hiệu chuẩn gần đèn vàng rồi đọc dưới đèn trần thì giấy ngả xanh. Đúng về vật lý, nhưng nên dặn người dùng hiệu chuẩn ngay tại chỗ sẽ đọc màu.
- Chưa thử dưới ánh sáng ban ngày (hẹn làm ngày 13/11).

### Đồng bộ giao diện (12/11/2026 tối, chưa commit)

Chủ dự án chọn: **theo cài đặt sáng/tối của hệ thống, riêng màn camera luôn tối.**

Hệ thống thiết kế trong `ui/Theme.kt`:
- `HuenoColors` giờ là token theo vai trò (`Background`, `Surface`, `Line`, `Text`, `TextMuted`, `Action`, `OnAction`, `Link`), đọc từ bộ màu sáng hoặc tối của `HuenoTheme` gần nhất. `HuenoTheme()` mặc định theo hệ thống; `HuenoTheme(dark = true)` để ép tối.
- Nền sáng đổi từ kem (#F6F4EF) sang gần trung tính (#F3F3F1), để nền không làm ấm màu mẫu bên cạnh.
- `Link` là màu riêng cho chữ dạng liên kết: xanh đậm trên nền sáng, xanh nhạt trên nền tối, để đủ tương phản.
- `HuenoBrand`: màu cố định cho splash và cho vòng tròn vẽ đè lên ảnh camera.
- `HuenoType`: 7 cỡ chữ (32/26/20/16/14/13/12), thay cho 17 cỡ trước đây. `HuenoShapes`: 3 cỡ bo góc (8/16/24). `HuenoSize`: lề màn hình 20dp, nút chính cao 56dp, vùng chạm 48dp.

Thành phần dùng chung trong `ui/Components.kt`: `HuenoPrimaryButton`, `HuenoSecondaryButton`, `OverlayHeader` (nút ← và tiêu đề cho các màn phủ), `ScreenTitle`, `TabTitle`, `relativeTime`.

Thay đổi theo màn hình:
- Ba tab có cùng thanh tiêu đề (cỡ Title, cao 64dp). Tab Photo khi chưa có ảnh đặt nội dung từ trên xuống thay vì giữa màn hình, và nút "Choose a photo" là nút chính màu xanh.
- Màn chi tiết màu, Settings, Pro cùng dùng nút ← (Pro trước đây dùng ✕). Settings đặt tiêu đề trên thanh, như màn chi tiết màu; Pro giữ tiêu đề lớn vì là trang giới thiệu.
- Mọi nút chính và nút phụ dùng hai kiểu chung. Thẻ "Use Pro for 6 hours" dùng viền mảnh như các thẻ khác, thay cho viền đen dày.
- Nút đọc to ở màn chi tiết màu giống nút ở camera: nền bề mặt, viền mảnh.
- History hiện thời gian bằng tiếng Anh ("59 min ago"). Trước đây dùng `DateUtils` nên hiện theo ngôn ngữ máy ("50 phút trước").

Đã kiểm trên Flip4 ở cả chế độ sáng và tối (`adb shell cmd uimode night yes|no`): camera luôn tối; các màn còn lại đổi theo hệ thống; không crash. 50 unit test qua. Chưa kiểm: màn hướng dẫn và các hộp thoại ở chế độ tối, chữ cỡ lớn.

Chỉnh thêm theo yêu cầu (cùng tối 12/11):
- Logo app (`hueno_mark`, 28dp, chỉ để trang trí) đứng trước tiêu đề trên thanh của tab Camera và tab Photo (`TabTitle(showLogo = true)`).
- **Chuyển tab không còn chớp nền.** `HuenoTheme(darkness: Float)` nội suy từng màu giữa bộ sáng và bộ tối. `HuenoApp` cho `darkness` chạy trong 280ms khi đổi giữa camera (luôn tối) và tab sáng, và nội dung tab mờ dần sang nhau (`Crossfade`). Đã quay màn hình trên Flip4: tối → xám → sáng trong khoảng 200ms. Hiệu ứng chỉ cập nhật khoảng 4 bước vì việc đóng camera chiếm luồng giao diện; muốn mượt hơn thì phải chuyển `ColorCamera.shutdown()` ra khỏi luồng chính.

Lưu ý khi tự động hóa bằng `adb` trên Flip4: nút chụp nằm ở y ≈ 2140, còn thanh tab ở y ≈ 2420. Chạm theo tọa độ tính từ ảnh đã thu nhỏ từng bấm nhầm nút chụp và lưu màu thừa vào History (đã xóa những màu chắc chắn do lệnh tạo ra). Luôn chạm theo chữ hoặc theo `uiautomator dump`.

### Thay bộ icon (12/11/2026 tối, chưa commit)

Chủ dự án gửi file Figma "Free UI Icons - Open Source Vector Icon Set (svg)". Đó là **Streamline Core Line Free** của Webalys, giấy phép **CC BY 4.0**: dùng thương mại được, nhưng phải ghi công kèm đường dẫn tới streamlinehq.com. Nguồn SVG chính thức: https://github.com/webalys-hq/streamline-vectors (thư mục `core/line`).

- `ui/HuenoIcons.kt` được tạo lại từ SVG gốc, giữ nguyên các đường vẽ, khung 14×14, nét dày 1, đầu nét tròn (khoảng 1,7dp ở cỡ 24dp; trước đây là 2dp).
- Icon tương ứng: Camera `camera-1`, Photo `landscape-2`, History `circle-clock`, Speak `volume-level-high`, Copy `copy-paste`, Torch `flash-1`, Calibrate `target`, Pause `button-pause-2`, Play `button-play`, Close `delete-1`, Check `check`, NotEqual `not-equal-sign`, Trash `recycle-bin-2`, Settings `cog`, Alert `warning-triangle`, Back là `arrow-up-1` xoay sang trái.
- Compare (hai vòng tròn) và Approx (≈) không có trong bộ miễn phí, nên được vẽ lại theo cùng phong cách.
- Ghi công nằm trong Settings, dưới số phiên bản: "Icons: Streamline Core Line Free, CC BY 4.0 (streamlinehq.com)", bấm vào thì mở streamlinehq.com. **Không được bỏ dòng này** khi còn dùng bộ icon. Nên thêm cả vào trang chính sách hoặc giới thiệu khi có.
- Đã kiểm trên Flip4: Camera, chi tiết màu, Settings.
