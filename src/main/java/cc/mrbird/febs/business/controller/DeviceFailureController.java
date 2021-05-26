package cc.mrbird.febs.business.controller;

import cc.mrbird.febs.business.dto.DeviceDto;
import cc.mrbird.febs.business.entity.Device;
import cc.mrbird.febs.business.listener.DeviceListener;
import cc.mrbird.febs.common.annotation.ControllerEndpoint;
import cc.mrbird.febs.common.entity.FebsResponse;
import cc.mrbird.febs.common.exception.FebsException;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    @PostMapping("import")
    @ControllerEndpoint(exceptionMessage = "导入Excel数据失败")
    public FebsResponse fixedValueImport(Long fixedValueVersionId, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new FebsException("导入数据为空");
        }
        String filename = file.getOriginalFilename();
        if (!StringUtils.endsWith(filename, ".xlsx")&&!StringUtils.endsWith(filename, ".xls")) {
            throw new FebsException("只支持.xlsx、.xls类型文件导入");
        }

        // 读取定值
        ReadSheet readSheet = EasyExcel.readSheet().build();
        ExcelReader fixedValueReader = EasyExcel.read(file.getInputStream(), DeviceDto.class, new DeviceListener(fixedValueVersionId))
                .headRowNumber(3).build();
        fixedValueReader.read(readSheet);
        return new FebsResponse().success();
    }
}
