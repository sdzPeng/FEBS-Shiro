package cc.mrbird.febs.business.util;

import java.math.BigDecimal;

/**
 * @author yinxp@dist.com.cn
 * @date 2019/4/28
 */
public abstract class NumberUtil {


    private static final String[] cnNumbers = new String[]{ "零", "一", "二", "三", "四", "五", "六", "七", "八", "九" };
    private static final String[] series = new String[]{ "十", "百", "千", "万", "十", "百", "千", "亿", "十", "百", "千" };


    /**
     * 阿拉伯数字转中文汉字
     * @param number
     * @return
     */
    public static String numberToChinaStr(Integer number) {
        if (number == null || number.longValue() < 0) {
            return null;
        }
        String str = String.valueOf(number);
        String result = "";
        int n = str.length();
        for (int i = 0; i < n; i++) {
            int num = str.charAt(i) - '0';
            if (i != n - 1 && num != 0) {
                result += cnNumbers[num] + series[n - 2 - i];
            } else {
                result += cnNumbers[num];
            }
        }
        return result;
    }

    /**
     * 尾部去零
     * @param str
     * @return
     */
    public static String delZero(String str) {
        for (String r = str; ; ) {
            if (r.endsWith("0")) {
                r = r.substring(0, r.length() - 1);
            } else {
                return r;
            }
        }
    }


    /**
     * 保留两位小数
     * @param num
     * @return
     */
    public static Double formatKeep2(Double num) {
        return formatKeepN(num, 2);
    }


    /**
     * 保留n位小数
     * @param num
     * @return
     */
    public static Double formatKeepN(Double num,int n) {
        BigDecimal bd = new BigDecimal(num);
        return bd.setScale(n,BigDecimal.ROUND_HALF_UP).doubleValue();
    }


    public static void main(String[] args) {
        String numberToChinaStr = numberToChinaStr(10);
        if (numberToChinaStr.length() == 3) {
            if (numberToChinaStr.startsWith("一")) {
                numberToChinaStr = numberToChinaStr.replaceFirst("一", "");
            }
            if (numberToChinaStr.contains("零")) {
                numberToChinaStr = numberToChinaStr.replaceAll("零", "");
            }
            System.out.println(numberToChinaStr);
        }
    }
}
