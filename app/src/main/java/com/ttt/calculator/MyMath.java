package com.ttt.calculator;

import java.math.BigDecimal;

public class MyMath {
    private static final BigDecimal bigCompare = BigDecimal.valueOf(100000000L);//用于判断显示方式

    public static String myPlus(String s1, String s2) {//加法
        return twoOp(s1, s2, "plus");
    }

    public static String myMinus(String s1, String s2) {//减法
        return twoOp(s1, s2, "minus");
    }

    public static String myMultiply(String s1, String s2) {//乘法
        return twoOp(s1, s2, "multiply");
    }

    public static String myDivide(String s1, String s2) {//除法
        return twoOp(s1, s2, "divide");
    }

    public static String myPow(String s1, String s2) {//次方
        return twoOp(s1, s2, "pow");
    }

    public static String myPercent(String s) {//百分比
        double db = Double.parseDouble(s);
        double result = db / 100;
        return String.valueOf(result);
    }

    public static String myRoot(String s) {//开根号
        double db = Double.parseDouble(s);
        double result = Math.sqrt(db);
        return String.valueOf(result);
    }

    public static String myLog(String s) {//log运算
        double db = Double.parseDouble(s);
        double result = Math.log10(db);
        return String.valueOf(result);
    }

    public static String myTan(String s) {
        double result = Double.parseDouble(mySin(s)) / Double.parseDouble(myCos(s));
        String resuls = String.valueOf(result);
        if (resuls.length() >= 16) {
            resuls = resuls.substring(0, 15);
            result = Double.parseDouble(resuls);
            resuls = String.valueOf(result);
        }
        return resuls;

    }

    public static String myCos(String s) {
        double db = Double.parseDouble(s);
        double result = Math.sqrt(1 - Math.pow(Math.sin(Math.toRadians(db)), 2));
        String resuls = String.valueOf(result);
        if (resuls.length() >= 16) {
            return String.valueOf(result).substring(0, 15);
        } else {
            return String.valueOf(result);
        }

    }

    public static String mySin(String s) {
        double db = Double.parseDouble(s);
        double result;
        if (db % 180 == 0) {
            result = 0;
        } else {
            result = Math.sin(Math.toRadians(db));
        }
        String resuls = String.valueOf(result);
        if (resuls.length() >= 16) {
            return String.valueOf(result).substring(0, 15);
        } else {
            return String.valueOf(result);
        }

    }

    public static String myFactorial(String s) {//阶乘
        double db = Double.parseDouble(s);
        double result;
        if (db > 171) {
            return CalculatorActivity.TOO_LARGE;
        } else {
            if (db == 0 || db == 1) {
                result = 1;
            } else {
                double dbTemp = Double.parseDouble(myFactorial(String.valueOf(db - 1)));
                result = db * dbTemp;
            }
            return String.valueOf(result);
        }

    }

    private static String twoOp(String s1, String s2, String op) {//有两个数字参与的运算，加减乘除次方
        BigDecimal bg1 = BigDecimal.valueOf(Double.parseDouble(s1));
        BigDecimal bg2 = BigDecimal.valueOf(Double.parseDouble(s2));
        double db1 = Double.parseDouble(s1);
        double db2 = Double.parseDouble(s2);
        BigDecimal resultBg;
        switch (op) {
            case "plus":
                resultBg = bg1.add(bg2);
                return getResult(resultBg);
            case "minus":
                resultBg = bg1.subtract(bg2);
                return getResult(resultBg);
            case "multiply":
                resultBg = bg1.multiply(bg2);
                return getResult(resultBg);
            case "divide":
                return String.valueOf(db1 / db2);
            case "pow":
                return String.valueOf(Math.pow(db1, db2));
            default:
                return "";
        }
    }

    private static String getResult(BigDecimal resultBg) {//判断显示方式
        if (resultBg.compareTo(bigCompare) > 0) {
            return resultBg.stripTrailingZeros().toString();
        } else {
            return resultBg.toString();
        }
    }


}
