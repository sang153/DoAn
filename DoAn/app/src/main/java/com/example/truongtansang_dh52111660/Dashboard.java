package com.example.truongtansang_dh52111660;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.util.ArrayList;
import com.github.mikephil.charting.formatter.PercentFormatter;

public class Dashboard extends AppCompatActivity {
    private PieChart pieChart;
    private SQLiteDatabase db;
    private int userId;
    private TextView valSoDu, valTotal, valTongChiTieu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Khởi tạo database trước khi sử dụng
        db = openOrCreateDatabase("database.db", MODE_PRIVATE, null);

        // Lấy userId từ Intent
        userId = getIntent().getIntExtra("USER_ID", -1);
        if (userId == -1) {
            Toast.makeText(this, "Có lỗi xảy ra!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        pieChart = findViewById(R.id.pieChart);
        valSoDu = findViewById(R.id.valSoDu);
        valTotal = findViewById(R.id.valTotal);
        valTongChiTieu = findViewById(R.id.valTongChiTieu);

        loadFinancialData();
        setupPieChart();

        // Xử lý sự kiện click button Chi tiêu
        Button btnChiTieu = findViewById(R.id.btnNhapChiTieu);
        btnChiTieu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, ChiTieu.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            }
        });

        // Xử lý sự kiện click button Thu nhập
        Button btnThuNhap = findViewById(R.id.btnNhapThuNhap);
        btnThuNhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, ThuNhap.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            }
        });
    }

    private void loadFinancialData() {
        try {
            // Tính tổng thu nhập
            String incomeQuery = "SELECT SUM(amount) FROM Transactions " +
                    "WHERE user_id = ? AND category_id IN " +
                    "(SELECT category_id FROM Categories WHERE type = 'TN' AND user_id = ?)";
            Cursor incomeCursor = db.rawQuery(incomeQuery,
                    new String[] { String.valueOf(userId), String.valueOf(userId) });

            // Tính tổng chi tiêu
            String expenseQuery = "SELECT SUM(amount) FROM Transactions " +
                    "WHERE user_id = ? AND category_id IN " +
                    "(SELECT category_id FROM Categories WHERE type = 'CT' AND user_id = ?)";
            Cursor expenseCursor = db.rawQuery(expenseQuery,
                    new String[] { String.valueOf(userId), String.valueOf(userId) });

            double income = 0;
            double expense = 0;

            if (incomeCursor.moveToFirst()) {
                income = incomeCursor.getDouble(0);
            }
            if (expenseCursor.moveToFirst()) {
                expense = expenseCursor.getDouble(0);
            }

            incomeCursor.close();
            expenseCursor.close();

            // Sửa từ updateChart thành updatePieChart
            updatePieChart(income, expense);

            // Cập nhật TextView hiển thị số liệu
            updateFinancialInfo(income, expense);

        } catch (Exception e) {
            Log.e("Dashboard", "Error loading financial data", e);
            Toast.makeText(this, "Không thể tải dữ liệu tài chính", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupPieChart() {
        pieChart.setDrawHoleEnabled(true);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(12);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setCenterText("Thống kê\ntài chính");
        pieChart.setCenterTextSize(16);
        pieChart.getDescription().setEnabled(false);
    }

    private void updatePieChart(double income, double expense) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        if (income > 0)
            entries.add(new PieEntry((float) income, "Thu nhập"));
        if (expense > 0)
            entries.add(new PieEntry((float) expense, "Chi tiêu"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(15f);
        data.setValueFormatter(new PercentFormatter(pieChart));

        pieChart.setData(data);
        pieChart.invalidate(); // refresh
        pieChart.animateY(1000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cập nhật dữ liệu khi quay lại màn hình
        loadFinancialData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }

    private void updateFinancialInfo(double income, double expense) {
        double balance = income - expense;

        // Cập nhật các TextView
        valSoDu.setText(String.format("%,.0f VNĐ", balance));
        valTotal.setText(String.format("%,.0f VNĐ", income));
        valTongChiTieu.setText(String.format("%,.0f VNĐ", expense));
    }
}