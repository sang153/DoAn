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

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class ChiTieu extends AppCompatActivity {
    private SQLiteDatabase db;
    private EditText editDanhMuc, editMoTa, editGia;
    private Button btnAdd, btnBack;
    private ListView lvChiTieu;
    private TextView txvSTT, tvDate, tvTien, tvDanhMuc, tvMoTa;
    private ArrayAdapter<Transactions> adapter;
    private ArrayList<Transactions> ls = new ArrayList<Transactions>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chi_tieu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        editDanhMuc = findViewById(R.id.editDanhMuc);
        editMoTa = findViewById(R.id.editMoTa);
        editGia = findViewById(R.id.editGia);
        lvChiTieu = findViewById(R.id.lvChiTieu);
        adapter = new ArrayAdapter<Categories>(this,0, ls){
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.data_item,null);

                txvSTT = convertView.findViewById(R.id.tvSTT);
                tvDate = convertView.findViewById(R.id.tvDate);
                tvTien = convertView.findViewById(R.id.tvTien);
                tvDanhMuc = convertView.findViewById(R.id.tvDanhMuc);
                tvMoTa = convertView.findViewById(R.id.tvMoTa);

                return convertView;
            }
        };
        lvChiTieu.setAdapter(adapter);
    }
    private void loadData() {
        ls.clear();
        String sql = "SELECT (transaction_id, date, amount, name, description ) FROM Transactions";
        Cursor cursor = db.rawQuery(sql, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int transaction_id = cursor.getInt(0);
            String date = cursor.getString(1);
            Double amount = cursor.getDouble(2);
            String name = cursor.getString(3);
            String description = cursor.getString(4);

            Transactions tran = new Transactions(transaction_id, amount, description);
            ls.add(tran);
            cursor.moveToNext();
        }
        adapter.notifyDataSetChanged();
    }
}