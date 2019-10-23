package com.practice.todo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

public class InputDialog extends Dialog {

    private final OnSendListener mListener;
    private final Activity mActivity;
    private EditText etInput;
    private TextView btnEnsure;

    public InputDialog(@NonNull Activity context, OnSendListener listener) {
        super(context, R.style.InputDialog); // 控制弹窗的样式
        mActivity = context;
        mListener = listener;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_input);
        // findViewById
        initView();
        // 设置弹窗的位置和大小
        Window window = getWindow();
        window.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = ViewGroup.LayoutParams.MATCH_PARENT;
        window.setAttributes(attributes);
        setCanceledOnTouchOutside(true);
        setListener();
    }

    /**
     * 展示时插入一段代码，让用户的手机0.15秒之后弹出软键盘
     */
    @Override
    public void show() {
        super.show();
        etInput.postDelayed(new Runnable() {
            @Override
            public void run() {
                openSoftKeyboard(etInput);
            }
        }, 150);
    }

    private void setListener() {
        // 监听软键盘的关闭，如果关闭，就将这个弹窗关掉
        KeyboardVisibilityEvent.setEventListener(mActivity, new KeyboardVisibilityEventListener() {
            @Override
            public void onVisibilityChanged(boolean isOpen) {
                if (!isOpen) dismiss();
            }
        });
        // 点击确认，判断输入，关闭弹窗
        btnEnsure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = getInput();
                if (input.isEmpty()) {
                    Toast.makeText(App.instance, mActivity.getString(R.string.tips_input_empty), Toast.LENGTH_LONG).show();
                    return;
                }
                hideKeyboard(mActivity, etInput);
                mListener.onSend(input);
                etInput.setText("");
                dismiss();
            }
        });
        // 根据输入框是否有输入来判断确认按钮是否是可点击的(这里多余了，本来想控制一下样式的，可删)
        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String trim = charSequence.toString().trim();
                btnEnsure.setEnabled(!TextUtils.isEmpty(trim));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    // 获取用户输入的字符串
    private String getInput() {
        String trim = etInput.getText().toString().trim();
        if (TextUtils.isEmpty(trim)) return "";
        else return trim;
    }

    // 关闭软键盘
    private void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // 开启软键盘
    private void openSoftKeyboard(EditText et) {
        if (et == null) return;
        et.setFocusable(true);
        et.setFocusableInTouchMode(true);
        et.requestFocus();
        InputMethodManager imm = (InputMethodManager) et.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(et, 0);
    }

    private void initView() {
        etInput = findViewById(R.id.et_input);
        btnEnsure = findViewById(R.id.btn_ensure);
    }

    public interface OnSendListener {
        void onSend(String content);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mActivity.onBackPressed();
    }
}
