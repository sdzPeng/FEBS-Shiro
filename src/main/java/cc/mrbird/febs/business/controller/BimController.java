package cc.mrbird.febs.business.controller;

import cc.mrbird.febs.business.config.CustomStringNumberConverter;
import cc.mrbird.febs.business.dto.BimDto;
import cc.mrbird.febs.business.entity.Bim;
import cc.mrbird.febs.business.listener.BimListener;
import cc.mrbird.febs.business.service.IBimService;
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
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @company: test
 * @department: 数据中心
 * @date: 2021-08-20 2:41 下午
 * @author: test
 * @email: test@163.com
 * @desc：
 */
@Slf4j
@RestController
@RequestMapping("bim")
@Api(tags = "BIM控制器")
public class BimController {

    @Autowired private IBimService bimService;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    // BIM 里程导入数据
    @PostMapping("import")
    @ApiOperation(value = "BIM数据导入")
//    @ControllerEndpoint(exceptionMessage = "导入Excel数据失败")
    public FebsResponse fixedValueImport(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new FebsException("导入数据为空");
        }
        String filename = file.getOriginalFilename();
        if (!StringUtils.endsWith(filename, ".xlsx")&&!StringUtils.endsWith(filename, ".xls")) {
            throw new FebsException("只支持.xlsx、.xls类型文件导入");
        }

        // 删除已有数据
        List<Long> ids = bimService.list().stream().map(Bim::getBimId).collect(Collectors.toList());
        bimService.removeByIds(ids);
        // 读取上行
        ReadSheet readSheet = EasyExcel.readSheet("上行1").build();
        ExcelReader bimReader = EasyExcel.read(file.getInputStream(), BimDto.class, new BimListener())
                .registerConverter(new CustomStringNumberConverter()).build();
        try {
            bimReader.read(readSheet);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new FebsException("BIM表上行1导入异常！");
        }


        // 读取下行
        ReadSheet readSheet2 = EasyExcel.readSheet("下行1").build();
        ExcelReader bimReader2 = EasyExcel.read(file.getInputStream(), BimDto.class, new BimListener())
                .registerConverter(new CustomStringNumberConverter()).build();
        try {
            bimReader2.read(readSheet2);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new FebsException("BIM表下行1导入异常！");
        }

        // 文件入库操作
        return new FebsResponse().success();
    }

    @GetMapping("/mileage")
    @ApiOperation(value = "通过里程查询BIM")
//    @ControllerEndpoint(exceptionMessage = "导入Excel数据失败")
    public FebsResponse bimByMileage(String mileage) throws IOException {
        QueryWrapper<Bim> queryWrapper = new QueryWrapper();
        queryWrapper.eq("MILEAGE", mileage);
        return new FebsResponse().success().data(bimService.getOne(queryWrapper));
    }

    @GetMapping("/all")
    @ApiOperation(value = "获取所有BIM属性")
    public FebsResponse bimAll() {
        return new FebsResponse().success().data(bimService.list());
    }

    @GetMapping("/id")
    @ApiOperation(value = "通过BIMID 获取bim信息")
    public FebsResponse bimById(Long bimId) throws IOException {
        QueryWrapper<Bim> queryWrapper = new QueryWrapper();
        queryWrapper.eq("BIM_ID", bimId);
        return new FebsResponse().success().data(bimService.getOne(queryWrapper));
    }

    @GetMapping("/bimCode")
    @ApiOperation(value = "通过BIM code 获取bim信息")
    public FebsResponse bimByBimCode(String bimCode) throws IOException {
        QueryWrapper<Bim> queryWrapper = new QueryWrapper();
        queryWrapper.eq("BIM_CODE", bimCode);
        return new FebsResponse().success().data(bimService.getOne(queryWrapper));
    }
}
