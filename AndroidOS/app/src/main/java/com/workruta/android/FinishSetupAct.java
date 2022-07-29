package com.workruta.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class FinishSetupAct extends SharedCompatActivity {

    TextView _continue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_setup);

        _continue = findViewById(R.id._continue);
        _continue.setOnClickListener((v) ->{
            finish();
            Intent intent = new Intent(this, ChangePhotoAct.class);
            Bundle bundle = new Bundle();
            bundle.putBoolean("backEnabled", false);
            intent.putExtras(bundle);
            startActivity(intent);
        });
    }
}