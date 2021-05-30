package cc.mrbird.febs.business.service.impl;

import cc.mrbird.febs.business.constants.DeviceFailureConstants;
import cc.mrbird.febs.business.constants.FixedValueConstants;
import cc.mrbird.febs.business.dto.DeviceDescDto;
import cc.mrbird.febs.business.dto.DeviceResourceDto;
import cc.mrbird.febs.business.dto.KeyValueResult;
import cc.mrbird.febs.business.entity.DeviceData;
import cc.mrbird.febs.business.entity.FixedValue;
import cc.mrbird.febs.business.service.ICalcService;
import cc.mrbird.febs.business.service.IFixedValueService;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-30 1:12 上午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class CalcServiceImpl implements ICalcService {

    @Autowired private IFixedValueService fixedValueService;

    @Override
    public List<KeyValueResult> analysisResult(Long fixedValueVersionId, Long deviceId) {
        List<KeyValueResult> keyValueResults = new ArrayList<>();
        // 故障类型
        // 规则：
        // FR故障：【变电所吸上电流】、【AT所吸上电流】、【分区所吸上电流】最大值小于【定值表】中【TF短路故障判别】
        // 如果不符合上述，则取最大值：
        // 【变电所上行T电流】、【变电所上行F电流】、【变电所下行T电流】、【变电所下行F电流】、
        // 【AT所上行T电流】、【AT所上行F电流】、【AT所下行T电流】、
        // 【分区所上行T电流】、【分区所上行F电流】、【分区所下行T电流】、【分区所下行F电流】，满足以下映射关系：
        //  变电所上行T电流：TR故障
        //  变电所上行F电流：FR故障
        //  变电所下行T电流：TR故障
        //  变电所下行F电流：FR故障
        //  AT所上行T电流：TR故障
        //  AT所上行F电流：FR故障
        //  AT所下行T电流：TR故障
        //  分区所上行T电流：FR故障
        //  分区所上行F电流：TR故障
        //  分区所下行T电流：FR故障
        //  分区所下行F电流：TR故障
        FixedValue TF短路故障判别 = fixedValueService.getOneByDeviceIdAndFixedValueName(fixedValueVersionId, FixedValueConstants.TF短路故障判别);
        List<DeviceFailureConstants.DIMENSION> params = new ArrayList<>();
        params.add(DeviceFailureConstants.DIMENSION.变电所吸上电流);
        params.add(DeviceFailureConstants.DIMENSION.AT所吸上电流);
        params.add(DeviceFailureConstants.DIMENSION.分区所吸上电流);
        List<DeviceDescDto> deviceDataGroups = this.fixedValueService.findByFixedValueVersionIdAndDimension(fixedValueVersionId, params);
        System.out.println("here");
        keyValueResults.add(new KeyValueResult("故障类型", "FR故障"));

        // 故障行别
        keyValueResults.add(new KeyValueResult("故障行别", "下航"));
        // 故障区段
        keyValueResults.add(new KeyValueResult("故障区段", "第一AT区段"));
        // 变电所吸上电流(A)
        keyValueResults.add(new KeyValueResult("变电所吸上电路（A）", 5408.403));
        // AT所吸上电流(A)
        keyValueResults.add(new KeyValueResult("变电所吸上电路（A）", 5408.403));
        // 分区所吸上电流(A)
        keyValueResults.add(new KeyValueResult("分区所吸上电路（A）", 5408.403));
        // 变电所横联电流Ihl0(A)
        keyValueResults.add(new KeyValueResult("变电所横联电流Ihl0(A)", 5408.403));
        // AT所横联电流Ihl1(A)
        keyValueResults.add(new KeyValueResult("分区所吸上电路（A）", 5408.403));
        // 分区所横联电流Ihl2(A)
        keyValueResults.add(new KeyValueResult("AT所横联电流Ihl1(A)", 5408.403));
        // 吸上电流比法T相距离（km）
        keyValueResults.add(new KeyValueResult("吸上电流比法T相距离（km）", 5408.403));
        // 吸上电流比法F相距离（km）
        keyValueResults.add(new KeyValueResult("吸上电流比法F相距离（km）", 5408.403));
        // 横联电流比法距离（km）
        keyValueResults.add(new KeyValueResult("横联电流比法距离（km）", 5408.403));
        // 上下行电流比法距离（km）
        keyValueResults.add(new KeyValueResult("上下行电流比法距离（km）", 5408.403));
        // 推荐故障距离（km）
        keyValueResults.add(new KeyValueResult("推荐故障距离（km）", 5408.403));
        // 故障点公里标（km）
        keyValueResults.add(new KeyValueResult("故障点公里标（km）", 5408.403));
        return keyValueResults;
    }
}
