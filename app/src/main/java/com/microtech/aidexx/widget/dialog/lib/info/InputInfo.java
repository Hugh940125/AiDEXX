package com.microtech.aidexx.widget.dialog.lib.info;

import android.view.textservice.TextInfo;

import androidx.annotation.ColorInt;

public class InputInfo {
    
    private int MAX_LENGTH = -1;    //最大长度,-1不生效
    private int inputType;          //类型详见 android.text.InputType
    private TextInfo textInfo;      //默认字体样式
    private boolean multipleLines;  //支持多行
    private boolean selectAllText;  //默认选中所有文字（便于修改）
    
    private Integer cursorColor;    //输入框光标颜色
    private Integer bottomLineColor;//输入框横线颜色
    
    public int getMAX_LENGTH() {
        return MAX_LENGTH;
    }
    
    public InputInfo setMAX_LENGTH(int MAX_LENGTH) {
        this.MAX_LENGTH = MAX_LENGTH;
        return this;
    }
    
    public int getInputType() {
        return inputType;
    }
    
    public InputInfo setInputType(int inputType) {
        this.inputType = inputType;
        return this;
    }
    
    public TextInfo getTextInfo() {
        return textInfo;
    }
    
    public InputInfo setTextInfo(TextInfo textInfo) {
        this.textInfo = textInfo;
        return this;
    }
    
    public boolean isMultipleLines() {
        return multipleLines;
    }
    
    public InputInfo setMultipleLines(boolean multipleLines) {
        this.multipleLines = multipleLines;
        return this;
    }
    
    public boolean isSelectAllText() {
        return selectAllText;
    }
    
    public InputInfo setSelectAllText(boolean selectAllText) {
        this.selectAllText = selectAllText;
        return this;
    }
    
    public Integer getCursorColor() {
        return cursorColor;
    }
    
    public InputInfo setCursorColor(@ColorInt int cursorColor) {
        this.cursorColor = cursorColor;
        return this;
    }
    
    public InputInfo setThemeColor(@ColorInt int themeColor) {
        this.cursorColor = themeColor;
        this.bottomLineColor = themeColor;
        return this;
    }
    
    public Integer getBottomLineColor() {
        return bottomLineColor;
    }
    
    public InputInfo setBottomLineColor(int bottomLineColor) {
        this.bottomLineColor = bottomLineColor;
        return this;
    }
}
