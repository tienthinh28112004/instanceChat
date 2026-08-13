# Bài 7.2 - Instance Chat

Instance Chat là ứng dụng instant chat đơn giản xây dựng bằng Spring Boot, lưu dữ liệu bằng các file JSON và storage local. Dự án mô phỏng một hệ thống chat nội bộ với đăng nhập, kết bạn, gửi tin nhắn 1-1, gửi file và nhận tin mới bằng long polling.

## Tổng quan

Hệ thống gồm các thành phần chính:

- **Auth**: đăng nhập bằng username/password hoặc Google email
- **Friend service**: xem danh sách bạn bè và thêm bạn
- **Message service**: gửi text, gửi file, nhận message mới
- **File service**: lưu file vào `storage/` và cấp link tải file
- **Token interceptor**: kiểm tra access token cho các API cần xác thực

## Tính năng

- Đăng nhập bằng tài khoản local đã có sẵn
- Đăng nhập bằng Google email
- Tạo access token và lưu token kèm thời gian tạo / hết hạn trong user data
- Lấy danh sách bạn bè của user đang request
- Kết bạn 2 chiều
- Chat 1-1 giữa 2 user
- Gửi tin nhắn text
- Gửi file/ảnh/bất kỳ file nào khi chat
- Lưu file đính kèm vào thư mục `storage/`
- Nhận message mới bằng long polling tối đa 10 giây
- Trả về link download khi message là file
- Bảo vệ file, chỉ người gửi hoặc người nhận mới được tải

## Công nghệ sử dụng

| Thành phần | Thư viện / Công nghệ |
|---|---|
| Ngôn ngữ | Java 17 |
| Framework | Spring Boot 3.3.5 |
| Web | Spring Web |
| JSON | `org.json` |
| Hash mật khẩu | BCrypt (`at.favre.lib:bcrypt`) |
| Build | Maven |

## Cấu trúc thư mục

```text
src/main/java/com/bai72/instancechat/
    InstanceChatApplication.java  Điểm vào ứng dụng Spring Boot
    config/
        AppDataInitializer.java  Khởi tạo data folder, storage và 5 user mẫu
        AppProperties.java        Cấu hình data-dir, storage-dir, token-ttl, long-poll-timeout
        WebConfig.java             Đăng ký interceptor xác thực
    controller/
        AuthController.java       API đăng nhập local và Google
        FriendController.java     API danh sách bạn và thêm bạn
        MessageController.java     API gửi / nhận message
        FileController.java        API tải file
        ApiExceptionHandler.java   Chuẩn hóa lỗi trả về
    dto/
        AccessTokenResponse.java
        ErrorResponse.java
        FriendRequest.java
        GoogleLoginRequest.java
        LoginRequest.java
        MessageView.java
        SendResult.java
        TextMessageRequest.java
    model/
        UserAccount.java
        QueuedMessage.java
        FileRecord.java
    service/
        AuthService.java
        FriendService.java
        MessageService.java
        FileService.java
    store/
        JsonStore.java
        UserStore.java
        QueueStore.java
        FileStore.java
    web/
        AccessTokenInterceptor.java
data/
    users.json
    queues.json
    files.json
storage/
postman/
    InstanceChat.postman_collection.json
    demo-upload.txt
```

## Dữ liệu lưu trữ

Dự án không dùng database quan hệ. Thay vào đó, dữ liệu được lưu bằng các file JSON cục bộ:

- `data/users.json`: thông tin user, bạn bè, token và thời gian hết hạn
- `data/queues.json`: hàng đợi message của từng user
- `data/files.json`: metadata của file đính kèm
- `storage/`: nơi lưu file upload

## Dữ liệu khởi tạo

Khi chạy lần đầu, server sẽ tự tạo 5 user mẫu:

- `alice`
- `bob`
- `carol`
- `dave`
- `erin`

Mật khẩu chung:

- `Pass@123`

Google email mẫu:

- `alice@gmail.com`
- `bob@gmail.com`
- `carol@gmail.com`
- `dave@gmail.com`
- `erin@gmail.com`

## Build và chạy

### Yêu cầu

- Java 17
- Maven 3.x

### Chạy ứng dụng

```bash
mvn spring-boot:run
```

Hoặc đóng gói:

```bash
mvn clean package
java -jar target/instance-chat-1.0.0.jar
```

Mặc định ứng dụng chạy tại:

```text
http://localhost:8080
```

## Xác thực

Tất cả API, trừ `POST /auth/login` và `POST /auth/google`, đều cần access token.

Token có thể truyền bằng một trong hai header:

- `Authorization: Bearer <token>`
- `Access-Token: <token>`

Token được kiểm tra hợp lệ và hết hạn trong `UserStore`. Theo cấu hình mặc định, token tồn tại trong `24h`.

## API

### 1. Đăng nhập local

`POST /auth/login`

Body:

```json
{
  "username": "alice",
  "password": "Pass@123"
}
```

Response:

```json
{
  "accessToken": "..."
}
```

### 2. Đăng nhập Google

`POST /auth/google`

Body:

```json
{
  "email": "newuser@gmail.com",
  "name": "newuser"
}
```

Nếu email chưa tồn tại, server sẽ tạo user mới.

### 3. Lấy danh sách bạn bè

`GET /friends`

Header:

```text
Authorization: Bearer <token>
```

Response:

```json
[
  "bob",
  "carol"
]
```

### 4. Kết bạn

`POST /friends`

Body:

```json
{
  "username": "bob"
}
```

Kết bạn là hai chiều: nếu Alice thêm Bob, cả hai bên đều nhận nhau là bạn.

### 5. Gửi tin nhắn text

`POST /messages`

Header:

```text
Authorization: Bearer <token>
```

Body:

```json
{
  "username": "bob",
  "message": "hello"
}
```

### 6. Gửi tin nhắn file

`POST /messages`

Content-Type:

```text
multipart/form-data
```

Fields:

- `username`: người nhận
- `message`: mô tả text, có thể để trống
- `file`: file đính kèm

Nếu message là file, server sẽ lưu vào `storage/` và trả về link download ở phần message của người nhận.

### 7. Nhận message mới

`GET /messages`

Header:

```text
Authorization: Bearer <token>
```

Cú pháp long polling:

- Nếu user đang có message trong queue, server trả về ngay
- Nếu chưa có message, server chờ tối đa `10s`
- Hết `10s` mà không có message mới thì trả về danh sách rỗng `[]`

Response:

```json
[
  {
    "time": "2026-08-13T09:00:00Z",
    "sender": "alice",
    "message": "Hello Bob"
  }
]
```

Nếu là file, `message` sẽ là link dạng:

```text
/files/<storedFileName>
```

### 8. Tải file

`GET /files/{name}`

Header:

```text
Authorization: Bearer <token>
```

Quyền truy cập:

- Chỉ người gửi hoặc người nhận của file mới được tải
- File không tồn tại sẽ trả về `404`

## Trạng thái gửi message

API gửi message trả về `status` để mô phỏng trạng thái xử lý:

- `1`: người nhận đang online, message được đẩy ngay
- `2`: người nhận offline, message được đưa vào hàng chờ
- `3`: người gửi không nằm trong danh sách bạn của người nhận

## Postman

Thư mục `postman/` đã có sẵn collection để test nhanh:

- `postman/InstanceChat.postman_collection.json`
- `postman/demo-upload.txt`

Collection gồm sẵn các request:

- Login Alice
- Login Bob
- Google login demo
- Get friends
- Add friend
- Send text
- Send file
- Bob receives text
- Bob receives file
- Download file

Gợi ý luồng test nhanh:

1. Login Alice và Bob.
2. Xem danh sách bạn bè.
3. Thêm Bob vào bạn bè nếu cần.
4. Alice gửi text sang Bob.
5. Bob gọi `GET /messages`.
6. Alice gửi file sang Bob.
7. Bob nhận message file và dùng link download.

## Ghi chú

- `src/main/resources/application.properties` cấu hình port `8080`, thư mục `data/` và `storage/`, token TTL và long-poll timeout
- `WebConfig` chứa interceptor xác thực cho tất cả API trừ `POST /auth/login` và `POST /auth/google`
- Dự án ưu tiên sử dụng JSON file để lưu trạng thái server
- Dung lượng file upload được xử lý theo đầu vào multipart và lưu metadata trong `files.json`
