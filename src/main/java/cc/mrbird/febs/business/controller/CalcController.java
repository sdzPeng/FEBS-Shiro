package cc.mrbird.febs.business.controller;

import cc.mrbird.febs.business.dto.CurrentValue;
import cc.mrbird.febs.business.dto.FailureReport;
import cc.mrbird.febs.business.dto.KeyValueResult;
import cc.mrbird.febs.business.entity.Device;
import cc.mrbird.febs.business.service.ICalcService;
import cc.mrbird.febs.business.service.IDeviceService;
import cc.mrbird.febs.common.entity.FebsResponse;
import cc.mrbird.febs.common.exception.ValidaException;
import com.deepoove.poi.XWPFTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @company: test
 * @department: 数据中心
 * @date: 2021-05-25 11:54 下午
 * @author: test
 * @email: test@163.com
 * @desc：
 */
@Slf4j
@RestController
@RequestMapping("calc")
@Api(tags = "设备故障计算服务")
public class CalcController {

    @Autowired ICalcService calcService;

    @Autowired private IDeviceService deviceService;

    @GetMapping("/analysis/result")
//    @ApiImplicitParam(name = "deviceId", value = "设备id", dataTypeClass = String.class)
    @ApiOperation(value = "分析计算结果")
    public FebsResponse analysisResult(Long deviceId) throws ValidaException {
        List<KeyValueResult> dataTable = calcService.analysisResult(deviceId);
        return new FebsResponse().success().data(dataTable);
    }

    @GetMapping("/calc/data")
    @ApiOperation(value = "计算源数据")
    public FebsResponse calcData(Long deviceId) throws ValidaException {
        List<KeyValueResult> keyValueResults = calcService.calcData(deviceId);
        return new FebsResponse().success().data(keyValueResults);
    }

    @GetMapping("/current/distribution/map")
    @ApiOperation(value = "电流分布图")
    public FebsResponse currentDistMap(
            Long deviceId
    ) throws ValidaException {
        Map<String, Object> result = calcService.currentDistMap(deviceId);
        return new FebsResponse().success().data(result);

    }

    @GetMapping("/download")
    @ApiOperation(value = "生成故障报文")
    public void download(Long deviceId) throws IOException, ValidaException {
        FailureReport failureReport = new FailureReport();
        List<KeyValueResult> dataTable = calcService.analysisResult(deviceId);
        KeyValueResult 故障类型 = dataTable.stream().filter(o -> StringUtils.equals(o.getKey(), "故障类型")).findFirst().orElse(null);
        if (null != 故障类型) {
            failureReport.setFailuretype(故障类型.getValue().toString());
        }
        Device device = deviceService.getById(deviceId);
        List<KeyValueResult> keyValueResults = calcService.calcData(deviceId);
        failureReport.setSiteName(device.getSiteName());
        failureReport.setDeviceName(device.getDeviceName());
        failureReport.setCreateTime(device.getFailureTime());
        // 横联电流比法
        dataTable.stream().filter(o -> StringUtils.equals(o.getKey(), "横联电流比法距离（km）"))
                .findFirst()
                .ifPresent(横联电流比法 -> failureReport.setHldlbjl(横联电流比法.getValue().toString()));
        // 上下行电流比法
        dataTable.stream().filter(o -> StringUtils.equals(o.getKey(), "上下行电流比法距离（km）"))
                .findFirst()
                .ifPresent(上下行电流比法距离 -> failureReport.setSxxdlbfcj(上下行电流比法距离.getValue().toString()));
        // 吸上电流比法
        dataTable.stream().filter(o -> StringUtils.equals(o.getKey(), "吸上电流比法F相距离（km）"))
                .findFirst()
                .ifPresent(吸上电流比法F相距离 -> failureReport.setXsdlbfjl(吸上电流比法F相距离.getValue().toString()));
        KeyValueResult 电流比法 = dataTable.stream().filter(o -> StringUtils.equals(o.getKey(), "横联电流比法距离（km）") ||
                StringUtils.equals(o.getKey(), "上下行电流比法距离（km）") ||
                StringUtils.equals(o.getKey(), "吸上电流比法F相距离（km）"))
                .max((o1, o2) ->
                        Double.parseDouble(o1.getValue().toString()) >
                                Double.parseDouble(o2.getValue().toString()) ? 1 : -1)
                .orElse(null);
        failureReport.setDlbf(电流比法.getValue().toString());
        failureReport.setTjgzjl(电流比法.getValue().toString());
        dataTable.stream().filter(o -> StringUtils.equals(o.getKey(), "故障点公里标（km）"))
                .findFirst()
                .ifPresent(故障点公里标 -> failureReport.setGlb(故障点公里标.getValue().toString()));
        Map<String, Object> result = calcService.currentDistMap(deviceId);

        failureReport.setI0(((CurrentValue)result.get("I0")).getValue().toString());
        failureReport.setImage("test");
        failureReport.setI1(((CurrentValue)result.get("I1")).getValue().toString());
        // 故障区段
        dataTable.stream().filter(o -> StringUtils.equals(o.getKey(), "故障区段"))
                .findFirst()
                .ifPresent(故障区段 -> failureReport.setOnetwo(故障区段.getValue().toString()));
        failureReport.setI2(((CurrentValue)result.get("I2")).getValue().toString());
        dataTable.stream().filter(o -> StringUtils.equals(o.getKey(), "故障行别"))
                .findFirst()
                .ifPresent(故障行别 -> failureReport.setUpdown(故障行别.getValue().toString()));
        File file = ResourceUtils.getFile("classpath:word/test.docx");
        XWPFTemplate template = XWPFTemplate.compile(file).render(failureReport);
        template.writeToFile("分析报告test.docx");
    }
}
