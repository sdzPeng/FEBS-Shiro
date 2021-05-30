package cc.mrbird.febs.business.controller;

import cc.mrbird.febs.business.dto.KeyValueResult;
import cc.mrbird.febs.business.service.ICalcService;
import cc.mrbird.febs.common.entity.FebsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-25 11:54 下午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Slf4j
@RestController
@RequestMapping("calc")
public class CalcController {

    @Autowired ICalcService calcService;

    @GetMapping("/analysis/result")
    public FebsResponse analysisResult(Long fixedValueVersionId, Long deviceId) {
        List<KeyValueResult> dataTable = calcService.analysisResult(fixedValueVersionId, deviceId);
        return new FebsResponse().success().data(dataTable);
    }

    @GetMapping("/calc/data")
    public FebsResponse calcData() {
        List<KeyValueResult> keyValueResults = new ArrayList<>();
        keyValueResults.add(new KeyValueResult("变电所上网点公里标", "K220+724.41"));
        keyValueResults.add(new KeyValueResult("变电所供电线电缆长度", "2.01"));
        keyValueResults.add(new KeyValueResult("AT所上网点公里标", ""));
        keyValueResults.add(new KeyValueResult("AT所供电线电缆长度", "-0.07"));
        keyValueResults.add(new KeyValueResult("分区所上网点公里标", ""));
        keyValueResults.add(new KeyValueResult("分区所供电线电缆长度", "0.25"));
        keyValueResults.add(new KeyValueResult("第一AT段长度", "12.96"));
        keyValueResults.add(new KeyValueResult("第二AT段长度", "12.57"));
        keyValueResults.add(new KeyValueResult("变电所Q值", "10.246"));
        keyValueResults.add(new KeyValueResult("AT所QT1", "25.677"));
        keyValueResults.add(new KeyValueResult("AT所QT2", "7"));
        keyValueResults.add(new KeyValueResult("分区所QT1", "9.779"));
        keyValueResults.add(new KeyValueResult("分区所QT2", "14"));
        keyValueResults.add(new KeyValueResult("AT所QF1", "12"));
        keyValueResults.add(new KeyValueResult("AT所QF2", "15"));
        keyValueResults.add(new KeyValueResult("分区所QF1", "14"));
        keyValueResults.add(new KeyValueResult("分区所QF2", "14"));
        keyValueResults.add(new KeyValueResult("变电所吸上电流", "178.64安,-360.90度"));
        keyValueResults.add(new KeyValueResult("变电所上行T电流", "178.64安,-360.90度"));
        keyValueResults.add(new KeyValueResult("变电所上行F电流", "178.64安,-360.90度"));
        keyValueResults.add(new KeyValueResult("变电所下行T电流", "178.64安,-360.90度"));
        keyValueResults.add(new KeyValueResult("变电所下行F电流", "178.64安,-360.90度"));
        keyValueResults.add(new KeyValueResult("变电所上行F电流", "178.64安,-360.90度"));
        keyValueResults.add(new KeyValueResult("AT所吸上电流", "178.64安,-360.90度"));
        keyValueResults.add(new KeyValueResult("AT所上行T电流", "178.64安,-360.90度"));
        keyValueResults.add(new KeyValueResult("AT所上行F电流", "178.64安,-360.90度"));
        keyValueResults.add(new KeyValueResult("AT所下行T电流", "178.64安,-360.90度"));
        keyValueResults.add(new KeyValueResult("AT所下行F电流", "178.64安,-360.90度"));
        keyValueResults.add(new KeyValueResult("分区所吸上电流", "178.64安,-360.90度"));
        keyValueResults.add(new KeyValueResult("分区所上行T电流", "178.64安,-360.90度"));
        keyValueResults.add(new KeyValueResult("分区所上行F电流", "178.64安,-360.90度"));
        keyValueResults.add(new KeyValueResult("分区所下行T电流", "178.64安,-360.90度"));
        keyValueResults.add(new KeyValueResult("分区所下行F电流", "178.64安,-360.90度"));
        keyValueResults.add(new KeyValueResult("Kp", "2000"));
        keyValueResults.add(new KeyValueResult("In TF短路故障判别系数", "0.05"));
        return new FebsResponse().success().data(keyValueResults);
    }
}
