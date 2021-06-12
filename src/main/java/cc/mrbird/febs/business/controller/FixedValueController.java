package cc.mrbird.febs.business.controller;

import cc.mrbird.febs.business.entity.FixedValue;
import cc.mrbird.febs.business.entity.FixedValueMeta;
import cc.mrbird.febs.business.entity.FixedValueVersion;
import cc.mrbird.febs.business.entity.Resource;
import cc.mrbird.febs.business.listener.FixValueListener;
import cc.mrbird.febs.business.listener.FixValueMetaListener;
import cc.mrbird.febs.business.service.IFixedValueService;
import cc.mrbird.febs.business.service.IFixedValueTableService;
import cc.mrbird.febs.business.service.IFixedValueVersionService;
import cc.mrbird.febs.common.annotation.ControllerEndpoint;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-22 2:09 下午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
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
//    @ControllerEndpoint(exceptionMessage = "导入Excel数据失败")
    public FebsResponse fixedValueImport(MultipartFile file) throws IOException {
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
        ExcelReader fixedValueReader = EasyExcel.read(file.getInputStream(), FixedValue.class, new FixValueListener(resource, callBack))
                .headRowNumber(2).build();
        fixedValueReader.read(readSheet);

        // 读取元数据值
        ReadSheet fixedValueSheet = EasyExcel.readSheet(FIXED_VALUE_META_REGEXP).build();
        ExcelReader excelReader = EasyExcel.read(file.getInputStream(), FixedValueMeta.class,
                new FixValueMetaListener()).build();
        excelReader.read(fixedValueSheet);

        // 文件入库操作
        return new FebsResponse().success().data(callBack.get("fixedValueTableId"));
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

}
