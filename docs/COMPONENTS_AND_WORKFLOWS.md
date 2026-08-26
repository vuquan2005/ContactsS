# Chi Tiết Hoạt Động & Quy Trình Từng Thành Phần (Components & Workflows)

Tài liệu này giải thích chi tiết cơ chế vận hành, quy trình xử lý (Workflows) và tương tác qua lại giữa các màn hình, thành phần trong hệ thống **ContactVIP**.

---

## 1. Bản Đồ Thành Phần Ứng Dụng (Component Map)

```mermaid
graph TD
    MA[MainActivity - Bottom Navigation Host]
    
    subgraph Tabs["Các Tab Chính (Bottom Navigation)"]
        CF[ContactsFragment - Tab Danh Bạ]
        FF[FavoritesFragment - Tab Yêu Thích]
        RF[RecentsFragment - Tab Lịch Sử Cuộc Gọi]
        SF[SettingsActivity - Màn Hình Cài Đặt]
    end

    subgraph Screens["Các Màn Hình Chức Năng (Activities)"]
        DA[DialerActivity - Bàn Phím T9 & Gợi Ý]
        AEA[AddEditContactActivity - Thêm / Sửa Liên Hệ]
        CDA[ContactDetailActivity - Xem Chi Tiết Liên Hệ]
    end

    MA --> CF
    MA --> FF
    MA --> RF
    MA --> SF

    MA -.->|Nút FAB động| AEA
    MA -.->|Nút FAB động| DA
    
    CF -->|Chạm vào liên hệ| CDA
    CF -->|Nút Thêm / Sửa| AEA
    FF -->|Chạm vào liên hệ| CDA
    RF -->|Nút Gọi Lại 1 chạm| CU[CallUtils - Gọi Điện]
    DA -->|Thực hiện cuộc gọi| CU
    CDA -->|Gọi / Nhắn tin / Chia sẻ| CU
    CDA -->|Nút Sửa| AEA
```

---

## 2. Chi Tiết Các Thành Phần Cốt Lõi

### 2.1. `MainActivity` & Điều Hướng Động (Dynamic FAB Navigation)
- **Vai trò**: Điểm vào chính của ứng dụng, lưu trữ `BottomNavigationView` chứa 4 màn hình: **Danh bạ**, **Yêu thích**, **Gần đây**, **Cài đặt**.
- **Cơ chế Nút Hành Động Nổi (Floating Action Button - FAB)**:
  - Khi người dùng ở Tab **Danh bạ**: FAB tự động chuyển sang icon **Thêm mới (`ic_add`)** $\rightarrow$ bấm vào sẽ mở `AddEditContactActivity`.
  - Khi người dùng ở các Tab khác (**Yêu thích, Gần đây, Cài đặt**): FAB tự động chuyển sang icon **Bàn phím quay số (`ic_dialer`)** $\rightarrow$ bấm vào sẽ mở `DialerActivity`.

---

### 2.2. Module Danh Sách Danh Bạ (`ContactsFragment`, `ContactAdapter`, `AlphabetIndexView`)

```mermaid
flowchart TD
    Start([Người dùng mở Tab Danh Bạ]) --> LoadData[Nạp dữ liệu từ ContactViewModel]
    LoadData --> Display[Render RecyclerView + Alphabet Bar]
    
    UserAction{Hành động người dùng}
    UserAction -->|Nhập ô tìm kiếm| Search[Lọc tức thời theo Tên hoặc Số điện thoại]
    UserAction -->|Chạm thanh A-Z bên phải| ScrollAZ[Cuộn tức thì đến vị trí ký tự đầu tiên]
    UserAction -->|Chọn bộ lọc Nhóm| FilterGroup[Lọc theo nhóm: Family, Work, Friends...]
    UserAction -->|Vuốt liên hệ sang Phải| SwipeCall[Thực hiện cuộc gọi ngay lập tức]
    UserAction -->|Vuốt liên hệ sang Trái| SwipeDelete[Hiển thị hộp thoại xác nhận xóa]
    UserAction -->|Chạm vào thẻ liên hệ| OpenDetail[Mở ContactDetailActivity]
```

- **Thanh Chỉ Mục A-Z (`AlphabetIndexView`)**: View tùy biến vẽ 26 ký tự chữ cái dọc theo mép phải màn hình, hỗ trợ cử chỉ vuốt ngón tay (`ACTION_MOVE`) để cuộn danh bạ tức thì kèm phản hồi haptic.
- **Vuốt Để Thao Tác (`ContactSwipeCallback`)**: Tích hợp `ItemTouchHelper` vẽ icon nền:
  - Vuốt phải: Nền xanh lá + Icon Gọi điện $\rightarrow$ Gọi số điện thoại chính.
  - Vuốt trái: Nền đỏ + Icon Thùng rác $\rightarrow$ Xác nhận xóa liên hệ.

---

### 2.3. Module Thêm / Chỉnh Sửa Danh Bạ (`AddEditContactActivity`)

```mermaid
sequenceDiagram
    autonumber
    actor User as Người dùng
    participant Act as AddEditContactActivity
    participant Picker as PhotoPicker Launcher
    participant AccUtil as AccountUtils
    participant VM as ContactViewModel
    participant Sync as SystemContactsSyncManager

    User->>Act: Mở màn hình Thêm/Sửa
    Act->>AccUtil: getAvailableAccounts(context)
    AccUtil-->>Act: Trả về danh sách (Google Account, Thiết bị, SIM)
    Act->>Act: Điền thông tin cũ (nếu là Sửa)
    
    opt Thay đổi ảnh đại diện
        User->>Act: Bấm "Đổi ảnh đại diện"
        Act->>Picker: Mở Android PhotoPicker (Image Only)
        Picker-->>Act: Trả về URI ảnh mới + cấp Persistable Permission
        Act->>Act: Hiển thị Preview ảnh đại diện
    end

    opt Thêm/Bớt số điện thoại
        User->>Act: Bấm "Thêm số điện thoại"
        Act->>Act: Tạo thêm dòng ItemPhoneInputBinding
    end

    User->>Act: Chọn nơi lưu (Google / Thiết bị / SIM) ở cuối trang
    User->>Act: Bấm nút "Lưu liên hệ" (btn_save)
    Act->>Act: Validate Họ tên & Định dạng Email
    Act->>VM: saveContactWithPhones(contact, phones, groups)
    VM->>Sync: Lưu vào CSDL Room và đồng bộ lên Google/SIM
    Sync-->>Act: Hoàn tất -> Đóng màn hình & Toast thông báo
```

- **Quản lý đa số điện thoại động**: Sử dụng `ItemPhoneInputBinding` được thêm/bớt động vào `LinearLayout container`.
- **Hỗ trợ chọn vị trí lưu (`AccountUtils`)**: Tự động phát hiện các tài khoản Google đã đăng nhập và các tài khoản thiết bị để người dùng quyết định nơi lưu trữ danh bạ.

---

### 2.4. Module Xem Chi Tiết Danh Bạ (`ContactDetailActivity`)
- **Avatar Hero Header**: Hiển thị ảnh đại diện kích thước lớn, tự động tạo Avatar chữ cái màu sắc ngẫu nhiên chuẩn Material nếu không có ảnh.
- **Huy Hiệu Vị Trí Lưu (Storage Account Badge)**: Hiển thị ngay dưới tên liên hệ (ví dụ: `Google: user@gmail.com` hoặc `Thiết bị`).
- **Thanh Thao Tác Nhanh (Quick Actions)**:
  - **Gọi điện (`ic_call`)**: Gọi số điện thoại chính.
  - **Nhắn tin (`ic_message`)**: Mở ứng dụng SMS mặc định gửi tin nhắn.
  - **Yêu thích (`ic_star`)**: Bật/tắt trạng thái Starred tức thì.
  - **Chia sẻ (`ic_share`)**: Chia sẻ thông tin liên hệ dưới dạng văn bản (Họ tên + Các số điện thoại + Email).
- **Xử lý Trạng Thái Rỗng (Empty States)**: Khi một liên hệ không có Email, Công ty, Địa chỉ hoặc Ghi chú, màn hình hiển thị thông báo *"Chưa có thông tin bổ sung"* tinh tế, giữ giao diện luôn cân đối.

---

### 2.5. Module Bàn Phím Quay Số Thông Minh (`DialerActivity` - Smart T9 Dialer)

```mermaid
flowchart TD
    UserType[Người dùng bấm phím số: 2..9] --> T9Engine[Công cụ phân tích T9]
    T9Engine --> QueryMatch[Khớp mẫu số điện thoại HOẶC ký tự T9 trên tên]
    QueryMatch --> LiveFilter[Cập nhật danh sách gợi ý phía trên bàn phím]
    LiveFilter --> RenderList[Hiển thị RecyclerView gợi ý liên hệ]
    
    UserTap{Người dùng chạm}
    UserTap -->|Chạm vào liên hệ gợi ý| DirectCall[Thực hiện cuộc gọi đến người đó]
    UserTap -->|Bấm nút Gọi chính| DialNumber[Thực hiện cuộc gọi đến số đã nhập]
    UserTap -->|Bấm giữ phím Xóa| ClearAll[Xóa toàn bộ dãy số đã nhập]
```

- **Bản đồ T9 Keypad**:
  - `2: ABC`, `3: DEF`, `4: GHI`, `5: JKL`, `6: MNO`, `7: PQRS`, `8: TUV`, `9: WXYZ`.
- **Tìm kiếm kết hợp**: Khớp đồng thời số điện thoại và chuỗi ký tự chuyển đổi từ chữ cái họ tên sang số T9.

---

### 2.6. Module Lịch Sử Cuộc Gọi (`RecentsFragment`, `CallHistoryAdapter`)
- **Phân loại cuộc gọi trực quan**:
  - `INCOMING`: Icon mũi tên xanh lá chỉ xuống.
  - `OUTGOING`: Icon mũi tên xanh dương chỉ lên.
  - `MISSED`: Icon cuộc gọi nhỡ màu **Đỏ rực** nổi bật, kèm số điện thoại màu đỏ để người dùng dễ nhận biết.
  - `REJECTED`: Icon từ chối cuộc gọi.
- **Nút Gọi Lại 1 Chạm (Quick Callback)**: Nút gọi nhanh bên phải mỗi dòng cho phép gọi lại ngay lập tức.
- **Xóa & Hoàn Tác (Delete with Undo)**: Khi xóa lịch sử cuộc gọi, một thanh `Snackbar` sẽ xuất hiện với nút **"Hoàn tác" (Undo)** trong 4 giây trước khi dữ liệu thực sự bị xóa vĩnh viễn khỏi hệ thống.

---

### 2.7. Module Cài Đặt Giao Diện (`SettingsActivity`, `PreferenceUtils`)

```mermaid
sequenceDiagram
    autonumber
    actor User as Người dùng
    participant SetAct as SettingsActivity
    participant Pref as PreferenceUtils (SharedPreferences)
    participant App as AppCompatDelegate

    User->>SetAct: Chọn Chế độ Giao diện (Sáng / Tối / Mặc định hệ thống)
    SetAct->>Pref: Lưu giá trị Theme Mode (LIGHT / DARK / SYSTEM)
    SetAct->>App: setDefaultNightMode(MODE_NIGHT_YES / NO / FOLLOW_SYSTEM)
    App-->>SetAct: Tự động đổi Theme toàn bộ các Activity ngay lập tức
```

---

### 2.8. Module Đồng Bộ Hệ Thống (`SystemContactsSyncManager`)

```mermaid
sequenceDiagram
    autonumber
    participant OS as Hệ điều hành Android
    participant Obs as ContentObserver (Debounce 1.5s)
    participant Sync as SystemContactsSyncManager
    participant DB as Room Database (AppDatabase)

    OS->>Obs: Bắn sự kiện onChange (Danh bạ hệ thống thay đổi)
    Obs->>Sync: Kích hoạt syncAllFromSystem()
    Sync->>OS: Truy vấn ContactsContract.Data (JOIN RawContacts)
    OS-->>Sync: Trả về Cursor dữ liệu toàn bộ danh bạ
    Sync->>DB: Thực thi db.runInTransaction(...)
    Note over Sync,DB: 1. Thêm mới các liên hệ chưa có<br/>2. Cập nhật các liên hệ đã có<br/>3. Xóa các liên hệ không còn tồn tại trên máy
    DB-->>Sync: Hoàn tất Transaction
    DB-->>DB: Tự động phát LiveData cho toàn bộ UI
```
