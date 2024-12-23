package com.example.truongtansang_dh52111660;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.util.ArrayList;
import com.github.mikephil.charting.formatter.PercentFormatter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.chip.Chip;

import com.github.mikephil.charting.components.Legend;

import java.util.Arrays;

/**
 * Dashboard Activity - Màn hình chính hiển thị tổng quan tài chính
 * Hiển thị biểu đồ, số dư, thu nhập và chi tiêu theo các khoảng thời gian
 */
public class Dashboard extends AppCompatActivity {
    // Constants cho bộ lọc thời gian
    private static final int FILTER_DAY = 1;
    private static final int FILTER_WEEK = 2;
    private static final int FILTER_MONTH = 3;
    private int currentFilter = FILTER_DAY;

    // UI Components
    private ChipGroup timeFilterGroup;
    private PieChart pieChart;
    private TextView valSoDu, valTotal, valTongChiTieu;

    // Database và user info
    private SQLiteDatabase db;
    private int userId;

    // Constants cho định dạng ngày tháng
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    private static final SimpleDateFormat SQL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("MM/yyyy");

    // Constants cho loại giao dịch
    private static final String TRANSACTION_TYPE_INCOME = "TN";
    private static final String TRANSACTION_TYPE_EXPENSE = "CT";

    // Các câu truy vấn SQL cơ bản
    private static final String BASE_QUERY = "SELECT COALESCE(SUM(amount), 0) FROM Transactions " +
            "WHERE user_id = ? AND %s AND category_id IN " +
            "(SELECT category_id FROM Categories WHERE type = ? AND user_id = ?)";

    // Điều kiện lọc theo thời gian
    private static final String DATE_CONDITION = "date = ?";
    private static final String DATE_RANGE_CONDITION = "date BETWEEN ? AND ?";
    private static final String MONTH_CONDITION = "strftime('%Y-%m', date) = strftime('%Y-%m', ?)";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        db = openOrCreateDatabase("database.db", MODE_PRIVATE, null);
        userId = getIntent().getIntExtra("USER_ID", -1);

        if (userId == -1) {
            Toast.makeText(this, "Có lỗi xảy ra!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupTimeFilter();
        loadFinancialData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (db == null || !db.isOpen()) {
            db = openOrCreateDatabase("database.db", MODE_PRIVATE, null);
        }
        loadFinancialData();
    }

    /**
     * Khởi tạo các thành phần UI và thiết lập ban đầu
     */
    private void initializeViews() {
        // Khởi tạo các view từ layout
        pieChart = findViewById(R.id.pieChart);
        valSoDu = findViewById(R.id.valSoDu);
        valTotal = findViewById(R.id.valTotal);
        valTongChiTieu = findViewById(R.id.valTongChiTieu);
        timeFilterGroup = findViewById(R.id.timeFilterGroup);

        // Thiết lập các thành phần
        setupPieChart();
        setupButtons();

        // Thiết lập filter mặc định
        Chip chipDay = findViewById(R.id.radioDay);
        chipDay.setChecked(true);
    }

    /**
     * Thiết lập bộ lọc thời gian
     */
    private void setupTimeFilter() {
        timeFilterGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Cập nhật filter dựa trên selection
            if (checkedId == R.id.radioDay)
                currentFilter = FILTER_DAY;
            else if (checkedId == R.id.radioWeek)
                currentFilter = FILTER_WEEK;
            else if (checkedId == R.id.radioMonth)
                currentFilter = FILTER_MONTH;
            loadFinancialData();
        });
    }

    /**
     * Thiết lập các nút chức năng
     */
    private void setupButtons() {
        // Button thêm chi tiêu
        findViewById(R.id.btnNhapChiTieu).setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, ChiTieu.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        // Button thêm thu nhập
        findViewById(R.id.btnNhapThuNhap).setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, ThuNhap.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });
    }

    /**
     * Tải dữ liệu tài chính theo filter đã chọn
     */
    private void loadFinancialData() {
        try {
            // Kiểm tra điều kiện
            if (userId <= 0 || db == null || !db.isOpen()) {
                Log.e("Dashboard", "Invalid state in loadFinancialData");
                return;
            }

            // Xác định điều kiện truy vấn theo filter
            String condition;
            String[] baseParams;
            Calendar calendar = Calendar.getInstance();

            // Thiết lập params theo loại filter
            switch (currentFilter) {
                case FILTER_DAY:
                    condition = DATE_CONDITION;
                    baseParams = new String[] { SQL_DATE_FORMAT.format(calendar.getTime()) };
                    break;
                case FILTER_WEEK:
                    Calendar weekAgo = Calendar.getInstance();
                    weekAgo.add(Calendar.DAY_OF_YEAR, -7);
                    condition = DATE_RANGE_CONDITION;
                    baseParams = new String[] {
                            SQL_DATE_FORMAT.format(weekAgo.getTime()),
                            SQL_DATE_FORMAT.format(calendar.getTime())
                    };
                    Log.d("Dashboard", "Week filter: " + Arrays.toString(baseParams));
                    break;
                case FILTER_MONTH:
                    condition = MONTH_CONDITION;
                    baseParams = new String[] { SQL_DATE_FORMAT.format(calendar.getTime()) };
                    Log.d("Dashboard", "Month filter params: " + Arrays.toString(baseParams));
                    break;
                default:
                    return;
            }

            // Thực hiện truy vấn và cập nhật UI
            String query = String.format(BASE_QUERY, condition);
            String[] incomeParams = createQueryParams(baseParams, TRANSACTION_TYPE_INCOME);
            String[] expenseParams = createQueryParams(baseParams, TRANSACTION_TYPE_EXPENSE);

            double income = executeQuery(query, incomeParams);
            double expense = executeQuery(query, expenseParams);

            updateUI(income, expense);
        } catch (Exception e) {
            handleError(e);
        }
    }

    /**
     * Thiết lập biểu đồ tròn
     */
    private void setupPieChart() {
        // Cấu hình cơ bản
        pieChart.setDrawHoleEnabled(true);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(12);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setCenterText("Thống kê\ntài chính");
        pieChart.setCenterTextSize(16);
        pieChart.getDescription().setEnabled(false);

        // Cấu hình nâng cao
        pieChart.setDrawEntryLabels(true);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);

        // Thiết lập chú thích
        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
    }

    /**
     * Cập nhật dữ liệu biểu đồ
     * 
     * @param income  Tổng thu nhập
     * @param expense Tổng chi tiêu
     */
    private void updatePieChart(double income, double expense) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        // Thêm dữ liệu vào biểu đồ
        if (income > 0 || expense > 0) {
            entries.add(new PieEntry((float) income, "Thu nhập"));
            entries.add(new PieEntry((float) expense, "Chi tiêu"));
        } else {
            entries.add(new PieEntry(1, "Chưa có giao dịch"));
        }

        // Thiết lập và cập nhật dữ liệu
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(15f);
        data.setValueFormatter(new PercentFormatter(pieChart));

        pieChart.setData(data);
        pieChart.invalidate();
        pieChart.animateY(1000);
    }

    /**
     * Cập nhật thông tin tài chính trên UI
     * 
     * @param income  Tổng thu nhập
     * @param expense Tổng chi tiêu
     */
    private void updateFinancialInfo(double income, double expense) {
        double balance = income - expense;
        valSoDu.setText(String.format("%,.0f VNĐ", balance));
        valTotal.setText(String.format("%,.0f VNĐ", income));
        valTongChiTieu.setText(String.format("%,.0f VNĐ", expense));
    }

    // Lifecycle methods
    @Override
    protected void onResume() {
        super.onResume();
        // Kiểm tra và tải lại dữ liệu khi activity được resume
        if (userId <= 0) {
            Log.e("Dashboard", "Invalid user ID");
            finish();
            return;
        }

        if (db == null || !db.isOpen()) {
            db = openOrCreateDatabase("database.db", MODE_PRIVATE, null);
        }

        loadFinancialData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }

    private String[] createQueryParams(String[] baseParams, String type) {
        String[] params = new String[baseParams.length + 3];
        params[0] = String.valueOf(userId);
        System.arraycopy(baseParams, 0, params, 1, baseParams.length);
        params[params.length - 2] = type;
        params[params.length - 1] = String.valueOf(userId);
        return params;
    }

    private double executeQuery(String query, String[] params) {
        double result = 0;
        try (Cursor cursor = db.rawQuery(query, params)) {
            if (cursor != null && cursor.moveToFirst()) {
                result = cursor.getDouble(0);
                Log.d("Dashboard", String.format("Query executed - Type: %s, Amount: %.2f",
                        params[params.length - 2], result));
            } else {
                Log.w("Dashboard", "No results found for query");
            }
        } catch (Exception e) {
            Log.e("Dashboard", "Query error: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    private void updateUI(double income, double expense) {
        runOnUiThread(() -> {
            try {
                Log.d("Dashboard", String.format("Updating UI - Income: %.2f, Expense: %.2f",
                        income, expense));
                updatePieChart(income, expense);
                updateFinancialInfo(income, expense);
                updateTimeRangeTitle(getTimeRangeTitle());
            } catch (Exception e) {
                Log.e("Dashboard", "Error updating UI", e);
                handleError(e);
            }
        });
    }

    private void handleError(Exception e) {
        Log.e("Dashboard", "Error loading financial data", e);
        e.printStackTrace();
        Toast.makeText(this, "Không thể tải dữ liệu tài chính", Toast.LENGTH_SHORT).show();
    }

    private String getTimeRangeTitle() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy");

        switch (currentFilter) {
            case FILTER_DAY:
                return "Ngày " + displayFormat.format(calendar.getTime());
            case FILTER_WEEK:
                Calendar weekAgo = Calendar.getInstance();
                weekAgo.add(Calendar.DAY_OF_YEAR, -7);
                return String.format("Tuần %s - %s",
                        displayFormat.format(weekAgo.getTime()),
                        displayFormat.format(calendar.getTime()));
            case FILTER_MONTH:
                return "Tháng " + new SimpleDateFormat("MM/yyyy").format(calendar.getTime());
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

    @Override
    protected void onStop() {
        super.onStop();
        // Đóng database khi activity dừng
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

}