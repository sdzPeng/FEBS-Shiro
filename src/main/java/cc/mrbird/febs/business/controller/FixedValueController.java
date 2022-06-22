package cc.mrbird.febs.business.controller;

import cc.mrbird.febs.business.config.CustomStringNumberConverter;
import cc.mrbird.febs.business.dto.FixedTableVersionDto;
import cc.mrbird.febs.business.dto.FixedValueTableVersion2Dto;
import cc.mrbird.febs.business.dto.FixedValueTableVersionDto;
import cc.mrbird.febs.business.entity.*;
import cc.mrbird.febs.business.listener.FixValueListener;
import cc.mrbird.febs.business.service.*;
import cc.mrbird.febs.business.util.ContextPathUtil;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.entity.FebsResponse;
import cc.mrbird.febs.common.entity.QueryRequest;
import cc.mrbird.febs.common.exception.FebsException;
import cc.mrbird.febs.common.exception.ValidaException;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
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

    @Autowired
    IResourceService resourceService;

    @Autowired
    private ISiteDicService siteDicService;

    private final String FIXED_VALUE_META_REGEXP = "元数据";

    @Value("${gaotie.preview}")
    private String previewUrl;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    // 定值入库 入库
    @PostMapping("import")
    @ApiOperation(value = "定值表导入")
    public FebsResponse fixedValueImport(@NotNull MultipartFile file, @RequestParam String siteId) throws IOException, ValidaException {
        if (null == file || file.isEmpty()) {
            throw new ValidaException("导入文件不得为空！");
        }
        String filename = file.getOriginalFilename();
        if (!StringUtils.endsWith(filename, ".xlsx")&&!StringUtils.endsWith(filename, ".xls")) {
            throw new ValidaException("只支持.xlsx、.xls类型文件导入！");
        }

        // 校验站点是否存在
        SiteDic siteDic = siteDicService.getById(siteId);
        if(null == siteDic) {
            throw new ValidaException("录入的站点不存在！");
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
    public FebsResponse delBatchValueTable(
            @PathVariable String fixedValueTableIds,
            @RequestParam String fixedValueTableVersionIds) {
        String[] split = fixedValueTableIds.split(",");
        String[] fixedValueTableVersionIdArr = fixedValueTableVersionIds.split(",");
        if (fixedValueTableVersionIdArr.length>0) {
            List<Long> fixedValueTableVersionColl = Arrays.stream(fixedValueTableVersionIdArr)
                    .map(Long::parseLong).collect(Collectors.toList());
            fixedValueTableService.delFixedValueVersion(fixedValueTableVersionColl);
        }else {
            for (String s : split) {
                this.fixedValueTableService.delValueTable(Long.parseLong(s));
            }
        }
        return new FebsResponse().success();
    }

    @GetMapping("/table/batch/fixedValue/{fixedValueTableVersionIds}")
    @ApiOperation(value = "批量删除定值表")
    public FebsResponse delBatchValueTableVersion(
            @PathVariable String fixedValueTableVersionIds) {
        String[] fixedValueTableVersionIdArr = fixedValueTableVersionIds.split(",");
        if (fixedValueTableVersionIdArr.length>0) {
            List<Long> fixedValueTableVersionColl = Arrays.stream(fixedValueTableVersionIdArr)
                    .map(Long::parseLong).collect(Collectors.toList());
            fixedValueTableService.delFixedValueVersion(fixedValueTableVersionColl);
        }
        return new FebsResponse().success();
    }

    @GetMapping("/table/batch/device/failure/{deviceTableIds}")
    @ApiOperation(value = "批量删除定值表")
    public FebsResponse delBatchFailure(
            @PathVariable String deviceTableIds) {
        String[] deviceTableArr = deviceTableIds.split(",");
        if (deviceTableArr.length>0) {
            List<Long> deviceTableIds2 = Arrays.stream(deviceTableArr)
                    .map(Long::parseLong).collect(Collectors.toList());
            fixedValueTableService.batchDeleteByDeviceTableIds(deviceTableIds2);
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
            if (CollectionUtils.isEmpty(versions)) {
                return null;
            }
            versions.stream()
                    .max((o1, o2) -> NumberUtils.compare(o1.getFixValueVersionId(), o2.getFixValueVersionId()))
                    .ifPresent(fixedValueVersion -> fixedValueTableDto.setFixedValueVersionId(fixedValueVersion.getFixValueVersionId()));
            fixedValueTableDto.setVersions(versions);
            return fixedValueTableDto;
        }).filter(ObjectUtils::isNotNull).collect(Collectors.toList());
        return new FebsResponse().success().data(fixedValueTableVersions);
    }

    @GetMapping("/v2/list/site")
    @ApiOperation(value = "通过siteId获取定值表列表")
    @ApiImplicitParam(name = "siteId", value = "定值版本id", dataTypeClass = Long.class, example="")
    public FebsResponse fixedTableVersionList2(Long siteId, HttpServletRequest servletRequest) {
        QueryWrapper<FixedValueTable> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("SITE_ID", siteId);
        List<FixedValueTable> list = fixedValueTableService.list(queryWrapper);
        List<FixedValueTableVersion2Dto> fixedValueTableVersions = new ArrayList<>();
        list.forEach(o -> {
            QueryWrapper<FixedValueVersion> fixedValueVersionQueryWrapper = new QueryWrapper<>();
            fixedValueVersionQueryWrapper.eq("FIXED_VALUE_TABLE_ID", o.getFixedValueTableId());
            List<FixedValueVersion> versions = fixedValueVersionService.list(fixedValueVersionQueryWrapper);
            for (FixedValueVersion version : versions) {
                FixedValueTableVersion2Dto fixedValueTableVersionDto = new FixedValueTableVersion2Dto();
                fixedValueTableVersionDto.setFixedValueTableId(o.getFixedValueTableId());
                fixedValueTableVersionDto.setFirstTime(o.getCreateTime());
                fixedValueTableVersionDto.setName(o.getName());
                fixedValueTableVersionDto.setFixedValueVersionId(version.getFixValueVersionId());
                fixedValueTableVersionDto.setResourceId(version.getResourceId());
                fixedValueTableVersionDto.setCreateTime(version.getCreateTime());
                Resource resource = resourceService.getById(version.getResourceId());
                fixedValueTableVersionDto.setResourceName(resource.getFileName());
                fixedValueTableVersionDto.setPreviewUrl(previewUrl);
                fixedValueTableVersionDto.setContextPath(ContextPathUtil.getBaseURL(servletRequest));
                fixedValueTableVersions.add(fixedValueTableVersionDto);
            }
        });
        return new FebsResponse().success().data(fixedValueTableVersions);
    }

    @GetMapping("/v3/list/site")
    @ApiOperation(value = "通过siteId获取最新定制表")
    @ApiImplicitParam(name = "siteId", value = "站点id", dataTypeClass = Long.class, example="")
    public FebsResponse fixedTableVersionLatest(Long siteId) {
        QueryWrapper<FixedValueTable> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("SITE_ID", siteId);
        List<FixedValueTable> list = fixedValueTableService.list(queryWrapper);
        List<FixedValueTableVersion2Dto> fixedValueTableVersions = new ArrayList<>();
        list.forEach(o -> {
            QueryWrapper<FixedValueVersion> fixedValueVersionQueryWrapper = new QueryWrapper<>();
            fixedValueVersionQueryWrapper.eq("FIXED_VALUE_TABLE_ID", o.getFixedValueTableId());
            List<FixedValueVersion> versions = fixedValueVersionService.list(fixedValueVersionQueryWrapper);
            for (FixedValueVersion version : versions) {
                FixedValueTableVersion2Dto fixedValueTableVersionDto = new FixedValueTableVersion2Dto();
                fixedValueTableVersionDto.setFixedValueTableId(o.getFixedValueTableId());
                fixedValueTableVersionDto.setFirstTime(o.getCreateTime());
                fixedValueTableVersionDto.setName(o.getName());
                fixedValueTableVersionDto.setFixedValueVersionId(version.getFixValueVersionId());
                fixedValueTableVersionDto.setResourceId(version.getResourceId());
                fixedValueTableVersionDto.setCreateTime(version.getCreateTime());
                Resource resource = resourceService.getById(version.getResourceId());
                fixedValueTableVersionDto.setResourceName(resource.getFileName());
                fixedValueTableVersions.add(fixedValueTableVersionDto);
            }
        });
        return new FebsResponse()
                .success()
                .data(fixedValueTableVersions.stream()
                        .max(Comparator.comparing(FixedValueTableVersion2Dto::getCreateTime))
                        .orElse(null)
                );
    }

}
