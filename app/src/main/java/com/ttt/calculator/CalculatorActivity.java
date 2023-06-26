package com.ttt.calculator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableRow;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class CalculatorActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {
    private EditText mInputView, mResultView;//输入框，结果框
    private Guideline mGuideline1;//确定位置
    private Button mClearButton, mEqualsButton, mDeleteButton, mMoreButton;//清零，等于，删除以及更多按键
    private Button[] mSmallButton, mOperatorButton, mNumberButton;//小按键集，运算按键集，以及数字按键集
    private long mPressDuration;//按压时常
    private long mPressTime;//开始按压的时间
    private boolean mShowFlag = false;//是否显示更多按键
    private TableRow mOperatorRow2;//默认隐藏的小按键所在位置
    private StringBuilder mInputText;//记录输入内容
    private int mBracketStatus = 0;//记录括号状态
    private int mCursorPosition;//记录指针位置
    private final String NOT_BEFORE_BRACKET_RIGHT = ".+-(tancosilg√^÷×";//不可存在于右括号之前
    private final String NOT_AFTER_OPERATOR_1 = ".+-^÷×!%";//根号等后不可存在
    private final String NOT_AFTER_OPERATOR_2 = ".+^÷×!%";//log等后不可存在
    private final String NOT_BEFORE_OPERATOR = ".+-^÷×(√";//次方等前不可存在
    private final String NUMBER_NO_POINT = "0123456789";//所有数字不包含小数点
    private final String NUMBER_POINT = ".0123456789";//所有数字包含小数点
    public static final String TOO_LARGE = "Infinity";//过大提醒
    private boolean canCalculate;//检测是否可以运算

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);
        mInputView = findViewById(R.id.input_edit);
        mResultView = findViewById(R.id.result_edit);
        mGuideline1 = findViewById(R.id.guideline_3);
        mClearButton = findViewById(R.id.button_clear);
        mEqualsButton = findViewById(R.id.button_equals);
        mDeleteButton = findViewById(R.id.button_delete);
        mOperatorRow2 = findViewById(R.id.operator_row_2);
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

                @Override
                public void onClick(View v) {
                    if (!mShowFlag) {
                        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.41f, 0.49f);
                        valueAnimator.setDuration(200);
                        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                        valueAnimator.addUpdateListener(animation -> {
                            lp.guidePercent = (float) valueAnimator.getAnimatedValue();
                            mGuideline1.setLayoutParams(lp);
                        });
                        valueAnimator.start();
                        mMoreButton.setText(R.string.more_up);
                        mOperatorRow2.setVisibility(View.VISIBLE);
                        mShowFlag = true;
                    } else {
                        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.49f, 0.41f);
                        valueAnimator.setDuration(200);
                        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                        valueAnimator.addUpdateListener(animation -> {
                            lp.guidePercent = (float) valueAnimator.getAnimatedValue();
                            mGuideline1.setLayoutParams(lp);
                        });
                        valueAnimator.start();
                        mMoreButton.setText(R.string.more_down);
                        mOperatorRow2.setVisibility(View.GONE);
                        mShowFlag = false;
                    }
                }
            });
        }
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
        overStatusBar();
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

    private void setMyTestSize() {//设置字体大小
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        mInputView.setTextSize((float) (height / 30));
        mResultView.setTextSize((float) (height / 55));
        mClearButton.setTextSize((float) (height / 70));
        mEqualsButton.setTextSize((float) (height / 70));
        mDeleteButton.setTextSize((float) (height / 70));
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mMoreButton.setTextSize((float) (height / 180));
        }

        for (Button button : mOperatorButton) {
            button.setTextSize((float) (height / 70));
        }
        for (Button button : mNumberButton) {
            button.setTextSize((float) (height / 70));
        }
        for (Button button : mSmallButton) {
            button.setTextSize((float) (height / 100));
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

    private void overStatusBar() {//沉浸状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    @Override
    public void onClick(View v) {
        setButtonBackground();
        Button button = (Button) v;
        String buttonString = button.getText().toString();//获取按键的文字
        mCursorPosition = mInputView.getSelectionStart();//获取当前指针位置
        mInputText = new StringBuilder(mInputView.getText().toString());//获取当前输入框文本内容
        boolean inputOk = true;//输入规则检测
        if (mInputText.length() != 0 && mCursorPosition != 0 && mCursorPosition != mInputText.length()) {
            //不能相邻前者
            String NOT_BEFORE_ANY = "anotcsilg";
            //不能相邻后者
            String NOT_AFTER_ANY = "anoig";
            if (NOT_BEFORE_ANY.indexOf(mInputText.charAt(mCursorPosition - 1)) != -1
                    || NOT_AFTER_ANY.indexOf(mInputText.charAt(mCursorPosition)) != -1) {
                inputOk = false;
            }
        }
        if (inputOk) {
            switch (buttonString) {
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
                    if (mInputText.length() == 0) {
                        updateInputView(mCursorPosition + 1, mCursorPosition,
                                getResources().getString(R.string.bracketleft));
                        mBracketStatus++;
                    } else {
                        //不可存在于左括号之前
                        String NOT_BEFORE_BRACKET_LEFT = ".anotcsilg";
                        //不可存在于左括号之后
                        String NOT_AFTER_BRACKET_LEFT = ".+^÷×!%anoig";
                        if (mCursorPosition == mInputText.length()) {
                            if (mBracketStatus > 0) {
                                if (NOT_BEFORE_BRACKET_RIGHT.indexOf(mInputText.charAt(mCursorPosition - 1)) == -1) {
                                    updateInputView(mCursorPosition + 1, mCursorPosition,
                                            getResources().getString(R.string.bracketright));
                                    mBracketStatus--;
                                    updateResultView();
                                } else if (NOT_BEFORE_BRACKET_LEFT.indexOf(mInputText.charAt(mCursorPosition - 1)) == -1) {
                                    updateInputView(mCursorPosition + 1, mCursorPosition,
                                            getResources().getString(R.string.bracketleft));
                                    mBracketStatus++;
                                }
                            } else {
                                if (NOT_BEFORE_BRACKET_LEFT.indexOf(mInputText.charAt(mCursorPosition - 1)) == -1) {
                                    updateInputView(mCursorPosition + 1, mCursorPosition,
                                            getResources().getString(R.string.bracketleft));
                                    mBracketStatus++;
                                }
                            }

                        } else if (mCursorPosition == 0) {
                            if (NOT_AFTER_BRACKET_LEFT.indexOf(mInputText.charAt(0)) == -1) {
                                updateInputView(mCursorPosition + 1, mCursorPosition,
                                        getResources().getString(R.string.bracketleft));
                                mBracketStatus++;
                            }
                        } else {
                            int countBracket = 0;
                            for (int i = 0; i < mCursorPosition + 1; i++) {
                                if (mInputText.charAt(i) == '(') countBracket++;
                                if (mInputText.charAt(i) == ')') countBracket--;
                            }
                            if (countBracket > 0) {
                                //不可存在于右括号之后
                                String NOT_AFTER_BRACKET_RIGHT = ".anoig";
                                if (NOT_BEFORE_BRACKET_RIGHT.indexOf(mInputText.charAt(mCursorPosition - 1)) == -1
                                        && NOT_AFTER_BRACKET_RIGHT.indexOf(mInputText.charAt(mCursorPosition)) == -1) {
                                    updateInputView(mCursorPosition + 1, mCursorPosition,
                                            getResources().getString(R.string.bracketright));
                                    mBracketStatus--;
                                } else if (NOT_BEFORE_BRACKET_LEFT.indexOf(mInputText.charAt(mCursorPosition - 1)) == -1
                                        && NOT_AFTER_BRACKET_LEFT.indexOf(mInputText.charAt(mCursorPosition)) == -1) {
                                    updateInputView(mCursorPosition + 1, mCursorPosition,
                                            getResources().getString(R.string.bracketleft));
                                    mBracketStatus++;
                                }
                            } else {
                                if (NOT_BEFORE_BRACKET_LEFT.indexOf(mInputText.charAt(mCursorPosition - 1)) == -1
                                        && NOT_AFTER_BRACKET_LEFT.indexOf(mInputText.charAt(mCursorPosition)) == -1) {
                                    updateInputView(mCursorPosition + 1, mCursorPosition,
                                            getResources().getString(R.string.bracketleft));
                                    mBracketStatus++;
                                }
                            }
                        }
                    }
                    break;
                case "√":
                    if (mInputText.length() == 0) {
                        updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                    } else {
                        if (mCursorPosition == mInputText.length()) {
                            if (mInputText.charAt(mCursorPosition - 1) != '.') {
                                updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                            }
                        } else if (mCursorPosition == 0) {
                            if (NOT_AFTER_OPERATOR_1.indexOf(mInputText.charAt(0)) == -1) {
                                updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                            }
                        } else {
                            if (mInputText.charAt(mCursorPosition - 1) != '.'
                                    && NOT_AFTER_OPERATOR_1.indexOf(mInputText.charAt(mCursorPosition)) == -1) {
                                updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                            }
                        }
                    }
                    break;
                case "log":
                    if (mInputText.length() == 0) {
                        updateInputView(mCursorPosition + 4, mCursorPosition,
                                buttonString + getResources().getString(R.string.bracketleft));
                        mBracketStatus++;
                    } else {
                        if (mCursorPosition == mInputText.length()) {
                            if (mInputText.charAt(mCursorPosition - 1) != '.') {
                                updateInputView(mCursorPosition + 4, mCursorPosition,
                                        buttonString + getResources().getString(R.string.bracketleft));
                                mBracketStatus++;
                            }
                        } else if (mCursorPosition == 0) {
                            if (NOT_AFTER_OPERATOR_1.indexOf(mInputText.charAt(0)) == -1) {
                                updateInputView(mCursorPosition + 4, mCursorPosition,
                                        buttonString + getResources().getString(R.string.bracketleft));
                                mBracketStatus++;
                            }
                        } else {
                            if (mInputText.charAt(mCursorPosition - 1) != '.'
                                    && NOT_AFTER_OPERATOR_1.indexOf(mInputText.charAt(mCursorPosition)) == -1) {
                                updateInputView(mCursorPosition + 4, mCursorPosition,
                                        buttonString + getResources().getString(R.string.bracketleft));
                                mBracketStatus++;
                            }
                        }
                    }
                    break;
                case "tan":
                case "cos":
                case "sin":
                    if (mInputText.length() == 0) {
                        updateInputView(mCursorPosition + 4, mCursorPosition,
                                buttonString + getResources().getString(R.string.bracketleft));
                        mBracketStatus++;
                    } else {
                        if (mCursorPosition == mInputText.length()) {
                            if (mInputText.charAt(mCursorPosition - 1) != '.') {
                                updateInputView(mCursorPosition + 4, mCursorPosition,
                                        buttonString + getResources().getString(R.string.bracketleft));
                                mBracketStatus++;
                            }
                        } else if (mCursorPosition == 0) {
                            if (NOT_AFTER_OPERATOR_2.indexOf(mInputText.charAt(0)) == -1) {
                                updateInputView(mCursorPosition + 4, mCursorPosition,
                                        buttonString + getResources().getString(R.string.bracketleft));
                                mBracketStatus++;
                            }
                        } else {
                            if (mInputText.charAt(mCursorPosition - 1) != '.'
                                    && NOT_AFTER_OPERATOR_2.indexOf(mInputText.charAt(mCursorPosition)) == -1) {
                                updateInputView(mCursorPosition + 4, mCursorPosition,
                                        buttonString + getResources().getString(R.string.bracketleft));
                                mBracketStatus++;
                            }
                        }
                    }
                    break;
                case "%":
                case "!":
                    if (mInputText.length() != 0 && mCursorPosition != 0) {
                        if (mCursorPosition == mInputText.length()) {
                            if (NOT_BEFORE_OPERATOR.indexOf(mInputText.charAt(mCursorPosition - 1)) == -1) {
                                updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                            }
                        } else {
                            if (NOT_BEFORE_OPERATOR.indexOf(mInputText.charAt(mCursorPosition - 1)) == -1
                                    && mInputText.charAt(mCursorPosition) != '.') {
                                updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                            }
                        }
                    }
                    updateResultView();
                    break;
                case "^":
                    if (mInputText.length() != 0 && mCursorPosition != 0) {
                        if (mCursorPosition == mInputText.length()) {
                            if (NOT_BEFORE_OPERATOR.indexOf(mInputText.charAt(mCursorPosition - 1)) == -1) {
                                updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                            }
                        } else {
                            if (NOT_BEFORE_OPERATOR.indexOf(mInputText.charAt(mCursorPosition - 1)) == -1
                                    && NOT_AFTER_OPERATOR_2.indexOf(mInputText.charAt(mCursorPosition)) == -1) {
                                updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                            }
                        }
                    }
                    break;
                case "-":
                    if (mInputText.length() == 0) {
                        updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                    } else {
                        if (mCursorPosition == mInputText.length()) {
                            if (NOT_BEFORE_OPERATOR.indexOf(mInputText.charAt(mCursorPosition - 1)) == -1) {
                                updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                            }
                        } else if (mCursorPosition == 0) {
                            if (NOT_AFTER_OPERATOR_1.indexOf(mInputText.charAt(0)) == -1) {
                                updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                            }
                        } else {
                            if (NOT_BEFORE_OPERATOR.indexOf(mInputText.charAt(mCursorPosition - 1)) == -1
                                    && NOT_AFTER_OPERATOR_1.indexOf(mInputText.charAt(mCursorPosition)) == -1) {
                                updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                            }
                        }
                    }
                    break;
                case "+":
                case "×":
                case "÷":
                    if (mInputText.length() != 0 && mCursorPosition != 0) {
                        if (mCursorPosition == mInputText.length()) {
                            if (NOT_BEFORE_OPERATOR.indexOf(mInputText.charAt(mCursorPosition - 1)) == -1) {
                                updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                            }
                        } else {
                            if (NOT_BEFORE_OPERATOR.indexOf(mInputText.charAt(mCursorPosition - 1)) == -1
                                    && NOT_AFTER_OPERATOR_1.indexOf(mInputText.charAt(mCursorPosition)) == -1) {
                                updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                            }
                        }
                    }
                    break;
                case "π":
                    if (mInputText.length() == 0) {
                        updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                    } else {
                        if (mCursorPosition == mInputText.length()) {
                            if (mInputText.charAt(mCursorPosition - 1) != '.') {
                                updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                            }
                        } else if (mCursorPosition == 0) {
                            if (mInputText.charAt(0) != '.' && mInputText.charAt(0) != '!') {
                                updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                            }
                        } else {
                            if (mInputText.charAt(mCursorPosition - 1) != '.' && mInputText.charAt(mCursorPosition) != '.'
                                    && mInputText.charAt(mCursorPosition) != '!') {
                                updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                            }
                        }
                    }
                    updateResultView();
                    break;
                case ".":
                    if (mInputText.length() != 0 && mCursorPosition != 0) {
                        int indexFirst = 0;
                        int indexSecond = mInputText.length() - 1;
                        if (mCursorPosition == mInputText.length()) {
                            for (int i = mCursorPosition - 1; i >= 0; i--) {
                                if (NUMBER_NO_POINT.indexOf(mInputText.charAt(i)) == -1) {
                                    indexFirst = i;
                                    break;
                                }
                            }
                            if (indexFirst == 0) {
                                if (mInputText.charAt(indexFirst) != '.') {
                                    updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                                }
                            } else {
                                if (mInputText.charAt(indexFirst) != '.' && indexFirst != mCursorPosition - 1) {
                                    updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                                }
                            }

                        } else {
                            for (int i = mCursorPosition - 1; i >= 0; i--) {
                                if (NUMBER_NO_POINT.indexOf(mInputText.charAt(i)) == -1) {
                                    indexFirst = i;
                                    break;
                                }
                            }
                            for (int i = mCursorPosition; i < mInputText.length(); i++) {
                                if (NUMBER_NO_POINT.indexOf(mInputText.charAt(i)) == -1) {
                                    indexSecond = i;
                                    break;
                                }
                            }
                            if (indexFirst == 0 && indexSecond == mInputText.length() - 1) {
                                if (mInputText.charAt(indexFirst) != '.' && mInputText.charAt(indexSecond) != '.') {
                                    updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                                }
                            } else if (indexFirst == 0 && indexSecond != mInputText.length() - 1) {
                                if (mInputText.charAt(indexFirst) != '.' && mInputText.charAt(indexSecond) != '.'
                                        && indexSecond != mCursorPosition) {
                                    updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                                }
                            } else if (indexFirst != 0 && indexSecond == mInputText.length() - 1
                                    && indexFirst != mCursorPosition - 1) {
                                if (mInputText.charAt(indexFirst) != '.' && mInputText.charAt(indexSecond) != '.') {
                                    updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                                }
                            } else {
                                if (mInputText.charAt(indexFirst) != '.' && mInputText.charAt(indexSecond) != '.'
                                        && indexFirst != mCursorPosition - 1 && indexSecond != mCursorPosition) {
                                    updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                                }
                            }

                        }
                    }
                    break;
                default:
                    if (mInputText.length() == 0) {
                        updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                    } else {
                        if (mInputText.length() == 1) {
                            if (mInputText.charAt(0) == '0') {
                                mInputText = new StringBuilder(buttonString);
                                mInputView.setText(mInputText);
                                mInputView.setSelection(mCursorPosition + 1);
                            } else {
                                updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                            }
                        } else {
                            updateInputView(mCursorPosition + 1, mCursorPosition, buttonString);
                        }
                    }
                    updateResultView();
            }
        }

    }

    private void updateInputView(int cursorPosition, int insertPosition, String insertText) {//更新输入框显示
        mInputText.insert(insertPosition, insertText);
        mInputView.setText(mInputText);
        mInputView.setSelection(cursorPosition);

    }

    private void equalsMethod() {
        updateResultView();
    }

    private void updateResultView() {
        removeThousandSeparator();
        StringBuilder operator = new StringBuilder();
        StringBuilder number = new StringBuilder();
        ArrayList<String> inputList = new ArrayList<>();
        for (int i = 0; i < mInputText.length(); i++) {
            if (NUMBER_POINT.indexOf(mInputText.charAt(i)) != -1) {
                number.append(mInputText.charAt(i));
            } else if (mInputText.charAt(i) == 'l') {
                if (number.length() != 0) {
                    inputList.add(String.valueOf(number));
                    number.setLength(0);
                }
                operator.append(getResources().getString(R.string.log));
                inputList.add(String.valueOf(operator));
                operator.setLength(0);
                inputList.add(getResources().getString(R.string.bracketleft));
                i = i + 3;
            } else if (mInputText.charAt(i) == 't') {
                if (number.length() != 0) {
                    inputList.add(String.valueOf(number));
                    number.setLength(0);
                }
                operator.append(getResources().getString(R.string.tan));
                inputList.add(String.valueOf(operator));
                operator.setLength(0);
                inputList.add(getResources().getString(R.string.bracketleft));
                i = i + 3;
            } else if (mInputText.charAt(i) == 'c') {
                if (number.length() != 0) {
                    inputList.add(String.valueOf(number));
                    number.setLength(0);
                }
                operator.append(getResources().getString(R.string.cos));
                inputList.add(String.valueOf(operator));
                operator.setLength(0);
                inputList.add(getResources().getString(R.string.bracketleft));
                i = i + 3;
            } else if (mInputText.charAt(i) == 's') {
                if (number.length() != 0) {
                    inputList.add(String.valueOf(number));
                    number.setLength(0);
                }
                operator.append(getResources().getString(R.string.sin));
                inputList.add(String.valueOf(operator));
                operator.setLength(0);
                inputList.add(getResources().getString(R.string.bracketleft));
                i = i + 3;
            } else {
                if (number.length() != 0) {
                    inputList.add(String.valueOf(number));
                    number.setLength(0);
                }
                inputList.add(String.valueOf(mInputText.charAt(i)));
            }
        }
        if (number.length() > 0) {
            inputList.add(String.valueOf(number));
            number.setLength(0);
        }
        insertMultiply(inputList);
        for (int i = 0; i < inputList.size(); i++) {
            if (inputList.get(i).equals(getResources().getString(R.string.pi))) {
                inputList.set(i, String.valueOf(Math.PI));
            }
        }
        for (int i = 0; i < inputList.size(); i++) {
            if (inputList.get(i).length() > 1) {
                if (inputList.get(i).startsWith(getResources().getString(R.string.zero))
                        && inputList.get(i).charAt(1) == '0') {
                    String s = inputList.get(i);
                    while (s.startsWith(getResources().getString(R.string.zero))) {
                        s = s.substring(1);
                    }
                    if (s.startsWith(getResources().getString(R.string.point))) {
                        s = "0" + s;
                    }
                    inputList.set(i, s);
                }
            }

        }
        canCalculated(inputList);

        if (canCalculate) {
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
        if (canCalculate) {
            if (inputList.size() == 1) {
                mResultView.setText(inputList.get(0));
            } else if (inputList.size() == 0) {
                mResultView.setText(null);
            }
        } else {
            //不能完成计算的提醒
            String WARNING = "Math Error !!!";
            mResultView.setText(WARNING);
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
            if (list.get(i).equals(getResources().getString(R.string.pi))
                    || list.get(i).equals(getResources().getString(R.string.bracketright))
                    || list.get(i).equals(getResources().getString(R.string.percent))
                    || list.get(i).equals(getResources().getString(R.string.factorial))) {
                if (list.get(i + 1).equals(getResources().getString(R.string.pi))
                        || list.get(i + 1).equals(getResources().getString(R.string.root))
                        || list.get(i + 1).startsWith("c") || list.get(i + 1).startsWith("s")
                        || list.get(i + 1).startsWith("t") || list.get(i + 1).startsWith("l")
                        || list.get(i + 1).equals(getResources().getString(R.string.bracketleft))
                        || NUMBER_POINT.indexOf(list.get(i + 1).charAt(0)) != -1) {
                    list.add(i + 1, getResources().getString(R.string.multiply));
                }
            }
        }
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i).equals(getResources().getString(R.string.pi))
                    || list.get(i).equals(getResources().getString(R.string.root))
                    || list.get(i).startsWith("c") || list.get(i).startsWith("s")
                    || list.get(i).startsWith("t") || list.get(i).startsWith("l")
                    || list.get(i).equals(getResources().getString(R.string.bracketleft))) {
                if (list.get(i - 1).equals(getResources().getString(R.string.pi))
                        || list.get(i - 1).equals(getResources().getString(R.string.bracketright))
                        || list.get(i - 1).equals(getResources().getString(R.string.percent))
                        || list.get(i - 1).equals(getResources().getString(R.string.factorial))
                        || NUMBER_POINT.indexOf(list.get(i - 1).charAt(0)) != -1) {
                    list.add(i, getResources().getString(R.string.multiply));
                }
            }
        }
    }

    private void canCalculated(ArrayList<String> inputList) {//检测是否可以运算
        canCalculate = mBracketStatus == 0;
        if (inputList.size() > 0) {
            if (NOT_AFTER_OPERATOR_2.contains(inputList.get(0))) {
                canCalculate = false;
            }
            if (NOT_BEFORE_OPERATOR.contains(inputList.get(inputList.size() - 1))) {
                canCalculate = false;
            }
            if (inputList.contains(TOO_LARGE) || inputList.contains("-" + TOO_LARGE)) {
                canCalculate = false;
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
                        canCalculate = false;
                    }
                }

            }
            for (int i = 0; i < inputList.size() - 1; i++) {
                if (NOT_BEFORE_OPERATOR.contains(inputList.get(i)) && NOT_AFTER_OPERATOR_1.contains(inputList.get(i + 1))) {
                    canCalculate = false;
                }
                if (NOT_BEFORE_BRACKET_RIGHT.contains(inputList.get(i))
                        && inputList.get(i + 1).equals(getResources().getString(R.string.bracketright))) {
                    canCalculate = false;
                }
                if (inputList.get(i).equals(getResources().getString(R.string.root))
                        && inputList.get(i + 1).equals(getResources().getString(R.string.minus))) {
                    canCalculate = false;
                }
                if (inputList.get(i).equals(getResources().getString(R.string.divide))
                        && inputList.get(i + 1).equals(getResources().getString(R.string.zero))) {
                    canCalculate = false;
                }

            }
            for (int i = 0; i < inputList.size() - 2; i++) {
                if (inputList.get(i).equals(getResources().getString(R.string.log))
                        && inputList.get(i + 1).equals(getResources().getString(R.string.bracketleft))
                        && NOT_AFTER_OPERATOR_1.contains(inputList.get(i + 2))) {
                    canCalculate = false;
                }
            }
            for (int i = inputList.size() - 1; i > 0; i--) {
                if (inputList.get(i).equals(getResources().getString(R.string.factorial))
                        && inputList.get(i - 1).contains(getResources().getString(R.string.point))) {
                    canCalculate = false;
                }
                if (inputList.get(i).equals(getResources().getString(R.string.factorial))
                        && inputList.get(i - 1).startsWith(getResources().getString(R.string.minus))) {
                    canCalculate = false;
                }
            }
        }

    }

    private void plusMethod(ArrayList<String> inputList) {//加法运算
        while (inputList.contains(getResources().getString(R.string.plus))) {
            canCalculated(inputList);
            if (!canCalculate) break;
            int index = inputList.indexOf(getResources().getString(R.string.plus));
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
        while (inputList.contains(getResources().getString(R.string.minus))) {
            canCalculated(inputList);
            if (!canCalculate) break;
            int index = inputList.indexOf(getResources().getString(R.string.minus));
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
        while (inputList.contains(getResources().getString(R.string.multiply))) {
            canCalculated(inputList);
            if (!canCalculate) break;
            int index = inputList.indexOf(getResources().getString(R.string.multiply));
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
        while (inputList.contains(getResources().getString(R.string.divide))) {
            canCalculated(inputList);
            if (!canCalculate) break;
            int index = inputList.indexOf(getResources().getString(R.string.divide));
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
        while (inputList.contains(getResources().getString(R.string.pow))) {
            canCalculated(inputList);
            if (!canCalculate) break;
            int index = inputList.indexOf(getResources().getString(R.string.pow));
            String s1 = inputList.get(index - 1);
            String s2 = inputList.get(index + 1);
            String result = MyMath.myPow(s1, s2);
            result = removePoint(result);
            inputList.remove(index + 1);
            inputList.remove(index);
            inputList.set(index - 1, result);
        }
    }

    private void factorialMethod(ArrayList<String> inputList) {//阶乘运算
        while (inputList.contains(getResources().getString(R.string.factorial))) {
            canCalculated(inputList);
            if (!canCalculate) break;
            int index = inputList.indexOf(getResources().getString(R.string.factorial));
            if (inputList.get(index - 1).equals(getResources().getString(R.string.percent))) {
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
        if (result.contains(getResources().getString(R.string.point))) {
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
        while (inputList.contains(getResources().getString(R.string.percent))) {
            canCalculated(inputList);
            if (!canCalculate) break;
            int index = inputList.indexOf(getResources().getString(R.string.percent));
            if (inputList.get(index - 1).equals(getResources().getString(R.string.factorial))) {
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

    private void sinMethod(ArrayList<String> inputList) {//正弦运算
        while (inputList.contains(getResources().getString(R.string.sin))) {
            canCalculated(inputList);
            if (!canCalculate) break;
            int index = inputList.lastIndexOf(getResources().getString(R.string.sin));
            if (inputList.get(index + 1).equals(getResources().getString(R.string.log))) {
                logMethod(inputList);
            } else if (inputList.get(index + 1).equals(getResources().getString(R.string.tan))) {
                tanMethod(inputList);
            } else if (inputList.get(index + 1).equals(getResources().getString(R.string.cos))) {
                cosMethod(inputList);
            } else if (inputList.get(index + 1).equals(getResources().getString(R.string.root))) {
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
        while (inputList.contains(getResources().getString(R.string.cos))) {
            canCalculated(inputList);
            if (!canCalculate) break;
            int index = inputList.lastIndexOf(getResources().getString(R.string.cos));
            if (inputList.get(index + 1).equals(getResources().getString(R.string.log))) {
                logMethod(inputList);
            } else if (inputList.get(index + 1).equals(getResources().getString(R.string.tan))) {
                tanMethod(inputList);
            } else if (inputList.get(index + 1).equals(getResources().getString(R.string.root))) {
                rootMethod(inputList);
            } else if (inputList.get(index + 1).equals(getResources().getString(R.string.sin))) {
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
        while (inputList.contains(getResources().getString(R.string.tan))) {
            canCalculated(inputList);
            if (!canCalculate) break;
            int index = inputList.lastIndexOf(getResources().getString(R.string.tan));
            if (inputList.get(index + 1).equals(getResources().getString(R.string.log))) {
                logMethod(inputList);
            } else if (inputList.get(index + 1).equals(getResources().getString(R.string.root))) {
                rootMethod(inputList);
            } else if (inputList.get(index + 1).equals(getResources().getString(R.string.cos))) {
                cosMethod(inputList);
            } else if (inputList.get(index + 1).equals(getResources().getString(R.string.sin))) {
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
        while (inputList.contains(getResources().getString(R.string.log))) {
            canCalculated(inputList);
            if (!canCalculate) break;
            int index = inputList.lastIndexOf(getResources().getString(R.string.log));
            if (inputList.get(index + 1).equals(getResources().getString(R.string.root))) {
                rootMethod(inputList);
            } else if (inputList.get(index + 1).equals(getResources().getString(R.string.tan))) {
                tanMethod(inputList);
            } else if (inputList.get(index + 1).equals(getResources().getString(R.string.cos))) {
                cosMethod(inputList);
            } else if (inputList.get(index + 1).equals(getResources().getString(R.string.sin))) {
                sinMethod(inputList);
            } else {
                String s = inputList.get(index + 1);
                if (s.startsWith(getResources().getString(R.string.minus))) {
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
        while (inputList.contains(getResources().getString(R.string.root))) {
            canCalculated(inputList);
            if (!canCalculate) break;
            int index = inputList.lastIndexOf(getResources().getString(R.string.root));
            if (inputList.get(index + 1).equals(getResources().getString(R.string.log))) {
                logMethod(inputList);
            } else if (inputList.get(index + 1).equals(getResources().getString(R.string.tan))) {
                tanMethod(inputList);
            } else if (inputList.get(index + 1).equals(getResources().getString(R.string.cos))) {
                cosMethod(inputList);
            } else if (inputList.get(index + 1).equals(getResources().getString(R.string.sin))) {
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
        ArrayList<String> tempList = new ArrayList<>();
        while (inputList.contains(getResources().getString(R.string.bracketright))) {
            int indexEnd = inputList.indexOf(getResources().getString(R.string.bracketright));//第一个右括号位置
            int indexStart = inputList.subList(0, indexEnd).lastIndexOf(getResources().getString(R.string.bracketleft));
            //与之对应的左括号位置
            for (int i = indexStart + 1; i < indexEnd; i++) {
                tempList.add(inputList.get(i));
            }
            canCalculated(tempList);
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
            if (indexEnd >= indexStart + 1) {
                inputList.subList(indexStart + 1, indexEnd + 1).clear();
            }
            inputList.set(indexStart, tempList.get(0));
            tempList.clear();
        }
    }

    private void checkMinus(ArrayList<String> list) {//检测负号
        if (list.size() > 1) {

            if (list.get(0).equals(getResources().getString(R.string.minus))) {
                if (NUMBER_NO_POINT.indexOf(list.get(1).charAt(0)) != -1) {
                    String s = list.get(0) + list.get(1);
                    list.remove(1);
                    list.set(0, s);
                } else if (list.get(1).equals(getResources().getString(R.string.pi))) {
                    String s = list.get(0) + list.get(1);
                    list.remove(1);
                    list.set(0, s);
                }
            }
        }

    }

    private void deleteMethod() {//删除功能
        if (mInputText.length() != 0 && mCursorPosition != 0) {
            if (mCursorPosition == mInputText.length()) {
                if (mInputText.charAt(mCursorPosition - 1) == '(') {
                    if (mInputText.charAt(mCursorPosition - 2) == 'n' || mInputText.charAt(mCursorPosition - 2) == 'g'
                            || mInputText.charAt(mCursorPosition - 2) == 's') {
                        String substring = mInputText.substring(0, mCursorPosition - 4);
                        mInputText = new StringBuilder(substring);
                        mInputView.setText(mInputText);
                        mInputView.setSelection(mInputText.length());
                        mBracketStatus--;
                    } else {
                        String substring = mInputText.substring(0, mCursorPosition - 1);
                        mInputText = new StringBuilder(substring);
                        mInputView.setText(mInputText);
                        mInputView.setSelection(mInputText.length());
                        mBracketStatus--;
                    }
                } else if (mInputText.charAt(mCursorPosition - 1) == ')') {
                    String substring = mInputText.substring(0, mCursorPosition - 1);
                    mInputText = new StringBuilder(substring);
                    mInputView.setText(mInputText);
                    mInputView.setSelection(mInputText.length());
                    mBracketStatus++;
                } else {
                    String substring = mInputText.substring(0, mCursorPosition - 1);
                    mInputText = new StringBuilder(substring);
                    mInputView.setText(mInputText);
                    mInputView.setSelection(mInputText.length());
                }
            } else {
                if (mInputText.charAt(mCursorPosition - 1) == '(') {
                    if (mInputText.charAt(mCursorPosition - 2) == 'n' || mInputText.charAt(mCursorPosition - 2) == 'g'
                            || mInputText.charAt(mCursorPosition - 2) == 's') {
                        String substring1 = mInputText.substring(0, mCursorPosition - 4);
                        String substring2 = mInputText.substring(mCursorPosition, mInputText.length());
                        mInputText = new StringBuilder(substring1 + substring2);
                        mInputView.setText(mInputText);
                        mInputView.setSelection(mCursorPosition - 4);
                        mBracketStatus--;
                    } else if (mInputText.charAt(mCursorPosition - 1) == ')') {
                        String substring1 = mInputText.substring(0, mCursorPosition - 1);
                        String substring2 = mInputText.substring(mCursorPosition, mInputText.length());
                        mInputText = new StringBuilder(substring1 + substring2);
                        mInputView.setText(mInputText);
                        mInputView.setSelection(mCursorPosition - 1);
                        mBracketStatus++;
                    } else {
                        String substring1 = mInputText.substring(0, mCursorPosition - 1);
                        String substring2 = mInputText.substring(mCursorPosition, mInputText.length());
                        mInputText = new StringBuilder(substring1 + substring2);
                        mInputView.setText(mInputText);
                        mInputView.setSelection(mCursorPosition - 1);
                        mBracketStatus--;
                    }
                } else {
                    String substring1 = mInputText.substring(0, mCursorPosition - 1);
                    String substring2 = mInputText.substring(mCursorPosition, mInputText.length());
                    mInputText = new StringBuilder(substring1 + substring2);
                    mInputView.setText(mInputText);
                    mInputView.setSelection(mCursorPosition - 1);
                }
            }
        }
        if (mInputView.getText().toString().length()>0){
            if (NUMBER_NO_POINT.indexOf(mInputView.getText().toString().charAt(mInputView.getText().toString().length()-1))!=-1
                    || mInputView.getText().toString().charAt(mInputView.getText().toString().length()-1) == ')'
                    || mInputView.getText().toString().charAt(mInputView.getText().toString().length()-1) == '!'
                    || mInputView.getText().toString().charAt(mInputView.getText().toString().length()-1) == '%' ) {
                updateResultView();
            }
        } else {
            updateResultView();
        }
    }

    private void clearMethod() {//清零功能
        mBracketStatus = 0;
        mCursorPosition = 0;
        mInputText.setLength(0);
        mInputView.setText(mInputText);
        mInputView.setSelection(mCursorPosition);
        mResultView.setText(mInputText);
    }

    private void setButtonBackground() {//根据点击时间长短设置不同点击背景效果
        if (mPressDuration <= 100) {
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
        mClearButton.setBackgroundResource(R.drawable.background_clear_rp);
        mDeleteButton.setBackgroundResource(R.drawable.background_number_rp);
        mEqualsButton.setBackgroundResource(R.drawable.background_equals_rp);
        for (Button button : mOperatorButton) {
            button.setBackgroundResource(R.drawable.background_operator_rp);
        }
        for (Button button : mNumberButton) {
            button.setBackgroundResource(R.drawable.background_number_rp);
        }
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