package com.practice.todo

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.dialog_input.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent


class InputDialog(
    private val mActivity: Activity,
    private val block: (String) -> Unit
) : Dialog(mActivity, R.style.InputDialog) {

    private val mInput get() = et_input.text.toString().trim()

    companion object {
        fun show(context: Activity, onSendListener: (String) -> Unit) =
            InputDialog(context, onSendListener).apply { show() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_input)
        val window = window!!
        window.setGravity(Gravity.BOTTOM)
        window.setWindowAnimations(0)
        val attributes = window.attributes
        attributes.width = ViewGroup.LayoutParams.MATCH_PARENT
        window.attributes = attributes
        setCanceledOnTouchOutside(true)
        setListener()
    }

    override fun show() {
        super.show()
        et_input.postDelayed({ openSoftKeyboard(et_input) }, 200)
    }

    private fun setListener() {
        KeyboardVisibilityEvent.setEventListener(mActivity) { if (!it) dismiss() }
        btn_ensure.setOnClickListener {
            if (mInput.isEmpty()) {
                Toast.makeText(
                    App.instance,
                    context.getString(R.string.tips_input_empty),
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            hideKeyboard(mActivity, et_input)
            block(mInput)
            et_input.setText("")
            dismiss()
        }
        et_input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val trim = s?.toString()?.trim() ?: ""
                btn_ensure.isEnabled = trim.isNotEmpty()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mActivity.onBackPressed()
    }

    private fun openSoftKeyboard(et: EditText?) {
        et ?: return
        et.isFocusable = true
        et.isFocusableInTouchMode = true
        et.requestFocus()
        val inputManager =
            et.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.showSoftInput(et, 0)
    }

    private fun hideKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}