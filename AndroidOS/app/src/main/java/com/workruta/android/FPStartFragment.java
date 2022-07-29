package com.workruta.android;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static com.workruta.android.ForgotPassActivity.changeViewPage;

public class FPStartFragment extends Fragment {

    Context context;
    TextView emailTextView, phoneTextView;

    public FPStartFragment(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fpstart, container, false);

        emailTextView = view.findViewById(R.id.emailTextView);
        phoneTextView = view.findViewById(R.id.phoneTextView);

        emailTextView.setOnClickListener((v) -> changeViewPage(1, true));
        phoneTextView.setOnClickListener((v) -> changeViewPage(2, true));

        return view;
    }
}