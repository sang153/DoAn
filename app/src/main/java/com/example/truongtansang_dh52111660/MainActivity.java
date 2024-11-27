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
import android.widget.TextView;

import com.google.firebase.FirebaseApp;

/**
 * MainActivity - Màn hình đăng nhập chính của ứng dụng
 * Xử lý việc đăng nhập người dùng và khởi tạo cơ sở dữ liệu
 */
public class MainActivity extends AppCompatActivity {
    // Khai báo các thành phần UI và biến toàn cục
    private Button btnRegister;
    private EditText txtEmail, txtPassword;
    private SQLiteDatabase db;
    private users user;
    private TextView txtLoginError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo cơ sở dữ liệu và các thành phần UI
        initDatabase();
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        txtLoginError = findViewById(R.id.txtLoginError);

        // Khởi tạo Firebase cho các tính năng phụ thuộc
        try {
            FirebaseApp.initializeApp(getApplicationContext());
        } catch (Exception e) {
            Log.e("MainActivity", "Firebase initialization failed", e);
        }

        // Xử lý sự kiện click nút đăng nhập
        findViewById(R.id.btnDangNhap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // Lấy thông tin đăng nhập từ form
                    String email = txtEmail.getText().toString();
                    String pwd = txtPassword.getText().toString();

                    // Kiểm tra dữ liệu đầu vào
                    if (email.isEmpty() || pwd.isEmpty()) {
                        txtLoginError.setText("Vui lòng nhập đầy đủ thông tin!");
                        txtLoginError.setVisibility(View.VISIBLE);
                        return;
                    }

                    // Kiểm tra tài khoản và chuyển hướng nếu thành công
                    if (accountChecking(email, pwd)) {
                        Intent myIntent = new Intent(MainActivity.this, Dashboard.class);
                        myIntent.putExtra("USER_ID", user.getUser_id());
                        myIntent.putExtra("USERNAME", user.getUsername());
                        startActivity(myIntent);
                    } else {
                        txtLoginError.setText("Email hoặc mật khẩu không chính xác!");
                        txtLoginError.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    Log.e("MainActivity", "Error in login", e);
                    txtLoginError.setText("Có lỗi xảy ra, vui lòng thử lại!");
                    txtLoginError.setVisibility(View.VISIBLE);
                }
            }
        });

        // Xử lý sự kiện click nút đăng ký
        btnRegister = findViewById(R.id.btnDangKy);
        btnRegister.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Register.class)));
    }

    /**
     * Khởi tạo cơ sở dữ liệu SQLite và tạo các bảng cần thiết
     * Bao gồm: Users, Categories, Budgets, Transactions, Notifications
     */
    private void initDatabase() {
        // Tạo hoặc mở database
        db = openOrCreateDatabase("database.db", MODE_PRIVATE, null);

        // Tạo bảng Users
        String sqlUsers = "CREATE TABLE IF NOT EXISTS \"Users\" (\n" +
                "\t\"user_id\"\tINTEGER,\n" +
                "\t\"username\"\tTEXT,\n" +
                "\t\"email\"\tTEXT,\n" +
                "\t\"password_hash\"\tTEXT,\n" +
                "\t\"created_at\"\tTEXT,\n" +
                "\t\"last_login\"\tTEXT,\n" +
                "\tPRIMARY KEY(\"user_id\" AUTOINCREMENT)\n" +
                ")";

        // Tạo bảng Categories
        String sqlCategories = "CREATE TABLE IF NOT EXISTS \"Categories\" (\n" +
                "\t\"category_id\"\tINTEGER,\n" +
                "\t\"name\"\tTEXT,\n" +
                "\t\"type\"\tTEXT,\n" +
                "\t\"user_id\"\tINTEGER,\n" +
                "\tPRIMARY KEY(\"category_id\" AUTOINCREMENT),\n" +
                "\tCONSTRAINT \"FK_user_id\" FOREIGN KEY(\"user_id\") REFERENCES \"Users\"(\"user_id\")\n" +
                ")";

        // Tạo bảng Budgets
        String sqlBudgets = "CREATE TABLE IF NOT EXISTS \"Budgets\" (\n" +
                "\t\"budget_id\"\tINTEGER,\n" +
                "\t\"category_id\"\tINTEGER,\n" +
                "\t\"amount\"\tREAL,\n" +
                "\t\"start_date\"\tTEXT,\n" +
                "\t\"end_date\"\tTEXT,\n" +
                "\t\"user_id\"\tINTEGER,\n" +
                "\tPRIMARY KEY(\"budget_id\" AUTOINCREMENT),\n" +
                "\tCONSTRAINT \"FK_budget_categories\" FOREIGN KEY(\"category_id\") REFERENCES \"Categories\"(\"category_id\"),\n"
                +
                "\tCONSTRAINT \"FK_budget_user\" FOREIGN KEY(\"user_id\") REFERENCES \"Users\"(\"user_id\")\n" +
                ")";

        // Tạo bảng Transactions
        String sqlTrans = "CREATE TABLE IF NOT EXISTS \"Transactions\" (\n" +
                "\t\"transaction_id\"\tINTEGER,\n" +
                "\t\"amount\"\tREAL,\n" +
                "\t\"date\"\tTEXT,\n" +
                "\t\"description\"\tTEXT,\n" +
                "\t\"category_id\"\tINTEGER,\n" +
                "\t\"user_id\"\tINTEGER,\n" +
                "\tPRIMARY KEY(\"transaction_id\" AUTOINCREMENT),\n" +
                "\tCONSTRAINT \"FK_trans_categories\" FOREIGN KEY(\"category_id\") REFERENCES \"Categories\"(\"category_id\"),\n"
                +
                "\tCONSTRAINT \"FK_trans_user\" FOREIGN KEY(\"user_id\") REFERENCES \"Users\"(\"user_id\")\n" +
                ")";

        // Tạo bảng Notifications
        String sqlNotifications = "CREATE TABLE IF NOT EXISTS \"Notifications\" (\n" +
                "\t\"notification_id\"\tINTEGER,\n" +
                "\t\"user_id\"\tINTEGER,\n" +
                "\t\"message\"\tTEXT,\n" +
                "\t\"date\"\tTEXT,\n" +
                "\t\"status\"\tNUMERIC,\n" +
                "\tPRIMARY KEY(\"notification_id\" AUTOINCREMENT),\n" +
                "\tCONSTRAINT \"FK_Notification_user\" FOREIGN KEY(\"user_id\") REFERENCES \"Users\"(\"user_id\")\n" +
                ")";

        // Thực thi các câu lệnh tạo bảng
        db.execSQL(sqlUsers);
        db.execSQL(sqlCategories);
        db.execSQL(sqlBudgets);
        db.execSQL(sqlTrans);
        db.execSQL(sqlNotifications);
    }

    /**
     * Kiểm tra thông tin đăng nhập của người dùng
     * 
     * @param mail Email người dùng
     * @param pwd  Mật khẩu người dùng
     * @return true nếu thông tin đăng nhập chính xác, false nếu không
     */
    private boolean accountChecking(String mail, String pwd) {
        try {
            // Truy vấn kiểm tra thông tin đăng nhập
            String sql = "SELECT * FROM Users WHERE email = ? AND password_hash = ?";
            Cursor cursor = db.rawQuery(sql, new String[] { mail, pwd });

            // Xử lý kết quả truy vấn
            if (cursor.moveToFirst()) {
                // Lấy thông tin người dùng nếu đăng nhập thành công
                int user_id = cursor.getInt(0);
                String username = cursor.getString(1);
                String email = cursor.getString(2);
                String password_hash = cursor.getString(3);
                String created_at = cursor.getString(4);
                String last_login = cursor.getString(5);
                user = new users(user_id, username, email, password_hash, created_at, last_login);
                cursor.close();
                return true;
            }
            cursor.close();
            return false;
        } catch (Exception e) {
            Log.e("MainActivity", "Error checking account", e);
            return false;
        }
    }
}