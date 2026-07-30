package com.emranhss.myapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.emranhss.myapplication.api.ApiClient;
import com.emranhss.myapplication.api.ApiService;
import com.emranhss.myapplication.model.Country;
import com.emranhss.myapplication.model.District;
import com.emranhss.myapplication.model.Division;
import com.emranhss.myapplication.model.PoliceStation;
import com.emranhss.myapplication.model.request.CustomerRegisterRequest;
import com.emranhss.myapplication.model.response.CustomerResponse;
import com.emranhss.myapplication.repository.CustomerRepository;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPhone, etDob, etPassword, etConfirmPassword, etStreetAddress;
    private TextInputLayout tilConfirmPassword;
    private MaterialButtonToggleGroup toggleGender;

    private AutoCompleteTextView spCountry, spDivision, spDistrict, spPoliceStation;
    private android.widget.TextView txtFullAddress;

    private ShapeableImageView imgPreview;
    private ImageButton btnRemovePhoto;
    private android.widget.Button btnChoosePhoto;
    private com.google.android.material.button.MaterialButton btnRegister;
    private ProgressBar progressBar;

    private ApiService apiService;
    private CustomerRepository customerRepository;

    // Location data + selection state
    private final List<Country> countries = new ArrayList<>();
    private final List<Division> divisions = new ArrayList<>();
    private final List<District> districts = new ArrayList<>();
    private final List<PoliceStation> policeStations = new ArrayList<>();

    private String countryName = "";
    private String divisionName = "";
    private String districtName = "";
    private String policeStationName = "";

    private Long selectedCountryId;
    private Long selectedDivisionId;
    private Long selectedDistrictId;
    private Long selectedPoliceStationId;

    private Uri selectedImageUri;

    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        apiService = ApiClient.getClient(getApplicationContext());
        customerRepository = new CustomerRepository(this);

        bindViews();
        setupToolbar();
        registerImagePicker();

        loadCountries();

        setupLocationListeners();
        setupAddressListeners();
        setupDobPicker();
        setupPhotoListeners();

        btnRegister.setOnClickListener(v -> register());
    }

    private void bindViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etDob = findViewById(R.id.etDob);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etStreetAddress = findViewById(R.id.etStreetAddress);

        toggleGender = findViewById(R.id.toggleGender);

        spCountry = findViewById(R.id.spCountry);
        spDivision = findViewById(R.id.spDivision);
        spDistrict = findViewById(R.id.spDistrict);
        spPoliceStation = findViewById(R.id.spPoliceStation);

        txtFullAddress = findViewById(R.id.txtFullAddress);

        imgPreview = findViewById(R.id.imgPreview);
        btnRemovePhoto = findViewById(R.id.btnRemovePhoto);
        btnChoosePhoto = findViewById(R.id.btnChoosePhoto);

        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    // ===== Photo picker =====

    private void registerImagePicker() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) return;
                    selectedImageUri = uri;
                    imgPreview.setImageURI(uri);
                    btnRemovePhoto.setVisibility(View.VISIBLE);
                });
    }

    private void setupPhotoListeners() {
        btnChoosePhoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnRemovePhoto.setOnClickListener(v -> {
            selectedImageUri = null;
            imgPreview.setImageResource(android.R.drawable.sym_def_app_icon);
            btnRemovePhoto.setVisibility(View.GONE);
        });
    }

    // ===== Date of birth =====

    private void setupDobPicker() {
        etDob.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        String dob = String.format(Locale.getDefault(),
                                "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                        etDob.setText(dob);
                    },
                    calendar.get(Calendar.YEAR) - 20,
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            dialog.show();
        });
    }

    // ===== Address auto-generation =====

    private void setupAddressListeners() {
        etStreetAddress.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                generateFullAddress();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    /**
     * Mirrors the Angular component's generateFullAddress(): combines the free-text
     * street address with the selected police station / district / division / country.
     */
    private void generateFullAddress() {
        String street = etStreetAddress.getText() != null
                ? etStreetAddress.getText().toString().trim() : "";

        List<String> parts = new ArrayList<>();
        if (!TextUtils.isEmpty(street)) parts.add(street);
        if (!TextUtils.isEmpty(policeStationName)) parts.add(policeStationName);
        if (!TextUtils.isEmpty(districtName)) parts.add(districtName);
        if (!TextUtils.isEmpty(divisionName)) parts.add(divisionName);
        if (!TextUtils.isEmpty(countryName)) parts.add(countryName);

        if (parts.isEmpty()) {
            txtFullAddress.setText("Enter your address and select location above to auto-generate");
        } else {
            txtFullAddress.setText(TextUtils.join(", ", parts));
        }
    }

    // ===== Cascading location dropdowns =====

    private void setupLocationListeners() {
        spCountry.setOnItemClickListener((parent, view, position, id) -> {
            Country c = countries.get(position);
            selectedCountryId = c.getId();
            countryName = c.getName();

            divisions.clear();
            districts.clear();
            policeStations.clear();
            spDivision.setText("", false);
            spDistrict.setText("", false);
            spPoliceStation.setText("", false);
            divisionName = "";
            districtName = "";
            policeStationName = "";
            selectedDivisionId = null;
            selectedDistrictId = null;
            selectedPoliceStationId = null;

            loadDivisions(selectedCountryId);
            generateFullAddress();
        });

        spDivision.setOnItemClickListener((parent, view, position, id) -> {
            Division d = divisions.get(position);
            selectedDivisionId = d.getId();
            divisionName = d.getName();

            districts.clear();
            policeStations.clear();
            spDistrict.setText("", false);
            spPoliceStation.setText("", false);
            districtName = "";
            policeStationName = "";
            selectedDistrictId = null;
            selectedPoliceStationId = null;

            loadDistricts(selectedDivisionId);
            generateFullAddress();
        });

        spDistrict.setOnItemClickListener((parent, view, position, id) -> {
            District d = districts.get(position);
            selectedDistrictId = d.getId();
            districtName = d.getName();

            policeStations.clear();
            spPoliceStation.setText("", false);
            policeStationName = "";
            selectedPoliceStationId = null;

            loadPoliceStations(selectedDistrictId);
            generateFullAddress();
        });

        spPoliceStation.setOnItemClickListener((parent, view, position, id) -> {
            PoliceStation p = policeStations.get(position);
            selectedPoliceStationId = p.getId();
            policeStationName = p.getName();
            generateFullAddress();
        });
    }

    private void loadCountries() {
        apiService.getCountries().enqueue(new Callback<List<Country>>() {
            @Override
            public void onResponse(Call<List<Country>> call, Response<List<Country>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    countries.clear();
                    countries.addAll(response.body());
                    String[] names = new String[countries.size()];
                    for (int i = 0; i < countries.size(); i++) names[i] = countries.get(i).getName();
                    spCountry.setAdapter(nameAdapter(names));
                }
            }

            @Override
            public void onFailure(Call<List<Country>> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Could not load countries", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDivisions(Long countryId) {
        apiService.getDivisions(countryId).enqueue(new Callback<List<Division>>() {
            @Override
            public void onResponse(Call<List<Division>> call, Response<List<Division>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    divisions.clear();
                    divisions.addAll(response.body());
                    String[] names = new String[divisions.size()];
                    for (int i = 0; i < divisions.size(); i++) names[i] = divisions.get(i).getName();
                    spDivision.setAdapter(nameAdapter(names));
                }
            }

            @Override
            public void onFailure(Call<List<Division>> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Could not load divisions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDistricts(Long divisionId) {
        apiService.getDistricts(divisionId).enqueue(new Callback<List<District>>() {
            @Override
            public void onResponse(Call<List<District>> call, Response<List<District>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    districts.clear();
                    districts.addAll(response.body());
                    String[] names = new String[districts.size()];
                    for (int i = 0; i < districts.size(); i++) names[i] = districts.get(i).getName();
                    spDistrict.setAdapter(nameAdapter(names));
                }
            }

            @Override
            public void onFailure(Call<List<District>> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Could not load districts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPoliceStations(Long districtId) {
        apiService.getPoliceStations(districtId).enqueue(new Callback<List<PoliceStation>>() {
            @Override
            public void onResponse(Call<List<PoliceStation>> call, Response<List<PoliceStation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    policeStations.clear();
                    policeStations.addAll(response.body());
                    String[] names = new String[policeStations.size()];
                    for (int i = 0; i < policeStations.size(); i++) names[i] = policeStations.get(i).getName();
                    spPoliceStation.setAdapter(nameAdapter(names));
                }
            }

            @Override
            public void onFailure(Call<List<PoliceStation>> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Could not load police stations", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private ArrayAdapter<String> nameAdapter(String[] names) {
        return new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names);
    }

    // ===== Submit =====

    private void register() {

        String name = textOf(etName);
        String email = textOf(etEmail);
        String phone = textOf(etPhone);
        String dob = textOf(etDob);
        String password = textOf(etPassword);
        String confirmPassword = textOf(etConfirmPassword);

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Phone number is required");
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }
        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            return;
        }
        tilConfirmPassword.setError(null);

        String gender = genderOf(toggleGender.getCheckedButtonId());

        CustomerRegisterRequest request = new CustomerRegisterRequest();
        request.setName(name);
        request.setEmail(email);
        request.setPhone(phone);
        request.setPassword(password);
        request.setDob(dob);
        request.setGender(gender);
        request.setPoliceStationId(selectedPoliceStationId);

        generateFullAddress();
        request.setAddress(txtFullAddress.getText().toString());

        submitRegistration(request);
    }

    private void submitRegistration(CustomerRegisterRequest request) {
        setLoading(true);

        RequestBody customerJson = RequestBody.create(
                MediaType.parse("application/json"),
                new Gson().toJson(request));

        MultipartBody.Part imagePart = null;
        if (selectedImageUri != null) {
            imagePart = buildImagePart(selectedImageUri);
            if (imagePart == null) {
                Toast.makeText(this, "Could not read selected photo, continuing without it", Toast.LENGTH_SHORT).show();
            }
        }

        customerRepository.registerCustomer(customerJson, imagePart, new Callback<CustomerResponse>() {
            @Override
            public void onResponse(Call<CustomerResponse> call, Response<CustomerResponse> response) {
                setLoading(false);

                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Registration successful. Please log in.", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(RegisterActivity.this, Login.class));
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Registration failed. Please check your details.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<CustomerResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /** Reads the picked content Uri into memory and wraps it as a multipart form field. */
    private MultipartBody.Part buildImagePart(Uri uri) {
        try {
            String mimeType = getContentResolver().getType(uri);
            if (mimeType == null) mimeType = "image/jpeg";

            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(chunk)) != -1) {
                buffer.write(chunk, 0, bytesRead);
            }
            inputStream.close();

            RequestBody fileBody = RequestBody.create(MediaType.parse(mimeType), buffer.toByteArray());
            return MultipartBody.Part.createFormData("image", fileNameOf(uri), fileBody);

        } catch (IOException e) {
            return null;
        }
    }

    private String fileNameOf(Uri uri) {
        String name = "photo.jpg";
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    String display = cursor.getString(nameIndex);
                    if (!TextUtils.isEmpty(display)) name = display;
                }
            }
        } catch (Exception ignored) {
        }
        return name;
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!loading);
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private String genderOf(int checkedButtonId) {
        if (checkedButtonId == R.id.btnGenderMale) return "MALE";
        if (checkedButtonId == R.id.btnGenderFemale) return "FEMALE";
        if (checkedButtonId == R.id.btnGenderOther) return "OTHER";
        return null;
    }
}
