# ContactVIP - Smart Phone & Contacts

Ứng dụng quản lý Danh bạ, Bàn phím quay số và Lịch sử cuộc gọi thông minh cho Android, xây dựng theo kiến trúc MVVM hiện đại với giao diện chuẩn Material Design 3 (hỗ trợ Light & Dark Theme).

---

## 📱 Tính Năng Chính

### 1. Quản Lý Danh Bạ Toàn Diện (Contacts)
- **Thêm & Chỉnh sửa danh bạ**:
  - Hỗ trợ thêm nhiều số điện thoại với nhãn tùy biến (Mobile, Home, Work, Other).
  - Điền đầy đủ thông tin: Họ tên, Email, Công ty, Chức vụ, Địa chỉ, Ghi chú (Notes).
  - Tùy chọn ảnh đại diện từ thư viện ảnh hoặc chụp trực tiếp.
- **Tìm kiếm & Phân loại**:
  - Thanh tìm kiếm tức thì theo tên hoặc số điện thoại.
  - Thanh chỉ mục chữ cái A-Z cuộn nhanh (Alphabet Fast Indexing).
  - Sắp xếp (A-Z, Z-A) và lọc theo nhóm (Family, Friends, Work,...).
- **Xem Chi Tiết Danh Bạ (Contact Detail)**:
  - Thanh thao tác nhanh: Gọi điện, Nhắn tin (SMS), Đánh dấu Yêu thích, Chia sẻ thông tin liên hệ.
  - Xử lý trạng thái rỗng thông minh (*Empty States*) cho các trường thông tin chưa nhập.
  - Vuốt nhanh (Swipe) để gọi điện hoặc xóa liên hệ.

### 2. Bàn Phím Quay Số Thông Minh (Smart Dialer)
- Bàn phím số T9 tiêu chuẩn với âm phản hồi trực quan.
- Tự động gợi ý tên danh bạ tương ứng ngay khi bấm số.
- Nút gọi nhanh và nút xóa ký tự hỗ trợ bấm giữ để xóa toàn bộ.

### 3. Màn Hình Cuộc Gọi Thực Tế (Call Screen)
- Giao diện cuộc gọi hiện đại với Avatar bo tròn và thông tin người gọi.
- Bộ điều khiển trong cuộc gọi: Bật/tắt micro (Mute), Bàn phím số (Keypad), Loa ngoài (Speaker), Kết thúc cuộc gọi (End Call).
- Tự động ghi lại lịch sử cuộc gọi sau khi kết thúc.

### 4. Danh Bạ Yêu Thích (Favorites)
- Quản lý danh sách các liên hệ thường xuyên liên lạc.
- Bật/tắt trạng thái yêu thích chỉ với 1 chạm.

### 5. Lịch Sử Cuộc Gọi (Recents / Call History)
- Phân loại rõ ràng: Cuộc gọi đến (*Incoming*), Cuộc gọi đi (*Outgoing*), Cuộc gọi nhỡ (*Missed*).
- Hiển thị thời gian, số điện thoại và tên liên hệ kèm nút gọi lại nhanh 1 chạm.

### 6. Cài Đặt Giao Diện & Theme (Settings)
- Hỗ trợ 3 chế độ hiển thị: **System Default**, **Light Theme** và **Dark Theme**.
- Toàn bộ màu chữ, màu thẻ và thanh điều hướng tự động tối ưu độ tương phản trên mọi chế độ.

---

## 🛠 Kiến Trúc & Công Nghệ (Tech Stack)

- **Ngôn ngữ**: Java / Android SDK (Min SDK: 24, Target SDK: 34)
- **Kiến trúc**: **MVVM (Model - View - ViewModel)** + **Repository Pattern**
- **Cơ sở dữ liệu cục bộ**: **Room Database** (SQLite abstraction)
  - `Contact`: Bảng thông tin chính của danh bạ.
  - `ContactPhone`: Quan hệ 1-N lưu các số điện thoại của một danh bạ.
  - `ContactGroup` & `ContactGroupCrossRef`: Quản lý danh mục nhóm và liên kết nhiều-nhiều.
  - `CallHistory`: Lưu nhật ký các cuộc gọi.
- **UI Components**:
  - **Material Design 3 (M3)** Components (`MaterialCardView`, `MaterialToolbar`, `ShapeableImageView`, `FloatingActionButton`, `ChipGroup`, `TextInputLayout`).
  - **View Binding** cho tương tác an toàn với view.
  - **LiveData & ViewModel** để quản lý trạng thái phản hồi mượt mà theo vòng đời Activity/Fragment.
- **Xử lý bất đồng bộ**: `ExecutorService` & `ThreadPool` đảm bảo các tác vụ database chạy ngầm không gây giật lag UI (main thread).

---

## 📁 Cấu Trúc Dự Án

```
app/src/main/java/com/example/contactvip/
├── ContactApplication.java       # Khởi tạo ứng dụng & áp dụng theme
├── MainActivity.java             # Điều hướng chính với BottomNavigationView
├── adapter/                      # Adapters cho RecyclerView (Contacts, History)
│   ├── ContactAdapter.java
│   └── CallHistoryAdapter.java
├── data/                         # Tầng dữ liệu (Room DB, DAO, Entities, Repository)
│   ├── dao/
│   ├── database/
│   ├── entity/
│   └── repository/
├── ui/                           # Tầng giao diện người dùng
│   ├── call/                    # Màn hình cuộc gọi (CallActivity)
│   ├── contacts/                # Danh sách, thêm/sửa, chi tiết danh bạ
│   ├── dialer/                  # Bàn phím quay số (DialerActivity)
│   ├── favorites/               # Tab liên hệ yêu thích
│   ├── recents/                 # Tab lịch sử cuộc gọi
│   └── settings/                # Màn hình cài đặt theme
├── utils/                        # Tiện ích (Avatar, Preferences)
└── viewmodel/                    # ViewModels cung cấp dữ liệu cho UI
```

---

## 🚀 Hướng Dẫn Biên Dịch & Chạy

1. Mở dự án trong **Android Studio** (hoặc môi trường build Gradle).
2. Đồng bộ Gradle dependencies.
3. Chạy lệnh:
   ```bash
   ./gradlew assembleDebug
   ```
4. Cài đặt APK hoặc khởi chạy trực tiếp trên thiết bị Android / Emulator (Android 7.0+).
