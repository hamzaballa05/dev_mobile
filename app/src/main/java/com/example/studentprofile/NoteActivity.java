package com.example.studentprofile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studentprofile.util.Constants;
import android.widget.TextView;
public class NoteActivity extends AppCompatActivity {
    private EditText etSubject;
    private EditText etScore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        TextView tvStudentTitle = findViewById(R.id.tvStudentTitle);
        String studentName = getIntent().getStringExtra(Constants.EXTRA_STUDENT_NAME);

        if (TextUtils.isEmpty(studentName)) {
            setTitle("Nouvelle note");
            tvStudentTitle.setText("Ajouter une nouvelle note");
        } else {
            setTitle("Nouvelle note - " + studentName);
            tvStudentTitle.setText("Ajouter une note - " + studentName);
        }

        etSubject = findViewById(R.id.etSubject);
        etScore = findViewById(R.id.etScore);
        Button btnCancel = findViewById(R.id.btnCancel);
        Button btnConfirm = findViewById(R.id.btnConfirm);

        btnCancel.setOnClickListener(v -> finish());
        btnConfirm.setOnClickListener(v -> validateAndReturn());
    }

    private void validateAndReturn() {
        String subject = etSubject.getText().toString().trim();
        String scoreText = etScore.getText().toString().trim();

        if (TextUtils.isEmpty(subject) || TextUtils.isEmpty(scoreText)) {
            Toast.makeText(this, "Veuillez remplir la matière et la note.", Toast.LENGTH_SHORT).show();
            return;
        }

        double score;
        try {
            score = Double.parseDouble(scoreText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Note invalide.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent data = new Intent();
        data.putExtra(Constants.EXTRA_SUBJECT, subject);
        data.putExtra(Constants.EXTRA_SCORE, score);
        setResult(RESULT_OK, data);
        finish();
    }
}
