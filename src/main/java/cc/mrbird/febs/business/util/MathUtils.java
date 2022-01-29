package cc.mrbird.febs.business.util;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.text.DecimalFormat;

/**
 * @company: test
 * @department: 数据中心
 * @date: 2021-06-04 12:58 上午
 * @author: test
 * @email: test@163.com
 * @desc：
 */
public final class MathUtils {

    public static final DecimalFormat FORMAT  = new DecimalFormat("##0.000");

    public static RealVector toRealVector(Double value, Double angle) {
        double radians = Math.toRadians(angle);
        double cosValue = value * Math.cos(radians);
        double sinValue = value * Math.sin(radians);
        return new ArrayRealVector(new Double[]{cosValue, sinValue});
    }

    public static Double toAngle(RealVector realVector) {
        double[] values = realVector.toArray();
        double degree = Math.toDegrees(Math.atan(values[1] / values[0]));
        if (values[0]<0) {
            // 第二、三象限
            return degree+180;
        }else {
            // 第四象限
            if (values[1]<0) {
                return degree<0?degree+360:degree;
            }
            // 第一象限
            else {
                return degree;
            }
        }
    }

    public static void main(String[] args) {
        RealVector add = toRealVector(402.695, 296.335)
                .subtract(toRealVector(89.245, 94.378))
                .subtract(toRealVector(381.431, 295.615))
                .add(toRealVector(6097.024, 111.239));
        double[] test = toRealVector(402.695, 296.335).toArray();
        System.out.println(test);
        float r = (float)Math.sqrt(Math.pow(test[0], 2) + Math.pow(test[1], 2));
        float v = (float)Math.atan(test[1] / test[0]);
        double v1 = Math.toDegrees(v);
        System.out.println(add.getNorm()/2);
//        RealVector add = toRealVector(402.695, 296.335)
//                .subtract(toRealVector(381.431, 295.615))
//                .add(toRealVector(6097.024, 111.239));
//        System.out.println(add.getNorm());
    }

    public static String base2scientific(Double mileage) {
        StringBuilder sb = new StringBuilder();
        sb.append("K")
                .append((int)Math.floor(mileage))
                .append("+")
                .append(FORMAT.format((mileage-Math.floor(mileage))*1000));
        return sb.toString();
    }
}
