package com.example.truongtansang_dh52111660;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Dashboard extends AppCompatActivity {
    private SQLiteDatabase db;
    Button chiTieu, thuNhap;
    private int userId;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Nhận user_id từ Intent
        userId = getIntent().getIntExtra("USER_ID", -1);
        username = getIntent().getStringExtra("USERNAME");

        if (userId == -1) {
            // Nếu không có user_id hợp lệ, quay về màn hình đăng nhập
            finish();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        chiTieu = findViewById(R.id.btnNhapChiTieu);
        thuNhap = findViewById(R.id.btnNhapThuNhap);

        chiTieu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(Dashboard.this, ChiTieu.class);
                // Truyền tiếp user_id sang ChiTieu
                myIntent.putExtra("USER_ID", userId);
                startActivity(myIntent);
            }
        });

        thuNhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(Dashboard.this, ThuNhap.class);
                // Truyền tiếp user_id sang ThuNhap
                myIntent.putExtra("USER_ID", userId);
                startActivity(myIntent);
            }
        });
    }
}