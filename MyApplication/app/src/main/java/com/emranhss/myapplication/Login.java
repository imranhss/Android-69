package com.emranhss.myapplication;

import android.content.Intent;
import android.graphics.Insets;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import com.emranhss.myapplication.model.request.LoginRequest;
import com.emranhss.myapplication.model.response.CustomerResponse;
import com.emranhss.myapplication.model.response.LoginResponse;
import com.emranhss.myapplication.repository.AuthRepository;
import com.emranhss.myapplication.repository.CustomerRepository;
import com.emranhss.myapplication.session.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class Login extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;

    private AuthRepository authRepository;
    private CustomerRepository customerRepository;
    private SessionManager sessionManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);


        init();

        btnLogin.setOnClickListener(v -> login());

    }

    private void init() {

        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        authRepository = new AuthRepository(this);
        customerRepository = new CustomerRepository(this);
        sessionManager = new SessionManager(this);

    }

    private void login() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email Required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password Required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        LoginRequest request = new LoginRequest();

        request.setEmail(email);
        request.setPassword(password);

        authRepository.login(request, new Callback<LoginResponse>() {

            @Override
            public void onResponse(Call<LoginResponse> call,
                                   Response<LoginResponse> response) {

                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                if (!response.isSuccessful()) {

                    Toast.makeText(Login.this,
                            "Invalid Email or Password",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                LoginResponse login = response.body();

                System.out.println(login);

                if (login == null) {
                    Toast.makeText(Login.this,
                            "Login Failed",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Save Token
                sessionManager.saveToken(login.getToken());

                // Save User
                sessionManager.saveUser(login);

                // Load Customer
                loadCustomer(login.getUserId());

            }

            @Override
            public void onFailure(Call<LoginResponse> call,
                                  Throwable t) {

                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                Toast.makeText(Login.this,
                        t.getMessage(),
                        Toast.LENGTH_LONG).show();

            }
        });

    }

    private void loadCustomer(Long userId) {

        customerRepository.getCustomerByUserId(userId,
                new Callback<CustomerResponse>() {

                    @Override
                    public void onResponse(Call<CustomerResponse> call,
                                           Response<CustomerResponse> response) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            sessionManager.saveCustomer(response.body());

                        }

                        Intent intent = new Intent(Login.this, CustomerDashboard.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);


                        finish();

                    }

                    @Override
                    public void onFailure(Call<CustomerResponse> call,
                                          Throwable t) {

                        startActivity(new Intent(Login.this,
                                MainActivity.class));

                        finish();

                    }
                });

    }
}