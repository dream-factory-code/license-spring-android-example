package com.licensespring.sample.android;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.licensespring.License;
import com.licensespring.LicenseManager;
import com.licensespring.LicenseSpringConfiguration;
import com.licensespring.model.ActivationLicense;
import com.licensespring.model.LicenseIdentity;

import java.util.Date;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String DEFAULT_APP_NAME = "Android SDK Test";
    private static final String DEFAULT_APP_VERSION = "1.0.0";

    private EditText etApiKey;
    private EditText etSharedKey;
    private EditText etProductCode;
    private EditText etServiceUrl;

    private EditText etAppName;
    private EditText etAppVersion;

    private Button btnInit;

    private EditText etEmail;
    private EditText etPassword;

    private Button btnApply;
    private Button btnDownload;

    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etApiKey = findViewById(R.id.et_api_key);
        etSharedKey = findViewById(R.id.et_shared_key);
        etProductCode = findViewById(R.id.et_product_code);
        etServiceUrl = findViewById(R.id.et_service_url);

        etAppName = findViewById(R.id.et_app_name);
        etAppVersion = findViewById(R.id.et_app_version);

        btnInit = findViewById(R.id.btn_init);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);

        btnApply = findViewById(R.id.btn_apply);
        btnDownload = findViewById(R.id.btn_download);

        checkPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initialize();
            }
        }
    }

    public void checkPermissions() {
        String[] permissions = new String[] {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET};

        if (ContextCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, 1);
        } else {
            initialize();
        }
    }

    public void initialize() {
        btnApply.setEnabled(false);
        btnDownload.setEnabled(false);

        btnInit.setOnClickListener(v -> {
            initializeSdk();
        });

        btnApply.setOnClickListener(v -> {
            activateLicense();
        });

        btnDownload.setOnClickListener(v -> {
            downloadLicense();
        });
    }

    private void initializeSdk() {
        String apiKey = etApiKey.getText().toString();
        String sharedKey = etSharedKey.getText().toString();
        String productCode = etProductCode.getText().toString();
        String serviceUrl = etServiceUrl.getText().toString();

        if ("".equals(apiKey)
                || "".equals(sharedKey)
                || "".equals(productCode)
                || "".equals(serviceUrl)) {
            Toast.makeText(getApplicationContext(),
                    "No fields should be empty.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String appName = etAppName.getText().toString();

        if ("".equals(appName)) {
            appName = DEFAULT_APP_NAME;
        }

        String appVersion = etAppVersion.getText().toString();

        if ("".equals(appVersion)) {
            appVersion = DEFAULT_APP_VERSION;
        }

        LicenseSpringConfiguration config =
                LicenseSpringConfiguration.builder()
                        .apiKey(apiKey)
                        .productCode(productCode)
                        .sharedKey(sharedKey)
                        .appName(appName)
                        .appVersion(appVersion)
                        .serviceURL(serviceUrl)
                        .build();

        LicenseManager.getInstance()
                .initialize(config, getApplicationContext());

        log.info("INIT LS service!!!");

        btnApply.setEnabled(true);
        btnInit.setEnabled(false);
    }

    private void activateLicense() {
        Date date = new Date();
        log.info("APPLY clicked!!! ts: {}", date);

        email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if ("".equals(email) || "".equals(password)) {
            Toast.makeText(getApplicationContext(),
                    "No fields should be empty.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        activate(email, password, getApplicationContext());
        btnApply.setEnabled(false);
        btnDownload.setEnabled(true);

        Date after = new Date();
        long difference = after.getTime() - date.getTime();
        log.info("APPLY click finished!!! millis: {},  ts: {}", difference, after);
    }

    private void downloadLicense() {
        log.info("DOWNLOAD clicked!!!");
        btnDownload.setEnabled(false);
        download(getApplicationContext());
        btnDownload.setEnabled(true);
        log.info("DOWNLOAD click finished!!!");
    }

    private void activate(String username, String password, Context context) {
        License license = LicenseManager.getInstance()
                .activateLicense(fromEmail(username, password));

        if (license != null) {
            Toast.makeText(context, "Your license was successfully activated!",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Sorry, something went wrong!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private ActivationLicense fromEmail(String username, String password) {
        return ActivationLicense.fromUsername(username, password);
    }


    private void download(Context context) {
        LicenseManager.getInstance()
                .offlineActivationFile(
                LicenseIdentity.fromKey(email), null);

        Toast.makeText(context, "Your license was saved in Download directory.",
                Toast.LENGTH_SHORT).show();
    }

}
