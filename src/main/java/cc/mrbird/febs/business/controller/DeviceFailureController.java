package cc.mrbird.febs.business.controller;

import cc.mrbird.febs.common.entity.FebsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-24 9:11 上午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Slf4j
@RestController
@RequestMapping("devicefailure")
public class DeviceFailureController {

    // 定值入库 入库
    @PostMapping("v1/import")
    public FebsResponse fixedValueImport(MultipartFile file) throws IOException {
        return null;
    }
}
