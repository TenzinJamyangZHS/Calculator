package com.ttt.calculator;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class CalculatorActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {
    private EditText mInputView, mResultView;//输入框，结果框
    private Guideline mGuideline1;//确定位置
    private Button mClearButton, mEqualsButton, mDeleteButton, mMoreButton;//清零，等于，删除以及更多按键
    private Button[] mSmallButton, mOperatorButton, mNumberButton;//小按键集，运算按键集，以及数字按键集
    private long mPressDuration;//按压时常
    private long mPressTime;//开始按压的时间
    private boolean mShowFlag = false;//是否显示更多按键
    private TableRow mOperatorRow3;
    private TableRow mOperatorRow2;//默认隐藏的小按键所在位置
    private StringBuilder mInputText;//记录输入内容
    private int mBracketStatus = 0;//记录括号状态
    private int mCursorPosition;//记录指针位置
    public static final String TOO_LARGE = "Infinity";//过大提醒
    private boolean canCalculate;//检测是否可以运算
    private StringBuilder mSavedText;//点击等号后记录之前输入的内容
    private SharedPreferences mHistoryPreferences;//记录历史记录和主题
    private SharedPreferences mThemePreferences;
    private Button mHistoryButton;//历史记录按键
    private Button mThemeButton;//主题切换按键
    private Button mCopyButton;//复制按键
    private Button mAboutButton;//关于按键
    private boolean isDarkThemeOn;//记录系统主题
    private boolean mCurrentTheme;//记录当前应用主题

    @SuppressLint({"ClickableViewAccessibility", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);
        mInputView = findViewById(R.id.input_edit);
        mResultView = findViewById(R.id.result_edit);
        mGuideline1 = findViewById(R.id.guideline_3_2);
        mClearButton = findViewById(R.id.button_clear);
        mEqualsButton = findViewById(R.id.button_equals);
        mDeleteButton = findViewById(R.id.button_delete);
        mOperatorRow2 = findViewById(R.id.operator_row_2);
        mOperatorRow3 = findViewById(R.id.operator_row_3);
        mHistoryButton = findViewById(R.id.button_history);
        mThemeButton = findViewById(R.id.button_theme);
        mCopyButton = findViewById(R.id.button_copy);
        mAboutButton = findViewById(R.id.button_about);
        mHistoryPreferences = getSharedPreferences("history", Context.MODE_PRIVATE);
        mThemePreferences = getSharedPreferences("themes", Context.MODE_PRIVATE);
        isDarkThemeOn = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        mSmallButton = new Button[]{
                findViewById(R.id.button_root),
                findViewById(R.id.button_pi),
                findViewById(R.id.button_pow),
                findViewById(R.id.button_factorial),
                findViewById(R.id.button_log),
                findViewById(R.id.button_tan),
                findViewById(R.id.button_cos),
                findViewById(R.id.button_sin)
        };
        mOperatorButton = new Button[]{
                findViewById(R.id.button_bracket),
                findViewById(R.id.button_percent),
                findViewById(R.id.button_divide),
                findViewById(R.id.button_mulitply),
                findViewById(R.id.button_minus),
                findViewById(R.id.button_plus)
        };
        mNumberButton = new Button[]{
                findViewById(R.id.button_point),
                findViewById(R.id.button_zero),
                findViewById(R.id.button_one),
                findViewById(R.id.button_two),
                findViewById(R.id.button_three),
                findViewById(R.id.button_four),
                findViewById(R.id.button_five),
                findViewById(R.id.button_six),
                findViewById(R.id.button_seven),
                findViewById(R.id.button_eight),
                findViewById(R.id.button_nine)
        };
        mHistoryButton.setOnTouchListener(this);
        mHistoryButton.setOnClickListener(this);
        mThemeButton.setOnTouchListener(this);
        mCopyButton.setOnTouchListener(this);
        mCopyButton.setOnClickListener(this);
        mAboutButton.setOnTouchListener(this);
        aboutMethod();
        copyResultMethod();
        getMyTheme();
        setThemeFollowSystem();
        setMyTheme();
        moreButtonMethod();
//        overStatusBar();
        setSingleLines();
        hideInput();
        setMyTestSize();
        mClearButton.setOnClickListener(this);
        mClearButton.setOnTouchListener(this);
        mEqualsButton.setOnClickListener(this);
        mEqualsButton.setOnTouchListener(this);
        mDeleteButton.setOnClickListener(this);
        mDeleteButton.setOnTouchListener(this);

        for (Button button : mOperatorButton) {
            button.setOnClickListener(this);
            button.setOnTouchListener(this);
        }
        for (Button button : mNumberButton) {
            button.setOnTouchListener(this);
            button.setOnClickListener(this);

        }
        for (Button button : mSmallButton) {
            button.setOnTouchListener(this);
            button.setOnClickListener(this);
        }

    }

    private void getMyTheme() {//获取保存的主题
        SharedPreferences getTheme = getSharedPreferences("themes", MODE_PRIVATE);
        String savedTheme = getTheme.getString("theme", "");
        switch (savedTheme) {
            case "system":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                boolean currentTheme = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
                if (currentTheme) {
                    mThemeButton.setText(getString(R.string.theme_dark));
                    mCurrentTheme = true;
                } else {
                    mThemeButton.setText(getString(R.string.theme_light));
                    mCurrentTheme = false;
                }
                break;
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                mThemeButton.setText(getString(R.string.theme_light));
                mCurrentTheme = false;
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                mThemeButton.setText(getString(R.string.theme_dark));
                mCurrentTheme = true;
                break;
            default:
                if (isDarkThemeOn) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    mThemeButton.setText(getString(R.string.theme_dark));
                    mCurrentTheme = true;
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    mThemeButton.setText(getString(R.string.theme_light));
                    mCurrentTheme = false;
                }
                break;
        }

    }

    private void setThemeFollowSystem() {//设置主题跟随系统
        mThemeButton.setOnLongClickListener(view -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            SharedPreferences.Editor editor = mThemePreferences.edit();
            editor.putString("theme", "system");
            editor.apply();
            mThemePreferences.edit().apply();
            return true;
        });
    }

    private void aboutMethod() {//关于
        mAboutButton.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(CalculatorActivity.this, R.style.MyAboutTheme);
            builder.setTitle(getString(R.string.about));
            builder.setMessage("Github:" + "\n" + "\n" + "https://github.com/TenzinJamyangZHS/Calculator");
            AlertDialog dialog = builder.create();
            dialog.getWindow().getAttributes().windowAnimations = R.style.MyAboutTheme;
            dialog.show();
        });
    }

    private void copyResultMethod() {//长按复制按键复制结果栏
        mCopyButton.setOnLongClickListener(view -> {
            if (mResultView.length() > 0) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("result", mResultView.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(CalculatorActivity.this, "Result Copied", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    private void setMyTheme() {//切换主题
        SharedPreferences.Editor editor = mThemePreferences.edit();
        mThemeButton.setOnClickListener(view -> {
            String saveTheme;
            if (mCurrentTheme) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                mThemeButton.setText(getString(R.string.theme_light));
                saveTheme = "light";
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                mThemeButton.setText(getString(R.string.theme_dark));
                saveTheme = "dark";
            }
            editor.putString("theme", saveTheme);
            editor.apply();
            mThemePreferences.edit().apply();
        });


    }

    private void showHistory() {//显示历史记录
        String title = "History";
        AlertDialog.Builder builder = new AlertDialog.Builder(CalculatorActivity.this, R.style.MyDialogTheme);
        builder.setTitle(title);
        Map<String, ?> allHistory = mHistoryPreferences.getAll();//获取历史列表
        Iterator<? extends Entry<String, ?>> iterator = allHistory.entrySet().iterator();
        ArrayList<String> historyList = new ArrayList<>();
        ArrayList<Long> keyList = new ArrayList<>();
        while (iterator.hasNext()) {
            Entry<String, ?> entry = iterator.next();
            String key = entry.getKey();
            keyList.add(Long.valueOf(key));
            Collections.sort(keyList);
        }
        for (int i = 0; i < keyList.size(); i++) {
            String value = String.valueOf(allHistory.get(String.valueOf(keyList.get(i))));
            historyList.add(value);
        }
        StringBuilder showHistory = new StringBuilder();
        for (int i = 0; i < historyList.size(); i++) {
            showHistory.append(historyList.get(i)).append("\n" + "\n");//把历史内容按顺序添加到字符串
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;//获取屏幕尺寸来确定字体大小
        ScrollView scrollView = new ScrollView(new ContextThemeWrapper(CalculatorActivity.this, R.style.MyDialogTheme));
        scrollView.setFillViewport(true);
        EditText mHistoryView = new EditText(new ContextThemeWrapper(CalculatorActivity.this, R.style.MyDialogTheme));
        mHistoryView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        scrollView.addView(mHistoryView);
        mHistoryView.setPadding(40, 40, 40, 40);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mHistoryView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 30));
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mHistoryView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 15));
        }
        mHistoryView.setText(showHistory);
        mHistoryView.setBackground(null);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        try {
            Class<EditText> cls = EditText.class;
            Method method = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
            method.setAccessible(true);
            method.invoke(mHistoryView, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        builder.setView(scrollView);
        builder.setNegativeButton("Clear", (dialog, which) -> {
            SharedPreferences.Editor editor = mHistoryPreferences.edit();
            editor.clear();
            editor.apply();
            dialog.dismiss();
        });
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.getWindow().getAttributes().windowAnimations = R.style.MyDialogTheme;
        dialog.show();

    }

    @SuppressLint("ClickableViewAccessibility")
    private void moreButtonMethod() {//更多按键操作
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {//确定是否为竖屏
            mMoreButton = findViewById(R.id.button_more);
            mMoreButton.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);//点击反馈震动
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return false;
            });
            mMoreButton.setOnClickListener(new View.OnClickListener() {//操作显示更多按键
                final ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) mGuideline1.getLayoutParams();
                ValueAnimator valueAnimator;

                @Override
                public void onClick(View v) {//重新设置guideline位置以显示或隐藏更多的按键
                    if (!mShowFlag) {
                        valueAnimator = ValueAnimator.ofFloat(0.4f, 0.54f);
                        valueAnimator.setDuration(300);
                        valueAnimator.setInterpolator(new AnticipateOvershootInterpolator());
                        valueAnimator.addUpdateListener(animation -> {
                            lp.guidePercent = (float) valueAnimator.getAnimatedValue();
                            mGuideline1.setLayoutParams(lp);
                        });
                        valueAnimator.start();
                        mMoreButton.setText(R.string.more_up);
                        mOperatorRow2.setVisibility(View.VISIBLE);
                        mOperatorRow3.setVisibility(View.VISIBLE);
                        mShowFlag = true;
                    } else {
                        valueAnimator = ValueAnimator.ofFloat(0.54f, 0.4f);
                        valueAnimator.setDuration(300);
                        valueAnimator.setInterpolator(new AnticipateOvershootInterpolator());
                        valueAnimator.addUpdateListener(animation -> {
                            lp.guidePercent = (float) valueAnimator.getAnimatedValue();
                            mGuideline1.setLayoutParams(lp);
                        });
                        valueAnimator.start();
                        mMoreButton.setText(R.string.more_down);
                        mOperatorRow2.setVisibility(View.GONE);
                        mOperatorRow3.setVisibility(View.GONE);
                        mShowFlag = false;
                    }
                }
            });
        }
    }

    /*设置字体沉浸状态栏隐藏输入法一行显示---*/
    private void setMyTestSize() {//设置字体大小
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int widthScreen = displayMetrics.widthPixels;
        int inputLength = widthScreen / (height / 14);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mInputView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 8));
            mResultView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 20));
            mInputView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {//根据输入框内容长度自动调节字体大小
                    if (s.length() <= inputLength) {
                        mInputView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 8));
                    } else if (s.length() > inputLength && s.length() <= inputLength + 2) {
                        mInputView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 10));
                    } else if (s.length() > inputLength + 2 && s.length() <= inputLength + 4) {
                        mInputView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 12));
                    } else if (s.length() > inputLength + 4 && s.length() <= inputLength + 6) {
                        mInputView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 14));
                    } else if (s.length() > inputLength + 6 && s.length() <= inputLength + 8) {
                        mInputView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 16));
                    } else if (s.length() > inputLength + 8 && s.length() <= inputLength + 10) {
                        mInputView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 18));
                    } else {
                        mInputView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 20));
                    }
                }
            });
            mHistoryButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 60));
            mMoreButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 80));
            mClearButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 25));
            mEqualsButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 25));
            mDeleteButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 25));
            mThemeButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 60));
            mCopyButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 60));
            mAboutButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 60));
            for (Button button : mOperatorButton) {
                button.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 25));
            }
            for (Button button : mNumberButton) {
                button.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 25));
            }
            for (Button button : mSmallButton) {
                button.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 40));
            }
        }
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mInputView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 12));
            mResultView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 20));
            for (Button button : mOperatorButton) {
                button.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 15));
            }
            for (Button button : mNumberButton) {
                button.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 15));
            }
            for (Button button : mSmallButton) {
                button.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 30));
            }
            mHistoryButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 30));
            mThemeButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 30));
            mCopyButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 30));
            mAboutButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 30));
            mHistoryButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 30));
            mClearButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 15));
            mEqualsButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 15));
            mDeleteButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / 15));
        }
    }

    private void hideInput() {//隐藏软键盘
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        try {
            Class<EditText> cls = EditText.class;
            Method method = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
            method.setAccessible(true);
            method.invoke(mInputView, false);
            method.invoke(mResultView, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setSingleLines() {//一行显示以及聚焦
        mInputView.setSingleLine(true);
        mResultView.setSingleLine(true);
        mInputView.requestFocus();
        mResultView.setCursorVisible(false);
        mResultView.setFocusable(false);
    }

    private void overStatusBar() {//沉浸状态栏覆盖屏幕刘海
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

    }
    /*---设置字体沉浸状态栏隐藏输入法一行显示*/

    @Override
    public void onClick(View v) {
        setButtonBackground();
        Button button = (Button) v;
        String buttonString = button.getText().toString();//获取按键的文字
        mCursorPosition = mInputView.getSelectionStart();//获取当前指针位置
        mInputText = new StringBuilder(mInputView.getText().toString());//获取当前输入框文本内容
        boolean inputOk = true;//输入规则检测
        if (notEmptyNotStart()//当输入框内容不为空 指针不在0位 不在末尾时 做以下判断 相邻的内容是否违规
                && mCursorPosition != mInputText.length()) {
            if (!notBeforeAny(String.valueOf(mInputText), mCursorPosition - 1) || !notAfterAny()) {
                inputOk = false;
            }
        }
        //检测输入内容时，一般先判断输入框内容是否为空，再判断指针位置是否为0或末尾，再根据输入的内容相应的规则判断
        //输入左括号时，mBracketStatus自增，输入右括号时，其自减，以此判断括号是否完整。
        if (inputOk) {//确保相邻内容不违规时
            switch (buttonString) {
                case "Copy":
                    copyInputMethod();
                    break;
                case "History":
                    showHistory();
                    break;
                case "AC":
                    clearMethod();
                    break;
                case "⌫":
                    deleteMethod();
                    break;
                case "=":
                    equalsMethod();
                    break;
                case "( )":
                    bracketInput();
                    break;
                case "√":
                    operateInputRoot(buttonString);
                    break;
                case "log":
                    operateInputLog(buttonString);
                    break;
                case "tan":
                case "cos":
                case "sin":
                    operateInputTCS(buttonString);
                    break;
                case "%":
                case "!":
                    operateInputPF(buttonString);
                    break;
                case "^":
                    operateInputPow(buttonString);
                    break;
                case "-":
                    operateInputMinus(buttonString);
                    break;
                case "+":
                case "×":
                case "÷":
                    operateInputPMD(buttonString);
                    break;
                case "π":
                    piInput(buttonString);
                    break;
                case ".":
                    pointInput(buttonString);
                    break;
                default:
                    numInput(buttonString);
            }
        }

    }

    private void copyInputMethod() {//单击复制按键复制输入框内容
        if (mInputView.length() > 0) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("input", mInputView.getText().toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Input Copied", Toast.LENGTH_SHORT).show();
        }
    }

    /*部分规则---*/
    private boolean notEmptyNotStart() {
        return mInputText.length() != 0 && mCursorPosition != 0;
    }

    private boolean numberNoPoint(String s, int i) {
        //所有数字不包含小数点
        String NUMBER_NO_POINT = "0123456789";
        return NUMBER_NO_POINT.indexOf(s.charAt(i)) == -1;
    }

    private boolean numberPoint(String s, int i) {
        //所有数字包含小数点
        String NUMBER_POINT = ".0123456789";
        return NUMBER_POINT.indexOf(s.charAt(i)) == -1;
    }


    private boolean notAfterOperator1(String s, int num) {
        //根号等后不可存在
        String NOT_AFTER_OPERATOR_1 = ".+-^÷×!%";
        return NOT_AFTER_OPERATOR_1.indexOf(s.charAt(num)) == -1;
    }

    private boolean notAfterAny() {
        //不能相邻后者
        String NOT_AFTER_ANY = "anoig";
        return NOT_AFTER_ANY.indexOf(mInputText.charAt(mCursorPosition)) == -1;
    }

    private boolean notBeforeAny(String s, int num) {
        //不能相邻前者
        String NOT_BEFORE_ANY = "anotcsilg";
        return NOT_BEFORE_ANY.indexOf(s.charAt(num)) == -1;
    }

    private boolean notAfterOperator2(String s, int num) {
        //log等后不可存在
        String NOT_AFTER_OPERATOR_2 = ".+^÷×!%";
        return NOT_AFTER_OPERATOR_2.indexOf(s.charAt(num)) == -1;
    }

    private boolean notBeforeOperator(String s, int num) {
        //次方等前不可存在
        String NOT_BEFORE_OPERATOR = ".+-^÷×(√";
        return NOT_BEFORE_OPERATOR.indexOf(s.charAt(num)) == -1;
    }

    private boolean notPoint(int num) {
        return mInputText.charAt(num) != '.';
    }
    /*---部分规则*/

    /*括号相关---*/
    private void bracketInput() {//输入括号
        if (mInputText.length() == 0) {
            addBracketLeft();
        } else {
            if (mCursorPosition == mInputText.length()) {
                bracketCheckEnd();
            } else if (mCursorPosition == 0) {
                bracketCheckStart();
            } else {
                bracketCheck();
            }
        }
    }

    private void bracketCheckStart() {//检测能否输入括号开端
        if (bracketLeftCheck()) {
            addBracketLeft();
        }
    }

    private void bracketCheckEnd() {//检测能否输入括号结尾
        if (mBracketStatus > 0) {
            if (bracketRightCheckBefore()) {
                addBracketRight();
            } else if (bracketLeftCheckBefore()) {
                addBracketLeft();
            }
        } else {
            if (bracketLeftCheckBefore()) {
                addBracketLeft();
            }
        }
    }

    private void bracketCheck() {//检测能否输入括号
        int countBracket = 0;//统计已有括号状态
        for (int i = 0; i < mCursorPosition + 1; i++) {
            if (mInputText.charAt(i) == '(') countBracket++;
            if (mInputText.charAt(i) == ')') countBracket--;
        }
        if (countBracket > 0) {
            if (bracketRightCheck()) {
                addBracketRight();
            } else if (bracketLeftCheck()) {
                addBracketLeft();
            }
        } else {
            if (bracketLeftCheck()) {
                addBracketLeft();
            }
        }
    }

    private boolean bracketRightCheck() {
        return bracketRightCheckBefore() && notPoint(mCursorPosition);
    }

    private boolean bracketLeftCheck() {//左括号检测
        return bracketLeftCheckBefore()
                && notAfterAny()
                && notAfterOperator2(String.valueOf(mInputText), mCursorPosition);
    }

    private boolean bracketRightCheckBefore() {//右括号检测前
        return notBeforeAny(String.valueOf(mInputText), mCursorPosition - 1)
                && notBeforeOperator(String.valueOf(mInputText), mCursorPosition - 1);
    }

    private boolean bracketLeftCheckBefore() {//左括号检测前
        return notBeforeAny(String.valueOf(mInputText), mCursorPosition - 1)
                && notPoint(mCursorPosition - 1);
    }

    private void addBracketLeft() {//添加做括号
        if (mSavedText != null) {
            mSavedText.setLength(0);
        }
        updateInputView(mCursorPosition + 1, mCursorPosition,
                getString(R.string.bracketleft));
        mBracketStatus++;

    }

    private void addBracketRight() {//添加右括号
        if (mSavedText != null) {
            mSavedText.setLength(0);
        }
        updateInputView(mCursorPosition + 1, mCursorPosition,
                getString(R.string.bracketright));
        mBracketStatus--;
        updateResultView();
    }

    /*---括号相关*/

    private void operateInputRoot(String buttonString) {//输入根号
        if (mSavedText != null) {
            mSavedText.setLength(0);
        }
        if (mInputText.length() == 0) {
            updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
        } else {
            if (mCursorPosition == mInputText.length()) {
                if (notPoint(mCursorPosition - 1)) {
                    updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                }
            } else if (mCursorPosition == 0) {
                if (notAfterOperator1(String.valueOf(mInputText), 0)) {
                    updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                    if (mInputText.length() > 1) {
                        updateResultView();
                    }
                }
            } else {
                if (notPoint(mCursorPosition - 1)
                        && notAfterOperator1(String.valueOf(mInputText), mCursorPosition)) {
                    updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                    updateResultView();
                }
            }
        }
    }

    private void operateInputLog(String buttonString) {//输入log
        if (mSavedText != null) {
            mSavedText.setLength(0);
        }
        if (mInputText.length() == 0) {
            updateInputView(mCursorPosition + 4, mCursorPosition,
                    buttonString + getString(R.string.bracketleft));
            mBracketStatus++;
        } else {
            if (mCursorPosition == mInputText.length()) {
                if (notPoint(mCursorPosition - 1)) {
                    updateInputView(mCursorPosition + 4, mCursorPosition,
                            buttonString + getString(R.string.bracketleft));
                    mBracketStatus++;
                }
            } else if (mCursorPosition == 0) {
                if (notAfterOperator1(String.valueOf(mInputText), 0)) {
                    updateInputView(mCursorPosition + 4, mCursorPosition,
                            buttonString + getString(R.string.bracketleft));
                    mBracketStatus++;
                }
            } else {
                if (notPoint(mCursorPosition - 1)
                        && notAfterOperator1(String.valueOf(mInputText), mCursorPosition)) {
                    updateInputView(mCursorPosition + 4, mCursorPosition,
                            buttonString + getString(R.string.bracketleft));
                    mBracketStatus++;
                }
            }
        }
    }

    private void operateInputTCS(String buttonString) {//输入三角函数
        if (mSavedText != null) {
            mSavedText.setLength(0);
        }
        if (mInputText.length() == 0) {
            updateInputView(mCursorPosition + 4, mCursorPosition,
                    buttonString + getString(R.string.bracketleft));
            mBracketStatus++;
        } else {
            if (mCursorPosition == mInputText.length()) {
                if (notPoint(mCursorPosition - 1)) {
                    updateInputView(mCursorPosition + 4, mCursorPosition,
                            buttonString + getString(R.string.bracketleft));
                    mBracketStatus++;
                }
            } else if (mCursorPosition == 0) {
                if (notAfterOperator2(String.valueOf(mInputText), 0)) {
                    updateInputView(mCursorPosition + 4, mCursorPosition,
                            buttonString + getString(R.string.bracketleft));
                    mBracketStatus++;
                }
            } else {
                if (notPoint(mCursorPosition - 1)
                        && notAfterOperator2(String.valueOf(mInputText), mCursorPosition)) {
                    updateInputView(mCursorPosition + 4, mCursorPosition,
                            buttonString + getString(R.string.bracketleft));
                    mBracketStatus++;
                }
            }
        }
    }

    private void operateInputPF(String buttonString) {//输入百分比与阶乘
        if (mSavedText != null) {
            mSavedText.setLength(0);
        }
        if (notEmptyNotStart()) {
            if (mCursorPosition == mInputText.length()) {
                if (notBeforeOperator(String.valueOf(mInputText), mCursorPosition - 1)) {
                    updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                }
            } else {
                if (notBeforeOperator(String.valueOf(mInputText), mCursorPosition - 1)
                        && notPoint(mCursorPosition)) {
                    updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                }
            }
        }
        updateResultView();
    }

    private void operateInputPow(String buttonString) {//输入次方
        if (mSavedText != null) {
            mSavedText.setLength(0);
        }
        if (notEmptyNotStart()) {
            if (mCursorPosition == mInputText.length()) {
                if (notBeforeOperator(String.valueOf(mInputText), mCursorPosition - 1)) {
                    updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                }
            } else {
                if (notBeforeOperator(String.valueOf(mInputText), mCursorPosition - 1)
                        && notAfterOperator2(String.valueOf(mInputText), mCursorPosition)) {
                    updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                    updateResultView();
                }
            }
        }
    }

    private void operateInputMinus(String buttonString) {//输入减号
        if (mSavedText != null) {
            mSavedText.setLength(0);
        }
        if (mInputText.length() == 0) {
            updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
        } else {
            if (mCursorPosition == mInputText.length()) {
                if (notBeforeOperator(String.valueOf(mInputText), mCursorPosition - 1)) {
                    updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                }
            } else if (mCursorPosition == 0) {
                if (notAfterOperator1(String.valueOf(mInputText), 0)) {
                    updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                    updateResultView();
                }
            } else {
                if (notBeforeOperator(String.valueOf(mInputText), mCursorPosition - 1)
                        && notAfterOperator1(String.valueOf(mInputText), mCursorPosition)) {
                    updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                    updateResultView();
                }
            }
        }
    }

    private void operateInputPMD(String buttonString) {//输入加乘除
        if (mSavedText != null) {
            mSavedText.setLength(0);
        }
        if (notEmptyNotStart()) {
            if (mCursorPosition == mInputText.length()) {
                if (notBeforeOperator(String.valueOf(mInputText), mCursorPosition - 1)) {
                    updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                }
            } else {
                if (notBeforeOperator(String.valueOf(mInputText), mCursorPosition - 1)
                        && notAfterOperator1(String.valueOf(mInputText), mCursorPosition)) {
                    updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                    updateResultView();
                }
            }
        }
    }

    private void piInput(String buttonString) {//输入圆周率
        if (mSavedText != null) {
            mSavedText.setLength(0);
        }
        if (mInputText.length() == 0) {
            updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
        } else {
            if (mCursorPosition == mInputText.length()) {
                if (notPoint(mCursorPosition - 1)) {
                    updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                }
            } else if (mCursorPosition == 0) {
                if (mInputText.charAt(0) != '.' && mInputText.charAt(0) != '!') {
                    updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                }
            } else {
                if (notPoint(mCursorPosition - 1) && notPoint(mCursorPosition)
                        && mInputText.charAt(mCursorPosition) != '!') {
                    updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                }
            }
        }
        updateResultView();
    }

    private void pointInput(String buttonString) {//输入小数点
        if (mSavedText != null) {
            mSavedText.setLength(0);
        }
        if (notEmptyNotStart()) {//小数点不能开头输入
            int indexFirst = 0;//当前输入所在位置的数字开始点
            int indexSecond = mInputText.length() - 1;//当前输入所在位置的数字结尾点
            if (mCursorPosition == mInputText.length()) {//若输入位置在结尾，判断所在位置的数字是否已有小数点，向前判断
                pointCheckEnd(indexFirst, buttonString);
            } else {//若输入位置不在结尾，判断所在位置的数字是否已有小数点，前后判断
                pointCheck(indexFirst, indexSecond, buttonString);
            }
        }
    }

    private void pointCheck(int indexFirst, int indexSecond, String buttonString) {
        indexFirst = getNumberStart(indexFirst);
        indexSecond = getNumberEnd(indexSecond);
        if (indexFirst == 0 && indexSecond == mInputText.length() - 1) {
            if (notPoint(indexFirst) && notPoint(indexSecond)) {
                updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                updateResultView();
            }
        } else if (indexFirst == 0 && indexSecond != mInputText.length() - 1) {
            if (notPoint(indexFirst) && notPoint(indexSecond)
                    && indexSecond != mCursorPosition) {
                updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                updateResultView();
            }
        } else if (indexFirst != 0 && indexSecond == mInputText.length() - 1
                && indexFirst != mCursorPosition - 1) {
            if (notPoint(indexFirst) && notPoint(indexSecond)) {
                updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                updateResultView();
            }
        } else {
            if (notPoint(indexFirst) && notPoint(indexSecond)
                    && indexFirst != mCursorPosition - 1 && indexSecond != mCursorPosition) {
                updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                updateResultView();
            }
        }
    }

    private int getNumberEnd(int indexSecond) {
        for (int i = mCursorPosition; i < mInputText.length(); i++) {
            if (numberNoPoint(String.valueOf(mInputText), i)) {
                indexSecond = i;
                break;
            }
        }
        return indexSecond;
    }

    private int getNumberStart(int indexFirst) {
        for (int i = mCursorPosition - 1; i >= 0; i--) {
            if (numberNoPoint(String.valueOf(mInputText), i)) {
                indexFirst = i;
                break;
            }
        }
        return indexFirst;
    }

    private void pointCheckEnd(int indexFirst, String buttonString) {
        indexFirst = getNumberStart(indexFirst);
        if (indexFirst == 0) {
            if (notPoint(indexFirst)) {
                updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                updateResultView();
            }
        } else {
            if (notPoint(indexFirst) && indexFirst != mCursorPosition - 1) {
                updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                updateResultView();
            }
        }
    }

    private void numInput(String buttonString) {//输入数字
        if (mSavedText != null) {
            mSavedText.setLength(0);
        }
        if (mInputText.length() == 0) {
            updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
        } else {
            if (mInputText.length() == 1) {
                if (mInputText.charAt(0) == '0') {
                    mInputText = new StringBuilder(buttonString);
                    mInputView.setText(mInputText);
                    mInputView.setSelection(1);
                } else {
                    updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                }
            } else {
                boolean zeroOK = true;
                boolean hasPoint;
                if (buttonString.equals(getString(R.string.zero))) {
                    if (mCursorPosition != 0 && mCursorPosition < mInputText.length()) {
                        zeroOK = zeroBefore();
                        if (!numberPoint(String.valueOf(mInputText), mCursorPosition)) {
                            if (numberPoint(String.valueOf(mInputText), mCursorPosition - 1)) {
                                hasPoint = zeroPoint();
                                if (hasPoint) {
                                    zeroOK = false;
                                } else {
                                    buttonString = "0.";
                                }
                            }
                        }
                    } else if (mCursorPosition == 0) {
                        if (!numberNoPoint(String.valueOf(mInputText), 0)) {
                            hasPoint = zeroPoint();
                            if (hasPoint) {
                                zeroOK = false;
                            } else {
                                buttonString = "0.";
                            }
                        }
                    } else {
                        zeroOK = zeroBefore();
                    }
                }
                if (zeroOK) {
                    updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                }
            }
        }
        updateResultView();
    }

    private boolean zeroPoint() {//处理小数点问题
        boolean hasPoint = false;
        for (int i = mCursorPosition; i < mInputText.length(); i++) {
            if (numberPoint(String.valueOf(mInputText), i)) {
                break;
            }
            if (numberNoPoint(String.valueOf(mInputText), i) && mInputText.charAt(i) == '.') {
                hasPoint = true;
            }
        }
        return hasPoint;
    }

    private boolean zeroBefore() {//处理小数点问题
        boolean zeroOK = true;
        if (!numberPoint(String.valueOf(mInputText), mCursorPosition - 1)) {
            for (int i = mCursorPosition - 1; i >= 0; i--) {
                if (mInputText.charAt(i) != '0' && !numberPoint(String.valueOf(mInputText), i)) {
                    break;
                } else if (mInputText.charAt(i) != '0' && numberPoint(String.valueOf(mInputText), i)) {
                    zeroOK = false;
                    break;
                } else if (mInputText.charAt(i) == '0' && i == 0) {
                    zeroOK = false;
                }
            }
        }
        return zeroOK;
    }

    private void updateInputView(int cursorPosition, int insertPosition, String insertText) {//更新输入框显示
        mInputText.insert(insertPosition, insertText);
        mInputView.setText(mInputText);
        mInputView.setSelection(cursorPosition);

    }

    private void equalsMethod() {//等号功能
        if (mInputText.length() > 0) {//记录之前输入的文本内容
            mSavedText = new StringBuilder(mInputText.toString());
        }
//        updateResultView();
        boolean isResultOk = true;//检测结果是否正常
        String sResult = mResultView.getText().toString();
        for (int i = 0; i < sResult.length(); i++) {//若运算结果非数字 即无正常运算结果
            if (numberPoint(sResult, i) && sResult.charAt(i) != '-') {
                isResultOk = false;
            }
        }
        if (isResultOk) {
            mInputView.setText(mResultView.getText().toString());//把结果显示到输入框
            mInputView.setSelection(mInputView.getText().toString().length());
        }
        SharedPreferences.Editor editor = mHistoryPreferences.edit();
        Date date = new Date();
        String sTime = String.valueOf(date.getTime());
        if (mResultView.getText().length() > 0) {
            String sInputText = mInputText.toString() + "=" + mResultView.getText().toString();
            editor.putString(sTime, sInputText);
            editor.apply();
            mHistoryPreferences.edit().apply();
        }
    }

    private void updateResultView() {
        removeThousandSeparator();
        StringBuilder operator = new StringBuilder();//记录超过单字符的运算符号
        StringBuilder number = new StringBuilder();//记录数字
        ArrayList<String> inputList = new ArrayList<>();//新建集合记录内容以便计算
        for (int i = 0; i < mInputText.length(); i++) {//向集合中依次添加内容
            if (!numberPoint(String.valueOf(mInputText), i)) {//若读取到的内容为数字则把其添加到number
                number.append(mInputText.charAt(i));
            } else if (mInputText.charAt(i) == 'l') {//添加log
                if (number.length() != 0) {
                    inputList.add(String.valueOf(number));
                    number.setLength(0);
                }
                operator.append(getString(R.string.log));
                inputList.add(String.valueOf(operator));
                operator.setLength(0);
                inputList.add(getString(R.string.bracketleft));
                i = i + 3;
            } else if (mInputText.charAt(i) == 't') {//添加tan
                if (number.length() != 0) {
                    inputList.add(String.valueOf(number));
                    number.setLength(0);
                }
                operator.append(getString(R.string.tan));
                inputList.add(String.valueOf(operator));
                operator.setLength(0);
                inputList.add(getString(R.string.bracketleft));
                i = i + 3;
            } else if (mInputText.charAt(i) == 'c') {//添加cos
                if (number.length() != 0) {
                    inputList.add(String.valueOf(number));
                    number.setLength(0);
                }
                operator.append(getString(R.string.cos));
                inputList.add(String.valueOf(operator));
                operator.setLength(0);
                inputList.add(getString(R.string.bracketleft));
                i = i + 3;
            } else if (mInputText.charAt(i) == 's') {//添加sin
                if (number.length() != 0) {
                    inputList.add(String.valueOf(number));
                    number.setLength(0);
                }
                operator.append(getString(R.string.sin));
                inputList.add(String.valueOf(operator));
                operator.setLength(0);
                inputList.add(getString(R.string.bracketleft));
                i = i + 3;
            } else {
                if (number.length() != 0) {
                    inputList.add(String.valueOf(number));//添加number
                    number.setLength(0);
                }
                inputList.add(String.valueOf(mInputText.charAt(i)));//添加其他运算符
            }
        }
        if (number.length() > 0) {//检查数字是否添加完全
            inputList.add(String.valueOf(number));
            number.setLength(0);
        }
        insertMultiply(inputList);
        piToValue(inputList);
        removeZero(inputList);
        canCalculated(inputList);
        if (canCalculate) {//以括号 根号 log 三角函数 百分比 阶乘 除乘减加的顺序依次运算
            checkMinus(inputList);
            bracketMethod(inputList);
            rootMethod(inputList);
            logMethod(inputList);
            tanMethod(inputList);
            cosMethod(inputList);
            sinMethod(inputList);
            percentMethod(inputList);
            factorialMethod(inputList);
            powMethod(inputList);
            divideMethod(inputList);
            multiplyMethod(inputList);
            minusMethod(inputList);
            plusMethod(inputList);
        }
        canCalculated(inputList);
        if (canCalculate) {//若运算完成 则集合内容剩余唯一
            if (isDarkThemeOn) {
                mResultView.setTextColor(getColor(R.color.textcolor_calculator_night));
            } else {
                mResultView.setTextColor(getColor(R.color.textcolor_calculator));
            }
            if (inputList.size() == 1) {
                mResultView.setText(inputList.get(0));
            } else if (inputList.size() == 0) {
                mResultView.setText(null);
            }
        } else {
            //不能完成计算的提醒
            //运算错误提示
            mResultView.setText(getString(R.string.warning));
            mResultView.setTextColor(getColor(R.color.warning_color));
        }

    }

    private void removeZero(ArrayList<String> inputList) {
        for (int i = 0; i < inputList.size(); i++) {//处理使用删除键时产生的多余0
            if (inputList.get(i).length() > 1) {
                if (inputList.get(i).startsWith(getString(R.string.zero))
                        && inputList.get(i).charAt(1) == '0') {
                    String s = inputList.get(i);
                    while (s.length() > 1 && s.startsWith(getString(R.string.zero)) && s.charAt(1) == '0') {
                        s = s.substring(1);
                    }
                    if (s.startsWith(getString(R.string.point))) {
                        s = "0" + s;
                    }
                    inputList.set(i, s);
                }
            }
        }
    }

    private void piToValue(ArrayList<String> inputList) {
        for (int i = 0; i < inputList.size(); i++) {//把圆周率转化为可计算数值
            if (inputList.get(i).equals(getString(R.string.pi))) {
                inputList.set(i, String.valueOf(Math.PI));
            }
        }
    }

    private void removeThousandSeparator() {//去除千分割副符，暂时无用
        for (int i = 0; i < mInputText.length(); i++) {
            if (mInputText.charAt(i) == ',') {
                mInputText.deleteCharAt(i);
            }
        }
    }

    private void insertMultiply(ArrayList<String> list) {//插入必要乘号
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i).equals(getString(R.string.pi))
                    || list.get(i).equals(getString(R.string.bracketright))
                    || list.get(i).equals(getString(R.string.percent))
                    || list.get(i).equals(getString(R.string.factorial))) {
                if (list.get(i + 1).equals(getString(R.string.pi))
                        || list.get(i + 1).equals(getString(R.string.root))
                        || list.get(i + 1).startsWith("c") || list.get(i + 1).startsWith("s")
                        || list.get(i + 1).startsWith("t") || list.get(i + 1).startsWith("l")
                        || list.get(i + 1).equals(getString(R.string.bracketleft))
                        || !numberPoint(list.get(i + 1), 0)) {
                    list.add(i + 1, getString(R.string.multiply));
                }
            }
        }
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i).equals(getString(R.string.pi))
                    || list.get(i).equals(getString(R.string.root))
                    || list.get(i).startsWith("c") || list.get(i).startsWith("s")
                    || list.get(i).startsWith("t") || list.get(i).startsWith("l")
                    || list.get(i).equals(getString(R.string.bracketleft))) {
                if (list.get(i - 1).equals(getString(R.string.pi))
                        || list.get(i - 1).equals(getString(R.string.bracketright))
                        || list.get(i - 1).equals(getString(R.string.percent))
                        || list.get(i - 1).equals(getString(R.string.factorial))
                        || !numberPoint(list.get(i - 1), 0)) {
                    list.add(i, getString(R.string.multiply));
                }
            }
        }
    }

    private void canCalculated(ArrayList<String> inputList) {//检测是否可以运算
        canCalculate = mBracketStatus == 0;//检测括号是否完整
        if (inputList.size() > 0) {
            if (!notAfterOperator2(inputList.get(0), 0)) {
                canCalculate = false;//这些不能开头
            }
            if (!notBeforeOperator(inputList.get(inputList.size() - 1),
                    inputList.get(inputList.size() - 1).length() - 1)) {
                canCalculate = false;//这些不能结尾
            }
            if (inputList.contains(TOO_LARGE) || inputList.contains("-" + TOO_LARGE)) {
                canCalculate = false;//出现超出运算范围
            }

            for (int i = 0; i < inputList.size(); i++) {
                if (inputList.get(i).contains(".")) {
                    int countPoint = 0;
                    for (int j = 0; j < inputList.get(i).length(); j++) {
                        if (inputList.get(i).charAt(j) == '.') {
                            countPoint++;
                        }
                    }
                    if (countPoint > 1 || inputList.get(i).charAt(0) == '.'
                            || inputList.get(i).charAt(inputList.get(i).length() - 1) == '.') {
                        canCalculate = false;//数字不能以小数点开头或结尾或包含多个小数点
                    }
                }

            }
            for (int i = 0; i < inputList.size() - 1; i++) {
                if (!notBeforeOperator(inputList.get(i), inputList.get(i).length() - 1)
                        && !notAfterOperator1(inputList.get(i + 1), inputList.get(i + 1).length() - 1)) {
                    canCalculate = false;//不可相邻的符号
                }
                if (!notBeforeAny(inputList.get(i), inputList.get(i).length() - 1)
                        && !notBeforeOperator(inputList.get(i), inputList.get(i).length() - 1)
                        && inputList.get(i + 1).equals(getString(R.string.bracketright))) {
                    canCalculate = false;//不可相邻的符号
                }
                if (inputList.get(i).equals(getString(R.string.root))
                        && inputList.get(i + 1).equals(getString(R.string.minus))) {
                    canCalculate = false;//根号不能跟负数
                }
                if (inputList.get(i).equals(getString(R.string.divide))
                        && inputList.get(i + 1).equals(getString(R.string.zero))) {
                    canCalculate = false;//不可以除以0
                }

            }
            for (int i = 0; i < inputList.size() - 2; i++) {
                if (inputList.get(i).equals(getString(R.string.log))
                        && inputList.get(i + 1).equals(getString(R.string.bracketleft))
                        && !notAfterOperator1(inputList.get(i + 2), 0)) {
                    canCalculate = false;//log运算检测
                }
            }
            for (int i = inputList.size() - 1; i > 0; i--) {
                if (inputList.get(i).equals(getString(R.string.factorial))
                        && inputList.get(i - 1).contains(getString(R.string.point))) {
                    canCalculate = false;//小数不能阶乘
                }
                if (inputList.get(i).equals(getString(R.string.factorial))
                        && inputList.get(i - 1).startsWith(getString(R.string.minus))) {
                    canCalculate = false;//负数不能阶乘
                }
            }
        }

    }

    private void plusMethod(ArrayList<String> inputList) {//加法运算
        while (inputList.contains(getString(R.string.plus))) {
            canCalculated(inputList);
            if (!canCalculate) break;
            int index = inputList.indexOf(getString(R.string.plus));
            String s1 = inputList.get(index - 1);
            String s2 = inputList.get(index + 1);
            String result = MyMath.myPlus(s1, s2);
            result = removePoint(result);
            inputList.remove(index + 1);
            inputList.remove(index);
            inputList.set(index - 1, result);
        }
    }

    private void minusMethod(ArrayList<String> inputList) {//减法运算
        while (inputList.contains(getString(R.string.minus))) {
            canCalculated(inputList);
            if (!canCalculate) break;
            int index = inputList.indexOf(getString(R.string.minus));
            String s1 = inputList.get(index - 1);
            String s2 = inputList.get(index + 1);
            String result = MyMath.myMinus(s1, s2);
            result = removePoint(result);
            inputList.remove(index + 1);
            inputList.remove(index);
            inputList.set(index - 1, result);
        }
    }

    private void multiplyMethod(ArrayList<String> inputList) {//乘法运算
        while (inputList.contains(getString(R.string.multiply))) {
            canCalculated(inputList);
            if (!canCalculate) break;
            int index = inputList.indexOf(getString(R.string.multiply));
            String s1 = inputList.get(index - 1);
            String s2 = inputList.get(index + 1);
            String result = MyMath.myMultiply(s1, s2);
            result = removePoint(result);
            inputList.remove(index + 1);
            inputList.remove(index);
            inputList.set(index - 1, result);
        }
    }

    private void divideMethod(ArrayList<String> inputList) {//除法运算
        while (inputList.contains(getString(R.string.divide))) {
            canCalculated(inputList);
            if (!canCalculate) break;
            int index = inputList.indexOf(getString(R.string.divide));
            String s1 = inputList.get(index - 1);
            String s2 = inputList.get(index + 1);
            String result = MyMath.myDivide(s1, s2);
            result = removePoint(result);
            inputList.remove(index + 1);
            inputList.remove(index);
            inputList.set(index - 1, result);
        }
    }

    private void powMethod(ArrayList<String> inputList) {//次方运算
        while (inputList.contains(getString(R.string.pow))) {
            canCalculated(inputList);
            if (!canCalculate) break;
            int index = inputList.indexOf(getString(R.string.pow));
            String s1 = inputList.get(index - 1);
            String s2 = inputList.get(index + 1);
            String result = MyMath.myPow(s1, s2);
            result = removePoint(result);
            inputList.remove(index + 1);
            inputList.remove(index);
            inputList.set(index - 1, result);
        }
    }

    /*
     *阶乘百分比都是运算符在右，数值在左，先靠近数值的运算符先运算，所以是先运算文本内容靠左边的内容
     * 例如，当找到一个最左边的百分号，要看其左边有没有阶乘号，若有则先算。
     */
    private void factorialMethod(ArrayList<String> inputList) {//阶乘运算
        while (inputList.contains(getString(R.string.factorial))) {
            canCalculated(inputList);
            if (!canCalculate) break;
            int index = inputList.indexOf(getString(R.string.factorial));
            if (inputList.get(index - 1).equals(getString(R.string.percent))) {
                percentMethod(inputList);
            } else {
                String s = inputList.get(index - 1);
                String result = MyMath.myFactorial(s);
                result = removePoint(result);
                inputList.remove(index);
                inputList.set(index - 1, result);
            }
        }
    }

    private String removePoint(String result) {//去除不必要小数点
        int count = 0;
        if (result.contains(getString(R.string.point))) {
            for (int i = result.length() - 1; i >= 0; i--) {
                if (result.charAt(i) == '0') {
                    count++;
                } else {
                    break;
                }
            }
            result = result.substring(0, result.length() - count);
            if (result.charAt(result.length() - 1) == '.') {
                result = result.substring(0, result.length() - 1);
            }
        }
        return result;
    }

    private void percentMethod(ArrayList<String> inputList) {//百分比运算
        while (inputList.contains(getString(R.string.percent))) {
            canCalculated(inputList);
            if (!canCalculate) break;
            int index = inputList.indexOf(getString(R.string.percent));
            if (inputList.get(index - 1).equals(getString(R.string.factorial))) {
                factorialMethod(inputList);
            } else {
                String s = inputList.get(index - 1);
                String result = MyMath.myPercent(s);
                result = removePoint(result);
                inputList.remove(index);
                inputList.set(index - 1, result);
            }
        }
    }

    /*
     *三角函数 log 开根号全部都是运算符在左，数值在右，先靠近数值的运算符先运算，所以是先运算文本内容靠右边的内容
     * 例如，当找到一个最右边的log，要看其右边有没有root或三角函数，若有则应先运算之。
     */
    private void sinMethod(ArrayList<String> inputList) {//正弦运算
        while (inputList.contains(getString(R.string.sin))) {
            canCalculated(inputList);
            if (!canCalculate) break;
            int index = inputList.lastIndexOf(getString(R.string.sin));
            if (inputList.get(index + 1).equals(getString(R.string.log))) {
                logMethod(inputList);
            } else if (inputList.get(index + 1).equals(getString(R.string.tan))) {
                tanMethod(inputList);
            } else if (inputList.get(index + 1).equals(getString(R.string.cos))) {
                cosMethod(inputList);
            } else if (inputList.get(index + 1).equals(getString(R.string.root))) {
                rootMethod(inputList);
            } else {
                String s = inputList.get(index + 1);
                String result = MyMath.mySin(s);
                result = removePoint(result);
                inputList.remove(index + 1);
                inputList.set(index, result);
            }
        }
    }

    private void cosMethod(ArrayList<String> inputList) {//余弦运算
        while (inputList.contains(getString(R.string.cos))) {
            canCalculated(inputList);
            if (!canCalculate) break;
            int index = inputList.lastIndexOf(getString(R.string.cos));
            if (inputList.get(index + 1).equals(getString(R.string.log))) {
                logMethod(inputList);
            } else if (inputList.get(index + 1).equals(getString(R.string.tan))) {
                tanMethod(inputList);
            } else if (inputList.get(index + 1).equals(getString(R.string.root))) {
                rootMethod(inputList);
            } else if (inputList.get(index + 1).equals(getString(R.string.sin))) {
                sinMethod(inputList);
            } else {
                String s = inputList.get(index + 1);
                String result = MyMath.myCos(s);
                result = removePoint(result);
                inputList.remove(index + 1);
                inputList.set(index, result);
            }
        }
    }

    private void tanMethod(ArrayList<String> inputList) {//正切运算
        while (inputList.contains(getString(R.string.tan))) {
            canCalculated(inputList);
            if (!canCalculate) break;
            int index = inputList.lastIndexOf(getString(R.string.tan));
            if (inputList.get(index + 1).equals(getString(R.string.log))) {
                logMethod(inputList);
            } else if (inputList.get(index + 1).equals(getString(R.string.root))) {
                rootMethod(inputList);
            } else if (inputList.get(index + 1).equals(getString(R.string.cos))) {
                cosMethod(inputList);
            } else if (inputList.get(index + 1).equals(getString(R.string.sin))) {
                sinMethod(inputList);
            } else {
                String s = inputList.get(index + 1);
                String result = MyMath.myTan(s);
                result = removePoint(result);
                inputList.remove(index + 1);
                inputList.set(index, result);
            }
        }
    }

    private void logMethod(ArrayList<String> inputList) {//log运算
        while (inputList.contains(getString(R.string.log))) {
            canCalculated(inputList);
            if (!canCalculate) break;
            int index = inputList.lastIndexOf(getString(R.string.log));
            if (inputList.get(index + 1).equals(getString(R.string.root))) {
                rootMethod(inputList);
            } else if (inputList.get(index + 1).equals(getString(R.string.tan))) {
                tanMethod(inputList);
            } else if (inputList.get(index + 1).equals(getString(R.string.cos))) {
                cosMethod(inputList);
            } else if (inputList.get(index + 1).equals(getString(R.string.sin))) {
                sinMethod(inputList);
            } else {
                String s = inputList.get(index + 1);
                if (s.startsWith(getString(R.string.minus))) {
                    canCalculate = false;
                    break;
                }
                String result = MyMath.myLog(s);
                result = removePoint(result);
                inputList.remove(index + 1);
                inputList.set(index, result);
            }
        }
    }

    private void rootMethod(ArrayList<String> inputList) {//根号运算
        while (inputList.contains(getString(R.string.root))) {
            canCalculated(inputList);
            if (!canCalculate) break;
            int index = inputList.lastIndexOf(getString(R.string.root));
            if (inputList.get(index + 1).equals(getString(R.string.log))) {
                logMethod(inputList);
            } else if (inputList.get(index + 1).equals(getString(R.string.tan))) {
                tanMethod(inputList);
            } else if (inputList.get(index + 1).equals(getString(R.string.cos))) {
                cosMethod(inputList);
            } else if (inputList.get(index + 1).equals(getString(R.string.sin))) {
                sinMethod(inputList);
            } else {
                String s = inputList.get(index + 1);
                String result = MyMath.myRoot(s);
                result = removePoint(result);
                inputList.remove(index + 1);
                inputList.set(index, result);
            }
        }
    }

    private void bracketMethod(ArrayList<String> inputList) {//括号运算
        ArrayList<String> tempList = new ArrayList<>();//新建一个临时集合来记录括号内的内容
        while (inputList.contains(getString(R.string.bracketright))) {
            int indexEnd = inputList.indexOf(getString(R.string.bracketright));//第一个右括号位置
            int indexStart = inputList.subList(0, indexEnd).lastIndexOf(getString(R.string.bracketleft));
            //与之对应的左括号位置
            for (int i = indexStart + 1; i < indexEnd; i++) {
                tempList.add(inputList.get(i));//把括号内的内容添加到临时集合里
            }
            canCalculated(tempList);//要检测是否符合运算规则
            if (!canCalculate) break;
            while (tempList.size() > 1) {
                checkMinus(tempList);
                rootMethod(tempList);
                logMethod(tempList);
                tanMethod(tempList);
                cosMethod(tempList);
                sinMethod(tempList);
                percentMethod(tempList);
                factorialMethod(tempList);
                powMethod(tempList);
                divideMethod(tempList);
                multiplyMethod(tempList);
                minusMethod(tempList);
                plusMethod(tempList);
            }
            if (indexEnd >= indexStart + 1) {//把原来集合里括号内内容删除
                inputList.subList(indexStart + 1, indexEnd + 1).clear();
            }
            if (tempList.size()==0){
                canCalculate=false;
            } else {
                inputList.set(indexStart, tempList.get(0));//替换为运算结果
            }

            tempList.clear();//把临时集合清空以便下一轮运算
        }
    }

    private void checkMinus(ArrayList<String> list) {//检测负号
        if (list.size() > 1) {

            if (list.get(0).equals(getString(R.string.minus))) {//第一位是负号
                if (!numberNoPoint(list.get(1), 0)) {//第二位是数字
                    String s = list.get(0) + list.get(1);//把二者合一
                    list.remove(1);
                    list.set(0, s);
                } else if (list.get(1).equals(getString(R.string.pi))) {//第二位是圆周率
                    String s = list.get(0) + list.get(1);//把二者合一
                    list.remove(1);
                    list.set(0, s);
                }
            }
        }

    }

    private void deleteMethod() {//删除功能
        savedTextMethod();
        if (notEmptyNotStart()) {
            checkDelete(mCursorPosition == mInputText.length());
        }
        if (mInputView.getText().toString().length() > 0) {
            if (!numberNoPoint(mInputView.getText().toString(), mInputView.getText().toString().length() - 1)
                    || mInputView.getText().toString().charAt(mInputView.getText().toString().length() - 1) == ')'
                    || mInputView.getText().toString().charAt(mInputView.getText().toString().length() - 1) == '!'
                    || mInputView.getText().toString().charAt(mInputView.getText().toString().length() - 1) == '%') {
                updateResultView();
            }
        } else {
            updateResultView();
        }
    }

    private void checkDelete(boolean isEnd) {//检测删除规则
        if (mInputText.charAt(mCursorPosition - 1) == '(') {//若光标前为（
            if (mCursorPosition > 1) {//检测是否为log或三角函数，此时输入文本长度必大于一
                if (mInputText.charAt(mCursorPosition - 2) == 'n' || mInputText.charAt(mCursorPosition - 2) == 'g'
                        || mInputText.charAt(mCursorPosition - 2) == 's') {//log tan sin cos
                    isEndCheck(isEnd, 4);
                } else {
                    isEndCheck(isEnd, 1);
                }
            } else {
                isEndCheck(isEnd, 1);
            }
            mBracketStatus--;
        } else if (mInputText.charAt(mCursorPosition - 1) == ')') {
            isEndCheck(isEnd, 1);
            mBracketStatus++;
        } else {
            isEndCheck(isEnd, 1);
        }
    }

    private void isEndCheck(boolean isEnd, int i) {//组合删除功能以便调用
        if (isEnd) {
            deleteEndFunction(i);
        } else {
            deleteFunction(i);
        }
    }

    private void deleteFunction(int num) {//不在结尾删除
        String substring1 = mInputText.substring(0, mCursorPosition - num);
        String substring2 = mInputText.substring(mCursorPosition, mInputText.length());
        mInputText = new StringBuilder(substring1 + substring2);
        mInputView.setText(mInputText);
        mInputView.setSelection(mCursorPosition - num);
    }


    private void deleteEndFunction(int num) {//在结尾删除
        String substring = mInputText.substring(0, mCursorPosition - num);
        mInputText = new StringBuilder(substring);
        mInputView.setText(mInputText);
        mInputView.setSelection(mInputText.length());
    }

    private void savedTextMethod() {//检测等号运算
        if (mSavedText != null && mSavedText.length() > 0) {
            mInputText = new StringBuilder(mSavedText);
            mInputView.setText(mInputText);
            mCursorPosition = mSavedText.length();
            mInputView.setSelection(mSavedText.length());
            mSavedText.setLength(0);
        }
    }

    private void clearMethod() {//清零功能
        if (mSavedText != null) {
            mSavedText.setLength(0);
        }
        mBracketStatus = 0;
        mCursorPosition = 0;
        mInputText.setLength(0);
        mInputView.setText(mInputText);
        mInputView.setSelection(mCursorPosition);
        mResultView.setText(mInputText);
    }

    private void setButtonBackground() {//根据点击时间长短设置不同点击背景效果
        if (mPressDuration <= 100) {
            setBackgroundDrawable(0);//当点击时间小于100 设置短背景
        } else {
            setBackgroundDrawable(1);//当点击时间大于100 设置长背景
        }
    }

    private void setBackgroundDrawable(int i) {//设置按键背景资源
        if (i == 0) {
            mClearButton.setBackgroundResource(R.drawable.background_clear_rp_short);
            mDeleteButton.setBackgroundResource(R.drawable.background_number_rp_short);
            mEqualsButton.setBackgroundResource(R.drawable.background_equals_rp_short);
            for (Button button : mOperatorButton) {
                button.setBackgroundResource(R.drawable.background_operator_rp_short);
            }
            for (Button button : mNumberButton) {
                button.setBackgroundResource(R.drawable.background_number_rp_short);
            }
        } else {
            mClearButton.setBackgroundResource(R.drawable.background_clear_rp);
            mDeleteButton.setBackgroundResource(R.drawable.background_number_rp);
            mEqualsButton.setBackgroundResource(R.drawable.background_equals_rp);
            for (Button button : mOperatorButton) {
                button.setBackgroundResource(R.drawable.background_operator_rp);
            }
            for (Button button : mNumberButton) {
                button.setBackgroundResource(R.drawable.background_number_rp);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //重置按键背景
        setBackgroundDrawable(1);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPressTime = System.currentTimeMillis();//开始点击的时间
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);//点击反馈震动
                break;
            case MotionEvent.ACTION_UP:
                mPressDuration = System.currentTimeMillis() - mPressTime;//点击时长
                break;
        }
        return false;
    }
}