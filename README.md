# Bai 7.2 - Quick Chat

Quick Chat la ung dung instant chat don gian xay dung bang Spring Boot, luu du lieu bang cac file JSON va storage local. Du an mo phong mot he thong chat noi bo voi dang nhap, ket ban, gui tin nhan 1-1, gui file va nhan tin moi bang long polling.

## Tong quan

He thong gom cac thanh phan chinh:

- **Auth**: dang nhap bang username/password hoac Google email
- **Friend service**: xem danh sach ban be va them ban
- **Message service**: gui text, gui file, nhan message moi
- **File service**: luu file vao `storage/` va cap link tai file
- **Token interceptor**: kiem tra access token cho cac API can xac thuc

## Tinh nang

- Dang nhap bang tai khoan local da co san
- Dang nhap bang Google email
- Tao access token va luu token kem thoi gian tao / het han trong user data
- Lay danh sach ban be cua user dang request
- Ket ban 2 chieu
- Chat 1-1 giua 2 user
- Gui tin nhan text
- Gui file/anh/bat ky file nao khi chat
- Luu file dinh kem vao thu muc `storage/`
- Nhan message moi bang long polling toi da 10 giay
- Tra ve link download khi message la file
- Bao ve file, chi nguoi gui hoac nguoi nhan moi duoc tai

## Cong nghe su dung

| Thanh phan | Thu vien / Cong nghe |
|---|---|
| Ngon ngu | Java 17 |
| Framework | Spring Boot 3.3.5 |
| Web | Spring Web |
| JSON | `org.json` |
| Hash mat khau | BCrypt (`at.favre.lib:bcrypt`) |
| Build | Maven |

## Cau truc thu muc

```text
src/main/java/com/bai72/quickchat/
    QuickChatApplication.java   Diem vao ung dung Spring Boot
    config/
        AppDataInitializer.java Khoi tao data folder, storage va 5 user mau
        AppProperties.java      Cau hinh data-dir, storage-dir, token-ttl, long-poll-timeout
        WebConfig.java          Dang ky interceptor xac thuc
    controller/
        AuthController.java     API dang nhap local va Google
        FriendController.java    API danh sach ban va them ban
        MessageController.java   API gui / nhan message
        FileController.java     API tai file
        ApiExceptionHandler.java Chuan hoa loi tra ve
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
    QuickChat.postman_collection.json
    demo-upload.txt
```

## Du lieu luu tru

Du an khong dung database quan he. Thay vao do, du lieu duoc luu bang cac file JSON cuc bo:

- `data/users.json`: thong tin user, ban be, token va thoi gian het han
- `data/queues.json`: hang doi message cua tung user
- `data/files.json`: metadata cua file dinh kem
- `storage/`: noi luu file upload

## Du lieu khoi tao

Khi chay lan dau, server se tu tao 5 user mau:

- `alice`
- `bob`
- `carol`
- `dave`
- `erin`

Mat khau chung:

- `Pass@123`

Google email mau:

- `alice@gmail.com`
- `bob@gmail.com`
- `carol@gmail.com`
- `dave@gmail.com`
- `erin@gmail.com`

## Build va chay

### Yeu cau

- Java 17
- Maven 3.x

### Chay ung dung

```bash
mvn spring-boot:run
```

Hoac dong goi:

```bash
mvn clean package
java -jar target/quick-chat-1.0.0.jar
```

Mac dinh ung dung chay tai:

```text
http://localhost:8080
```

## Xac thuc

Tat ca API, tru `POST /auth/login` va `POST /auth/google`, deu can access token.

Token co the truyen bang mot trong hai header:

- `Authorization: Bearer <token>`
- `Access-Token: <token>`

Token duoc kiem tra hop le va het han trong `UserStore`. Theo cau hinh mac dinh, token ton tai trong `24h`.

## API

### 1. Dang nhap local

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

### 2. Dang nhap Google

`POST /auth/google`

Body:

```json
{
  "email": "newuser@gmail.com",
  "name": "newuser"
}
```

Neu email chua ton tai, server se tao user moi.

### 3. Lay danh sach ban be

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

### 4. Ket ban

`POST /friends`

Body:

```json
{
  "username": "bob"
}
```

Ket ban la hai chieu: neu Alice them Bob, ca hai ben deu nhan nhau la ban.

### 5. Gui tin nhan text

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

### 6. Gui tin nhan file

`POST /messages`

Content-Type:

```text
multipart/form-data
```

Fields:

- `username`: nguoi nhan
- `message`: mo ta text, co the de trong
- `file`: file dinh kem

Neu message la file, server se luu vao `storage/` va tra ve link download o phan message cua nguoi nhan.

### 7. Nhan message moi

`GET /messages`

Header:

```text
Authorization: Bearer <token>
```

Cu phap long polling:

- Neu user dang co message trong queue, server tra ve ngay
- Neu chua co message, server cho toi da `10s`
- Het `10s` ma khong co message moi thi tra ve danh sach rong `[]`

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

Neu la file, `message` se la link dang:

```text
/files/<storedFileName>
```

### 8. Tai file

`GET /files/{name}`

Header:

```text
Authorization: Bearer <token>
```

Quyen truy cap:

- Chi nguoi gui hoac nguoi nhan cua file moi duoc tai
- File khong ton tai se tra ve `404`

## Trang thai gui message

API gui message tra ve `status` de mo phong trang thai xu ly:

- `1`: nguoi nhan dang online, message duoc day ngay
- `2`: nguoi nhan offline, message duoc dua vao hang cho
- `3`: nguoi gui khong nam trong danh sach ban cua nguoi nhan

## Postman

Thu muc `postman/` da co san collection de test nhanh:

- `postman/QuickChat.postman_collection.json`
- `postman/demo-upload.txt`

Collection gom san cac request:

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

Goi y luong test nhanh:

1. Login Alice va Bob.
2. Xem danh sach ban be.
3. Them Bob vao ban be neu can.
4. Alice gui text sang Bob.
5. Bob goi `GET /messages`.
6. Alice gui file sang Bob.
7. Bob nhan message file va dung link download.

## Ghi chu

- `src/main/resources/application.properties` cau hinh port `8080`, thu muc `data/` va `storage/`, token TTL va long-poll timeout
- `WebConfig` chua interceptor xac thuc cho tat ca API tru `POST /auth/login` va `POST /auth/google`
- Du an uu tien su dung JSON file de luu trang thai server
- Dung luong file upload duoc xu ly theo dau vao multipart va luu metadata trong `files.json`

## Checklist nhanh

1. Server chay duoc tren `localhost:8080`
2. User login thanh cong va nhan access token
3. Lay danh sach ban be bang token
4. Gui text va gui file thanh cong
5. Nhan message moi bang long polling
6. Tai file chi khi co quyen
7. Du lieu server luu trong cac file JSON va `storage/`

