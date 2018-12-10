package com.wecare.app.util;

import java.util.regex.Pattern;

/**
 * Created by Administrator Chengzj
 *
 * @date 2018/10/31 13:49
 */
public class StringUtils {
    //方法一：用JAVA自带的函数
    public static boolean isNumeric(String str){
        for (int i = str.length();--i>=0;){
            if (!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }

    /*方法二：推荐，速度最快
     * 判断是否为整数
     * @param str 传入的字符串
     * @return 是整数返回true,否则返回false
     */

    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }
}
