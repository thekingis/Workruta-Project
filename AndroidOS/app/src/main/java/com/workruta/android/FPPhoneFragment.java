package com.workruta.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.workruta.android.Utils.StringUtils;

import static com.workruta.android.ForgotPassActivity.changeViewPage;
import static com.workruta.android.ForgotPassActivity.getVerificationCode;

public class FPPhoneFragment extends Fragment {

    Context context;
    RelativeLayout mainView;
    EditText phoneNumberET;
    TextView switchPage, submitBtn;
    @SuppressLint("StaticFieldLeak")
    static TextView errorLog;
    boolean changingText, keyPressed;

    public FPPhoneFragment(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fpphone, container, false);

        changingText = false;
        keyPressed = false;
        mainView = view.findViewById(R.id.mainView);
        phoneNumberET = view.findViewById(R.id.phoneNumber);
        errorLog = view.findViewById(R.id.errorLog);
        switchPage = view.findViewById(R.id.switchPage);
        submitBtn = view.findViewById(R.id.submit);

        switchPage.setOnClickListener((v) -> changeViewPage(1, true));
        phoneNumberET.setOnKeyListener((v, keyCode, event) -> {
            if(event.getAction() == KeyEvent.ACTION_DOWN) {
                keyPressed = true;
                if(keyCode != KeyEvent.KEYCODE_DEL && phoneNumberET.getText().toString().replaceAll(" ", "").length() > 9)
                    return true;
            }
            if(event.getAction() == KeyEvent.ACTION_UP){
                if(keyCode == KeyEvent.KEYCODE_DEL){
                    int caret = phoneNumberET.getSelectionStart() - 1;
                    if(caret > 0) {
                        char c = phoneNumberET.getText().charAt(caret);
                        if (String.valueOf(c).equals(" ")) {
                            phoneNumberET.setText(phoneNumberET.getText().delete(caret, caret + 1));
                            phoneNumberET.setSelection(caret);
                        }
                    }
                }
                Editable s = phoneNumberET.getText();
                arraignNumber(s);
                new android.os.Handler().postDelayed(() -> keyPressed = false, 50);
            }
            return false;
        });
        phoneNumberET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!changingText && !keyPressed)
                    arraignNumber(s);
            }
        });
        submitBtn.setOnClickListener((v) -> serializeData());

        setupUI(mainView);

        return view;
    }

    @SuppressLint("SetTextI18n")
    private void serializeData(){
        errorLog.setText("");
        String phoneNum = phoneNumberET.getText().toString().replaceAll(" ", "");
        if(StringUtils.isEmpty(phoneNum)){
            errorLog.setText("Please fill in Your Phone Number");
            return;
        }
        getVerificationCode(context, phoneNum, "phone");
    }

    public static void displayError(String errorMsg){
        errorLog.setText(errorMsg);
    }

    private void arraignNumber(Editable s){
        String text = s.toString().replaceAll(" ", "");
        int textLn = text.length(), caret = phoneNumberET.getSelectionStart();
        String str = phoneNumberET.getText().subSequence(0, caret).toString();
        int numSpc = str.replaceAll("[^ ]", "").length();
        caret -= numSpc;
        if(textLn > 2) {
            changingText = true;
            String newStr = text.substring(0, 3);
            if (textLn > 3) {
                int strt = 3;
                newStr += " ";
                caret++;
                if (textLn > 6) {
                    strt = 6;
                    newStr += text.substring(3, 6);
                    newStr += " ";
                    caret++;
                }
                newStr += text.substring(strt);
            }
            phoneNumberET.setText(newStr);
            phoneNumberET.setSelection(caret);
            changingText = false;
        }
    }

    public void toggleSoftKeyboard(View view, boolean hide) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(hide)
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        else
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                toggleSoftKeyboard(v, true);
                phoneNumberET.clearFocus();
                return false;
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }
}