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
public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String DEFAULT_APP_NAME = "Android SDK Test";
    private static final String DEFAULT_APP_VERSION = "1.0.0";

    private EditText etEmail;
    private EditText etPassword;

    private Button btnApply;
    private Button btnDownload;

    private static LicenseManager manager;

    private static String email;
    private static String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);

        btnApply = findViewById(R.id.btn_login);
        btnDownload = findViewById(R.id.btn_download);

        checkPermissions();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initialize();
            }
            return;
        }
    }


    public void checkPermissions() {
        String[] permissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET};


        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, 1);
        } else {
            initialize();
        }
    }

    public void initialize() {

        LicenseSpringConfiguration config =
                LicenseSpringConfiguration.builder()
                        .apiKey(apiKey)
                        .productCode(productCode)
                        .sharedKey(sharedKey)
                        .appName(appName)
                        .appVersion(appVer)
                        .serviceURL(serviceURL)
                        .build();

        manager = LicenseManager.getInstance();
        manager.initialize(config, getApplicationContext());

        log.info("INIT LS staging service!!!");

        btnDownload.setEnabled(false);

        btnDownload.setOnClickListener(v -> {
            log.info("DOWNLOAD clicked!!!");
            btnDownload.setEnabled(false);
            download(getApplicationContext());
            btnDownload.setEnabled(true);
            log.info("DOWNLOAD click finished!!!");
        });

        btnApply.setOnClickListener(v -> {
            Date date = new Date();
            log.info("APPLY clicked!!! ts: {}", date);
            btnApply.setEnabled(false);
            email = etEmail.getText().toString();
            password = etPassword.getText().toString();

            if ("".equals(email) || "".equals(password)) {
                Toast.makeText(getApplicationContext(), "No fields should be empty.", Toast.LENGTH_SHORT).show();
                btnApply.setEnabled(true);
            } else {
                activate(email, password, getApplicationContext());
                btnDownload.setEnabled(true);
            }

            Date after = new Date();
            long difference = after.getTime() - date.getTime();
            log.info("APPLY click finished!!! millis: {},  ts: {}", difference, after);
        });
    }

    public static void activate(String username, String password, Context context){
        manager= LicenseManager.getInstance();

        License license = manager.activateLicense(fromEmail(username, password));
        if (license !=null){
            Toast.makeText(context, "Your license was successfully activated!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Sorry, something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    private static ActivationLicense fromEmail(String username, String password) {
        return ActivationLicense.fromUsername(username, password);
    }


    public static void download(Context context){
        manager.offlineActivationFile(LicenseIdentity.fromKey(email), null);
        Toast.makeText(context, "Your license was saved in Download directory.", Toast.LENGTH_SHORT).show();
    }


}
