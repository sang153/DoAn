package com.example.truongtansang_dh52111660;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {
    private Button btnRegister;
    private EditText txtEmail, txtPassword;
    private SQLiteDatabase db;
    private users user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDatabase();
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);

        // Khởi tạo Firebase
        try {
            FirebaseApp.initializeApp(getApplicationContext());
        } catch (Exception e) {
            Log.e("MainActivity", "Firebase initialization failed", e);
        }

        findViewById(R.id.btnDangNhap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = txtEmail.getText().toString();
                String pwd = txtPassword.getText().toString();
                if(accountChecking(email, pwd)){
                    Intent myItent = new Intent(MainActivity.this, Dashboard.class);
                    startActivity(myItent);
                }
            }
        });

        btnRegister = findViewById(R.id.btnDangKy);
        btnRegister.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Register.class)));
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
        String sqlTrans = "CREATE TABLE IF NOT EXISTS \"Transactions\" (\n" +
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
        String sqlNotifications = "CREATE TABLE IF NOT EXISTS \"Notifications\" (\n" +
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
    private boolean accountChecking(String mail, String pwd){
        String sql = "SELECT * FROM Users WHERE (email ='" + mail + "') and ( password_hash='" + pwd + "')" ;
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            int user_id = cursor.getInt(0);
            String username = cursor.getString(1);
            String email = cursor.getString(2);
            String password_hash = cursor.getString(3);
            String created_at = cursor.getString(4);
            String last_login = cursor.getString(5);
            user = new users(user_id,username,email,password_hash,created_at,last_login);
            return true;
        }
        cursor.close();
        return false;
    }
    private void addData(){
        String sql = "INSERT INTO Users (username, email, password_hash) VALUES ('Sang Truong', 'sangtruong123@gmail.com', '123456')";
        db.execSQL(sql);
    }
}