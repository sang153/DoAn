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
import android.widget.RadioGroup;
import android.widget.RadioButton;

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
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Dashboard extends AppCompatActivity {
    private RadioGroup timeFilterGroup;
    private static final int FILTER_DAY = 1;
    private static final int FILTER_WEEK = 2;
    private static final int FILTER_MONTH = 3;
    private int currentFilter = FILTER_DAY;

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

        timeFilterGroup = findViewById(R.id.timeFilterGroup);
        timeFilterGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d("Dashboard", "Radio button changed: " + checkedId); // Debug log

                if (checkedId == R.id.radioDay) {
                    currentFilter = FILTER_DAY;
                    Log.d("Dashboard", "Selected: Day"); // Debug log
                } else if (checkedId == R.id.radioWeek) {
                    currentFilter = FILTER_WEEK;
                    Log.d("Dashboard", "Selected: Week"); // Debug log
                } else if (checkedId == R.id.radioMonth) {
                    currentFilter = FILTER_MONTH;
                    Log.d("Dashboard", "Selected: Month"); // Debug log
                }

                // Load data based on new filter
                loadFinancialData();
            }
        });

        initializeViews();
        loadFinancialData();

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

    private void initializeViews() {
        // Initialize other views
        pieChart = findViewById(R.id.pieChart);
        valSoDu = findViewById(R.id.valSoDu);
        valTotal = findViewById(R.id.valTotal);
        valTongChiTieu = findViewById(R.id.valTongChiTieu);

        // Ensure radioDay is checked by default
        RadioButton radioDay = findViewById(R.id.radioDay);
        radioDay.setChecked(true);
    }

    private void loadFinancialData() {
        try {
            String incomeQuery;
            String expenseQuery;
            String[] queryParams;

            // Lấy ngày hiện tại
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String currentDate = dateFormat.format(calendar.getTime());

            // Tạo câu query dựa trên filter
            switch (currentFilter) {
                case FILTER_DAY:
                    // Query cho ngày hiện tại
                    incomeQuery = "SELECT SUM(amount) FROM Transactions " +
                            "WHERE user_id = ? AND date = ? AND category_id IN " +
                            "(SELECT category_id FROM Categories WHERE type = 'TN' AND user_id = ?)";
                    expenseQuery = "SELECT SUM(amount) FROM Transactions " +
                            "WHERE user_id = ? AND date = ? AND category_id IN " +
                            "(SELECT category_id FROM Categories WHERE type = 'CT' AND user_id = ?)";
                    queryParams = new String[] { String.valueOf(userId), currentDate, String.valueOf(userId) };
                    break;

                case FILTER_WEEK:
                    // Query cho tuần hiện tại
                    calendar.add(Calendar.DAY_OF_YEAR, -7);
                    String weekAgo = dateFormat.format(calendar.getTime());
                    incomeQuery = "SELECT SUM(amount) FROM Transactions " +
                            "WHERE user_id = ? AND date BETWEEN ? AND ? AND category_id IN " +
                            "(SELECT category_id FROM Categories WHERE type = 'TN' AND user_id = ?)";
                    expenseQuery = "SELECT SUM(amount) FROM Transactions " +
                            "WHERE user_id = ? AND date BETWEEN ? AND ? AND category_id IN " +
                            "(SELECT category_id FROM Categories WHERE type = 'CT' AND user_id = ?)";
                    queryParams = new String[] { String.valueOf(userId), weekAgo, currentDate, String.valueOf(userId) };
                    break;

                case FILTER_MONTH:
                    // Query cho tháng hiện tại
                    calendar = Calendar.getInstance();
                    String yearMonth = new SimpleDateFormat("yyyy-MM").format(calendar.getTime());
                    incomeQuery = "SELECT SUM(amount) FROM Transactions " +
                            "WHERE user_id = ? AND strftime('%Y-%m', date) = ? AND category_id IN " +
                            "(SELECT category_id FROM Categories WHERE type = 'TN' AND user_id = ?)";
                    expenseQuery = "SELECT SUM(amount) FROM Transactions " +
                            "WHERE user_id = ? AND strftime('%Y-%m', date) = ? AND category_id IN " +
                            "(SELECT category_id FROM Categories WHERE type = 'CT' AND user_id = ?)";
                    queryParams = new String[] { String.valueOf(userId), yearMonth, String.valueOf(userId) };
                    break;

                default:
                    return;
            }

            // Thực hiện query thu nhập
            Cursor incomeCursor = db.rawQuery(incomeQuery, queryParams);
            double income = 0;
            if (incomeCursor.moveToFirst()) {
                income = incomeCursor.getDouble(0);
            }
            incomeCursor.close();

            // Thực hiện query chi tiêu
            Cursor expenseCursor = db.rawQuery(expenseQuery, queryParams);
            double expense = 0;
            if (expenseCursor.moveToFirst()) {
                expense = expenseCursor.getDouble(0);
            }
            expenseCursor.close();

            // Cập nhật biểu đồ và thông tin
            updatePieChart(income, expense);
            updateFinancialInfo(income, expense);

            // Cập nhật tiêu đề thời gian
            String timeRange = getTimeRangeTitle();
            updateTimeRangeTitle(timeRange);

        } catch (Exception e) {
            Log.e("Dashboard", "Error loading financial data", e);
            Toast.makeText(this, "Không thể tải dữ liệu tài chính", Toast.LENGTH_SHORT).show();
        }
    }

    private String getTimeRangeTitle() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM/yyyy");

        switch (currentFilter) {
            case FILTER_DAY:
                return "Ngày " + dateFormat.format(calendar.getTime());
            case FILTER_WEEK:
                Calendar weekAgo = Calendar.getInstance();
                weekAgo.add(Calendar.DAY_OF_YEAR, -7);
                return "Tuần " + dateFormat.format(weekAgo.getTime()) + " - " + dateFormat.format(calendar.getTime());
            case FILTER_MONTH:
                return "Tháng " + monthFormat.format(calendar.getTime());
            default:
                return "";
        }
    }

    private void updateTimeRangeTitle(String timeRange) {
        // Thêm TextView để hiển thị khoảng thời gian
        TextView timeRangeView = findViewById(R.id.timeRangeTitle);
        if (timeRangeView != null) {
            timeRangeView.setText(timeRange);
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