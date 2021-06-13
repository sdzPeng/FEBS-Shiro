package cc.mrbird.febs.business.controller;

import cc.mrbird.febs.business.dto.KeyValueResult;
import cc.mrbird.febs.business.service.ICalcService;
import cc.mrbird.febs.common.entity.FebsResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
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
@Api(tags = "设备故障计算服务")
public class CalcController {

    @Autowired ICalcService calcService;

    @GetMapping("/analysis/result")
//    @ApiImplicitParam(name = "deviceId", value = "设备id", dataTypeClass = String.class)
    @ApiOperation(value = "分析计算结果")
    public FebsResponse analysisResult(Long deviceId) {
        List<KeyValueResult> dataTable = calcService.analysisResult(deviceId);
        return new FebsResponse().success().data(dataTable);
    }

    @GetMapping("/calc/data")
    @ApiOperation(value = "计算源数据")
    public FebsResponse calcData(Long deviceId) {
        List<KeyValueResult> keyValueResults = calcService.calcData(deviceId);
        return new FebsResponse().success().data(keyValueResults);
    }
}
