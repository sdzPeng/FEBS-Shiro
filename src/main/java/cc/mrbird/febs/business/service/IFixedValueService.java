package cc.mrbird.febs.business.service;

import cc.mrbird.febs.business.constants.DeviceFailureConstants;
import cc.mrbird.febs.business.constants.FixedValueConstants;
import cc.mrbird.febs.business.dto.DeviceDataDto;
import cc.mrbird.febs.business.dto.DeviceDescDto;
import cc.mrbird.febs.business.dto.FixedTableVersionDto;
import cc.mrbird.febs.business.entity.FixedValue;
import cc.mrbird.febs.business.entity.Resource;
import cc.mrbird.febs.common.exception.ValidaException;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @company: test
 * @department: 数据中心
 * @date: 2021-05-22 2:52 下午
 * @author: test
 * @email: test@163.com
 * @desc：
 */
public interface IFixedValueService extends IService<FixedValue> {

    FixedTableVersionDto analysis(ReadSheet readSheet, List<FixedValue> list, Resource resource, Long siteId);

    void saveData(List<FixedValue> list, Long fixedValueVersionId);

    void updateVersion(FixedValue fixValue, Long fixedValueVersionId);

    FixedValue getOneByDeviceIdAndFixedValueName(Long fixedValueVersionId, String fixedValueName);

    List<DeviceDataDto> findByFixedValueVersionIdAndDimension(Long deviceId, List<DeviceFailureConstants.DIMENSION> params) throws ValidaException;

    FixedValue findByDeviceIdAndFixedName(Long deviceId, FixedValueConstants.DIMENSION dimension);
}
