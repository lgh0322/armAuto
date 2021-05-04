package com.vaca.modifiId;


import android.text.InputFilter;
import android.text.Spanned;

/**
 * 编写： 黄双
 * 时间： 2017/6/121519.
 * 邮箱： 15378412400@163.com
 */

public class InputFilterMinMax implements InputFilter{
    private int min, max;

    public InputFilterMinMax(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public InputFilterMinMax(String min, String max) {
        this.min = Integer.parseInt(min);
        this.max = Integer.parseInt(max);
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            String s=dest.toString() + source.toString();
            if(s.length()>3){
                return "";
            }
            int input = Integer.parseInt(s);
            if (isInRange(min, max, input))
                return null;

        } catch (Exception nfe) { }
        return "";
    }

    private boolean isInRange(int a, int b, int c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
}
