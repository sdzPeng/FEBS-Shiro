package cc.mrbird.febs.business.controller;

import cc.mrbird.febs.business.config.CustomStringNumberConverter;
import cc.mrbird.febs.business.dto.BimDto;
import cc.mrbird.febs.business.dto.LabelDto;
import cc.mrbird.febs.business.entity.Bim;
import cc.mrbird.febs.business.entity.Label;
import cc.mrbird.febs.business.listener.BimListener;
import cc.mrbird.febs.business.listener.LabelListener;
import cc.mrbird.febs.business.service.ILabelService;
import cc.mrbird.febs.common.entity.FebsResponse;
import cc.mrbird.febs.common.exception.FebsException;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-08-23 7:44 上午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Slf4j
@RestController
@RequestMapping("label")
@Api(tags = "LABEL控制器")
public class LabelController {

    @Autowired private ILabelService labelService;
    // BIM 里程导入数据
    @PostMapping("import")
    @ApiOperation(value = "LABEL数据导入")
//    @ControllerEndpoint(exceptionMessage = "导入Excel数据失败")
    public FebsResponse labelImport(MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new FebsException("导入数据为空");
        }
        String filename = file.getOriginalFilename();
        if (!StringUtils.endsWith(filename, ".xlsx")&&!StringUtils.endsWith(filename, ".xls")) {
            throw new FebsException("只支持.xlsx、.xls类型文件导入");
        }

        // 删除已有数据
        List<Long> ids = labelService.list().stream().map(Label::getLabelId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(ids)) {
            labelService.removeByIds(ids);
        }
        // 读取上行
        ReadSheet readSheet = EasyExcel.readSheet("上行").build();
        ExcelReader bimReader = EasyExcel.read(file.getInputStream(), LabelDto.class, new LabelListener())
                .registerConverter(new CustomStringNumberConverter()).build();
        try {
            bimReader.read(readSheet);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new FebsException("LABEL表上行1导入异常！");
        }


        // 读取下行
        ReadSheet readSheet2 = EasyExcel.readSheet("下行").build();
        ExcelReader bimReader2 = EasyExcel.read(file.getInputStream(), LabelDto.class, new LabelListener())
                .registerConverter(new CustomStringNumberConverter()).build();
        try {
            bimReader2.read(readSheet2);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new FebsException("LABEL表下行1导入异常！");
        }

        // 文件入库操作
        return new FebsResponse().success();
    }

    @GetMapping("/all")
    @ApiOperation(value = "查询所有label")
    public FebsResponse findAll() {
        return new FebsResponse().success().data(labelService.list());
    }

    @GetMapping("/mileage")
    @ApiOperation(value = "通过里程查询LABEL")
    public FebsResponse bimByMileage(String mileage) {
        QueryWrapper<Label> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("MILEAGE", mileage);
        return new FebsResponse().success().data(labelService.getOne(queryWrapper));
    }

    @GetMapping("/id")
    @ApiOperation(value = "通过LABEL_ID 获取label信息")
    public FebsResponse bimById(Long labelId) throws IOException {
        QueryWrapper<Label> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("LABEL_ID", labelId);
        return new FebsResponse().success().data(labelService.getOne(queryWrapper));
    }
}
