# Hueno — bộ ASO cho Google Play (en-US)

Ngày lập: 12/11/2026. Thuộc [kế hoạch sản phẩm](product-plan.md). Logo và giao diện nằm trên canvas thiết kế "Hueno ASO Design Kit"; file logo: [hueno-icon.svg](hueno-icon.svg), [hueno-icon-512.png](hueno-icon-512.png).

Phạm vi: chỉ Google Play, chỉ một listing en-US. Phần văn bản để dán vào Play Console nằm trong các khối mã, giữ nguyên tiếng Anh.

## 1. Tên, package ID, danh mục

| Mục | Giá trị | Ghi chú |
|---|---|---|
| Tên thương hiệu | Hueno | Đã chốt 12/11/2026, còn chờ tra nhãn hiệu và tên miền. Đọc gần như "hue know". 5 ký tự, để dành 25 ký tự cho từ khóa |
| Package ID | `com.hueno.coloridentifier` | Không đổi được sau khi phát hành. Ngày 12/11/2026 chưa có app nào trên Play dùng ID này |
| Danh mục | Tools | Cùng danh mục với phần lớn app nhận màu đang xếp hạng cho "color identifier" |
| Thẻ (tags) | Chọn trong Play Console các thẻ gần nhất với: công cụ camera, tiện ích, trợ năng | Danh sách thẻ do Google quy định; mình chưa xem được danh sách hiện hành |
| Ngôn ngữ mặc định | English (United States) – en-US | Không thêm bản dịch ở phiên bản này |

Lưu ý về tên:
- Mình chỉ kiểm tra trên Google Play: không có app nhận màu nào tên Hueno. Kết quả tìm "hueno" hiện ra "ueno bank" (một ngân hàng), khác ngành.
- Mình chưa tra nhãn hiệu đã đăng ký và chưa kiểm tra tên miền. Cần làm hai việc này trước khi chốt.
- Nếu bạn đã có tên miền riêng, nên đổi package ID theo tên miền đó (ví dụ `com.<tênmiền>.hueno`), miễn là giữ cụm `coloridentifier` hoặc `hueno` trong ID.
- Nhiều người làm ASO cho rằng từ khóa trong package ID có giúp xếp hạng một chút. Google không xác nhận điều này, nên coi nó là điểm cộng nhỏ, không phải lý do chính.

## 2. Nghiên cứu từ khóa

Nguồn: gợi ý tìm kiếm thật của Google Play (en-US) và 10 kết quả đầu cho mỗi cụm, lấy ngày 12/11/2026. Mình không có số liệu lượng tìm kiếm; việc một cụm xuất hiện trong gợi ý chỉ cho biết có người tìm, không cho biết bao nhiêu.

### Gợi ý tìm kiếm Play trả về

| Gõ | Play gợi ý |
|---|---|
| color i | **color identifier**, color identifier app |
| color identifier | color identifier app, color identifier camera, color identifier paint match |
| color name | color name, **color name identifier**, color namer, color name app |
| color detector | color detector, color detector app, color detector camera |
| color finder | color finder, color finder app, color finder camera |
| color blind | color blind test, color blind camera, **color blind app** |
| color blind h / a | **color blind helper**, color blind assistant |
| color recogni | color recognition app, color recognizer |
| what color is | what color is it, **what color is this** |
| paint color | paint color match app, paint color match |
| identify color | identify color app, identify paint color |
| hex color | hex color picker, hex color code, hex color identifier |
| color picker | color picker from image, color picker camera |
| color compar | color compare, color comparison |
| color tell | color teller |

### Đánh giá độ cạnh tranh

| Cụm từ | Ai đang đứng đầu | Nhận định | Vai trò |
|---|---|---|---|
| color identifier | Lẫn giữa app lớn và nhiều app nhỏ chưa có điểm đánh giá, đều có đúng cụm này trong tên | Dễ vào nhất trong các cụm chính: app nhỏ vẫn lên trang đầu nhờ tên | **Chính, đặt trong tên** |
| color name / color name identifier | App nhận màu lẫn với game tô màu | Ít đối thủ đúng nghĩa, hợp nhất với lời hứa sản phẩm | **Chính, đặt trong tên** |
| color detector | Các app 1M+ lượt tải | Khó hơn, nhưng là cụm phổ biến | Phụ, mô tả ngắn |
| color blind helper / app | Bài kiểm tra mù màu và app mô phỏng; app trợ giúp tốt nhất chỉ 3.4★ | Khoảng trống rõ nhất về chất lượng | Phụ, mô tả ngắn + một mục riêng |
| color identifier camera, color finder, color recognition | Cùng nhóm app nhận màu | Đuôi dài, đi kèm cụm chính | Mô tả dài |
| what color is this | Lẫn game tô màu | Đúng câu người dùng tự hỏi | Tiêu đề mục trong mô tả |
| color picker from image, hex color code | App cho designer | Không phải nhóm mục tiêu nhưng tính năng có thật | Mô tả dài, một lần |
| paint color match | Ứng dụng của các hãng sơn | Rất khó, và app chưa có thư viện màu sơn | Chỉ nhắc nhẹ, không nhắm |
| color picker | Hàng chục app lâu năm | Không đua ở v1 | Chỉ xuất hiện trong "color picker from image" |

### Nơi đặt từ khóa
Google Play lập chỉ mục tên, mô tả ngắn và mô tả dài. Tên có trọng số cao nhất.

| Vị trí | Từ khóa |
|---|---|
| Tên | color identifier, color name |
| Mô tả ngắn | camera, color names, detector, color blind helper |
| Câu đầu mô tả dài | camera color identifier, color name, hex color code |
| Phần còn lại | color detector, what color is this, color blind / colorblind, compare two colors, color picker from image, color finder, color recognition, paint color |

## 3. Listing en-US

**App title (30/30)**

```
Hueno: Color Identifier & Name
```

**Short description (80/80)**

```
Identify any color by camera. Simple color names, detector & color blind helper.
```

**Full description (3075/4000)**

```
Hueno is a camera color identifier that tells you what color something is, in words you already know. Point your phone at any object and read the color name: "Dark blue", "Light green", "Reddish brown". The exact shade name and hex color code sit right underneath.

Made for people who are color blind, for anyone matching paint, fabric or makeup, and for artists working from real life.

WHAT COLOR IS THIS?
• Live color detector: point the camera and the color name appears right away
• Simple color names first (red, orange, yellow, green, blue, purple, pink, brown, gray, black, white), with light and dark shades
• A detailed color name and hex code when you need the exact shade
• Tap to hear the color name read aloud
• Steady readings: Hueno averages the area inside the ring, so the name does not flicker

KNOW WHEN TO TRUST THE READING
• One-tap white calibration: point at a sheet of white paper and tap. Hueno corrects for the light in your room
• Confidence indicator: Good, Fair or Poor, with a tip such as "Too dark, add light"
• Honest by design. A phone camera is not a lab instrument, and Hueno tells you when a reading is shaky

COMPARE TWO COLORS
• Capture color A, point at color B, and Hueno says Same, Close or Different
• See how they differ: "B is green and a little lighter"
• Handy for telling wires apart, checking status lights, pairing clothes, or holding a paint color match against a wall

COLOR PICKER FROM IMAGE
• Open any photo or screenshot and tap to pick a color
• Zoom in and move the pointer to find the color of a single pixel
• Copy the hex color code with one tap

A COLOR BLIND HELPER THAT SPEAKS PLAINLY
Many color apps answer with names like "arsenic" or "camel". Hueno starts with the color family, then the shade. If you are colorblind, red-green or any other type, you get an answer you can act on: which wire, which shirt, which line on the chart. Works with TalkBack and large text.

SAVE AND ORGANIZE
• History of the colors you capture, each with its name and code
• Name your colors and group them into collections
• Export to CSV with Pro

RESPECTFUL ADS, FAIR UPGRADE
• The camera and photo screens never show ads
• No full-screen ads that interrupt what you are doing
• Hueno Pro is a one-time purchase with no subscription. It removes ads and unlocks unlimited history, unlimited comparisons and a saved calibration for every room
• Not ready to buy? Watch one short video to use Pro for 6 hours

WHO USES HUENO
• Color blind users who want a color finder for everyday objects
• DIY and home projects: identify a paint color before you shop
• Sewing, knitting and crafts: match thread, yarn and fabric
• Artists: find the true color of shadows and skin tones in a reference
• Designers and developers: use color recognition to grab a hex code from the real world

GOOD TO KNOW
Color readings depend on your camera and lighting. For the most reliable result, use daylight or bright white light and calibrate first. Hueno is a reference tool. It is not a medical device and not a certified color measurement instrument.
```

### Kiểm tra theo quy định của Play
- Tên không có chữ in hoa toàn bộ, emoji, "free", "best", "#1".
- Mô tả không nhắc tên đối thủ hay thương hiệu khác. "arsenic" và "camel" là tên màu.
- Không có lời chứng thực của người dùng.
- Mật độ từ khóa: "color identifier", "color detector", "color finder", "color recognition" mỗi cụm một lần; "color name" năm lần vì là tính năng chính; không lặp nhồi.
- Mọi tính năng trong mô tả đều nằm trong phạm vi v1.0 của kế hoạch sản phẩm. Nếu tính năng nào bị cắt khi làm, phải xóa dòng tương ứng trước khi đăng. Các dòng dễ bị ảnh hưởng: TalkBack, CSV, "a saved calibration for every room", "6 hours".

## 4. Hình ảnh

| Tài sản | Quy cách | Trạng thái |
|---|---|---|
| App icon | 512 × 512 PNG | Có file PNG và SVG |
| Feature graphic | 1024 × 500 | Có thiết kế trên canvas, cần xuất ảnh |
| Ảnh chụp điện thoại | 1080 × 1920, 6 ảnh | Có bố cục và chú thích; ảnh thật phải chụp từ bản build |
| Video | 20–30 giây, quay màn hình thật | Chưa làm; nên có |

Logo: vòng ngắm đồng thời là bong bóng lời thoại, nghĩa là "chĩa vào màu, app nói tên màu". Bốn cung màu khác nhau cả về độ sáng, để người mù màu vẫn phân biệt được.

Thứ tự ảnh và chú thích (đã đếm ký tự):

| # | Màn hình | Chú thích | Từ khóa gánh |
|---|---|---|---|
| 1 | Camera | Point. Know the color. (22) | — |
| 2 | Màu đã chụp | Color names you already know (28) | color names |
| 3 | So hai màu | Same or different? Compare two colors (37) | compare colors |
| 4 | Hiệu chuẩn | Calibrate with a sheet of white paper (37) | calibrate |
| 5 | Chọn màu từ ảnh | Pick a color from any photo (27) | color from photo |
| 6 | Lịch sử | Every color you capture, saved (30) | — |

Ba ảnh đầu quyết định phần lớn, vì chúng hiện ngay trong kết quả tìm kiếm: nhận màu, tên dễ hiểu, so hai màu. Chú thích không có giá, "free" hay lời so sánh thứ hạng. Play yêu cầu ảnh phản ánh đúng app, nên các bản xem trước trên canvas chỉ là bố cục.

## 5. Giao diện app

Canvas có 8 màn hình điện thoại 390 × 844: hướng dẫn lần đầu, camera, màu đã chụp, chọn màu từ ảnh, so hai màu, hiệu chuẩn, lịch sử, Pro và Pro tạm thời.

- Màn hình camera và ảnh dùng nền tối, không có quảng cáo.
- Các màn hình còn lại dùng nền sáng; ô banner 320 × 50 chỉ có ở "màu đã chụp" và "lịch sử".
- Giao diện chỉ dùng đen, trắng ngà và một màu xanh cho nút bấm, để màu vừa đo là màu nổi bật duy nhất.
- Trạng thái (độ tin cậy, kết quả so màu) luôn có chữ và biểu tượng, không chỉ dựa vào màu.
- Chưa có màn hình cài đặt, bộ sưu tập chi tiết và hộp thoại đồng ý quảng cáo.

## 6. Việc cần xong trước khi gửi duyệt

- Tra nhãn hiệu và tên miền cho "Hueno"; chốt package ID.
- Chính sách quyền riêng tư có đường dẫn công khai.
- Data safety khai đúng theo SDK quảng cáo và thanh toán thực tế dùng.
- Khai "Contains ads" và tạo sản phẩm mua trong app cho Pro.
- Bảng câu hỏi xếp hạng nội dung và đối tượng mục tiêu.
- Thử trường hợp từ chối quảng cáo cá nhân hóa trên máy thật.
- Đối chiếu từng dòng mô tả với bản build gửi duyệt.
- Chụp 6 ảnh thật, xuất feature graphic, quay video.

## 7. Sau khi phát hành

- Theo dõi thứ hạng cho: color identifier, color name identifier, color detector, color blind helper, color blind app, what color is this.
- Sau 4–6 tuần, xem báo cáo từ khóa trong Play Console rồi mới sửa tên hoặc mô tả ngắn; không đổi nhiều thứ cùng lúc.
- Dùng thử nghiệm listing của Play Console để thử ảnh đầu tiên và mô tả ngắn.
- Phương án mô tả ngắn để thử (75/80):

```
Color detector camera: find the color name of anything. Color blind helper.
```
