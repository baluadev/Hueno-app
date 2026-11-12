# Kế hoạch sản phẩm — app nhận màu mới

Ngày lập: 12/11/2026. Dựa trên [nghiên cứu người dùng](color-picker-user-research.md), [competitive brief](color-picker-competitive-brief.md) và [đào sâu Color & Palette](color-and-palette-deep-dive.md).

**Giả định nền:** bạn xây một app Android mới, cạnh tranh với cả 6 app đã nghiên cứu (kể cả Color Picker của Mikhail Gribanov), do một người hoặc nhóm rất nhỏ làm. Tên app đã chốt là Hueno (12/11/2026), còn chờ tra nhãn hiệu và tên miền; chi tiết tên, package ID, từ khóa và listing nằm trong [bộ ASO](hueno-aso.md).

**Phạm vi đã chốt (12/11/2026):** phiên bản này chỉ có tiếng Anh (en-US). Không làm phỏng vấn người dùng vì không có ngân sách; thay bằng đánh giá từ dữ liệu sẵn có ở phần 10.

**Mô hình đã chốt (12/11/2026):** tải miễn phí, có quảng cáo. Mua Pro một lần để bỏ quảng cáo và mở toàn bộ tính năng. Người không mua có thể xem một quảng cáo có thưởng để dùng Pro trong một số giờ.

Các con số mục tiêu, giá, thời lượng và doanh thu trong tài liệu là đề xuất của mình, chưa được kiểm chứng. Bằng chứng thị trường chỉ đến từ review trên Google Play.

## 1. Chiến lược

### Chọn sân chơi
Không làm "color picker đầy đủ nhất". Sân đó đã chật: Color & Palette giữ mảng bảng màu, Color Picker giữ mảng dữ liệu khoa học, Color Grab giữ lượng người dùng cũ.

Làm **app nhận màu vật thật mà người dùng tin được, nói bằng từ ai cũng hiểu**.

| Khoảng trống đã thấy | Bằng chứng | Hueno trả lời bằng |
|---|---|---|
| Tên màu khó hiểu | Phàn nàn ở cả Color Picker, Color & Palette, Color Blind Pal ("arsenic", "Naval Night", "dull brown") | Tên hai tầng: họ màu đơn giản trước, tên chi tiết sau |
| Không biết kết quả có đúng không | Xin hiệu chuẩn trắng ở Color Picker; Color Grab có nhưng khó dùng và bỏ ngỏ từ 2021 | Hiệu chuẩn một chạm + chỉ báo độ tin cậy |
| Màu nhảy liên tục ở camera | Phàn nàn ở Color & Palette, appsvek | Làm mượt theo thời gian + nút đóng băng rõ ràng |
| Quảng cáo và màn hình đồng ý chặn người dùng | Lý do 1 sao số một của Color Picker | Quảng cáo có kỷ luật: từ chối cá nhân hóa vẫn dùng được, không bao giờ che camera, luôn có cách thoát quảng cáo không tốn tiền |
| Người mù màu chưa có app tốt | Color Blind Pal 3.4★, "the only app of its kind" | Nhóm người dùng số một |

Có quảng cáo nghĩa là mình không còn dùng được thông điệp "không thu thập dữ liệu" của Color & Palette. Điểm khác biệt về quảng cáo chuyển thành: có quảng cáo nhưng không làm phiền, và người dùng tự chọn.

### Người dùng mục tiêu
1. **Chính (bàn đạp):** người mù màu cần biết màu của vật trước mặt: dây điện, đèn LED, quần áo, biểu đồ.
2. **Thứ hai:** người so màu vật thật để mua hoặc pha: sơn, vải, len, mỹ phẩm, màu vẽ.
3. **Không phục vụ ở v1:** designer cần bảng màu và phối màu; người cần dữ liệu đo chuyên sâu.

### Câu định vị
Dành cho người cần biết chắc một vật màu gì, Hueno là app nhận màu bằng camera cho ra tên màu dễ hiểu và cho biết kết quả đáng tin đến đâu, vì nó hiệu chuẩn theo ánh sáng thật và không để quảng cáo chen vào việc bạn đang làm.

### Nguyên tắc sản phẩm
- Mở app là thấy màu trong vòng 3 giây, không có màn hình nào chắn trước.
- Từ dễ hiểu trước, con số sau.
- Luôn nói thật về độ tin cậy; không hứa "chính xác chuyên nghiệp".
- Mỗi màn hình một việc.
- Quảng cáo không bao giờ đứng giữa người dùng và kết quả màu.

## 2. Phạm vi v1.0 (MVP)

### Tính năng

| # | Tính năng | Mô tả cụ thể | Miễn phí | Pro |
|---|---|---|---|---|
| F1 | Camera nhận màu trực tiếp | Vòng ngắm giữa màn hình, lấy trung bình vùng (3 cỡ), làm mượt qua nhiều khung hình để màu không nhảy | Có | Có |
| F2 | Tên màu hai tầng | Dòng lớn: họ màu + sắc độ ("Xanh dương đậm"). Dòng nhỏ: tên chi tiết gần nhất ("Navy") và mã hex. Khoảng 11 họ màu × 3–4 sắc độ | Có | Có |
| F3 | Đóng băng và chụp | Nút chụp to, rõ. Bấm là đóng băng khung hình, di con trỏ để xem các điểm khác. Tắt được tiếng | Có | Có |
| F4 | Sao chép | Chạm vào mã là sao chép, có xác nhận. Tùy chọn có/không dấu `#` | Có | Có |
| F5 | Ảnh từ thư viện | Trình chọn ảnh của hệ thống (không xin quyền bộ nhớ), đọc đúng chiều EXIF, không nén lại, zoom và di con trỏ | Có | Có |
| F6 | Hiệu chuẩn một chạm | "Chĩa vào tờ giấy trắng rồi bấm". Khóa phơi sáng và cân bằng trắng, lưu hệ số cho phiên đó | Có | Có |
| F7 | Chỉ báo độ tin cậy | Ba mức (tốt / tạm / kém) kèm gợi ý: "thiếu sáng, hãy bật đèn" | Có | Có |
| F8 | So hai màu | Chụp màu A, chĩa vào màu B: app nói "giống nhau", "gần giống" hay "khác nhau" và khác ở đâu | 3 lần mỗi ngày | Không giới hạn |
| F9 | Đọc to | Chạm để nghe tên màu; hỗ trợ TalkBack, chữ lớn | Có | Có |
| F10 | Lịch sử và bộ sưu tập | Lưu màu kèm ảnh thu nhỏ của vật, đặt tên, gom nhóm | 20 màu gần nhất | Không giới hạn, xuất CSV |
| F11 | Ngôn ngữ | Chỉ tiếng Anh (en-US). Chuỗi giao diện và bảng tên màu tách riêng để thêm ngôn ngữ sau | Có | Có |
| F12 | Nhiều hồ sơ hiệu chuẩn | Lưu hệ số theo nơi dùng ("phòng khách", "xưởng") để chọn lại nhanh | 1 hồ sơ | Không giới hạn |
| F13 | Hướng dẫn lần đầu | Ba bước ngắn: chĩa và đọc tên màu, bấm chụp, hiệu chuẩn. Bỏ qua được | Có | Có |
| — | Quảng cáo | Xem phần 3 | Có | Không |

Toàn bộ lời hứa lõi (tên dễ hiểu, hiệu chuẩn, độ tin cậy, đọc to) nằm ở bản miễn phí. Pro bán sự thoải mái và số lượng, không bán phần trợ năng.

### Tiêu chí hoàn thành chính
- Giữ yên máy 1 giây thì tên màu không đổi.
- 9/10 người thử gọi đúng họ màu với bộ mẫu chuẩn dưới ánh sáng ban ngày.
- Người mới tự chụp được màu đầu tiên trong 60 giây kể từ khi mở app, không gặp quảng cáo hay hộp thoại nào trước đó.
- Mã hex khớp tuyệt đối với ảnh PNG mẫu.
- So hai màu phân biệt được hai dây điện đỏ và xanh lá.

### Cố ý không làm ở v1
Sinh bảng màu, vòng màu và phối màu, AI, xuất ASE/ACO/CSS, chế độ chuyên gia (bước sóng, Kelvin), web server, bộ lọc dịch màu cho người mù màu, tài khoản và đồng bộ.

### Ghi chú kỹ thuật
- **Đặt tên màu (F2):** chuyển màu sang không gian cảm nhận (OKLCH hoặc CIELAB), chia vùng theo sắc độ, độ sáng và độ rực để ra họ màu; tên chi tiết lấy bằng khoảng cách CIEDE2000 tới một bảng tên có sẵn. Ranh giới vùng cần tinh chỉnh bằng thử nghiệm với người thật, nhất là nâu/cam, xám/bạc, xanh dương/tím.
- **Nền tảng:** đã chốt Kotlin native (12/11/2026). Chi tiết ở [kiến trúc kỹ thuật](tech-architecture.md).
- **Camera (F1, F6):** cần khóa phơi sáng và cân bằng trắng qua CameraX và Camera2. Một số máy có thể không giữ khóa đúng; đây là rủi ro kỹ thuật lớn nhất, làm thử ngay tuần đầu.
- **Thiết bị:** thử trên ít nhất 5 máy khác hãng. Lỗi lấy nét và lật hình theo máy là nguồn 1 sao của Color Picker.
- **Quyền:** chỉ xin camera, và chỉ khi người dùng mở chế độ camera.

## 3. Kiếm tiền: quảng cáo + Pro + Pro tạm thời

### Ba cách người dùng "trả"

| Cách | Người dùng nhận | Người dùng bỏ ra |
|---|---|---|
| Miễn phí | Toàn bộ phần lõi, có giới hạn ở F8, F10, F12 | Thấy banner ở một số màn hình |
| Pro tạm thời | Bỏ quảng cáo và mở hết giới hạn trong 6 giờ | Tự chọn xem một quảng cáo có thưởng |
| Pro vĩnh viễn | Bỏ quảng cáo và mở hết giới hạn mãi mãi | Mua một lần, đề xuất 2,99 USD |

### Quy tắc quảng cáo
Mỗi quy tắc dưới đây chặn một lời chê có thật trong review của Color Picker.

| Quy tắc | Lời chê nó chặn |
|---|---|
| Phiên dùng đầu tiên không có quảng cáo và không có hộp thoại đồng ý | "I uninstalled the app within one minute" |
| Từ chối quảng cáo cá nhân hóa vẫn vào app bình thường, với quảng cáo không cá nhân hóa | "it takes me back to the page explaining that I have a choice" |
| Hộp thoại đồng ý chỉ hiện ở vùng luật yêu cầu, có nút từ chối ngang hàng nút đồng ý | "a long process to opt out" |
| Không có quảng cáo toàn màn hình tự bật, không có quảng cáo lúc mở app | "an ad that can't be closed in the middle of the screen" |
| Banner không bao giờ nằm trên màn hình camera hay che vùng ngắm, mã màu, nút bấm | "ads cover everything", "ad pop up bottom frames color picker" |
| Mất mạng hoặc quảng cáo lỗi thì chỗ đó để trống, app chạy bình thường | "showing only ads against a black screen" |
| Mua Pro là quảng cáo biến mất ngay, khôi phục được trên máy khác | "paid for add free, but they still appeared" |

**Vị trí banner:** màn hình lịch sử, chi tiết một màu, bộ sưu tập. Một banner mỗi màn hình. Màn hình camera và màn hình ảnh không có quảng cáo.

### Pro tạm thời bằng quảng cáo có thưởng
- **Điểm vào:** nút "Dùng Pro miễn phí 6 giờ" trong màn hình Pro, và xuất hiện đúng lúc người dùng chạm giới hạn (lần so màu thứ 4 trong ngày, màu thứ 21 trong lịch sử, hồ sơ hiệu chuẩn thứ 2). Ở đó luôn có hai lựa chọn cạnh nhau: xem quảng cáo, hoặc mua Pro với giá ghi rõ.
- **Luôn do người dùng tự bấm.** App không bao giờ tự phát quảng cáo có thưởng.
- **Thời lượng đề xuất:** 6 giờ mỗi lần xem, cộng dồn tối đa 24 giờ.
- **Trong thời gian Pro tạm thời:** banner ẩn hết, mọi giới hạn mở, có đồng hồ đếm nhỏ ở màn hình cài đặt.
- **Khi hết giờ:** không ngắt việc đang làm. Dữ liệu tạo trong lúc Pro (màu đã lưu, hồ sơ hiệu chuẩn) vẫn giữ và xem được; chỉ không thêm mới vượt giới hạn.
- **Khi không có quảng cáo để phát** (mất mạng, hết quảng cáo): nói rõ lý do và mời thử lại sau, không treo màn hình.
- **Kỹ thuật:** tính giờ bằng đồng hồ hệ thống kết hợp thời gian chạy của máy để việc chỉnh giờ không gia hạn được.

Vì sao 6 giờ: app được dùng theo từng việc (sơn một căn phòng, vẽ một bức tranh, đi một ca làm). 6 giờ phủ trọn một việc, nhưng người dùng thường xuyên sẽ thấy mua một lần tiện hơn. Con số này cần thử: nếu quá ít người mua Pro thì giảm xuống 2–3 giờ, nếu ít người chịu xem thì tăng lên.

### Pro vĩnh viễn
- Mua một lần trong app, đề xuất 2,99 USD. Không thuê bao.
- Giá luôn hiện trước khi bấm mua.
- Mức này nằm giữa 1,99 USD của đối thủ và khoảng 5 USD của Color Picker. Thử 1,99 và 3,99 sau khi có đủ lượt cài.

### Món mua thêm sau v1.0 (nếu có)
Gói thư viện màu sơn hoặc chuẩn công nghiệp (0,99–1,99 USD mỗi gói, sau khi kiểm tra bản quyền) và gói báo cáo so màu PDF (0,99 USD). Người đã mua Pro vẫn phải mua riêng các gói này, nên cần ghi rõ ở màn hình Pro.

### Việc bắt buộc đi kèm khi có quảng cáo
- **Hộp thoại đồng ý:** dùng nền tảng quản lý đồng ý được mạng quảng cáo chấp nhận cho các vùng có yêu cầu pháp lý (EU, Anh và một số nơi khác). Phải tự thử trường hợp "từ chối tất cả" trên máy thật trước khi phát hành.
- **Data safety:** khai đúng những gì SDK quảng cáo thu thập. Không dùng câu "không thu thập dữ liệu" ở bất kỳ đâu.
- **Chính sách quyền riêng tư** công khai, có đường dẫn trong app và trên trang cửa hàng.
- **Mục "Quyền riêng tư" trong cài đặt** để đổi lựa chọn đồng ý bất kỳ lúc nào.
- **Kiểm tra chính sách của mạng quảng cáo** về quảng cáo có thưởng và về việc phát quảng cáo khi người dùng từ chối cá nhân hóa. Mình chưa tra các điều khoản hiện hành.

### Ước tính doanh thu
Chưa có dữ liệu thật. Nhận định định tính: app nhận màu được mở vài lần mỗi tháng, mỗi lần ngắn, nên banner sẽ mang lại ít tiền trên mỗi người dùng. Phần lớn doanh thu nhiều khả năng đến từ Pro và quảng cáo có thưởng. Đo ba nguồn riêng trong 90 ngày đầu rồi mới tối ưu.

## 4. Lộ trình

| Giai đoạn | Thời gian | Việc | Kết quả để đi tiếp |
|---|---|---|---|
| 0. Chuẩn bị | 16–29/11/2026 | Thử kỹ thuật camera trên 2–3 máy. Bản nháp bảng tên màu tiếng Anh, tự thử với bộ mẫu màu in sẵn. Tra nhãn hiệu và tên miền cho tên app. Mở tài khoản mạng quảng cáo và đọc chính sách | Bản thử kỹ thuật chạy ổn; tên và package ID chốt được |
| 1. Xây lõi | 30/11–27/12/2026 | F1–F5, F7. Thử nội bộ hằng tuần với bộ mẫu màu in sẵn | Người mới chụp được màu đầu tiên trong 30 giây |
| 2. Hoàn thiện | 28/12/2026–24/1/2027 | F6, F8–F13, trợ năng, thử trên 5 máy. Tích hợp banner, quảng cáo có thưởng, hộp thoại đồng ý, mua Pro, Pro tạm thời | Hết lỗi chặn; qua bài thử 9/10 họ màu; qua đủ 7 quy tắc quảng cáo |
| 3. Thử kín | 25/1–14/2/2027 | 15–20 người dùng thật, ưu tiên người mù màu, có ít nhất 2 người ở EU/Anh để thử hộp thoại đồng ý. Chuẩn bị trang cửa hàng | Không crash nghiêm trọng; không ai kẹt ở hộp thoại đồng ý; ít nhất 70% nói sẽ dùng tiếp |
| 4. Phát hành | Tuần 15/2/2027 | Mở công khai, listing en-US | — |
| 5. v1.1 | Đến cuối 3/2027 | Sửa theo review, chỉnh thời lượng Pro tạm thời và giá theo số liệu | Điểm từ 4.5 trở lên |

Giai đoạn 2 dài hơn một tuần so với bản kế hoạch trước vì thêm phần tích hợp quảng cáo, đồng ý và mua hàng.

Nếu tài khoản Play là tài khoản cá nhân mới, Google có yêu cầu thử kín trước khi phát hành công khai. Kiểm tra điều kiện hiện hành trong Play Console ngay từ giai đoạn 0.

## 5. Sau v1.0 (theo thứ tự ưu tiên)

1. **Chế độ đèn và dây:** tối ưu cho nguồn sáng nhỏ và vật mảnh (LED trạng thái, dây điện). Miễn phí.
2. **Thư viện màu sơn / chuẩn công nghiệp (món mua thêm):** "màu gần nhất trong bảng X". Cần kiểm tra bản quyền từng hệ màu.
3. **Widget và ô cài đặt nhanh:** mở thẳng camera nhận màu.
4. **Lấy màu từ ảnh chụp màn hình** qua menu chia sẻ, chỉ đăng ký cho loại ảnh.
5. **Thêm ngôn ngữ**, bắt đầu từ các thị trường có nhiều review ở app đối thủ.
6. iOS, nếu Android đạt mục tiêu.

## 6. Đưa ra thị trường

- **Tên trên cửa hàng:** dạng "Tên app: Color Identifier & Name". Từ khóa chính: color identifier, color name, color blind, color detector, paint match.
- **Ba dòng đầu mô tả:** hành động và kết quả ("Chĩa camera vào bất cứ thứ gì và biết ngay nó màu gì, bằng từ dễ hiểu"), rồi "biết khi nào kết quả đáng tin", rồi "quảng cáo không bao giờ che camera; bỏ quảng cáo bằng một lần mua, hoặc xem một video để dùng Pro miễn phí".
- **Ảnh chụp màn hình:** (1) tên màu to trên vật thật, (2) so hai dây điện, (3) hiệu chuẩn bằng tờ giấy trắng, (4) so màu sơn tường, (5) màn hình camera sạch, không quảng cáo.
- **Mục "dành cho ai"** gọi tên từng nhóm.
- **Kênh ban đầu:** cộng đồng người mù màu, cộng đồng vẽ và thủ công, nhóm thợ điện/IT. Xin góp ý chứ không quảng cáo.
- **Trả lời mọi review trong 48 giờ**, lịch sự kể cả với review vô lý.

## 7. Chỉ số

| Chỉ số | Mục tiêu 90 ngày sau phát hành | Nguồn |
|---|---|---|
| Điểm trung bình | Từ 4.5 trở lên | Play Console |
| Lượt cài | 10.000 | Play Console |
| Tỷ lệ gỡ trong 1 ngày | Dưới 30% | Play Console |
| Tỷ lệ mua Pro | 2–3% lượt cài | Play Console |
| Tỷ lệ người dùng từng xem quảng cáo có thưởng | 10–20% | Bảng điều khiển mạng quảng cáo |
| Review 1–2 sao nhắc quảng cáo hoặc đồng ý | Dưới 5% số review | Đọc review |
| Tỷ lệ crash | Dưới 1% phiên | Android vitals |

Các mục tiêu này là ước đoán. Nếu sau 90 ngày điểm dưới 4.3, hoặc review chê quảng cáo vượt 10%, dừng thêm tính năng và sửa phần quảng cáo trước.

## 8. Rủi ro

| Rủi ro | Mức | Cách giảm |
|---|---|---|
| Lặp lại lỗi hộp thoại đồng ý của Color Picker | Cao | 7 quy tắc quảng cáo là tiêu chí phát hành; thử "từ chối tất cả" trên máy thật và với người thử ở EU/Anh |
| Camera điện thoại không đủ chính xác để giữ lời hứa "tin được" | Cao | Hứa "biết khi nào đáng tin", không hứa "chính xác". Kiểm chứng ở giai đoạn 0 |
| Một số máy không giữ khóa phơi sáng và cân bằng trắng | Trung bình–cao | Thử kỹ thuật tuần đầu trên 3 máy; phát hiện khóa không giữ và hạ độ tin cậy thay vì báo sai |
| Pro tạm thời quá hào phóng làm không ai mua Pro | Trung bình | Bắt đầu ở 6 giờ, theo dõi tỷ lệ mua, chỉnh thời lượng ở v1.1 |
| Doanh thu banner thấp do tần suất dùng thấp | Trung bình | Không dựa vào banner; nếu không đáng thì bỏ banner, chỉ giữ quảng cáo có thưởng và Pro |
| SDK quảng cáo làm app nặng, chậm mở, thêm crash | Trung bình | Chỉ khởi tạo SDK sau phiên đầu và sau khi camera đã chạy; đo thời gian mở app trước và sau |
| Mất thông điệp "không thu dữ liệu" trước Color & Palette | Trung bình | Cạnh tranh bằng tên màu dễ hiểu và độ tin cậy, không cạnh tranh bằng quyền riêng tư |
| Nhóm mù màu nhỏ, khó tìm qua tìm kiếm | Trung bình | Nhóm so màu vật thật là nhóm thứ hai ngay từ v1 |
| Bản quyền thư viện màu | Trung bình | Không đưa vào v1 |
| Lỗi theo thiết bị gây 1 sao sớm | Trung bình | Thử 5 máy, thử kín 3 tuần, nút báo lỗi kèm thông tin máy |

## 9. Việc làm ngay tuần này

1. Tra nhãn hiệu và tên miền cho "Hueno", chốt package ID.
2. Dựng bản thử kỹ thuật camera: khóa phơi sáng và cân bằng trắng, lấy trung bình vùng, làm mượt.
3. Soạn bảng họ màu tiếng Anh, in một bộ mẫu màu để thử.
4. Mở tài khoản mạng quảng cáo; đọc chính sách về quảng cáo có thưởng, về hộp thoại đồng ý và về quảng cáo không cá nhân hóa.
5. Kiểm tra điều kiện phát hành và điều kiện bán hàng trong app của tài khoản Play Console.

## 10. Đánh giá thay cho phỏng vấn

Không có phỏng vấn, nên các giả định dưới đây được chấm theo bằng chứng đang có: review trên Google Play của 6 app và gợi ý tìm kiếm của Play. Review chỉ phản ánh người chịu viết review bằng tiếng Anh, nên mức "mạnh" ở đây vẫn yếu hơn việc quan sát người dùng thật.

| Giả định | Bằng chứng | Độ chắc | Còn chưa biết |
|---|---|---|---|
| Người dùng muốn tên màu đơn giản | Cùng một phàn nàn lặp lại ở 3 app khác nhau, có review được nhiều người bấm hữu ích | Mạnh | Cách chia họ màu của mình có khớp cách họ gọi không |
| Màn hình đồng ý quảng cáo đuổi người dùng | Khoảng 21 trong 36 review 1–2 sao của Color Picker giai đoạn 2023–2026 | Mạnh | — |
| Người dùng muốn hiệu chuẩn theo vật trắng | Vài yêu cầu rõ ràng ở Color Picker; lời khen ở Color Grab | Trung bình | Bao nhiêu người thật sự chịu làm bước này |
| Người dùng loại app này chịu trả tiền một lần | Review ở Color & Palette và Color Picker nói đã mua | Trung bình | Mức giá 2,99 USD; tỷ lệ mua |
| Người mù màu là nhóm đủ lớn để làm bàn đạp | 8 review ở Color Picker, cả một app riêng 100K+ lượt tải, Play có gợi ý "color blind helper/app" | Trung bình | Quy mô thật; họ tìm app bằng từ khóa nào |
| So hai màu là tính năng đáng tiền | Suy ra từ tình huống dây điện, đèn LED; có gợi ý tìm kiếm "color compare" | Yếu | Gần như chưa ai xin tính năng này trong review |
| Xem quảng cáo có thưởng để dùng Pro 6 giờ được đón nhận | Không có bằng chứng nào trong dữ liệu đã thu | Chưa có | Tất cả |
| Camera điện thoại đủ tốt cho lời hứa "biết khi nào đáng tin" | Review trái chiều về độ chính xác | Yếu | Phải đo bằng bản thử kỹ thuật |

Nhận định chung: phần lõi (tên màu đơn giản, quảng cáo không chặn người dùng) có cơ sở tốt. Phần kiếm tiền và tính năng so hai màu là đặt cược, chưa có bằng chứng.

Cách bù miễn phí cho việc thiếu phỏng vấn:
- Giai đoạn thử kín vẫn giữ: 15–20 người dùng bản thử là nguồn phản hồi thật đầu tiên.
- Thêm nút "Gửi góp ý" trong cài đặt, mở email soạn sẵn.
- Đọc và trả lời mọi review trong 90 ngày đầu, ghi lại theo đúng bảng giả định này.
- Hai giả định yếu nhất (so hai màu, Pro tạm thời) được đo riêng bằng số liệu sau phát hành trước khi đầu tư thêm.
