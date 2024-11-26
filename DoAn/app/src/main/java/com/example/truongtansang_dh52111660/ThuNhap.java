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

public class ThuNhap extends AppCompatActivity {
    private int userId;
    private SQLiteDatabase db;
    private EditText editDanhMuc, editMoTa, editGia, editTextDate;
    private Button btnAdd, btnBack;
    private ListView lvThuNhap;
    private TextView txvSTT, tvDate, tvTien, tvDanhMuc, tvMoTa;
    private ArrayAdapter<Transactions> adapter;
    private ArrayList<Transactions> ls = new ArrayList<Transactions>();

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

                txvSTT = convertView.findViewById(R.id.tvSTT);
                tvDate = convertView.findViewById(R.id.tvDate);
                tvTien = convertView.findViewById(R.id.tvTien);
                tvDanhMuc = convertView.findViewById(R.id.tvDanhMuc);
                tvMoTa = convertView.findViewById(R.id.tvMoTa);

                Transactions transaction = getItem(position);
                if (transaction != null) {
                    txvSTT.setText(String.valueOf(transaction.getTransaction_id()));
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

        loadData();
    }

    private void loadData() {
        ls.clear();
        String sql = "SELECT t.transaction_id, t.date, t.amount, c.name, t.description " +
                "FROM Transactions t " +
                "LEFT JOIN Categories c ON t.category_id = c.category_id " +
                "WHERE t.user_id = ? AND c.type = 'TN'"; // Thay đổi type thành TN

        Cursor cursor = db.rawQuery(sql, new String[] { String.valueOf(userId) });

        if (cursor.moveToFirst()) {
            do {
                int transaction_id = cursor.getInt(0);
                String date = cursor.getString(1);
                Double amount = cursor.getDouble(2);
                String category = cursor.getString(3);
                String description = cursor.getString(4);

                Transactions tran = new Transactions(transaction_id, amount,date,  category, description);
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
            String checkCategory = "SELECT category_id FROM Categories WHERE name = ? AND type = 'TN'";
            Cursor cursor = db.rawQuery(checkCategory, new String[] { danhmuc });

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
}