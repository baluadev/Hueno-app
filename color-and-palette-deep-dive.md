# Đào sâu đối thủ: Color & Palette: Color Picker

Ngày nghiên cứu: 12/11/2026. Bổ sung cho [competitive brief](color-picker-competitive-brief.md); "mình" vẫn là Color Picker (`gmikhail.colorpicker`).

## Nguồn và giới hạn

- Trang Google Play bản Mỹ/tiếng Anh của `ua.com.compose.colorpicker.camera.image`: mô tả, mục "About this app", data safety, "What's new", trang nhà phát triển.
- 107 review có nội dung theo sắp xếp "phù hợp nhất" (5★ 81, 4★ 11, 3★ 3, 2★ 3, 1★ 9), viết từ 2023 đến 2026. Đây không phải mẫu ngẫu nhiên của 987 review trên điện thoại.
- Chưa cài app. Mọi nhận xét về giao diện, giá gói và tính năng trả phí là theo trang cửa hàng và lời người dùng.

## 1. Hồ sơ

| | Color & Palette | Color Picker (mình) |
|---|---|---|
| Nhà phát triển | Ruslan Lepekha, cá nhân, chỉ có 1 app trên Play | Mikhail Gribanov, cá nhân, nhiều app |
| Phát hành | 9/11/2022 | Review sớm nhất đọc được là 2019 |
| Phiên bản / cập nhật | 3.23.0, 23/9/2026 | 8/7/2026 |
| Điểm | 4.6 (điện thoại 4.7, 987 review) | 4.2 (điện thoại 4.1, 5,6K review) |
| Lượt tải | 100K+ | 1M+ |
| Danh mục | Art & Design | Tools |
| Quảng cáo | Không | Có |
| Mua trong app | 0,49–1,99 USD mỗi món | Có, một review nói khoảng 5 USD |
| Data safety | Không thu thập, không chia sẻ dữ liệu | Thu thập và chia sẻ vị trí, hoạt động trong app và 2 loại khác; không xóa được dữ liệu |
| Android tối thiểu | 7.0 | Chưa xem |

Hai khác biệt đáng chú ý ngoài điểm số: app này xếp ở danh mục Art & Design chứ không phải Tools, và phần data safety của hai app đối lập hoàn toàn.

## 2. Định vị và thông điệp

- **Câu mở đầu:** "Point your camera at any object and instantly know its exact color". Mở bằng hành động và kết quả, không mở bằng thuật ngữ.
- **Câu khóa:** "Completely ad-free. Zero data collected. Ever." Lặp lại lần nữa ở cuối: "Unlike other color apps… zero data and contains zero ads — not now, not ever."
- **Câu định vị suy ra:** dành cho designer, họa sĩ và dev cần chọn màu và dựng bảng màu, Color & Palette là công cụ đầy đủ nhất trên Android, vì không quảng cáo, không thu dữ liệu và xuất được ra mọi công cụ làm việc.
- **Câu chuyện:** kẻ xấu là các app màu đầy quảng cáo và popup quyền riêng tư; người dùng được hứa một công cụ sạch, và nhà phát triển là người đứng ra bảo đảm ("bugs typically fixed within days").
- **Cấu trúc mô tả:** mỗi tính năng một tiêu đề in hoa và một đoạn lợi ích, rồi mục "WHO IT'S FOR" liệt kê 7 nhóm người dùng kèm việc của từng nhóm, trong đó có "Anyone with color blindness" và thiết kế nội thất so màu sơn.
- **Tính năng được nhấn:** nhận màu từ camera và ảnh, trích màu chủ đạo, sinh bảng màu bằng AI, vòng màu và các kiểu phối, mô phỏng mù màu, hơn 12 định dạng xuất (CSS, ASE, ACO, GPL, SVG, JSON…), nhập bảng màu.

**Điểm yếu trong thông điệp:** "the most complete" và "more export formats than any comparable color tool" là tuyên bố không có bằng chứng. Mô phỏng mù màu là tính năng cho designer kiểm tra bảng màu, không phải trợ giúp cho người mù màu, nên lời hứa với nhóm này mỏng hơn vẻ ngoài.

## 3. Sản phẩm và mô hình kiếm tiền

- **Miễn phí:** chọn màu từ camera và ảnh, đủ hệ mã, 3 bảng màu khởi đầu, không quảng cáo.
- **Trả phí:** nhiều bảng màu hơn, gợi ý AI, vòng màu OKLCH (theo "What's new"). Review nhắc cả "$1.99 one time", "subscription" và "lifetime option", nên có vẻ tồn tại nhiều gói; mức giá trên Play là 0,49–1,99 USD mỗi món.
- **Lịch sử:** một review 2023 khen "no in-app purchases", một review 2025 viết "stay fossy plz". Có vẻ app khởi đầu hoàn toàn miễn phí rồi thêm gói trả phí sau, và người dùng nhìn chung chấp nhận.
- **Nhịp phát triển:** bản mới nhất thêm lựa chọn vòng màu RGB/RYB/OKLCH và sửa lỗi crash. Review ghi nhận tính năng được thêm theo yêu cầu chỉ trong vài ngày.

## 4. Người dùng nói gì

### Vì sao họ cho 5 sao
1. **Không quảng cáo, không popup quyền riêng tư.** Khoảng 10 review nêu thẳng lý do này.
   > "The only app I found that just tells you color information of a picture without bombarding you with privacy and advertisement popups." — 5★, 2026
   > "I hate that playstore hides this in the second page and only pushes the ad ridden ones first." — 5★, 2024
2. **Bảng màu là sản phẩm chính.** Người dùng gọi nó là "palette tool", dùng để lấy cảm hứng, sắp xếp và xuất file. Phần lớn góp ý cũng xoay quanh bảng màu.
3. **Nhà phát triển phản hồi nhanh.** 28 trên 107 review có trả lời; vài người sửa review lên 5 sao sau khi lỗi được sửa.
   > "edit: you added them THIS fast?!!" — 5★, 2025
4. **Giá được coi là rẻ.** "Full version is only $1.99. A bargain."
5. **Đơn giản.** "Three buttons. Drag photo to color picker. Full color details immediately."

### Họ phàn nàn và xin gì
- **Bảng màu:** chép màu hàng loạt và kéo màu giữa các bảng (một người nói tính năng kéo đã bị bỏ), gắn ảnh nguồn vào bảng màu, sao lưu/đồng bộ qua tài khoản.
- **Chọn màu:** màu nhảy liên tục ở camera, xin lấy trung bình vùng (1px, 3×3, 5×5…), ảnh chia sẻ vào app bị nén JPEG làm sai màu, xin thông tin bước sóng. Color Picker đã có sẵn lấy trung bình vùng và bước sóng.
- **Tên màu quá lạ:** "Naval Night, Sea goddess and jet-black" thay vì "Navy, Light-blue, Black" (4★, 2024, 5 người thấy hữu ích). Cùng vấn đề với Color Picker.
- **Độ chính xác theo ánh sáng:** lệch trong ánh sáng lạnh trong nhà. Không ai nhắc tới hiệu chuẩn hay cân bằng trắng.
- **Khó hiểu lúc đầu:** "took me some time to figure it out without a tutorial", "no user manual".
- **Mua hàng:** không thấy giá trước khi bấm mua; một người đã trả tiền nói không được hỗ trợ.
- **Crash khi mở:** nhà phát triển trả lời rằng app tự thoát nếu phát hiện máy root hoặc công cụ chèn mã.

### Giọng trả lời review
Phần lớn lịch sự và hỏi lại chi tiết. Có vài câu gay gắt hoặc đùa cợt với người chê ("Your comment is exactly spam", "Cool sounds like 5 stars to me"). Đây là rủi ro hình ảnh nhỏ của một app do một người vận hành.

## 5. So trực tiếp với Color Picker

| Việc cần làm của người dùng | Color & Palette | Color Picker | Ai hơn |
|---|---|---|---|
| Vào app và dùng ngay | Không rào cản | Kẹt ở màn hình đồng ý quảng cáo | Color & Palette |
| Lấy mã màu từ ảnh, dán đi nơi khác | Tốt, đơn giản | Tốt, có tự sao chép | Ngang nhau |
| Dựng, phối, xuất bảng màu | Trọng tâm: AI, vòng màu, 12+ định dạng | Bảng màu tùy chỉnh, CSV | Color & Palette |
| Đọc màu vật thật chính xác | Chưa có hiệu chuẩn, chưa lấy trung bình vùng | Lấy trung bình vùng, điều chỉnh cân bằng trắng, phơi sáng, CIEDE2000 | Color Picker |
| Biết tên màu dễ hiểu (người mù màu) | Tên lạ; chỉ có mô phỏng mù màu | Tên lạ; nhưng đã có tệp người dùng mù màu trung thành | Chưa ai thắng |
| Chuẩn công nghiệp, dữ liệu đo | Không có RAL, bước sóng | RAL, bước sóng, Kelvin, web server | Color Picker |
| Quyền riêng tư | Không thu dữ liệu | Thu và chia sẻ dữ liệu cho quảng cáo | Color & Palette |
| Quy mô và lịch sử | 100K+, từ 2022 | 1M+, lâu năm | Color Picker |

## 6. Hàm ý

1. **Đừng đua bảng màu.** Đó là sân nhà của Color & Palette, họ ra tính năng nhanh và người dùng ở đó là designer số, nhóm ít gắn bó nhất với Color Picker.
2. **Thắng ở phần đo màu vật thật.** Color & Palette yếu rõ ở camera: màu nhảy, lệch theo ánh sáng, không hiệu chuẩn. Người mù màu, người so màu sơn, họa sĩ vẽ từ mẫu thật đều cần đúng phần này, và Color Picker đã có nền tảng kỹ thuật.
3. **Gỡ lý do người dùng bỏ sang đối thủ.** Lời khen số một của Color & Palette chính là lời chê số một của Color Picker. Chừng nào màn hình đồng ý còn chặn người dùng, mỗi review 1 sao bên này là một quảng cáo cho bên kia.
4. **Học cách viết trang cửa hàng của họ.** Mở bằng hành động, mỗi tính năng một lợi ích, có mục "dành cho ai" gọi tên từng nhóm.
5. **Tên màu đơn giản là cơ hội còn mở cho cả hai.** Ai làm trước sẽ có câu trả lời cho nhóm mù màu mà bên kia chưa có.

## 7. Dấu hiệu cần theo dõi

- Color & Palette thêm hiệu chuẩn trắng, lấy trung bình vùng hoặc tên màu đơn giản: khi đó lợi thế đo màu của Color Picker thu hẹp.
- Chuyển từ mua một lần sang thuê bao: review đã bắt đầu nhắc "subscription", có thể làm giảm thiện cảm về giá.
- Vượt mốc 500K lượt tải hoặc lên trang đầu kết quả "color picker".
- Thay đổi trong phần data safety hoặc xuất hiện quảng cáo, vì toàn bộ định vị của họ dựa vào cam kết "not now, not ever".
