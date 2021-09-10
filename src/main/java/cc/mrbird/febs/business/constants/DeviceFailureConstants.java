package cc.mrbird.febs.business.constants;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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
}
