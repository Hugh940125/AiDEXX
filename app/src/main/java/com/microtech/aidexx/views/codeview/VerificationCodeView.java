package com.microtech.aidexx.views.codeview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.microtech.aidexx.R;
import com.microtech.aidexx.utils.DensityUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * description: 自定义view 验证码 输入框
 * Created by Jack on 2017/6/2.
 * 邮箱：839539179@qq.com
 */

public class VerificationCodeView extends RelativeLayout {

    private GridLayout containerEt;

    private PwdEditText et;
    // 输入框数量
    private int mEtNumber;
    // 输入框的宽度
    private int mEtWidth;
    //输入框分割线
    private Drawable mEtDividerDrawable;
    //输入框文字颜色
    private int mEtTextColor;
    //输入框文字大小
    private float mEtTextSize;
    //输入框获取焦点时背景
    private Drawable mEtBackgroundDrawableFocus;
    //输入框没有焦点时背景
    private Drawable mEtBackgroundDrawableNormal;
    //是否是密码模式
    private boolean mEtPwd;
    //密码模式时圆的半径
    private float mEtPwdRadius;

    //存储TextView的数据 数量由自定义控件的属性传入
    private PwdTextView[] mPwdTextViews;

    private final MyTextWatcher myTextWatcher = new MyTextWatcher();


    public VerificationCodeView(Context context) {
        this(context, null);
    }

    public VerificationCodeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerificationCodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    //初始化 布局和属性
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.layout_identifying_code, this);
        containerEt = this.findViewById(R.id.container_et);
        et = this.findViewById(R.id.et_code);
        et.setInputType(EditorInfo.TYPE_CLASS_TEXT);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VerificationCodeView, defStyleAttr, 0);
        mEtNumber = typedArray.getInteger(R.styleable.VerificationCodeView_icv_et_number, 6);
        mEtWidth = typedArray.getDimensionPixelSize(R.styleable.VerificationCodeView_icv_et_width, DensityUtils.dp2px(35));
        mEtDividerDrawable = typedArray.getDrawable(R.styleable.VerificationCodeView_icv_et_divider_drawable);
        mEtTextSize = typedArray.getDimensionPixelSize(R.styleable.VerificationCodeView_icv_et_text_size, (int) sp2px(16, context));
        mEtTextColor = typedArray.getColor(R.styleable.VerificationCodeView_icv_et_text_color, Color.BLACK);
        mEtBackgroundDrawableFocus = typedArray.getDrawable(R.styleable.VerificationCodeView_icv_et_bg_focus);
        mEtBackgroundDrawableNormal = typedArray.getDrawable(R.styleable.VerificationCodeView_icv_et_bg_normal);
        mEtPwd = typedArray.getBoolean(R.styleable.VerificationCodeView_icv_et_pwd, false);
        mEtPwdRadius = typedArray.getDimensionPixelSize(R.styleable.VerificationCodeView_icv_et_pwd_radius, 0);
        //释放资源
        typedArray.recycle();


        // 当xml中未配置时 这里进行初始配置默认图片
        if (mEtDividerDrawable == null) {
            mEtDividerDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.shape_divider_identifying, null);
        }

        if (mEtBackgroundDrawableFocus == null) {
            mEtBackgroundDrawableFocus = ResourcesCompat.getDrawable(context.getResources(), R.drawable.shape_icv_et_bg_focus, null);
        }

        if (mEtBackgroundDrawableNormal == null) {
            mEtBackgroundDrawableNormal = ResourcesCompat.getDrawable(context.getResources(), R.drawable.shape_icv_et_bg_normal, null);
        }
        initUI();
    }

    // 初始UI
    private void initUI() {
        initTextViews(getContext(), mEtNumber, DensityUtils.dp2px(40), mEtDividerDrawable, DensityUtils.sp2px(16), mEtTextColor);
        initEtContainer(mPwdTextViews);
        setListener();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 设置当 高为 warpContent 模式时的默认值 为 50dp
        int mHeightMeasureSpec = heightMeasureSpec;

        int heightMode = MeasureSpec.getMode(mHeightMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST) {
            if (mEtNumber > 6) {
                mHeightMeasureSpec = MeasureSpec.makeMeasureSpec((int) dp2px(100, getContext()), MeasureSpec.EXACTLY);
            } else {
                mHeightMeasureSpec = MeasureSpec.makeMeasureSpec((int) dp2px(50, getContext()), MeasureSpec.EXACTLY);
            }
        }

        super.onMeasure(widthMeasureSpec, mHeightMeasureSpec);
    }


    //初始化TextView
    private void initTextViews(Context context, int etNumber, int etWidth, Drawable etDividerDrawable, float etTextSize, int etTextColor) {
        // 设置 editText 的输入长度
        et.setCursorVisible(false);//将光标隐藏

        //限制只能输入中文，英文，数字
        InputFilter typeFilter = (source, start, end, dest, dstart, dend) -> {
            Pattern p = Pattern.compile("[0-9a-zA-Z]+");
            Matcher m = p.matcher(source.toString());
            if (!m.matches()) return "";
            return null;
        };
        et.setFilters(new InputFilter[]{typeFilter, new InputFilter.LengthFilter(etNumber)}); //最大输入长度
        // 设置分割线的宽度
//        if (etDividerDrawable != null) {
//            etDividerDrawable.setBounds(0, 0, etDividerDrawable.getMinimumWidth(), etDividerDrawable.getMinimumHeight());
//            containerEt.setDividerDrawable(etDividerDrawable);
//        }
        mPwdTextViews = new PwdTextView[etNumber];
        for (int i = 0; i < mPwdTextViews.length; i++) {
            PwdTextView textView = new PwdTextView(context);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, etTextSize);
            textView.setTextColor(etTextColor);
            textView.setWidth(etWidth);
            textView.setHeight(etWidth);
            textView.setFocusable(true);
            textView.setFocusableInTouchMode(true);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(DensityUtils.dp2px(3), DensityUtils.dp2px(3), DensityUtils.dp2px(3), DensityUtils.dp2px(3));
            textView.setLayoutParams(layoutParams);
            if (i == 0) {
                textView.setBackgroundDrawable(mEtBackgroundDrawableFocus);
            } else {
                textView.setBackgroundDrawable(mEtBackgroundDrawableNormal);
            }
            textView.setGravity(Gravity.CENTER);

            textView.setFocusable(false);

            mPwdTextViews[i] = textView;
        }
    }

    //初始化存储TextView 的容器
    private void initEtContainer(TextView[] mTextViews) {
        if (mEtNumber > 6) {
            containerEt.setColumnCount(5);
        } else {
            containerEt.setColumnCount(6);
        }
        for (TextView mTextView : mTextViews) {
            containerEt.addView(mTextView);
        }
    }


    private void setListener() {
        // 监听输入内容
        et.addTextChangedListener(myTextWatcher);

        // 监听删除按键
        et.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    onKeyDelete();
                    return true;
                }
                return false;
            }
        });
    }


    // 给TextView 设置文字
    private void setText(String inputContent) {
        for (int i = 0; i < mPwdTextViews.length; i++) {
            PwdTextView tv = mPwdTextViews[i];
            if (tv.getText().toString().trim().equals("")) {
                if (mEtPwd) {
                    tv.drawPwd(mEtPwdRadius);
                }
                tv.setText(inputContent.toUpperCase());
                tv.setBackgroundDrawable(mEtBackgroundDrawableNormal);
                if (i < mEtNumber - 1) {
                    mPwdTextViews[i + 1].setBackgroundDrawable(mEtBackgroundDrawableFocus);
                } else {
                    InputMethodManager manager = ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE));
                    if (manager != null)
                        manager.hideSoftInputFromWindow(getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    // 添加输入完成的监听
                    if (inputCompleteListener != null) {
                        inputCompleteListener.inputComplete();
                    }
                }
                break;
            }
        }
    }

    // 监听删除
    private void onKeyDelete() {
        for (int i = mPwdTextViews.length - 1; i >= 0; i--) {
            PwdTextView tv = mPwdTextViews[i];
            if (!tv.getText().toString().trim().equals("")) {
                if (mEtPwd) {
                    tv.clearPwd();
                }
                tv.setText("");
                // 添加删除完成监听
                tv.setBackgroundDrawable(mEtBackgroundDrawableFocus);
                if (i < mEtNumber - 1) {
                    mPwdTextViews[i + 1].setBackgroundDrawable(mEtBackgroundDrawableNormal);
                }
                // 添加输入完成的监听
                if (inputCompleteListener != null) {
                    inputCompleteListener.deleteContent();
                }
                break;
            }
        }
    }


    /**
     * 获取输入文本
     *
     * @return string
     */
    public String getInputContent() {
        StringBuilder buffer = new StringBuilder();
        for (TextView tv : mPwdTextViews) {
            buffer.append(tv.getText().toString().trim());
        }
        return buffer.toString();
    }

    /**
     * 删除输入内容
     */
    public void clearInputContent() {
        for (int i = 0; i < mPwdTextViews.length; i++) {
            if (i == 0) {
                mPwdTextViews[i].setBackgroundDrawable(mEtBackgroundDrawableFocus);
            } else {
                mPwdTextViews[i].setBackgroundDrawable(mEtBackgroundDrawableNormal);
            }
            if (mEtPwd) {
                mPwdTextViews[i].clearPwd();
            }
            mPwdTextViews[i].setText("");
        }
    }

    /**
     * 设置输入框个数
     *
     * @param etNumber
     */
    public void setEtNumber(int etNumber) {
        this.mEtNumber = etNumber;
        et.removeTextChangedListener(myTextWatcher);
        containerEt.removeAllViews();
        initUI();
    }


    /**
     * 获取输入的位数
     *
     * @return int
     */
    public int getEtNumber() {
        return mEtNumber;
    }


    /**
     * 设置是否是密码模式 默认不是
     *
     * @param isPwdMode
     */
    public void setPwdMode(boolean isPwdMode) {
        this.mEtPwd = isPwdMode;
    }


    /**
     * 获取输入的EditText 用于外界设置键盘弹出
     *
     * @return EditText
     */
    public EditText getEditText() {
        return et;
    }

    // 输入完成 和 删除成功 的监听
    private InputCompleteListener inputCompleteListener;

    public void setInputCompleteListener(InputCompleteListener inputCompleteListener) {
        this.inputCompleteListener = inputCompleteListener;
    }


    public interface InputCompleteListener {
        void inputComplete();

        void deleteContent();
    }


    public float dp2px(float dpValue, Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpValue, context.getResources().getDisplayMetrics());
    }

    public float sp2px(float spValue, Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spValue, context.getResources().getDisplayMetrics());
    }


    private class MyTextWatcher implements TextWatcher {


        String stringFilter(String str) {
            String regEx = "[^a-zA-Z0-9]";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(str);
            return m.replaceAll("").trim();
        }


        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {


        }

        @Override
        public void afterTextChanged(Editable editable) {

            String inputStr = editable.toString();
            if (!TextUtils.isEmpty(inputStr)) {

                String[] strArray = inputStr.split("");

                for (int i = 0; i < strArray.length; i++) {

                    // 不能大于输入框个数
                    if (i > mEtNumber) {
                        break;
                    }
                    setText(strArray[i]);
                    et.setText("");
                }
            }
        }
    }


}
