package cc.mrbird.febs.business.controller;

import cc.mrbird.febs.business.config.CustomStringNumberConverter;
import cc.mrbird.febs.business.dto.FixedTableVersionDto;
import cc.mrbird.febs.business.dto.FixedValueTableVersionDto;
import cc.mrbird.febs.business.entity.*;
import cc.mrbird.febs.business.listener.FixValueListener;
import cc.mrbird.febs.business.service.IFixedValueService;
import cc.mrbird.febs.business.service.IFixedValueTableService;
import cc.mrbird.febs.business.service.IFixedValueVersionService;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.entity.FebsResponse;
import cc.mrbird.febs.common.entity.QueryRequest;
import cc.mrbird.febs.common.exception.FebsException;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @company: test
 * @department: 数据中心
 * @date: 2021-05-22 2:09 下午
 * @author: test
 * @email: test@163.com
 * @desc： 定值入库
 */
@Slf4j
@RestController
@RequestMapping("fixedvalue")
@Api(tags = "定值表服务")
public class FixedValueController extends BaseController {

    @Autowired
    private IFixedValueTableService fixedValueTableService;

    @Autowired
    private IFixedValueVersionService fixedValueVersionService;

    @Autowired
    IFixedValueService fixedValueService;

    private final String FIXED_VALUE_META_REGEXP = "元数据";

    @Autowired
    private GridFsTemplate gridFsTemplate;

    // 定值入库 入库
    @PostMapping("import")
    @ApiOperation(value = "定值表导入")
//    @ControllerEndpoint(exceptionMessage = "导入Excel数据失败")
    public FebsResponse fixedValueImport(MultipartFile file, @RequestParam String siteId) throws IOException {
        if (file.isEmpty()) {
            throw new FebsException("导入数据为空");
        }
        String filename = file.getOriginalFilename();
        if (!StringUtils.endsWith(filename, ".xlsx")&&!StringUtils.endsWith(filename, ".xls")) {
            throw new FebsException("只支持.xlsx、.xls类型文件导入");
        }

        // 文件入库操作
        String uuid;
        try(InputStream is = file.getInputStream();) {
            uuid = UUID.randomUUID().toString();
            DBObject metadata = new BasicDBObject();
            metadata.put("uuid", uuid);
            gridFsTemplate.store(is, filename, file.getContentType(), metadata);
        }

        Resource resource = new Resource();
        resource.setFileName(file.getOriginalFilename());
        resource.setSuffix(Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf(".")));
        resource.setContentType(file.getContentType());
        resource.setUuid(uuid);
        resource.setFileLength(file.getSize());

        // 读取定值
        ReadSheet readSheet = EasyExcel.readSheet().build();
        Map<String, Object> callBack = new HashMap<>();
        callBack.put("siteId", siteId);
        ExcelReader fixedValueReader = EasyExcel.read(file.getInputStream(), FixedValue.class, new FixValueListener(resource, callBack))
                .registerConverter(new CustomStringNumberConverter())
                .headRowNumber(2).build();
        try {
            fixedValueReader.read(readSheet);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new FebsException("定值表不符合要求！");
        }
        FixedTableVersionDto versionDto = new FixedTableVersionDto();
        versionDto.setFixedValueVersionId(Long.parseLong(callBack.get("fixedValueVersionId").toString()));
        versionDto.setFixedValueTableId(Long.parseLong(callBack.get("fixedValueTableId").toString()));

//        // 读取元数据值
//        ReadSheet fixedValueSheet = EasyExcel.readSheet(FIXED_VALUE_META_REGEXP).build();
//        ExcelReader excelReader = EasyExcel.read(file.getInputStream(), FixedValueMeta.class,
//                new FixValueMetaListener()).build();
//        try {
//            excelReader.read(fixedValueSheet);
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            throw new FebsException("元数据表不符合要求！");
//        }

        // 文件入库操作
        return new FebsResponse().success().data(versionDto);
    }

    @GetMapping("/table/list/page")
    @ApiOperation(value = "定值表分页查询")
    public FebsResponse fixedValueTableListPage(QueryRequest request) {
        Map<String, Object> dataTable = getDataTable(this.fixedValueTableService.fixedValueTableList(request));
        return new FebsResponse().success().data(dataTable);
    }

    @GetMapping("/table/list")
    @ApiOperation(value = "获取所有定值表列表")
    public FebsResponse fixedValueTableList() {
        return new FebsResponse().success().data(this.fixedValueTableService.fixedValueTableList());
    }

    @GetMapping("/table/list/table")
    @ApiOperation(value = "获取所定值表的版本列表")
    public FebsResponse fixedValueTableVersionList(Long fixedValueTableId) {
        return new FebsResponse().success().data(this.fixedValueTableService.fixedValueTableVersionList(fixedValueTableId));
    }

    @GetMapping("/version/list/page")
    @ApiOperation(value = "获取定值表版本信息")
    public FebsResponse fixedValueTableList(Long fixedValueTableId, QueryRequest request) {
        Map<String, Object> dataTable = getDataTable(this.fixedValueVersionService.fixedValueVersionList(fixedValueTableId, request));
        return new FebsResponse().success().data(dataTable);
    }

    @DeleteMapping("/table")
    @ApiOperation(value = "删除定值表")
    public FebsResponse delValueTable(Long fixedValueTableId) {
        this.fixedValueTableService.delValueTable(fixedValueTableId);
        return new FebsResponse().success();
    }

    @GetMapping("/table/batch/{fixedValueTableIds}")
    @ApiOperation(value = "批量删除定值表")
    public FebsResponse delBatchValueTable(@PathVariable String fixedValueTableIds) {
        String[] split = fixedValueTableIds.split(",");
        for (String s : split) {
            this.fixedValueTableService.delValueTable(Long.parseLong(s));
        }
        return new FebsResponse().success();
    }

    @DeleteMapping("/table/all")
    @ApiOperation(value = "删除所有定值表【删除所有数据，慎用】")
    public FebsResponse delAllValueTable() {
        fixedValueTableService.delAllValueTable();
        return new FebsResponse().success();
    }

    @GetMapping("origin")
    @ApiOperation(value = "获取定值表源数据")
    @ApiImplicitParam(name = "fixedValueVersionId", value = "定值版本id", dataTypeClass = Long.class, example="14")
    public FebsResponse fixedTableInfo(Long fixedValueVersionId) {
        QueryWrapper<FixedValue> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("FIXED_VALUE_VERSION_ID", fixedValueVersionId);
        return new FebsResponse().success().data(fixedValueService.list(queryWrapper));
    }

    @GetMapping("/list/site")
    @ApiOperation(value = "通过siteId获取定值表列表")
    @ApiImplicitParam(name = "siteId", value = "定值版本id", dataTypeClass = Long.class, example="")
    public FebsResponse fixedTableVersionList(Long siteId) {
        QueryWrapper<FixedValueTable> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("SITE_ID", siteId);
        List<FixedValueTable> list = fixedValueTableService.list(queryWrapper);
        List<FixedValueTableVersionDto> fixedValueTableVersions = list.stream().map(o -> {
            FixedValueTableVersionDto fixedValueTableDto = new FixedValueTableVersionDto();
            BeanUtils.copyProperties(o, fixedValueTableDto);
            QueryWrapper<FixedValueVersion> fixedValueVersionQueryWrapper = new QueryWrapper<>();
            fixedValueVersionQueryWrapper.eq("FIXED_VALUE_TABLE_ID", o.getFixedValueTableId());
            List<FixedValueVersion> versions = fixedValueVersionService.list(fixedValueVersionQueryWrapper);
            versions.stream()
                    .max((o1, o2) -> NumberUtils.compare(o1.getFixValueVersionId(), o2.getFixValueVersionId()))
                    .ifPresent(fixedValueVersion -> fixedValueTableDto.setFixedValueVersionId(fixedValueVersion.getFixValueVersionId()));
            fixedValueTableDto.setVersions(versions);
            return fixedValueTableDto;
        }).collect(Collectors.toList());
        return new FebsResponse().success().data(fixedValueTableVersions);
    }

}
