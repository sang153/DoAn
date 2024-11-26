package com.example.truongtansang_dh52111660;

import android.util.Log;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.FirebaseApp;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.safetynet.SafetyNet;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Register extends AppCompatActivity {

    private EditText txtUserName, txtEmailR, txtPwdR;
    private Button btnRegist;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Khởi tạo views
        txtUserName = findViewById(R.id.txtUserName);
        txtEmailR = findViewById(R.id.txtEmailR);
        txtPwdR = findViewById(R.id.txtPwdR);
        btnRegist = findViewById(R.id.btnRegist);
        progressBar = findViewById(R.id.progressBar);

        // Khởi tạo database
        db = openOrCreateDatabase("database.db", MODE_PRIVATE, null);

        btnRegist.setOnClickListener(v -> {
            String email = txtEmailR.getText().toString().trim();
            String password = txtPwdR.getText().toString().trim();
            String username = txtUserName.getText().toString().trim();

            if (validateInputs(username, email, password)) {
                registerUser(username, email, password);
            }
        });
    }

    private void registerUser(String username, String email, String password) {
        progressBar.setVisibility(View.VISIBLE);
        btnRegist.setEnabled(false);

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        if (user != null) {
                            user.sendEmailVerification()
                                    .addOnCompleteListener(emailTask -> {
                                        if (emailTask.isSuccessful()) {
                                            // Lưu thông tin vào SQLite
                                            saveUserToDatabase(username, email, password);

                                            Toast.makeText(Register.this,
                                                    "Đăng ký thành công. Vui lòng kiểm tra email để xác thực tài khoản.",
                                                    Toast.LENGTH_LONG).show();

                                            // Đăng xuất
                                            FirebaseAuth.getInstance().signOut();

                                            // Chuyển về màn hình đăng nhập
                                            startActivity(new Intent(Register.this, MainActivity.class));
                                            finish();
                                        } else {
                                            Toast.makeText(Register.this,
                                                    "Không thể gửi email xác thực: "
                                                            + emailTask.getException().getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        String errorMessage;
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthWeakPasswordException) {
                            errorMessage = "Mật khẩu phải có ít nhất 6 ký tự";
                        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            errorMessage = "Email không hợp lệ";
                        } else if (e instanceof FirebaseAuthUserCollisionException) {
                            errorMessage = "Email đã được sử dụng";
                        } else {
                            errorMessage = "Lỗi đăng ký: " + e.getMessage();
                        }
                        Toast.makeText(Register.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                    progressBar.setVisibility(View.GONE);
                    btnRegist.setEnabled(true);
                });
    }

    private boolean validateInputs(String username, String email, String password) {
        if (TextUtils.isEmpty(username)) {
            txtUserName.setError("Vui lòng nhập tên người dùng");
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            txtEmailR.setError("Vui lòng nhập email");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            txtPwdR.setError("Vui lòng nhập mật khẩu");
            return false;
        }
        if (password.length() < 6) {
            txtPwdR.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return false;
        }
        return true;
    }

    private void saveUserToDatabase(String username, String email, String password) {
        try {
            // Lấy thời gian hiện tại theo định dạng SQL datetime
            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new Date());

            String sql = "INSERT INTO Users (username, email, password_hash, created_at, last_login) " +
                    "VALUES (?, ?, ?, ?, ?)";
            SQLiteStatement statement = db.compileStatement(sql);
            statement.bindString(1, username);
            statement.bindString(2, email);
            statement.bindString(3, password);
            statement.bindString(4, currentTime); // created_at
            statement.bindString(5, currentTime); // last_login

            long result = statement.executeInsert();
            if (result == -1) {
                Log.e("RegisterActivity", "Failed to insert user into database");
            }
        } catch (Exception e) {
            Log.e("RegisterActivity", "Error saving user to database", e);
            Toast.makeText(this, "Lỗi khi lưu thông tin người dùng", Toast.LENGTH_SHORT).show();
        }
    }
}