package com.example.memorix.helper;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

public class HideSoftKeyboard {
    /**
     * Thiết lập ẩn bàn phím cho Activity
     * @param activity Activity cần áp dụng
     * @param rootView View gốc (layout chính)
     */
    public static void setupHideKeyboard(Activity activity, View rootView) {
        setupUI(activity, rootView);
    }

    /**
     * Thiết lập ẩn bàn phím cho Fragment
     * @param fragment Fragment cần áp dụng
     * @param rootView View gốc của Fragment
     */
    public static void setupHideKeyboard(Fragment fragment, View rootView) {
        if (fragment.getActivity() != null) {
            setupUI(fragment.getActivity(), rootView);
        }
    }

    /**
     * Phương thức chính để thiết lập sự kiện ẩn bàn phím
     * @param activity Activity context
     * @param view View cần thiết lập
     */
    private static void setupUI(Activity activity, View view) {
        // Nếu view không phải EditText thì gán OnTouchListener
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        hideSoftKeyboard(activity);
                    }
                    return false;
                }
            });
        }

        // Nếu là ViewGroup thì duyệt đệ quy các view con
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View innerView = viewGroup.getChildAt(i);
                setupUI(activity, innerView);
            }
        }
    }

    /**
     * Ẩn bàn phím mềm
     * @param activity Activity context
     */
    public static void hideSoftKeyboard(Activity activity) {
        if (activity == null) return;

        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

        View currentFocus = activity.getCurrentFocus();
        if (inputMethodManager != null && currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(
                    currentFocus.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
            currentFocus.clearFocus();
        }
    }

    /**
     * Ẩn bàn phím từ Fragment
     * @param fragment Fragment context
     */
    public static void hideSoftKeyboard(Fragment fragment) {
        if (fragment.getActivity() != null) {
            hideSoftKeyboard(fragment.getActivity());
        }
    }

    /**
     * Ẩn bàn phím từ View bất kỳ
     * @param view View có context
     */
    public static void hideSoftKeyboard(View view) {
        if (view == null || view.getContext() == null) return;

        Context context = view.getContext();
        InputMethodManager inputMethodManager =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(
                    view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
            view.clearFocus();
        }
    }

    /**
     * Hiển thị bàn phím cho EditText
     * @param editText EditText cần hiển thị bàn phím
     */
    public static void showSoftKeyboard(EditText editText) {
        if (editText == null || editText.getContext() == null) return;

        editText.requestFocus();
        InputMethodManager inputMethodManager =
                (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        if (inputMethodManager != null) {
            inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    /**
     * Kiểm tra xem bàn phím có đang hiển thị không
     * @param activity Activity context
     * @return true nếu bàn phím đang hiển thị
     */
    public static boolean isKeyboardVisible(Activity activity) {
        if (activity == null) return false;

        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

        return inputMethodManager != null && inputMethodManager.isAcceptingText();
    }
}
