package cc.mrbird.febs.business.constants;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @company: test
 * @department: 数据中心
 * @date: 2021-05-30 7:23 上午
 * @author: test
 * @email: test@163.com
 * @desc：
 */
public class DeviceFailureConstants {

    public enum DEVICE_RESOURCE {
        变电所测距数据("变电所测距数据"),
        子站1测距数据("子站1测距数据"),
        子站2测距数据("子站2测距数据"),;
        private String resource;
        DEVICE_RESOURCE(String resource) {
            this.resource = resource;
        }
    }

    @Getter
    public enum DIMENSION {
        变电所吸上电流("变电所测距数据", "总吸上电流", "", ""),
        变电所吸上电流角度("变电所吸上电流角度", "总吸上电流角度", "", ""),
        AT所吸上电流("子站1测距数据", "总吸上电流", "", ""),
        AT所吸上电流角度("子站1测距数据", "总吸上电流角度", "", ""),
        分区所吸上电流("子站2测距数据", "总吸上电流", "", ""),
        分区所吸上电流角度("子站2测距数据", "总吸上电流角度", "", ""),
        变电所上行T电流("变电所测距数据", "上行T线电流", "TR故障", "上行"),
        变电所上行T电流角度("变电所测距数据", "上行T线电流角度", "", ""),
        变电所上行F电流("变电所测距数据", "上行F线电流", "FR故障", "上行"),
        变电所上行F电流角度("变电所测距数据", "上行F线电流角度", "", ""),
        变电所下行T电流("变电所测距数据", "下行T线电流", "TR故障", "下行"),
        变电所下行T电流角度("变电所测距数据", "下行T线电流角度", "", ""),
        变电所下行F电流("变电所测距数据", "下行F线电流", "FR故障", "下行"),
        变电所下行F电流角度("变电所测距数据", "下行F线电流角度", "", ""),
        AT所上行T电流("子站1测距数据", "上行T线电流", "TR故障", "上行"),
        AT所上行T电流角度("子站1测距数据", "上行T线电流角度", "", ""),
        AT所上行F电流("子站1测距数据", "上行F线电流", "FR故障", "上行"),
        AT所上行F电流角度("子站1测距数据", "上行F线电流角度", "", ""),
        AT所下行T电流("子站1测距数据", "下行T线电流", "TR故障", "下行"),
        AT所下行T电流角度("子站1测距数据", "下行T线电流角度", "", ""),
        AT所下行F电流("子站1测距数据", "下行F线电流", "TR故障", "下行"),
        AT所下行F电流角度("子站1测距数据", "下行F线电流角度", "", ""),
        分区所上行T电流("子站2测距数据", "上行T线电流", "TR故障", "上行"),
        分区所上行T电流角度("子站2测距数据", "上行T线电流角度", "", ""),
        分区所上行F电流("子站2测距数据", "上行F线电流", "FR故障", "上行"),
        分区所上行F电流角度("子站2测距数据", "上行F线电流角度", "", ""),
        分区所下行T电流("子站2测距数据", "下行T线电流", "TR故障", "下行"),
        分区所下行T电流角度("子站2测距数据", "下行T线电流角度", "", ""),
        分区所下行F电流("子站2测距数据", "下行F线电流", "FR故障", "下行"),
        分区所下行F电流角度("子站2测距数据", "下行F线电流角度", "", ""),
        ;
        private String resource;
        private String name;
        private String desc;
        private String direction;
        DIMENSION(String resource, String name, String desc, String direction) {
            this.resource = resource;
            this.name = name;
            this.desc = desc;
            this.direction = direction;
        }

        public static List<String> getNamesByResource(String resource) {
            return Arrays.stream(DIMENSION.values())
                    .filter(o-> StringUtils.equals(o.getResource(), resource))
                    .map(DIMENSION::getName)
                    .collect(Collectors.toList());
        }

        public static DIMENSION getByNameAndResource(String name, String resource) {
            return Arrays.stream(DIMENSION.values())
                    .filter(o->StringUtils.equals(name, o.getName())&&StringUtils.equals(resource, o.getResource()))
                    .findFirst()
                    .get();
        }
    }

    @Getter
    public enum SHORT_STATE {
        第一AT段下行FR故障("第一AT区段", "下行", "FR故障", 1),
        第一AT段上行FR故障("第一AT区段", "上行", "FR故障", 2),
        第二AT段下行FR故障("第二AT区段", "下行", "FR故障", 3),
        第二AT段上行FR故障("第二AT区段", "上行", "FR故障", 4),
        第一AT段上行TR故障("第一AT区段", "上行", "TR故障", 5),
        第一AT段下行TR故障("第一AT区段", "下行", "TR故障", 6),
        第二AT段上行TR故障("第二AT区段", "上行", "TR故障", 7),
        第二AT段下行TR故障("第二AT区段", "下行", "TR故障", 8),
        第一AT段下行TF故障("第一AT区段", "下行", "TF故障", 9),
        第二AT段下行TF故障("第二AT区段", "下行", "TF故障", 10),
        第一AT段上行TF故障("第一AT区段", "上行", "TF故障", 11),
        第二AT段上行TF故障("第二AT区段", "上行", "TF故障", 12),
        ;
        private String position;
        private String direction;
        private String type;
        private Integer order;

        SHORT_STATE(String position, String direction, String type, Integer order) {
            this.position=position;
            this.direction=direction;
            this.type=type;
            this.order=order;
        }

        public static Integer findState(String position, String direction, String type) {
            for (SHORT_STATE value : SHORT_STATE.values()) {
                if (StringUtils.equals(position, value.getPosition())
                        &&StringUtils.equals(direction, value.getDirection())
                        &&StringUtils.equals(type, value.getType())) {
                    return value.getOrder();
                }
            }
            return null;
        }

    }

    @Getter
    public enum CURRENT_TYPE {
        变电所下行T线电流left("left", -1, "I0xt"),
        变电所下行T线电流right("right", 1, "I0xt"),
        变电所上行T线电流left("left", -1, "I0st"),
        变电所上行T线电流right("right", 1, "I0st"),
        变电所上行F线电流left("left", -1, "I0sf"),
        变电所上行F线电流right("right", 1, "I0sf"),
        变电所下行F线电流left("right", 1, "I0xf"),
        变电所下行F线电流right("left", -1, "I0xf"),
        子站1上行T线电流left("up", 1, "I1st"),
        子站1上行T线电流right("down", -1, "I1st"),
        子站1下行T线电流left("up", 1, "I1xt"),
        子站1下行T线电流right("down", -1, "I1xt"),
        子站1上行F线电流left("up", -1, "I1sf"),
        子站1上行F线电流right("down", 1, "I1sf"),
        子站1下行F线电流left("up", -1, "I1xf"),
        子站1下行F线电流right("down", 1, "I1xf"),
        子站2上行T线电流left("up", 1, "I2st"),
        子站2上行T线电流right("down", -1, "I2st"),
        子站2下行T线电流left("up", 1, "I2xt"),
        子站2下行T线电流right("down", -1, "I2xt"),
        子站2上行F线电流left("up", -1, "I2sf"),
        子站2上行F线电流right("down", 1, "I2sf"),
        子站2下行F线电流left("down", 1, "I2xf"),
        子站2下行F线电流right("up", -1, "I2xf"),
        I1fleft("up", -1, "I1f"),
        I1fright("down", 1, "I1f"),
        I1tleft("up", 1, "I1t"),
        I1tright("down", -1, "I1t"),
        I2fleft("up", -1, "I2f"),
        I2fright("down", 1, "I2f"),
        I2tleft("up", 1, "I2t"),
        I2tright("down", -1, "I2t"),
        I0left("left", 1, "I0"),
        I0right("right", -1, "I0"),
        I1left("left", -1, "I1"),
        I1right("right", 1, "I1"),
        I2left("left", 1, "I2"),
        I2right("right", -1, "I2"),
        I短路left("up", 1, "I短路"),
        I短路right("down", -1, "I短路"),
        Ilastleft("left", 1, "Ilast"),
        Ilastright("right", -1, "Ilast"),
        ;
        private String direction;
        private String label;
        private Integer type;

        CURRENT_TYPE(String direction, Integer type, String label) {
            this.direction = direction;
            this.label = label;
            this.type = type;
        }

        public static String getByTypeAndLabel(Integer type, String label) {
            for (CURRENT_TYPE value : CURRENT_TYPE.values()) {
                if (StringUtils.equals(value.getLabel(), label) && NumberUtils.compare(value.getType(), type) ==0) {
                    return value.getDirection();
                }
            }
            return null;
        }
    }

    @Getter
    public enum ALGORITHM_TYPE {
        横联电流比法距离(1, "横联电流比法距离（km）"),
        吸上电流比法距离(2, "吸上电流比法F相距离（km）"),
        上下行电流比法距离(3, "上下行电流比法距离（km）"),
        ;
        private Integer num;
        private String desc;
        ALGORITHM_TYPE(Integer num, String desc) {
            this.desc = desc;
            this.num = num;
        }

        public static ALGORITHM_TYPE getNameByNum(Integer num) {
            for (ALGORITHM_TYPE value : ALGORITHM_TYPE.values()) {
                if (NumberUtils.compare(value.getNum(), num)==0) {
                    return value;
                }
            }
            return null;
        }
    }
}
