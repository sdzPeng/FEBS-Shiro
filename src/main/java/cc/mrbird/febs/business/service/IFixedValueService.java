package cc.mrbird.febs.business.service;

import cc.mrbird.febs.business.constants.DeviceFailureConstants;
import cc.mrbird.febs.business.constants.FixedValueConstants;
import cc.mrbird.febs.business.dto.DeviceDataDto;
import cc.mrbird.febs.business.dto.DeviceDescDto;
import cc.mrbird.febs.business.entity.FixedValue;
import cc.mrbird.febs.business.entity.Resource;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-22 2:52 下午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
public interface IFixedValueService extends IService<FixedValue> {

    Long analysis(ReadSheet readSheet, List<FixedValue> list, Resource resource);

    void saveData(List<FixedValue> list);

    void updateVersion(FixedValue fixValue);

    FixedValue getOneByDeviceIdAndFixedValueName(Long fixedValueVersionId, String fixedValueName);

    List<DeviceDataDto> findByFixedValueVersionIdAndDimension(Long deviceId, List<DeviceFailureConstants.DIMENSION> params);

    FixedValue findByDeviceIdAndFixedName(Long deviceId, FixedValueConstants.DIMENSION dimension);
}
