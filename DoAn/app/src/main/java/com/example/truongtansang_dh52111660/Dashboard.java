package com.example.truongtansang_dh52111660;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Dashboard extends AppCompatActivity {
    private SQLiteDatabase db;
    Button chiTieu, thuNhap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
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
                startActivity(myIntent);
            }
        });
        thuNhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(Dashboard.this, ThuNhap.class);
                startActivity(myIntent);
            }
        });
    }
    private void initDatabase(){
        db = openOrCreateDatabase("database.db", MODE_PRIVATE, null);
        String sqlUsers = "CREATE TABLE IF NOT EXISTS \"Users\" (\n" +
                "\t\"user_id\"\tINTEGER,\n" +
                "\t\"username\"\tTEXT,\n" +
                "\t\"email\"\tTEXT,\n" +
                "\t\"password_hash\"\tTEXT,\n" +
                "\t\"created_at\"\tTEXT,\n" +
                "\t\"last_login\"\tTEXT,\n" +
                "\tPRIMARY KEY(\"user_id\" AUTOINCREMENT)\n" +
                ")";
        String sqlCategories = "CREATE TABLE IF NOT EXISTS \"Categories\" (\n" +
                "\t\"category_id\"\tINTEGER,\n" +
                "\t\"name\"\tTEXT,\n" +
                "\t\"type\"\tTEXT,\n" +
                "\t\"user_id\"\tINTEGER,\n" +
                "\tPRIMARY KEY(\"category_id\" AUTOINCREMENT),\n" +
                "\tCONSTRAINT \"FK_user_id\" FOREIGN KEY(\"user_id\") REFERENCES \"Users\"(\"user_id\")\n" +
                ")";
        String sqlBudgets = "CREATE TABLE IF NOT EXISTS \"Budgets\" (\n" +
                "\t\"budget_id\"\tINTEGER,\n" +
                "\t\"category_id\"\tINTEGER,\n" +
                "\t\"amount\"\tREAL,\n" +
                "\t\"start_date\"\tTEXT,\n" +
                "\t\"end_date\"\tTEXT,\n" +
                "\t\"user_id\"\tINTEGER,\n" +
                "\tPRIMARY KEY(\"budget_id\" AUTOINCREMENT),\n" +
                "\tCONSTRAINT \"FK_budget_categories\" FOREIGN KEY(\"category_id\") REFERENCES \"Categories\"(\"category_id\"),\n" +
                "\tCONSTRAINT \"FK_budget_user\" FOREIGN KEY(\"user_id\") REFERENCES \"Users\"(\"user_id\")\n" +
                ")";
        String sqlTrans = "CREATE TABLE \"Transactions\" (\n" +
                "\t\"transaction_id\"\tINTEGER,\n" +
                "\t\"amount\"\tREAL,\n" +
                "\t\"date\"\tTEXT,\n" +
                "\t\"description\"\tTEXT,\n" +
                "\t\"category_id\"\tINTEGER,\n" +
                "\t\"user_id\"\tINTEGER,\n" +
                "\tPRIMARY KEY(\"transaction_id\" AUTOINCREMENT),\n" +
                "\tCONSTRAINT \"FK_trans_categories\" FOREIGN KEY(\"category_id\") REFERENCES \"Categories\"(\"category_id\"),\n" +
                "\tCONSTRAINT \"FK_trans_user\" FOREIGN KEY(\"user_id\") REFERENCES \"Users\"(\"user_id\")\n" +
                ")";
        String sqlNotifications = "CREATE TABLE \"Notifications\" (\n" +
                "\t\"notification_id\"\tINTEGER,\n" +
                "\t\"user_id\"\tINTEGER,\n" +
                "\t\"message\"\tTEXT,\n" +
                "\t\"date\"\tTEXT,\n" +
                "\t\"status\"\tNUMERIC,\n" +
                "\tPRIMARY KEY(\"notification_id\" AUTOINCREMENT),\n" +
                "\tCONSTRAINT \"FK_Notification_user\" FOREIGN KEY(\"user_id\") REFERENCES \"Users\"(\"user_id\")\n" +
                ")";
        db.execSQL(sqlUsers);
        db.execSQL(sqlCategories);
        db.execSQL(sqlBudgets);
        db.execSQL(sqlTrans);
        db.execSQL(sqlNotifications);

    }
}