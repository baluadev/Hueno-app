# Color Picker: Find Name & Code — nghiên cứu người dùng

Ứng dụng: `gmikhail.colorpicker` (Mikhail Gribanov), Google Play. 4.2★, 5,79K lượt đánh giá, hơn 1 triệu lượt tải, có quảng cáo và mua trong ứng dụng, cập nhật gần nhất 8/7/2026.

## 1. Nguồn dữ liệu và giới hạn

- Đã đọc 320 bài đánh giá có nội dung trên trang Play bản Mỹ/tiếng Anh ngày 12/11/2026 (5★ 195, 4★ 41, 3★ 20, 2★ 16, 1★ 48), viết trong giai đoạn 2019–2026.
- Đây là tập Play hiển thị theo sắp xếp mặc định "phù hợp nhất", không phải mẫu ngẫu nhiên của 5,6K đánh giá trên điện thoại. Các con số bên dưới là số đếm trong 320 bài này, chỉ nên đọc như xu hướng chứ không phải tỷ lệ thực.
- Người viết đánh giá là tự chọn. Dữ liệu này không nói gì về người rời bỏ trong im lặng, thị trường không nói tiếng Anh, hay hành vi của người đã trả tiền. Phần 4 đề xuất nghiên cứu để lấp các khoảng trống đó.
- Các câu trích được giữ nguyên tiếng Anh vì là bằng chứng gốc, kèm ý chính bằng tiếng Việt.

## 2. Ai dùng và họ "thuê" ứng dụng để làm gì (jobs to be done)

| Nhóm | Việc cần làm | Bằng chứng trong đánh giá |
|---|---|---|
| Người mù màu | "Cho tôi biết đây là màu gì, bằng từ tôi đã biết" | 8 bài, trong đó có những bài được bấm hữu ích nhiều nhất (49, 47, 14). Có cả một thợ điện kiểm tra màu dây. |
| Họa sĩ và người vẽ nghiệp dư | "Tách ảnh mẫu thành các màu tôi có thể pha hoặc so với bút chì/màu vẽ của mình" | Nhóm khen đông nhất. Màu da, bóng đổ, vòng màu nước. |
| So màu đồ vật thật | "Tìm sơn, keo vá gạch, ga giường, kem nền hay thuốc nhuộm tóc khớp với món này" | Vá gạch, ga khớp vỏ chăn, đối chiếu bảng RAL, tông trang điểm. |
| Designer số và người tùy biến máy | "Lấy mã hex của thứ trên màn hình rồi dán sang chỗ khác" | Ảnh chụp màn hình, widget KWGT, icon khớp hình nền, logo, web dev. |
| Người thích đo đạc | "Dùng điện thoại như máy đo màu tạm" | Que thử pH/clo hồ bơi, theo dõi vết thâm da, cân màn hình, xuất CSV giá trị LAB. |

Trang cửa hàng mở đầu bằng "scientific colorimetry" và "professional-grade precision". Nhưng đánh giá lại chủ yếu đến từ ba nhóm đầu, những người cần một câu trả lời dễ hiểu chứ không cần số liệu.

## 3. Các chủ đề (affinity map)

### T1. Màn hình xin đồng ý quảng cáo là lý do chính của đánh giá 1 sao từ 2023
Khoảng 21 trong 36 bài 1–2 sao giai đoạn 2023–2026 nói về hộp thoại đồng ý quảng cáo: từ chối quảng cáo cá nhân hóa thì bị quay lại đúng hộp thoại đó, hoặc chỉ còn hai lựa chọn "đồng ý" hay "trả tiền". Người dùng gọi đây là ứng dụng trả phí trá hình và gỡ "trong vòng một phút". Vài người nói họ sẵn sàng xem quảng cáo không cá nhân hóa. Điểm trung bình của mẫu giảm từ 4,51 (2023) xuống 3,87 (2024), 3,79 (2025) và 3,62 (2026) khi các bài này xuất hiện.

> "Unable to get into app: if I exercise my choice of cookies (none), it takes me back to the page explaining that I have a choice" — 1★, 2024, 34 người thấy hữu ích
> (Chọn không cookie thì bị đưa lại trang giải thích rằng mình có quyền chọn.)

Những người này chưa từng vào được sản phẩm, nên cũng không thể cân nhắc gói trả phí.

### T2. Tên màu không khớp cách mọi người gọi màu
Hai phàn nàn trái ngược nhưng chung một nguyên nhân (so khớp gần nhất với một bảng màu có tên khá nhỏ):
- Nhiều màu khác nhau bị gộp về một tên: "arsenic" cho xám, xanh đậm và navy; "silver" hoặc "Battleship Grey" cho giấy trắng; hai màu khác hẳn nhau đều là "banana 100".
- Tên quá lạ với chính những người cần nhất. Một người mù màu muốn "đỏ nhạt, đỏ đậm, tím" chứ không phải "chestnut" hay "camel".

Đề xuất từ người dùng: hiển thị họ màu cơ bản gần nhất; bảng màu lớn hơn hoặc quen thuộc hơn.

### T3. Niềm tin vào độ chính xác mong manh, và người dùng không phân biệt được lỗi camera với lỗi ứng dụng
Lời khen ("outstanding accuracy", khớp bảng RAL K7) và lời chê (lệch 10–20% trên dải xám, "dusty plum" bị đọc thành "slate gray") cùng tồn tại. Yêu cầu lặp lại: cân bằng trắng thủ công theo một vật trắng đã biết, hiệu chuẩn theo mẫu màu chuẩn, phơi sáng thủ công, nhập nhiệt độ màu Kelvin. Các bài cũ (2019–2021) còn báo ảnh nhập vào bị nén hoặc lệch màu.

### T4. Khó tìm ra cách chụp và sao chép
- Không có nút "chụp / đóng băng" rõ ràng; chạm vào ô màu mới là chụp.
- Mã hex không được sao chép, hoặc sao chép kèm `#` trong khi người dùng không muốn.
- Không tắt được tiếng màn trập.
- "Tìm 10 phút không ra", "quá phức tạp so với thứ tôi cần" — song song với rất nhiều bài khen "đơn giản, trực quan". Điều này gợi ý vấn đề nằm ở lần dùng đầu chứ không phải lâu dài.

### T5. Trở ngại ở chế độ ảnh
Xin quyền bộ nhớ nhưng không cấp được trên Android 13 (cụm bài 2022), không mở được nếu từ chối quyền camera, camera bật mỗi lần mở app, không xoay được ảnh và không đọc EXIF, zoom hạn chế, phải kéo ảnh thay vì di con trỏ, phải đóng ảnh mới chọn được ảnh khác.

### T6. Lỗi camera theo thiết bị
Không lấy nét được trên Galaxy Note 9/10+/20 Ultra, hình xem trước bị lật trên Surface Duo và một báo cáo năm 2025, hình xem trước bị xanh nhiễu, không đổi được camera.

### T7. Người dùng muốn gì tiếp theo
Tạo bảng màu từ một màu, nhóm và nạp lại các màu đã lưu, công cụ chọn màu dạng nổi (S-Pen), lấy mẫu theo khe/gradient, "dùng màu" sau khi chọn, xuất LAB/XYZ ra CSV. Bảng màu tùy chỉnh và sắp xếp lại đã được phát hành, nên một số yêu cầu có thể đã được giải quyết.

### Tác động / công sức

| Phát hiện | Tác động | Công sức | Quyết định |
|---|---|---|---|
| T1 vòng lặp đồng ý và "đồng ý hoặc trả tiền" | Rất cao (chặn lần dùng đầu, kéo điểm xuống) | Thấp–trung bình | Sửa trước tiên |
| T2 nhãn họ màu dễ hiểu cạnh tên màu | Cao với người mù màu và người dùng phổ thông | Thấp | Làm kế tiếp |
| T4 nút chụp/sao chép rõ ràng, tắt được tiếng màn trập | Trung bình–cao | Thấp | Làm kế tiếp |
| T3 hiệu chuẩn điểm trắng | Cao với nhóm so màu và đo đạc | Trung bình | Kiểm chứng qua nghiên cứu |
| T5 xử lý ảnh | Trung bình | Trung bình | Kiểm tra lỗi nào còn tồn tại |
| T6 lỗi camera theo thiết bị | Trung bình, phạm vi hẹp | Cao | Phân loại theo thị phần thiết bị |

## 4. Kế hoạch nghiên cứu

**Mục tiêu**
1. Xác nhận điều gì xảy ra ở hộp thoại đồng ý khi cài mới (trong và ngoài EU) và nó làm mất bao nhiêu người dùng mới.
2. Hiểu mỗi nhóm đánh giá kết quả "đúng" bằng cách nào, và làm gì khi nghi ngờ.
3. Tìm hiểu người mù màu và người dùng phổ thông có hành động được dựa trên tên màu ứng dụng đưa ra không.
4. Kiểm tra người dùng lần đầu có tự chụp, sao chép và lưu được một màu không.

**Phương pháp**

| Phương pháp | Câu hỏi | Người tham gia | Thời gian |
|---|---|---|---|
| Usability test, cài mới, nói ra suy nghĩ | Mục tiêu 1 và 4 | 6–8 người, trộn các nhóm, ít nhất 2 người ở EU/Anh | 1–2 tuần |
| Phỏng vấn (45 phút) kèm "cho tôi xem lần dùng gần nhất" | Mục tiêu 2 và 3 | 8 người: 3 mù màu, 3 họa sĩ/thủ công, 2 designer | 2–3 tuần |
| Khảo sát trong app hoặc từ trang cửa hàng | Đo quy mô các nhóm và việc cần làm chính | 150+ | 1–2 tuần |
| Phân tích phễu (nếu có dữ liệu) | Tỷ lệ rớt giữa cài đặt, đồng ý, lần chụp đầu | — | Vài ngày |

Nên tuyển cả người đã bỏ dùng lẫn người đang dùng; đánh giá vốn đã nghiêng về những người ở lại.

**Nhiệm vụ usability**
1. Cài và mở ứng dụng; chọn quyền riêng tư như bạn vẫn thường chọn.
2. Tìm xem vật này màu gì (có mẫu màu thật) và nói tên màu cho tôi.
3. Đưa mã hex của một màu trong ảnh chụp màn hình này vào ứng dụng ghi chú.
4. Lưu ba màu từ một tấm ảnh để lần sau xem lại.
5. Bạn nghĩ kết quả hơi lệch. Cho tôi xem bạn sẽ làm gì.

Đo: tỷ lệ hoàn thành, thời gian đến màu đầu tiên chụp được, lỗi, và người tham gia có tin kết quả đủ để đi mua sơn theo nó không.

## 5. Hướng dẫn phỏng vấn

**Khởi động (5 phút)**
- Bạn kể một chút về công việc của mình và màu sắc xuất hiện ở đâu trong đó.

**Bối cảnh (10 phút)**
- Lần gần nhất bạn cần nhận biết hoặc so khớp một màu là khi nào? Kể lại từ đầu giúp tôi.
- Trước ứng dụng này bạn dùng gì? Điều gì khiến bạn tìm thứ khác?
- Sau đó bạn làm gì với kết quả? (Gợi hỏi: dán, ghi lại, mang ra cửa hàng, pha màu.)

**Đào sâu (20 phút)**
- Cho tôi xem lần trước bạn làm thế nào. (Quan sát: camera hay ảnh, kích thước vùng lấy mẫu, họ đọc gì trên màn hình.)
- Làm sao bạn biết kết quả đúng? Đã bao giờ sai chưa? Chuyện gì xảy ra?
- Khi ứng dụng đưa ra tên như "arsenic" hay "camel", bạn hiểu ra điều gì? Bạn muốn nó nói thế nào hơn?
- Người mù màu: bạn cần trợ giúp nhất với những màu nào, trong tình huống nào? Nếu sai thì hậu quả là gì?
- Người so màu: lúc đó ánh sáng thế nào? Bạn có làm gì để bù không?
- Bạn dùng những con số nào trên màn hình? Bỏ qua những số nào?
- Kể về lần đầu bạn mở ứng dụng. Có gì khiến bạn bối rối hay khó chịu không?

**Phản hồi với concept (10 phút)**
- Cho xem ba concept: nhãn họ màu dễ hiểu ("xanh đậm · Arsenic"), bước hiệu chuẩn "chĩa vào vật màu trắng", nút chụp rõ ràng kèm xác nhận đã sao chép. Với mỗi cái: bạn nghĩ nó làm gì, và khi nào bạn sẽ dùng?

**Kết thúc (5 phút)**
- Nếu được đổi một thứ, bạn sẽ đổi gì?
- Bạn có trả tiền để bỏ quảng cáo không? Vì sao?
- Có điều gì tôi nên hỏi mà chưa hỏi không?

## 6. Trích dẫn tiêu biểu

- "I'm colorblind and needed to identify colors, I need simple like light red, dark red, purple. instead this gives me things like chestnut and camel." — 1★, 2025 (người mù màu cần tên đơn giản)
- "arsenic is apparently a shade of blue" — người mù màu, 5★, 2023, 47 hữu ích (tên màu khó hiểu)
- "So it's either pay without trialing or consent to ads other apps don't require to show ads." — 1★, 2025 (trả tiền mà không được thử, hoặc phải đồng ý)
- "Everything that isn't a bright vibrant primary colour is identified as grey or silver" — 1★, 2024 (màu không rực đều thành xám/bạc)
- "Color Picker identified color as Airforce blue, the sheet was purchased and was a perfect match." — 5★, 2023 (mua ga giường khớp màu)
- "I'm a colorblind electrician (green/red). Use this app quite often when colors are in question!" — 5★, 2024 (thợ điện mù màu đỏ/lục)
- "manual white balance calibration (e.g. pointing it to something that is known to be white)" — 5★, 2024 (xin hiệu chuẩn cân bằng trắng)
