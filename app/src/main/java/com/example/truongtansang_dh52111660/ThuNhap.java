package com.example.truongtansang_dh52111660;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.app.DatePickerDialog;
import android.widget.DatePicker;
import java.util.Calendar;
import java.util.ArrayList;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.app.AlertDialog;
import android.widget.AdapterView;

// Class quản lý chức năng thu nhập của ứng dụng
public class ThuNhap extends AppCompatActivity {
    // Khai báo các biến thành viên
    private int userId; // ID của người dùng hiện tại
    private SQLiteDatabase db; // Database instance
    private EditText editDanhMuc; // Trường nhập danh mục thu nhập
    private EditText editMoTa; // Trường nhập mô tả
    private EditText editGia; // Trường nhập số tiền
    private EditText editTextDate; // Trường nhập ngày
    private Button btnAdd; // Nút thêm/sửa thu nhập
    private Button btnBack; // Nút quay lại
    private ListView lvThuNhap; // ListView hiển thị danh sách thu nhập
    private TextView tvSTT, tvDate, tvTien, tvDanhMuc, tvMoTa; // Các TextView hiển thị thông tin
    private ArrayAdapter<Transactions> adapter; // Adapter cho ListView
    private ArrayList<Transactions> ls; // Danh sách các giao dịch thu nhập

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thu_nhap);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userId = getIntent().getIntExtra("USER_ID", -1);
        editDanhMuc = findViewById(R.id.editDanhMuc);
        editMoTa = findViewById(R.id.editMoTa);
        editGia = findViewById(R.id.editGiaTien);
        editTextDate = findViewById(R.id.editTextDate);

        lvThuNhap = findViewById(R.id.lvTN);
        adapter = new ArrayAdapter<Transactions>(this, android.R.layout.simple_list_item_1, ls) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.data_item, null);

                tvSTT = convertView.findViewById(R.id.tvSTT);
                tvDate = convertView.findViewById(R.id.tvDate);
                tvTien = convertView.findViewById(R.id.tvTien);
                tvDanhMuc = convertView.findViewById(R.id.tvDanhMuc);
                tvMoTa = convertView.findViewById(R.id.tvMoTa);

                Transactions transaction = getItem(position);
                if (transaction != null) {
                    tvSTT.setText(String.valueOf(position + 1));
                    tvDate.setText(transaction.getDate());
                    tvTien.setText(String.valueOf(transaction.getAmount()));
                    tvDanhMuc.setText(transaction.getCategory());
                    tvMoTa.setText(transaction.getDescription());
                }

                return convertView;
            }
        };
        lvThuNhap.setAdapter(adapter);

        db = openOrCreateDatabase("database.db", MODE_PRIVATE, null);

        btnBack = findViewById(R.id.btnBack);
        btnAdd = findViewById(R.id.btnAdd);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addData();
            }
        });

        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        lvThuNhap.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Transactions transaction = ls.get(position);
                editDanhMuc.setText(transaction.getCategory());
                editMoTa.setText(transaction.getDescription());
                editGia.setText(String.valueOf(transaction.getAmount()));
                editTextDate.setText(transaction.getDate());

                btnAdd.setText("Sửa");
                btnAdd.setOnClickListener(v -> updateData(transaction.getTransaction_id()));
            }
        });

        lvThuNhap.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Transactions transaction = ls.get(position);
                new AlertDialog.Builder(ThuNhap.this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa mục này?")
                        .setPositiveButton("Xóa", (dialog, which) -> deleteData(transaction.getTransaction_id()))
                        .setNegativeButton("Hủy", null)
                        .show();
                return true;
            }
        });

        loadData();
    }

    // Phương thức tải dữ liệu từ database
    private void loadData() {
        ls.clear(); // Xóa dữ liệu cũ

        // Query lấy danh sách thu nhập của user hiện tại
        String sql = "SELECT t.transaction_id, t.date, t.amount, c.name, t.description " +
                "FROM Transactions t " +
                "LEFT JOIN Categories c ON t.category_id = c.category_id " +
                "WHERE t.user_id = ? AND c.type = 'TN'";

        // Thực hiện query và xử lý kết quả
        Cursor cursor = db.rawQuery(sql, new String[] { String.valueOf(userId) });

        if (cursor.moveToFirst()) {
            do {
                int transaction_id = cursor.getInt(0);
                String date = cursor.getString(1);
                Double amount = cursor.getDouble(2);
                String category = cursor.getString(3);
                String description = cursor.getString(4);

                Transactions tran = new Transactions(transaction_id, amount, date, category, description);
                ls.add(tran);
            } while (cursor.moveToNext());
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    // Phương thức thêm thu nhập mới
    private void addData() {
        String danhmuc = editDanhMuc.getText().toString().trim();
        String mota = editMoTa.getText().toString().trim();
        String ngay = editTextDate.getText().toString().trim();

        if (danhmuc.isEmpty() || mota.isEmpty() || ngay.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double sotien = Double.parseDouble(editGia.getText().toString());

            // Kiểm tra xem danh mục đã tồn tại chưa
            String checkCategory = "SELECT category_id FROM Categories WHERE name = ? AND type = ? AND user_id = ?";

            Cursor cursor = db.rawQuery(checkCategory, new String[] { danhmuc, "TN", String.valueOf(userId) });

            long categoryId;

            if (cursor.moveToFirst()) {
                categoryId = cursor.getLong(0);
            } else {
                String insertCategory = "INSERT INTO Categories (name, type, user_id) VALUES (?, 'TN', ?)";
                db.execSQL(insertCategory, new Object[] { danhmuc, userId });

                cursor = db.rawQuery("SELECT last_insert_rowid()", null);
                cursor.moveToFirst();
                categoryId = cursor.getLong(0);
            }
            cursor.close();

            String insertTransaction = "INSERT INTO Transactions (amount, date, description, category_id, user_id) " +
                    "VALUES (?, ?, ?, ?, ?)";
            db.execSQL(insertTransaction, new Object[] { sotien, ngay, mota, categoryId, userId });

            editDanhMuc.setText("");
            editMoTa.setText("");
            editGia.setText("");
            editTextDate.setText("");

            hideKeyboard();

            Toast.makeText(this, "Thêm thu nhập thành công", Toast.LENGTH_SHORT).show();

            loadData();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Vui lòng nhập số tiền hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }

    // Phương thức hiển thị DatePicker để chọn ngày
    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                            int monthOfYear, int dayOfMonth) {
                        String selectedDate = String.format("%04d-%02d-%02d", year, monthOfYear + 1, dayOfMonth);
                        editTextDate.setText(selectedDate);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Phương thức cập nhật thông tin thu nhập
    private void updateData(int transactionId) {
        String danhmuc = editDanhMuc.getText().toString().trim();
        String mota = editMoTa.getText().toString().trim();
        String ngay = editTextDate.getText().toString().trim();

        if (danhmuc.isEmpty() || mota.isEmpty() || ngay.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double sotien = Double.parseDouble(editGia.getText().toString());

            // Lấy category_id hiện tại của giao dịch
            String getCurrentCategory = "SELECT c.category_id, c.name FROM Transactions t " +
                    "JOIN Categories c ON t.category_id = c.category_id " +
                    "WHERE t.transaction_id = ? AND t.user_id = ?";
            Cursor currentCursor = db.rawQuery(getCurrentCategory,
                    new String[] { String.valueOf(transactionId), String.valueOf(userId) });

            long categoryId;
            if (currentCursor.moveToFirst()) {
                categoryId = currentCursor.getLong(0);
                String currentCategoryName = currentCursor.getString(1);

                // Nếu tên danh mục thay đổi, cập nhật trong bảng Categories
                if (!danhmuc.equals(currentCategoryName)) {
                    String updateCategory = "UPDATE Categories SET name = ? " +
                            "WHERE category_id = ? AND user_id = ? AND type = 'TN'";
                    db.execSQL(updateCategory, new Object[] { danhmuc, categoryId, userId });
                }
            }
            currentCursor.close();

            // Cập nhật giao dịch
            String updateTransaction = "UPDATE Transactions SET " +
                    "amount = ?, " +
                    "date = ?, " +
                    "description = ? " +
                    "WHERE transaction_id = ? AND user_id = ?";

            db.execSQL(updateTransaction, new Object[] {
                    sotien,
                    ngay,
                    mota,
                    transactionId,
                    userId
            });

            // Xóa nội dung các EditText
            editDanhMuc.setText("");
            editMoTa.setText("");
            editGia.setText("");
            editTextDate.setText("");

            // Đổi lại text của button
            btnAdd.setText("Thêm");
            btnAdd.setOnClickListener(v -> addData());

            hideKeyboard();

            Toast.makeText(this, "Cập nhật thu nhập thành công", Toast.LENGTH_SHORT).show();

            // Cập nhật lại ListView
            loadData();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Vui lòng nhập số tiền hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }

    // Phương thức xóa thu nhập
    private void deleteData(int transactionId) {
        // Xóa dữ liệu trong cơ sở dữ liệu
        String deleteSql = "DELETE FROM Transactions WHERE transaction_id = ?";
        db.execSQL(deleteSql, new Object[] { transactionId });
        loadData();
        Toast.makeText(this, "Đã xóa thu nhập", Toast.LENGTH_SHORT).show();
    }
}