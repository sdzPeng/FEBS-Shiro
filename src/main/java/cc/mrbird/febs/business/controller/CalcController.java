package cc.mrbird.febs.business.controller;

import cc.mrbird.febs.business.constants.DeviceFailureConstants;
import cc.mrbird.febs.business.constants.FixedValueConstants;
import cc.mrbird.febs.business.dto.CurrentValue;
import cc.mrbird.febs.business.dto.FailureReportDto;
import cc.mrbird.febs.business.dto.KeyValueResult;
import cc.mrbird.febs.business.entity.*;
import cc.mrbird.febs.business.service.*;
import cc.mrbird.febs.business.util.ContextPathUtil;
import cc.mrbird.febs.business.util.MathUtils;
import cc.mrbird.febs.common.entity.FebsResponse;
import cc.mrbird.febs.common.exception.ValidaException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.data.*;
import com.deepoove.poi.data.style.BorderStyle;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    ICalcService calcService;

    @Autowired
    private IDeviceService deviceService;

    @Autowired
    private IResourceService resourceService;

    @Autowired
    private ILabelService labelService;

    @Autowired
    private IBimService bimService;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Value("${gaotie.temp.dir}")
    private String tempDir;

    @GetMapping("/analysis/result")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value = "设备id", dataTypeClass = String.class),
            @ApiImplicitParam(name = "algorithmType", value = "算法类型", dataTypeClass = Integer.class)
    })
    @ApiOperation(value = "分析计算结果")
    public FebsResponse analysisResult(Long deviceId, @RequestParam(defaultValue = "1")Integer algorithmType) throws ValidaException {
        List<KeyValueResult> dataTable = calcService.analysisResult(deviceId, algorithmType);
        return new FebsResponse().success().data(dataTable);
    }

    @GetMapping("/calc/data")
    @ApiOperation(value = "计算源数据")
    public FebsResponse calcData(Long deviceId) throws ValidaException {
        List<KeyValueResult> keyValueResults = calcService.calcData2(deviceId);
        return new FebsResponse().success().data(keyValueResults);
    }

    @GetMapping("/current/distribution/map")
    @ApiOperation(value = "电流分布图")
    public FebsResponse currentDistMap(
            Long deviceId
    ) throws ValidaException {
        Map<String, Object> result = calcService.currentDistMap(deviceId, DeviceFailureConstants.ALGORITHM_TYPE.横联电流比法距离.getNum());
        return new FebsResponse().success().data(result);

    }

    @PostMapping("/image")
    @ApiOperation(value = "保存图片")
    public FebsResponse snapshot(
            Long deviceId, MultipartFile file
    ) throws IOException {
        Device device = deviceService.getById(deviceId);
        // 删除原始文件
        if (null != device.getSnapshot()) {
            Query query = Query.query(GridFsCriteria.where("metadata.uuid").is(device.getSnapshot()));
            gridFsTemplate.delete(query);
        }
        // 文件入库操作
        String uuid;
        try (InputStream is = file.getInputStream();) {
            uuid = UUID.randomUUID().toString();
            DBObject metadata = new BasicDBObject();
            metadata.put("uuid", uuid);
            gridFsTemplate.store(is, file.getName(),
                    "", metadata);
        }
        device.setSnapshot(uuid);
        QueryWrapper<Device> deviceQueryWrapper = new QueryWrapper<>();
        deviceQueryWrapper.eq("DEVICE_ID", deviceId);
        deviceService.update(device, deviceQueryWrapper);
        return new FebsResponse().success();
    }

    @GetMapping("/generate/report")
    @ApiOperation(value = "生成并下载故障报文报告（入库）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value = "设备id", dataTypeClass = Long.class),
            @ApiImplicitParam(name = "algorithmType", value = "算法类型", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "isDownload", value = "是否需要下载", dataTypeClass = String.class, example = "false")
    })
    public void generateReport(Long deviceId, HttpServletRequest request, @RequestParam(defaultValue = "1") Integer algorithmType,
                               HttpServletResponse response, Boolean isDownload) throws IOException, ValidaException {
        XWPFTemplate template = generateWordReport(deviceId, algorithmType);
        String dataStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String uuidName = UUID.randomUUID().toString();
        String contextPath = ContextPathUtil.getContextPath(dataStr +
                File.separator + uuidName, request);
        new File(contextPath).mkdirs();
        String filePath = contextPath + File.separator + UUID.randomUUID() + ".docx";
        OutputStream os = new BufferedOutputStream(new FileOutputStream(filePath));
        template.writeAndClose(os);
        // 文件入库操作
        String uuid;
        File file = new File(filePath);
        try (InputStream is = new FileInputStream(filePath)) {
            uuid = UUID.randomUUID().toString();
            DBObject metadata = new BasicDBObject();
            metadata.put("uuid", uuid);
            gridFsTemplate.store(is, file.getName(),
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document", metadata);
        }
        Resource resource = new Resource();
        resource.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        resource.setFileName(file.getName());
        resource.setUuid(uuid);
        resource.setFileLength(file.length());
        resource.setSuffix(".docx");
        resourceService.save(resource);
        Device device = deviceService.getById(deviceId);
        QueryWrapper<Device> deviceQueryWrapper = new QueryWrapper<>();
        deviceQueryWrapper.eq("DEVICE_ID", device.getDeviceId());
        device.setReportResourceId(resource.getResourceId());
        deviceService.update(device, deviceQueryWrapper);
        if (isDownload) {
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            // chrome浏览器下载文件可能出现：ERR_RESPONSE_HEADERS_MULTIPLE_CONTENT_DISPOSITION，
            // 产生原因：可能是因为文件名中带有英文半角逗号,
            // 解决办法：确保 filename 参数使用双引号包裹[1]
            response.setHeader("Content-Disposition", "attachment; filename=" + new File(filePath).getName());
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
            try (InputStream is = new FileInputStream(new File(filePath));
                 OutputStream outputStream = response.getOutputStream()) {
                // 下载文件
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                }
            }
        }
    }

    @GetMapping("/generate/download")
    @ApiOperation(value = "生成并下载故障报文报告（不入库）")
    public void download(Long deviceId, @RequestParam(defaultValue = "1")Integer algorithmType, HttpServletResponse response) throws IOException, ValidaException {
        XWPFTemplate template = generateWordReport(deviceId, algorithmType);
        // 下载文件
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        // chrome浏览器下载文件可能出现：ERR_RESPONSE_HEADERS_MULTIPLE_CONTENT_DISPOSITION，
        // 产生原因：可能是因为文件名中带有英文半角逗号,
        // 解决办法：确保 filename 参数使用双引号包裹[1]
        response.setHeader("Content-Disposition", "attachment; filename=test.docx");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
        template.writeAndClose(response.getOutputStream());
    }

    private XWPFTemplate generateWordReport(Long deviceId, Integer algorithmType) throws ValidaException, IOException {
        DecimalFormat df2 = new DecimalFormat("###.000");
        FailureReportDto failureReport = new FailureReportDto();
        List<KeyValueResult> dataTable = calcService.analysisResult(deviceId, algorithmType);
        KeyValueResult 故障类型 = dataTable.stream().filter(o -> StringUtils.equals(o.getKey(), "故障类型")).findFirst().orElse(null);
        if (null != 故障类型) {
            failureReport.setFailuretype(故障类型.getValue().toString());
        }
        Device device = deviceService.getById(deviceId);
        List<KeyValueResult> keyValueResults = calcService.calcData2(deviceId);
        failureReport.setSiteName(device.getSiteName());
        failureReport.setDeviceName(device.getDeviceName());
        failureReport.setCreateTime(device.getFailureTime());

        calcService.extracted(deviceId, algorithmType, df2, failureReport, dataTable);
        if (null != DeviceFailureConstants.ALGORITHM_TYPE.getNameByNum(algorithmType)) {
            KeyValueResult 电流比法 = dataTable.stream().filter(o -> StringUtils.equals(o.getKey(), DeviceFailureConstants.ALGORITHM_TYPE.getNameByNum(algorithmType).getDesc()))
                    .filter(o -> null != o.getValue() && StringUtils.isNotEmpty(o.getValue().toString()))
                    .min((o1, o2) ->
                            Double.parseDouble((null == o1.getValue() ? 0 : o1.getValue()).toString()) >
                                    Double.parseDouble((null == o2.getValue() ? 0 : o2.getValue()).toString()) ? 1 : -1)
                    .orElse(null);
            failureReport.setDlbf(电流比法.getKey());
            failureReport.setTjgzjl(df2.format(Double.parseDouble(null == 电流比法.getValue() ? "0" : 电流比法.getValue().toString())));
        }
        KeyValueResult 故障点公里标 = dataTable.stream().filter(o -> StringUtils.equals(o.getKey(), "故障点公里标（km）"))
                .findFirst()
                .orElse(null);
        failureReport.setGlb(MathUtils.base2scientific((Double.parseDouble(故障点公里标.getValue().toString()))));
        Map<String, Object> result = calcService.currentDistMap(deviceId, DeviceFailureConstants.ALGORITHM_TYPE.横联电流比法距离.getNum());

        failureReport.setI0(df2.format(Double.parseDouble(((CurrentValue) result.get("I0")).getValue().toString())));
        if (null != device.getSnapshot()) {
            Query query = Query.query(GridFsCriteria.where("metadata.uuid").is(device.getSnapshot()));
            GridFSFile one = gridFsTemplate.findOne(query);
            GridFsResource resource = gridFsTemplate.getResource(one);
            try (InputStream inputStream = resource.getInputStream();) {
                PictureRenderData pictureRenderData = Pictures.ofStream(inputStream).fitSize().center().create();
                failureReport.setSnapshot(pictureRenderData);
            }
        }
        failureReport.setI1(df2.format(Double.parseDouble(((CurrentValue) result.get("I1")).getValue().toString())));
        // 故障区段
        dataTable.stream().filter(o -> StringUtils.equals(o.getKey(), "故障区段"))
                .findFirst()
                .ifPresent(故障区段 -> failureReport.setOnetwo(故障区段.getValue().toString()));
        failureReport.setI2(df2.format(Double.parseDouble(((CurrentValue) result.get("I2")).getValue().toString())));
        dataTable.stream().filter(o -> StringUtils.equals(o.getKey(), "故障行别"))
                .findFirst()
                .ifPresent(故障行别 -> failureReport.setUpdown(故障行别.getValue().toString()));
        // 生成第一张表
        failureReport.setKeyFacilities(extractedFirstTable(故障点公里标.getValue().toString()));
        // 生成第二张表
        failureReport.setClosedFacilities(extractedSecondTable(故障点公里标.getValue().toString()));
        // 生成第三张表
        failureReport.setTouchNetInfo(extractedThirdTable(故障点公里标.getValue().toString()));
        // 生成第四张表
        failureReport.setCalcData(extractedForthTable(keyValueResults));
        File file;
//        String path = URLDecoder.decode(Objects.requireNonNull(CalcController.class.getClassLoader().getResource("word"))
//                .getPath());
//        file = new File(path);
        if (StringUtils.isEmpty(tempDir)) {
            file = ResourceUtils.getFile("classpath:word/test.docx");
        } else {
            file = new File(tempDir);
        }
        XWPFTemplate template = XWPFTemplate.compile(file).render(failureReport);
        return template;
    }

    private TableRenderData extractedForthTable(List<KeyValueResult> keyValueResults) {
        // 创建表格
        RowRenderData header = Rows.of("序号", "名称", "内容").bgColor("F2F2F2").center()
                .textColor("7F7f7F").textFontFamily("Hei").textFontSize(6).create();
        Tables.TableBuilder tableBuilder = Tables.ofA4MediumWidth().addRow(header);
        int i = 0;
        for (KeyValueResult keyValueResult : keyValueResults) {
            RowRenderData row = Rows.of(String.valueOf(i++),
                    keyValueResult.getKey(),
                    null == keyValueResult.getValue() ? "" : keyValueResult.getValue().toString())
                    .textFontSize(10)
                    .center()
                    .create();
            tableBuilder.addRow(row);
        }

        BorderStyle borderStyle = new BorderStyle();
        borderStyle.setColor("A6A6A6");
        borderStyle.setSize(4);
        borderStyle.setType(XWPFTable.XWPFBorderType.SINGLE);
        return tableBuilder.border(borderStyle).center()
                .create();
    }

    private TableRenderData extractedThirdTable(String glb) {
        // 创建表格
        RowRenderData header = Rows.of("支柱编号", "里程", "行别", "支柱型号", "基础型号",
                "拉线", "拉线基础", "腕臂安装图号", "接触网下锚", "中心锚结", "区间/车站", "其他").bgColor("F2F2F2").center()
                .textColor("7F7f7F").textFontFamily("Hei").textFontSize(6).create();
        // 故障点附近前后500m距离
        List<Bim> bims = bimService.list()
                .stream()
                .filter(o -> Math.abs(Double.parseDouble(o.getMileage())
                        - Double.parseDouble(glb) * 1000) < 500)
                .collect(Collectors.toList());
        Tables.TableBuilder tableBuilder = Tables.ofPercentWidth("100%").addRow(header);
        for (Bim bim : bims) {
            RowRenderData row = Rows.of(bim.getPillarCode(),
                    MathUtils.base2scientific(Double.parseDouble(bim.getMileage()) / 1000),
                    bim.getLine(),
                    bim.getPillarModel(),
                    bim.getBaseModel(),
                    bim.getStayWire(),
                    bim.getStayWireBase(),
                    bim.getWbazth(),
                    bim.getJcwxm(),
                    bim.getZxmj(),
                    bim.getRegion(),
                    bim.getOther())
                    .textFontSize(6)
                    .center()
                    .create();
            tableBuilder.addRow(row);
        }

        BorderStyle borderStyle = new BorderStyle();
        borderStyle.setColor("A6A6A6");
        borderStyle.setSize(4);
        borderStyle.setType(XWPFTable.XWPFBorderType.SINGLE);
        return tableBuilder.border(borderStyle).center()
                .create();
    }

    private TableRenderData extractedSecondTable(String glb) {
        List<Label> list = labelService.list();
        Double glbDouble = Double.parseDouble(glb);
        // 创建表格
        RowRenderData header = Rows.of("行别", "里程", "类别", "附加信息", "区间/车站").bgColor("F2F2F2").center()
                .textColor("7F7f7F").textFontFamily("Hei").textFontSize(9).create();
        Tables.TableBuilder tableBuilder = Tables.ofPercentWidth("100%").addRow(header);
        // 故障点最近的 上行
        Label labelUpLeft = list
                .stream()
                .filter(o -> StringUtils.equals(o.getLabel(), "上道口") &&
                        StringUtils.equals(o.getLine(), "上行") && Double.parseDouble(o.getMileage()) < glbDouble * 1000)
                .min((o1, o2) -> {
                    double abs1 = Math.abs(Double.parseDouble(o1.getMileage()) - glbDouble);
                    double abs2 = Math.abs(Double.parseDouble(o2.getMileage()) - glbDouble);
                    return abs1 >= abs2 ? 1 : -1;
                }).orElse(null);
        if (null != labelUpLeft) {
            tableBuilder.addRow(extracted(labelUpLeft));
        }

        // 故障点最近的 上行
        Label labelUpRight = list
                .stream()
                .filter(o -> StringUtils.equals(o.getLabel(), "上道口") &&
                        StringUtils.equals(o.getLine(), "上行") && Double.parseDouble(o.getMileage()) > glbDouble * 1000)
                .min((o1, o2) -> {
                    double abs1 = Math.abs(Double.parseDouble(o1.getMileage()) - glbDouble);
                    double abs2 = Math.abs(Double.parseDouble(o2.getMileage()) - glbDouble);
                    return abs1 >= abs2 ? -1 : 1;
                }).orElse(null);
        if (null != labelUpRight) {
            tableBuilder.addRow(extracted(labelUpRight));
        }
        // 故障点最近的 上行
        Label labelDownloadLeft = list
                .stream()
                .filter(o -> StringUtils.equals(o.getLabel(), "上道口") &&
                        StringUtils.equals(o.getLine(), "下行") && Double.parseDouble(o.getMileage()) < glbDouble * 1000)
                .min((o1, o2) -> {
                    double abs1 = Math.abs(Double.parseDouble(o1.getMileage()) - glbDouble);
                    double abs2 = Math.abs(Double.parseDouble(o2.getMileage()) - glbDouble);
                    return abs1 >= abs2 ? 1 : -1;
                }).orElse(null);
        if (null != labelDownloadLeft) {
            tableBuilder.addRow(extracted(labelDownloadLeft));
        }

        // 故障点最近的 上行
        Label labelDownRight = list
                .stream()
                .filter(o -> StringUtils.equals(o.getLabel(), "上道口") &&
                        StringUtils.equals(o.getLine(), "下行") && Double.parseDouble(o.getMileage()) > glbDouble * 1000)
                .min((o1, o2) -> {
                    double abs1 = Math.abs(Double.parseDouble(o1.getMileage()) - glbDouble);
                    double abs2 = Math.abs(Double.parseDouble(o2.getMileage()) - glbDouble);
                    return abs1 >= abs2 ? -1 : 1;
                }).orElse(null);
        if (null != labelDownRight) {
            tableBuilder.addRow(extracted(labelDownRight));
        }

        BorderStyle borderStyle = new BorderStyle();
        borderStyle.setColor("A6A6A6");
        borderStyle.setSize(4);
        borderStyle.setType(XWPFTable.XWPFBorderType.SINGLE);
        return tableBuilder.border(borderStyle).center()
                .create();
    }

    private RowRenderData extracted(Label label) {
        return Rows.of(label.getLine(),
                MathUtils.base2scientific(Double.parseDouble(label.getMileage()) / 1000),
                label.getLabel(),
                label.getAddition(),
                label.getRegion())
                .textFontSize(8)
                .center()
                .create();
    }

    private TableRenderData extractedFirstTable(String glb) {
        // 创建表格
        RowRenderData header = Rows.of("行别", "支柱号", "里程", "类别", "附加信息", "区间/车站").bgColor("F2F2F2").center()
                .textColor("7F7f7F").textFontFamily("Hei").textFontSize(9).create();
        // 故障点附近前后500m距离
        List<Label> labels = labelService.list()
                .stream()
                .filter(o -> Math.abs(Double.parseDouble(o.getMileage())
                        - Double.parseDouble(glb) * 1000) < 500)
                .collect(Collectors.toList());
        Tables.TableBuilder tableBuilder = Tables.ofPercentWidth("100%").addRow(header);
        for (Label label : labels) {
            RowRenderData row = Rows.of(label.getLine(), label.getPillarNum(),
                    MathUtils.base2scientific(Double.parseDouble(label.getMileage()) / 1000),
                    label.getLabel(),
                    label.getAddition(),
                    label.getRegion())
                    .textFontSize(8)
                    .center()
                    .create();
            tableBuilder.addRow(row);
        }

        BorderStyle borderStyle = new BorderStyle();
        borderStyle.setColor("A6A6A6");
        borderStyle.setSize(4);
        borderStyle.setType(XWPFTable.XWPFBorderType.SINGLE);
        return tableBuilder.border(borderStyle).center()
                .create();
    }
}
