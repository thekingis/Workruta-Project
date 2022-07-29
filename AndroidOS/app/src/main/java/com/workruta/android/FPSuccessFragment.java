package com.workruta.android;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static com.workruta.android.ForgotPassActivity.finishActivity;

public class FPSuccessFragment extends Fragment {

    Context context;
    TextView goToLogin;

    public FPSuccessFragment(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fpsuccess, container, false);

        goToLogin = view.findViewById(R.id.goToLogin);

        goToLogin.setOnClickListener((v) -> finishActivity(context));

        return view;
    }
}