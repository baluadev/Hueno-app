# Hueno — kiến trúc kỹ thuật (Kotlin native)

Ngày lập: 12/11/2026. Thuộc [kế hoạch sản phẩm](product-plan.md).

**Đã chốt:** Android native bằng Kotlin. iOS chưa quyết định, có thể làm sau khi đo được hiệu quả trên Android.

Tên thư viện và API dưới đây là theo hiểu biết của mình, chưa đối chiếu với tài liệu phiên bản mới nhất. Số phiên bản cụ thể và yêu cầu target SDK của Play cần kiểm tra khi tạo project.

## 1. Công nghệ

| Phần | Lựa chọn | Lý do |
|---|---|---|
| Ngôn ngữ | Kotlin, coroutines + Flow | Chuẩn hiện nay của Android |
| Giao diện | Jetpack Compose + Material 3 | 8 màn hình đơn giản, ít mã, hỗ trợ trợ năng sẵn |
| Camera | CameraX (Preview + ImageAnalysis), kèm Camera2 interop | CameraX lo khác biệt giữa các máy; interop để khóa phơi sáng và cân bằng trắng |
| Lưu lịch sử | Room | Danh sách màu, bộ sưu tập, hồ sơ hiệu chuẩn |
| Lưu cài đặt | DataStore | Cài đặt, hạn Pro tạm thời, bộ đếm so màu trong ngày |
| Chọn ảnh | Photo Picker của hệ thống | Không cần xin quyền bộ nhớ |
| Đọc to | TextToSpeech của Android | Không cần thư viện ngoài |
| Quảng cáo | Google Mobile Ads SDK (banner, rewarded) | — |
| Đồng ý quảng cáo | User Messaging Platform của Google | Đi cùng SDK quảng cáo |
| Mua Pro | Google Play Billing Library | Sản phẩm mua một lần |
| Báo lỗi | Android vitals trong Play Console | Không thêm SDK thu dữ liệu |
| Kiểm thử | JUnit cho phần màu, Compose UI test cho luồng chính | — |

Không dùng ở v1: máy chủ, tài khoản, Firebase, thư viện DI lớn. Truyền phụ thuộc bằng tay là đủ cho quy mô này.

**Android tối thiểu:** đề xuất Android 8.0 (API 26). Đây là đề xuất để giảm số máy cũ phải thử; nếu muốn phủ rộng hơn có thể hạ xuống, đổi lại phải thử thêm máy.

## 2. Cấu trúc module

| Module | Chứa gì | Phụ thuộc Android |
|---|---|---|
| `:core-color` | Chuyển đổi không gian màu, đặt tên màu, so hai màu, tính độ tin cậy, hệ số hiệu chuẩn | Không |
| `:camera` | CameraX, khóa phơi sáng/cân bằng trắng, lấy mẫu vùng, làm mượt | Có |
| `:data` | Room, DataStore, xuất CSV | Có |
| `:monetization` | Quảng cáo, đồng ý, mua Pro, Pro tạm thời | Có |
| `:app` | Màn hình Compose, điều hướng, ViewModel | Có |

`:core-color` là Kotlin thuần, không đụng tới Android. Nó chứa toàn bộ "chất xám" của app nên có hai lợi ích:
- Kiểm thử bằng unit test thường, chạy nhanh, không cần máy.
- Nếu sau này làm iOS, module này chuyển sang Kotlin Multiplatform để dùng lại, chỉ phải viết mới phần camera và giao diện.

## 3. Đường đi của một khung hình

1. **Lấy khung hình:** ImageAnalysis, chỉ giữ khung mới nhất, độ phân giải vừa phải (không cần ảnh lớn để lấy màu).
2. **Cắt vùng ngắm:** lấy các điểm ảnh trong vòng tròn giữa màn hình theo cỡ S, M, L.
3. **Lấy trung bình:** đổi sang ánh sáng tuyến tính rồi mới tính trung bình, sau đó đổi lại. Tính trung bình trực tiếp trên giá trị sRGB sẽ cho màu tối hơn thực tế.
4. **Áp hệ số hiệu chuẩn:** nhân từng kênh với hệ số đã lưu từ lần chĩa vào giấy trắng.
5. **Làm mượt theo thời gian:** trung vị hoặc trung bình trượt qua khoảng 8 khung gần nhất, để tên màu không nhảy.
6. **Đặt tên:** đổi sang OKLCH, tra họ màu và sắc độ, rồi tìm tên chi tiết gần nhất.
7. **Chấm độ tin cậy:** từ độ sáng, độ đồng đều trong vùng ngắm, độ ổn định giữa các khung, có bị cháy sáng không, đã hiệu chuẩn chưa.
8. **Phát kết quả:** một StateFlow chứa màu, hai tầng tên, mã hex, độ tin cậy. Giới hạn khoảng 10 lần mỗi giây; giao diện không cần nhanh hơn.

Bước 2–7 chạy trên luồng nền. Giao diện chỉ nhận kết quả cuối, không bao giờ nhận khung hình.

## 4. Các phần cần thiết kế kỹ

### Khóa phơi sáng và cân bằng trắng
- Dùng Camera2 interop để đặt khóa tự động phơi sáng và khóa tự động cân bằng trắng trên phiên CameraX.
- Khi chưa hiệu chuẩn: để camera tự động, và độ tin cậy tối đa chỉ ở mức "tạm".
- Khi hiệu chuẩn: chờ camera ổn định trên tờ giấy trắng, khóa cả hai, rồi đo màu tờ giấy để tính hệ số từng kênh. Cách này không cần điều khiển cân bằng trắng thủ công, vốn mỗi máy hỗ trợ một kiểu.
- Một số máy có thể không tôn trọng lệnh khóa. Phải phát hiện được (màu giấy trắng vẫn trôi sau khi khóa) và hạ độ tin cậy thay vì báo sai.

### Đặt tên màu
- Bảng họ màu là dữ liệu, không viết cứng trong mã: mỗi họ màu là một vùng theo sắc độ, độ sáng, độ rực trong OKLCH.
- Màu ít rực được xét trước để ra đen, trắng, xám, tránh lỗi "cái gì cũng là xám/bạc" mà người dùng chê ở app khác.
- Tên chi tiết lấy từ một bảng tên màu có giấy phép dùng tự do; cần kiểm tra giấy phép của nguồn trước khi đưa vào.
- Bộ kiểm thử: một danh sách vài trăm màu kèm tên mong đợi, chạy mỗi lần đổi bảng.

### So hai màu
- Tính khoảng cách màu theo CIEDE2000 để ra ba mức giống, gần giống, khác.
- Câu giải thích ("B xanh lá và sáng hơn một chút") sinh từ chênh lệch sắc độ, độ sáng, độ rực.
- Ngưỡng ba mức cần chỉnh bằng thử nghiệm thực tế với camera, không lấy thẳng ngưỡng trong sách.

### Pro và Pro tạm thời
- Một nguồn sự thật duy nhất: `isPro = đã mua || hạn Pro tạm thời > hiện tại`. Mọi nơi (banner, giới hạn) chỉ đọc giá trị này.
- Hạn Pro tạm thời lưu cả giờ hệ thống và thời gian chạy của máy lúc cấp, để chỉnh đồng hồ không gia hạn được.
- Trạng thái mua lấy từ Play Billing mỗi lần mở app; mua xong là banner biến mất ngay trong phiên đó.

### Quảng cáo và đồng ý
- Phiên đầu tiên không khởi tạo SDK quảng cáo.
- Từ phiên thứ hai: hỏi đồng ý nếu vùng đó yêu cầu, rồi mới khởi tạo SDK, và chỉ sau khi camera đã chạy.
- Khung banner có chiều cao cố định, để trống khi không có quảng cáo, không làm xô lệch bố cục.
- Mọi lỗi của SDK quảng cáo đều bị nuốt; app không bao giờ chờ quảng cáo.

## 5. Bản thử kỹ thuật tuần đầu

Mục đích: trả lời rủi ro lớn nhất trước khi dựng giao diện. Một màn hình duy nhất, không cần đẹp.

| Câu hỏi | Cách thử | Đạt khi |
|---|---|---|
| Khóa phơi sáng và cân bằng trắng có giữ được không | Khóa trên giấy trắng, lia sang vật màu rồi quay lại giấy | Màu giấy đọc lại gần như cũ trên ít nhất 2 trong 3 máy |
| Màu có ổn định không | Giữ yên máy trên một mẫu màu 5 giây | Tên họ màu không đổi |
| Hiệu chuẩn có giúp không | Đo dải xám in sẵn dưới đèn vàng, trước và sau hiệu chuẩn | Sau hiệu chuẩn các ô xám gần trung tính hơn rõ rệt |
| Tốc độ | Đo thời gian từ mở app đến kết quả đầu tiên | Dưới 3 giây trên máy tầm trung |
| Đặt tên | Chạy 30 mẫu màu in sẵn dưới ánh sáng ban ngày | Đúng họ màu ít nhất 27/30 |

Nếu câu đầu không đạt trên phần lớn máy, lời hứa "hiệu chuẩn" phải thu hẹp lại (chỉ hiệu chuẩn bằng phần mềm, độ tin cậy tối đa thấp hơn) trước khi làm tiếp.

## 6. Thứ tự dựng

1. `:core-color` kèm unit test (không cần máy).
2. `:camera` và bản thử kỹ thuật.
3. Màn hình camera, màu đã chụp, chọn màu từ ảnh.
4. `:data`: lịch sử, bộ sưu tập.
5. Hiệu chuẩn, so hai màu, đọc to, hướng dẫn lần đầu.
6. `:monetization`: mua Pro, rồi quảng cáo có thưởng, rồi banner và đồng ý.
7. Trợ năng, thử trên 5 máy, chuẩn bị phát hành.

Phần kiếm tiền để sau cùng vì nó không ảnh hưởng tới câu hỏi "sản phẩm có chạy tốt không", và tách riêng thì dễ gỡ lỗi hơn.
