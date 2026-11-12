# Competitive brief — Color Picker: Find Name & Code

Ngày nghiên cứu: 12/11/2026. Góc nhìn: "sản phẩm của mình" là Color Picker (`gmikhail.colorpicker`); phần về app này lấy từ [báo cáo nghiên cứu người dùng](color-picker-user-research.md).

## Nguồn và giới hạn

- Nguồn duy nhất là Google Play bản Mỹ/tiếng Anh: kết quả tìm kiếm "color picker" và "color identifier color blind", trang cửa hàng của 5 đối thủ, và 20 review "phù hợp nhất" của 4 trong số đó.
- 20 review mỗi app là rất ít, chỉ đủ để thấy chủ đề lặp lại, không đủ để đếm tỷ lệ. Color Identifier: Paint Match chỉ được đọc phần mô tả.
- Chưa cài thử app nào. Chưa xem website, mạng xã hội, từ khóa ASO hay doanh thu. Giá lấy từ review hoặc trang cửa hàng, chưa kiểm tra trong app.
- Các đối thủ đều là nhà phát triển nhỏ, không có blog hay nội dung marketing đáng kể, nên phần "content strategy" được thay bằng phân tích trang cửa hàng.

## 1. Tóm tắt

Thị trường chọn màu trên Android gồm nhiều app nhỏ gần giống nhau về tính năng lõi (camera + ảnh, hex/RGB, tên màu). Chúng khác nhau ở ba điểm: mô hình kiếm tiền, công cụ bảng màu, và nhóm người dùng nhắm tới. Color Picker có lượt tải ngang app lớn nhất (1M+) nhưng điểm thấp nhất nhóm chọn màu (4.2 so với 4.5–4.6).

- **Cơ hội lớn nhất:** nhu cầu "tên màu dễ hiểu + đáng tin" của người mù màu chưa app nào làm tốt. Color Blind Pal nhắm đúng nhóm này nhưng chỉ đạt 3.4★, và chính người dùng của nó cũng phàn nàn về tên màu và độ lệch camera.
- **Mối đe dọa lớn nhất:** Color & Palette (4.6★, cập nhật 9/2026) định vị thẳng vào điểm yếu của Color Picker: "Completely ad-free. Zero data collected. Ever." Trong khi lý do 1 sao số một của Color Picker là màn hình bắt đồng ý quảng cáo.

## 2. Hồ sơ đối thủ

### Bảng tổng quan

| App | Điểm | Lượt đánh giá | Lượt tải | Kiếm tiền | Cập nhật |
|---|---|---|---|---|---|
| **Color Picker: Find Name & Code** (mình) | 4.2 | 5,79K | 1M+ | Quảng cáo + mua bỏ quảng cáo (một review nói khoảng 5 USD) | 7/2026 |
| Color Grab (Loomatix) | 4.5 | 16,4K | 1M+ | Quảng cáo + mua trong app | 2/2021 |
| Color & Palette (Ruslan Lepekha) | 4.6 | 1,11K | 100K+ | Không quảng cáo, mở khóa một lần (review nói 1,99 USD) | 9/2026 |
| Color Picker & Generator (appsvek) | 4.5 | 1,26K | 100K+ | Quảng cáo; bản Pro riêng 1,99 USD (4.7★) | 4/2026 |
| Color Blind Pal (Vincent Fiorentini) | 3.4 | 847 | 100K+ | Miễn phí, không quảng cáo | 10/2026 |
| Color Identifier: Paint Match (Four Cats Lab) | 4.3 | 110 | 5K+ | Không quảng cáo, mua trong app | 10/2026 |

### Color Grab — "ông lớn" đã ngừng phát triển
- **Định vị:** "the ultimate on-the-go color tool", "#1 must have app for designers & artists". Nhắm designer, họa sĩ, dân chuyên, nhà khoa học và người mù màu.
- **Nhấn mạnh:** đo màu thời gian thực, hiệu chuẩn theo vật trắng tham chiếu, cân bằng trắng tùy chỉnh, tạo bảng màu và phối màu, pha trộn màu, xuất sang Photoshop/Illustrator/CSV/AutoCAD, đọc tên màu bằng giọng nói qua nút âm lượng.
- **Mạnh:** nhiều review và điểm cao nhất nhóm; là app duy nhất được khen vì hiệu chuẩn trắng ("None of the color apps will calibrate the white balance like this one").
- **Yếu:** không cập nhật từ 2/2021. Review chê khó dùng ("Not intuitive", hiệu chuẩn "takes a long time"), lỗi trộn màu RGB, cài đặt không có tác dụng, không kiểm soát được tên màu.

### Color & Palette — đối thủ trực diện nguy hiểm nhất
- **Định vị:** "the most complete color picker and palette generator for Android", cho designer, họa sĩ, lập trình viên. Câu khóa: không quảng cáo, không thu thập dữ liệu.
- **Nhấn mạnh:** nhận màu từ camera và ảnh với đủ hệ mã, trích màu chủ đạo từ ảnh, tạo bảng màu bằng AI, vòng màu và các kiểu phối.
- **Mạnh:** 16/20 review đọc được là 5 sao, phần lớn viết năm 2026. Được khen đơn giản ("Three buttons"), không quảng cáo, giá mở khóa rẻ, nhà phát triển thêm tính năng theo góp ý rất nhanh. Có cả người mù màu khen.
- **Yếu:** còn nhỏ (100K+). Bản miễn phí giới hạn số bảng màu. Góp ý chủ yếu về thao tác bảng màu (chép hàng loạt, kéo màu giữa các bảng). Không thấy nhắc tới hiệu chuẩn hay dữ liệu khoa học.

### Color Picker & Generator (appsvek) — nghiêng về tạo bảng màu
- **Định vị:** công cụ eyedropper "for designers, illustrators and any artist working with a color palette"; có nhắc "capture color name for colorblind people".
- **Nhấn mạnh:** lấy hex từ ảnh, tự trích bảng màu, camera trực tiếp, bộ sinh bảng màu để lấy cảm hứng.
- **Mạnh:** được yêu thích như công cụ cảm hứng phối màu; người dùng sẵn sàng mua Pro.
- **Yếu:** camera trực tiếp nhảy liên tục, không khóa được khung hình; tên màu không nhất quán giữa kính lúp và bảng màu đã lưu; mất tính năng nhập hex thủ công.

### Color Blind Pal — đúng nhóm người dùng, sản phẩm chưa tới
- **Định vị:** "helps people who are color blind see the colors around them". Kẻ thù trong câu chuyện là việc phải hỏi người khác. Tên màu mô tả kiểu "bold brown", bộ lọc dịch màu, mô phỏng mù màu.
- **Mạnh:** tình huống dùng rất cụ thể và có giá trị cao: phân biệt đèn LED đỏ/xanh ở chỗ làm, màu dây điện, mua quần áo. Một review 2026: "it's the only app of its kind I have found".
- **Yếu:** 3.4★. Nhiều màu bị gọi "dull orange/dull brown", người dùng muốn tên đơn giản hơn nữa ("red, pink, or dark red"), màu lệch theo camera và ánh sáng, không zoom, ngón tay che điểm ngắm, không mở được ảnh có sẵn.

### Color Identifier: Paint Match — nhỏ nhưng đúng "việc cần làm"
- **Định vị:** cho designer, họa sĩ, người trang trí nhà. Điểm riêng: "Paint Color Matching: Find the closest paint shades", "Match wall, paint, or fabric colors before buying".
- **Ghi chú:** mới 5K+ lượt tải, chưa đọc review. Đáng theo dõi vì nói thẳng vào nhu cầu so màu sơn mà người dùng Color Picker đang tự xoay xở.

## 3. Ma trận thông điệp

| Tiêu chí | Color Picker (mình) | Color Grab | Color & Palette | appsvek | Color Blind Pal |
|---|---|---|---|---|---|
| Câu mở đầu | "Scientific colorimetry tool… professional-grade precision" | "The ultimate on-the-go color tool" | "Point your camera at any object and instantly know its exact color" | "Useful color eyedropper tool" | "Helps people who are color blind see the colors around them" |
| Người mua nhắm tới | Designer UI/UX, web dev, họa sĩ, thợ sơn, nhà khoa học | Designer, họa sĩ, dân chuyên | Designer, họa sĩ, dev | Designer, họa sĩ minh họa | Người mù màu |
| Khác biệt tuyên bố | CIEDE2000, Expert Mode, web server nội bộ, RAL | Hiệu chuẩn vật trắng, xuất nhiều định dạng | Không quảng cáo, không thu dữ liệu, AI | Sinh bảng màu | Bộ lọc dịch màu, tên mô tả |
| Giọng điệu | Kỹ thuật, dày đặc thuật ngữ | Quảng cáo, viết hoa | Tự tin, hướng lợi ích | Nhồi từ khóa | Gần gũi, kể tình huống đời thường |
| Bằng chứng | Liệt kê thuật toán và hệ màu | "Leading and worldwide used" | Cam kết quyền riêng tư | Không có | Ví dụ tình huống cụ thể |

Trang cửa hàng của Color Picker có một mâu thuẫn: mở đầu bằng "professional-grade precision" nhưng kết thúc bằng "Not intended for high-accuracy matching".

### Bản đồ định vị (dễ dùng × độ sâu kỹ thuật)

| | Ít dữ liệu kỹ thuật | Nhiều dữ liệu kỹ thuật |
|---|---|---|
| **Dễ dùng** | Color & Palette, appsvek | *(trống)* |
| **Khó dùng lần đầu** | Color Blind Pal | Color Picker, Color Grab |

Ô "dễ dùng mà vẫn đáng tin về độ chính xác" đang trống. Color Grab có tính năng hiệu chuẩn nhưng khó dùng và đã ngừng cập nhật.

## 4. Khoảng trống trên trang cửa hàng

| Chủ đề | Color Picker | Color Grab | Color & Palette | appsvek | Color Blind Pal | Nhận xét |
|---|---|---|---|---|---|---|
| Người mù màu | Không nhắc | Có, kèm đọc bằng giọng nói | Không nhắc | Một dòng | Trọng tâm | Color Picker có người dùng mù màu trung thành nhưng không nói với họ |
| So màu sơn / đồ thật | "paint matching" một lần | Không rõ | Không | Không | Không | Chỉ Paint Match khai thác |
| Quyền riêng tư / không quảng cáo | Không | Không | Trọng tâm | Không | Không | Điểm yếu của Color Picker là điểm bán của đối thủ |
| Hiệu chuẩn / cân bằng trắng | Có điều chỉnh, chưa có hiệu chuẩn theo vật trắng | Trọng tâm | Không | Không | Không | Người dùng Color Picker đang xin đúng tính năng này |
| Tạo bảng màu / phối màu | Bảng màu tùy chỉnh, chưa sinh tự động | Có | Trọng tâm, có AI | Trọng tâm | Không | Color Picker đi sau |
| Dữ liệu khoa học, CSV, web server | Trọng tâm | Xuất nhiều định dạng | Không | Không | Không | Color Picker dẫn đầu, nhưng nhóm cần nó nhỏ |

## 5. Cơ hội

1. **Chiếm vị trí "app nhận màu đáng tin cho người mù màu".** Nhóm này đã yêu thích Color Picker, và đối thủ chuyên biệt duy nhất đang ở 3.4★ với đúng những phàn nàn Color Picker có thể giải quyết: tên màu đơn giản theo họ màu, mở ảnh có sẵn, zoom.
2. **Thay thế Color Grab ở nhóm cần độ chính xác.** App này bỏ ngỏ từ 2021. Hiệu chuẩn theo vật trắng mà dễ dùng sẽ lấy được nhóm so màu sơn, đo que thử, cân màn hình.
3. **Nói thẳng về so màu đồ vật thật.** Review của Color Picker đầy câu chuyện vá gạch, mua ga giường, tìm kem nền, nhưng trang cửa hàng gần như không nhắc.
4. **Biến dữ liệu khoa học thành lợi thế phụ chứ không phải câu mở đầu.** Không đối thủ nào có CIEDE2000, RAL hay web server; giữ chúng cho nhóm chuyên sâu, nhưng mở đầu bằng lợi ích dễ hiểu.

## 6. Mối đe dọa

1. **Color & Palette đang tăng nhanh với mô hình sạch hơn.** Không quảng cáo, mở khóa rẻ một lần, review 2026 gần như toàn 5 sao. Người dùng bị kẹt ở màn hình đồng ý của Color Picker có sẵn một lựa chọn thay thế ngay trong kết quả tìm kiếm.
2. **Điểm số đang kéo thứ hạng xuống.** 4.2 là thấp nhất nhóm chọn màu, và xu hướng trong mẫu review của Color Picker đang giảm từ 2023.
3. **Tạo bảng màu trở thành tính năng mặc định.** Hai đối thủ lấy nó làm trọng tâm, một bên đã gắn nhãn AI. Designer số có thể rời đi vì lý do này.
4. **Giá.** Hai đối thủ mở khóa ở 1,99 USD; mức khoảng 5 USD của Color Picker (theo một review, chưa kiểm chứng) kèm việc không được dùng thử làm khoảng cách rõ hơn.

## 7. Khuyến nghị

**Làm ngay trong tuần**
1. Sửa luồng đồng ý quảng cáo để người từ chối cá nhân hóa vẫn vào được app với quảng cáo không cá nhân hóa. Đây là việc duy nhất vừa chặn mất người dùng vừa đang là điểm bán của đối thủ.
2. Viết lại 2–3 dòng đầu trang cửa hàng theo việc người dùng thật sự làm: biết tên màu, lấy mã màu, so màu đồ thật. Bỏ cặp câu mâu thuẫn "professional-grade precision" và "not intended for high-accuracy matching".
3. Thêm một đoạn riêng cho người mù màu vào mô tả, dùng chính tình huống từ review (màu dây điện, đèn LED, quần áo).

**Đầu tư dài hơn**
4. Thêm nhãn họ màu đơn giản cạnh tên màu, rồi dùng nó làm thông điệp chính cho nhóm mù màu.
5. Làm hiệu chuẩn "chĩa vào vật màu trắng" dễ dùng hơn Color Grab, và định vị lại quanh "kết quả bạn tin được".
6. Cân nhắc lại gói trả phí: có dùng thử trước khi mua, và so mức giá với 1,99 USD của đối thủ.

Thứ tự ưu tiên này dựa trên dữ liệu trang cửa hàng và một lượng review nhỏ; nên kiểm chứng mục 4–6 bằng kế hoạch nghiên cứu trong báo cáo người dùng trước khi đầu tư.
