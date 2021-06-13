package cc.mrbird.febs.business.constants;

import lombok.Getter;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-30 7:10 上午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
public class FixedValueConstants {

    public static final String TF短路故障判别 = "TF短路故障判别";

    public static final String 吸上电流流互变比 = "吸上电流流互变比";

    @Getter
    public enum DIMENSION {
        起点公里标("起点公里标"),
        区间1长度("区间1长度"),
        区间2长度("区间2长度"),
        区间4长度("区间4长度"),
        变电所的QT值("变电所的QT值"),
        变电所供电线长度("变电所供电线长度"),
        AT1的QT1值("AT1的QT1值"),
        AT1的QT2值("AT1的QT2值"),
        AT2的QT2值("AT2的QT2值"),
        AT2的QT1值("AT2的QT1值"),
        AT3的QT1值("AT3的QT1值"),
        AT1供电线长度("AT1供电线长度"),
        AT2供电线长度("AT2供电线长度"),
        AT1的QF1值("AT1的QF1值"),
        AT1的QF2值("AT1的QF2值"),
        AT2的QF1值("AT2的QF1值"),
        AT2的QF2值("AT2的QF2值"),

        ;
        private String name;
        DIMENSION(){}
        DIMENSION(String name) {
            this.name = name;
        }
    }
}
