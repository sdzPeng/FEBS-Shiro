package cc.mrbird.febs.business.controller;

import cc.mrbird.febs.business.entity.FixedValue;
import cc.mrbird.febs.business.entity.FixedValueMeta;
import cc.mrbird.febs.business.listener.FixValueListener;
import cc.mrbird.febs.business.listener.FixValueMetaListener;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

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
public class FixedValueController extends BaseController {

    @Autowired
    private IFixedValueTableService fixedValueTableService;

    @Autowired
    private IFixedValueVersionService fixedValueVersionService;

    private final String FIXED_VALUE_META_REGEXP = "元数据";

    // 定值入库 入库
    @PostMapping("import")
    @ControllerEndpoint(exceptionMessage = "导入Excel数据失败")
    public FebsResponse fixedValueImport(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new FebsException("导入数据为空");
        }
        String filename = file.getOriginalFilename();
        if (!StringUtils.endsWith(filename, ".xlsx")&&!StringUtils.endsWith(filename, ".xls")) {
            throw new FebsException("只支持.xlsx、.xls类型文件导入");
        }

        // 读取定值
        ReadSheet readSheet = EasyExcel.readSheet().build();
        ExcelReader fixedValueReader = EasyExcel.read(file.getInputStream(), FixedValue.class, new FixValueListener())
                .headRowNumber(2).build();
        fixedValueReader.read(readSheet);

        // 读取元数据值
        ReadSheet fixedValueSheet = EasyExcel.readSheet(FIXED_VALUE_META_REGEXP).build();
        ExcelReader excelReader = EasyExcel.read(file.getInputStream(), FixedValueMeta.class,
                new FixValueMetaListener()).build();
        excelReader.read(fixedValueSheet);

        return new FebsResponse().success();
    }

    @GetMapping("/table/list")
    public FebsResponse fixedValueTableList(QueryRequest request) {
        Map<String, Object> dataTable = getDataTable(this.fixedValueTableService.fixedValueTableList(request));
        return new FebsResponse().success().data(dataTable);
    }

    @GetMapping("/version/list")
    public FebsResponse fixedValueTableList(Long fixedValueVersionId, QueryRequest request) {
        Map<String, Object> dataTable = getDataTable(this.fixedValueVersionService.fixedValueVersionList(fixedValueVersionId, request));
        return new FebsResponse().success().data(dataTable);
    }

    @DeleteMapping("/table")
    public FebsResponse delValueTable(Long fixedValueTableId) {
        this.fixedValueTableService.delValueTable(fixedValueTableId);
        return new FebsResponse().success();
    }

    @DeleteMapping("/table/all")
    public FebsResponse delAllValueTable() {
        fixedValueTableService.delAllValueTable();
        return new FebsResponse().success();
    }

}
