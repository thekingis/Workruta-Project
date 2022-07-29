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
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.workruta.android.Utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import static com.workruta.android.ForgotPassActivity.getVerificationCode;

public class FPCodeFragment extends Fragment {

    Context context;
    RelativeLayout mainView;
    EditText vCode1, vCode2;
    TextView textView, submitBtn;
    @SuppressLint("StaticFieldLeak")
    static TextView errorLog;
    boolean changingText, keyPressed;
    JSONObject strObj = new JSONObject();

    public FPCodeFragment(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fpcode, container, false);

        changingText = false;
        keyPressed = false;
        mainView = view.findViewById(R.id.mainView);
        vCode1 = view.findViewById(R.id.vCode1);
        vCode2 = view.findViewById(R.id.vCode2);
        textView = view.findViewById(R.id.textView);
        submitBtn = view.findViewById(R.id.submit);
        errorLog = view.findViewById(R.id.errorLog);

        try {
            strObj.put("email", "A verification code have been sent to Your Email. If you don't get any, check your spam folder");
            strObj.put("number", "A verification code have been sent to Your Phone Number");
            String key = ForgotPassActivity.curType, text = strObj.getString(key);
            textView.setText(text);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        vCode1.setLongClickable(false);
        vCode2.setLongClickable(false);
        vCode1.setAccessibilityDelegate(new View.AccessibilityDelegate(){
            @Override
            public void sendAccessibilityEvent(View host, int eventType) {
                super.sendAccessibilityEvent(host, eventType);
                if(eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED){
                    int selStart = vCode1.getSelectionStart(),
                            selEnd = vCode1.getSelectionEnd();
                    if(selStart != selEnd)
                        vCode1.setSelection(selEnd);
                    if(selEnd == 3){
                        vCode1.clearFocus();
                        vCode2.requestFocus();
                        vCode2.setSelection(0);
                    }
                }
            }
        });
        vCode2.setAccessibilityDelegate(new View.AccessibilityDelegate(){
            @Override
            public void sendAccessibilityEvent(View host, int eventType) {
                super.sendAccessibilityEvent(host, eventType);
                if(eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED){
                    int selStart = vCode2.getSelectionStart(),
                            selEnd = vCode2.getSelectionEnd();
                    if(selStart != selEnd)
                        vCode2.setSelection(selEnd);
                    if(vCode2.getText().length() == 0 && vCode1.getText().length() < 3) {
                        vCode2.clearFocus();
                        vCode1.requestFocus();
                        vCode1.setSelection(vCode1.getText().length());
                    }
                }
            }
        });
        vCode2.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus &&vCode2.getText().length() == 0 && vCode1.getText().length() < 3) {
                vCode2.clearFocus();
                vCode1.requestFocus();
                vCode1.setSelection(vCode1.getText().length());
                toggleSoftKeyboard(vCode1, false);
            }
        });
        vCode1.setOnKeyListener((v, keyCode, event) -> {
            if(event.getAction() == KeyEvent.ACTION_DOWN)
                keyPressed = true;
            if(event.getAction() == KeyEvent.ACTION_UP)
                keyPressed = false;
            return keyCode != KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN && vCode1.getText().length() == 3 && vCode2.getText().length() == 3;
        });
        vCode2.setOnKeyListener((v, keyCode, event) -> {
            if(event.getAction() == KeyEvent.ACTION_DOWN){
                keyPressed = true;
                if(keyCode != KeyEvent.KEYCODE_DEL && vCode2.getText().length() == 3)
                    return true;
                if(keyCode == KeyEvent.KEYCODE_DEL && vCode2.getSelectionStart() == 0){
                    vCode1.setText(vCode1.getText().delete(2, 3));
                    vCode2.clearFocus();
                    vCode1.requestFocus();
                    vCode1.setSelection(2);
                }
            }
            if(event.getAction() == KeyEvent.ACTION_UP)
                keyPressed = false;
            return false;
        });
        vCode1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!changingText && s.length() > 3) {
                    changingText = true;
                    String extraStr = s.subSequence(3, s.length()).toString();
                    int caret = vCode1.getSelectionStart();
                    vCode1.setText(vCode1.getText().delete(3, s.length()));
                    vCode2.setText(vCode2.getText().insert(0, extraStr));
                    if(caret < 3)
                        vCode1.setSelection(caret);
                    else {
                        vCode1.clearFocus();
                        vCode2.requestFocus();
                        vCode2.setSelection(caret - 3);
                    }
                    changingText = false;
                }
                if(!changingText && s.length() < 3 && vCode2.getText().length() > 0) {
                    changingText = true;
                    String extraStr = vCode2.getText().subSequence(0, 3 - s.length()).toString();
                    int caret = vCode1.getSelectionStart();
                    vCode2.setText(vCode2.getText().delete(0, 3 - s.length()));
                    vCode1.setText(vCode1.getText().insert(s.length(), extraStr));
                    vCode1.setSelection(caret);
                    changingText = false;
                }
            }
        });
        submitBtn.setOnClickListener((v) -> serializeData());
        setupUI(mainView);

        return view;
    }

    @SuppressLint("SetTextI18n")
    private void serializeData(){
        errorLog.setText("");
        String code = vCode1.getText().toString();
        code += vCode2.getText().toString();
        if(code.length() < 6){
            errorLog.setText("Incomplete Verification Code");
            return;
        }
        getVerificationCode(context, code);
    }

    public static void displayError(String errorMsg){
        errorLog.setText(errorMsg);
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
                vCode1.clearFocus();
                vCode2.clearFocus();
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