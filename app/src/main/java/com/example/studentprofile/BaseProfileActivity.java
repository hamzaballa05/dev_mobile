package com.example.studentprofile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.studentprofile.adapter.NoteAdapter;
import com.example.studentprofile.model.Note;
import com.example.studentprofile.model.Profile;
import com.example.studentprofile.network.ProfileRepository;
import com.example.studentprofile.service.TrackingService;
import com.example.studentprofile.util.Constants;
import com.example.studentprofile.util.ImageLoader;
import com.example.studentprofile.util.NetworkUtils;

import java.util.ArrayList;

public abstract class BaseProfileActivity extends AppCompatActivity {
    private static final String TAG = "LIFECYCLE";
    private static final String STATE_PROFILE = "state_profile";
    private static final String STATE_SOURCE = "state_source";

    protected ImageView ivProfile;
    protected EditText etFullName;
    protected EditText etEmail;
    protected EditText etPhone;
    protected EditText etRemarks;
    protected TextView tvSource;
    protected ListView lvNotes;
    protected Button btnSave;
    protected Button btnCall;
    protected Button btnAddNote;
    protected Button btnSwitchUi;

    protected Profile currentProfile;
    protected final ArrayList<Note> notes = new ArrayList<>();
    protected NoteAdapter noteAdapter;
    protected ProfileRepository repository;

    private final ActivityResultLauncher<Void> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(),
            bitmap -> {
                if (bitmap != null && ivProfile != null) {
                    ivProfile.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    ivProfile.setImageBitmap(bitmap);
                    Toast.makeText(this, getString(R.string.camera_success), Toast.LENGTH_SHORT).show();
                }
            }
    );

    private final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> {
                if (granted) {
                    cameraLauncher.launch(null);
                } else {
                    Toast.makeText(this, getString(R.string.camera_permission_denied), Toast.LENGTH_SHORT).show();
                }
            }
    );

    private final ActivityResultLauncher<Intent> noteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String subject = result.getData().getStringExtra(Constants.EXTRA_SUBJECT);
                    double score = result.getData().getDoubleExtra(Constants.EXTRA_SCORE, 0.0);
                    Note note = new Note(subject, score);
                    notes.add(note);
                    if (currentProfile != null) {
                        currentProfile.getNotes().add(note);
                    }
                    if (noteAdapter != null) {
                        noteAdapter.notifyDataSetChanged();
                    }
                    Toast.makeText(this, "Nouvelle note ajoutée : " + subject, Toast.LENGTH_SHORT).show();
                }
            }
    );

    private final ActivityResultLauncher<String> backgroundLocationLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> maybeStartTrackingService()
    );

    private final ActivityResultLauncher<String[]> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                        && NetworkUtils.hasForegroundLocationPermission(this)
                        && !NetworkUtils.hasBackgroundLocationPermission(this)) {
                    backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                } else {
                    maybeStartTrackingService();
                }
            }
    );

    private final ActivityResultLauncher<String> notificationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> {
            }
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());
        setTitle(getScreenTitleText());
        logLifecycle("onCreate");

        repository = new ProfileRepository(this);
        bindViews();
        initUi();
        ensurePermissionsForDemo();

        Profile extraProfile = null;
        if (getIntent() != null && getIntent().hasExtra(Constants.EXTRA_PROFILE)) {
            extraProfile = (Profile) getIntent().getSerializableExtra(Constants.EXTRA_PROFILE);
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_PROFILE)) {
            currentProfile = (Profile) savedInstanceState.getSerializable(STATE_PROFILE);
            renderProfile(currentProfile, savedInstanceState.getString(STATE_SOURCE, getString(R.string.source_placeholder)));
        } else if (extraProfile != null) {
            currentProfile = extraProfile;
            String source = getIntent().getStringExtra(Constants.EXTRA_SOURCE);
            renderProfile(currentProfile, source == null ? "Source : Activité précédente" : source);
        } else {
            loadProfile();
        }
    }

    protected abstract @LayoutRes int getLayoutResId();

    protected abstract String getScreenTitleText();

    protected boolean useVolleyMode() {
        return Constants.USE_VOLLEY_BY_DEFAULT;
    }

    private void bindViews() {
        ivProfile = findViewById(R.id.ivProfile);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etRemarks = findViewById(R.id.etRemarks);
        tvSource = findViewById(R.id.tvSource);
        lvNotes = findViewById(R.id.lvNotes);
        btnSave = findViewById(R.id.btnSave);
        btnCall = findViewById(R.id.btnCall);
        btnAddNote = findViewById(R.id.btnAddNote);
        btnSwitchUi = findViewById(R.id.btnSwitchUi);
    }

    private void initUi() {
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> Toast.makeText(this,
                    "Message temporaire : demande d'enregistrement effectuée.",
                    Toast.LENGTH_SHORT).show());
        }

        if (ivProfile != null) {
            ivProfile.setOnClickListener(v -> openCamera());
        }

        if (btnCall != null) {
            btnCall.setOnClickListener(v -> openDialer());
        }

        if (btnAddNote != null) {
            btnAddNote.setOnClickListener(v -> openNoteActivity());
        }

        if (btnSwitchUi != null) {
            btnSwitchUi.setOnClickListener(v -> switchUi());
            btnSwitchUi.setText(this instanceof MainActivity
                    ? R.string.switch_ui
                    : R.string.switch_ui_constraint);
        }

        if (lvNotes != null) {
            noteAdapter = new NoteAdapter(this, notes);
            lvNotes.setAdapter(noteAdapter);
        }
    }

    private void switchUi() {
        Intent intent = new Intent(this,
                this instanceof MainActivity ? ProfileLinearActivity.class : MainActivity.class);
        if (currentProfile != null) {
            intent.putExtra(Constants.EXTRA_PROFILE, currentProfile);
            intent.putExtra(Constants.EXTRA_SOURCE, tvSource != null ? tvSource.getText().toString() : "Source : Activité précédente");
        }
        startActivity(intent);
    }

    private void openCamera() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            cameraLauncher.launch(null);
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraLauncher.launch(null);
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            Toast.makeText(this, getString(R.string.camera_permission_rationale), Toast.LENGTH_SHORT).show();
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openDialer() {
        String phone = etPhone != null ? etPhone.getText().toString().trim() : "";
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Aucun numéro de téléphone disponible.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
        startActivity(intent);
    }

    private void openNoteActivity() {
        Intent intent = new Intent(this, NoteActivity.class);
        intent.putExtra(Constants.EXTRA_STUDENT_NAME,
                currentProfile != null ? currentProfile.getFullName() : etFullName.getText().toString());
        noteLauncher.launch(intent);
    }

    private void loadProfile() {
        if (tvSource != null) {
            tvSource.setText("");
        }
        ProfileRepository.ProfileListener listener = new ProfileRepository.ProfileListener() {
            @Override
            public void onSuccess(Profile profile, String sourceLabel) {
                currentProfile = profile;
                renderProfile(profile, sourceLabel);
            }

            @Override
            public void onError(String message) {
                if (tvSource != null) {
                    tvSource.setText("");
                }
                Toast.makeText(BaseProfileActivity.this, message, Toast.LENGTH_LONG).show();
            }
        };

        if (useVolleyMode()) {
            repository.loadProfileWithVolley(Constants.DEFAULT_PROFILE_ID, true, listener);
        } else {
            repository.loadProfileWithHttp(Constants.DEFAULT_PROFILE_ID, true, listener);
        }
    }

    protected void renderProfile(Profile profile, String sourceLabel) {
        etFullName.setText(profile.getFullName());
        etEmail.setText(profile.getEmail());
        etPhone.setText(profile.getPhone());
        etRemarks.setText(profile.getRemarks());
        if (tvSource != null && !TextUtils.isEmpty(sourceLabel)) {
            tvSource.setText(sourceLabel);
            tvSource.setVisibility(android.view.View.VISIBLE);
        }

        notes.clear();
        if (profile.getNotes() != null) {
            notes.addAll(profile.getNotes());
        }
        if (noteAdapter != null) {
            noteAdapter.notifyDataSetChanged();
        }

        if (useVolleyMode()) {
            ImageLoader.loadWithVolley(this, profile.getImageUrl(), ivProfile);
        } else {
            ImageLoader.loadWithHttp(profile.getImageUrl(), ivProfile);
        }
    }

    private void ensurePermissionsForDemo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }

        if (!NetworkUtils.hasForegroundLocationPermission(this)) {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            });
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && !NetworkUtils.hasBackgroundLocationPermission(this)) {
            backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        } else {
            maybeStartTrackingService();
        }
    }

    protected void maybeStartTrackingService() {
        if (NetworkUtils.hasForegroundLocationPermission(this)) {
            TrackingService.start(this);
        }
    }

    private void logLifecycle(String methodName) {
        Log.d(TAG, getClass().getSimpleName() + " -> " + methodName);
    }

    @Override
    protected void onStart() {
        super.onStart();
        logLifecycle("onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        logLifecycle("onResume");
    }

    @Override
    protected void onPause() {
        logLifecycle("onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        logLifecycle("onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        logLifecycle("onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        logLifecycle("onRestart");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        logLifecycle("onSaveInstanceState");
        if (currentProfile != null) {
            outState.putSerializable(STATE_PROFILE, currentProfile);
            if (tvSource != null) {
                outState.putString(STATE_SOURCE, tvSource.getText().toString());
            }
        }
    }
}
