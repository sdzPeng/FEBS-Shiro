package cc.mrbird.febs.business.util;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-06-04 12:58 上午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
public final class MathUtils {

    public static RealVector toRealVector(Double value, Double angle) {
        double radians = Math.toRadians(angle);
        double cosValue = value * Math.cos(radians);
        double sinValue = value * Math.sin(radians);
        return new ArrayRealVector(new Double[]{cosValue, sinValue});
    }

    public static void main(String[] args) {
        RealVector add = toRealVector(402.695, 296.335)
                .subtract(toRealVector(89.245, 94.378))
                .subtract(toRealVector(381.431, 295.615))
                .add(toRealVector(6097.024, 111.239));
        System.out.println(add.getNorm()/2);
//        RealVector add = toRealVector(402.695, 296.335)
//                .subtract(toRealVector(381.431, 295.615))
//                .add(toRealVector(6097.024, 111.239));
//        System.out.println(add.getNorm());
    }
}
