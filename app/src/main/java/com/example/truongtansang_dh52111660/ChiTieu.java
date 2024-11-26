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

import java.util.ArrayList;
import android.app.DatePickerDialog;
import android.widget.DatePicker;
import java.util.Calendar;

public class ChiTieu extends AppCompatActivity {
    private int userId;
    private SQLiteDatabase db;
    private EditText editDanhMuc, editMoTa, editGia, editTextDate;
    private Button btnAdd, btnBack;
    private ListView lvChiTieu;
    private TextView txvSTT, tvDate, tvTien, tvDanhMuc, tvMoTa;
    private ArrayAdapter<Transactions> adapter;
    private ArrayList<Transactions> ls = new ArrayList<Transactions>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tieu);
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

        lvChiTieu = findViewById(R.id.lvChiTieu);
        adapter = new ArrayAdapter<Transactions>(this, android.R.layout.simple_list_item_1, ls) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.data_item, null);

                txvSTT = convertView.findViewById(R.id.tvSTT);
                tvDate = convertView.findViewById(R.id.tvDate);
                tvTien = convertView.findViewById(R.id.tvTien);
                tvDanhMuc = convertView.findViewById(R.id.tvDanhMuc);
                tvMoTa = convertView.findViewById(R.id.tvMoTa);

                Transactions transaction = getItem(position);
                if (transaction != null) {
                    txvSTT.setText(String.valueOf(transaction.getTransaction_id()));
                    tvTien.setText(String.valueOf(transaction.getAmount()));
                    tvMoTa.setText(transaction.getDescription());
                    tvDanhMuc.setText(transaction.getCategory());
                    tvDate.setText(transaction.getDate());
                }

                return convertView;
            }
        };
        lvChiTieu.setAdapter(adapter);

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

        loadData();
    }

    private void loadData() {
        ls.clear();
        String sql = "SELECT t.transaction_id, t.date, t.amount, c.name, t.description " +
                "FROM Transactions t " +
                "LEFT JOIN Categories c ON t.category_id = c.category_id " +
                "WHERE t.user_id = ? AND c.type = 'CT'";

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
            String checkCategory = "SELECT category_id FROM Categories WHERE name = ? AND type = 'CT'";
            Cursor cursor = db.rawQuery(checkCategory, new String[] { danhmuc });

            long categoryId;

            if (cursor.moveToFirst()) {
                // Nếu danh mục đã tồn tại, lấy category_id
                categoryId = cursor.getLong(0);
            } else {
                // Nếu danh mục chưa tồn tại, tạo mới với type = 'CT'
                String insertCategory = "INSERT INTO Categories (name, type, user_id) VALUES (?, 'CT', ?)";
                db.execSQL(insertCategory, new Object[] { danhmuc, userId });

                // Lấy category_id vừa tạo
                cursor = db.rawQuery("SELECT last_insert_rowid()", null);
                cursor.moveToFirst();
                categoryId = cursor.getLong(0);
            }
            cursor.close();

            // Thêm chi tiêu mới với category_id đã có
            String insertTransaction = "INSERT INTO Transactions (amount, date, description, category_id, user_id) " +
                    "VALUES (?, ?, ?, ?, ?)";
            db.execSQL(insertTransaction, new Object[] { sotien, ngay, mota, categoryId, userId });

            // Xóa nội dung các EditText sau khi thêm
            editDanhMuc.setText("");
            editMoTa.setText("");
            editGia.setText("");
            editTextDate.setText("");

            Toast.makeText(this, "Thêm chi tiêu thành công", Toast.LENGTH_SHORT).show();

            // Cập nhật lại ListView
            loadData();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Vui lòng nhập số tiền hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }

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
                        // Định dạng ngày tháng theo chuẩn SQL
                        String selectedDate = String.format("%04d-%02d-%02d", year, monthOfYear + 1, dayOfMonth);
                        editTextDate.setText(selectedDate);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }
}