package cc.mrbird.febs.business.controller;

import cc.mrbird.febs.business.dto.CurrentValue;
import cc.mrbird.febs.business.dto.FailureReportDto;
import cc.mrbird.febs.business.dto.KeyValueResult;
import cc.mrbird.febs.business.entity.Bim;
import cc.mrbird.febs.business.entity.Device;
import cc.mrbird.febs.business.entity.Label;
import cc.mrbird.febs.business.service.IBimService;
import cc.mrbird.febs.business.service.ICalcService;
import cc.mrbird.febs.business.service.IDeviceService;
import cc.mrbird.febs.business.service.ILabelService;
import cc.mrbird.febs.business.util.MathUtils;
import cc.mrbird.febs.common.entity.FebsResponse;
import cc.mrbird.febs.common.exception.ValidaException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.data.RowRenderData;
import com.deepoove.poi.data.Rows;
import com.deepoove.poi.data.TableRenderData;
import com.deepoove.poi.data.Tables;
import com.deepoove.poi.data.style.BorderStyle;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

    @Autowired ICalcService calcService;

    @Autowired private IDeviceService deviceService;

    @Autowired private ILabelService labelService;

    @Autowired private IBimService bimService;

    @GetMapping("/analysis/result")
//    @ApiImplicitParam(name = "deviceId", value = "设备id", dataTypeClass = String.class)
    @ApiOperation(value = "分析计算结果")
    public FebsResponse analysisResult(Long deviceId) throws ValidaException {
        List<KeyValueResult> dataTable = calcService.analysisResult(deviceId);
        return new FebsResponse().success().data(dataTable);
    }

    @GetMapping("/calc/data")
    @ApiOperation(value = "计算源数据")
    public FebsResponse calcData(Long deviceId) throws ValidaException {
        List<KeyValueResult> keyValueResults = calcService.calcData(deviceId);
        return new FebsResponse().success().data(keyValueResults);
    }

    @GetMapping("/current/distribution/map")
    @ApiOperation(value = "电流分布图")
    public FebsResponse currentDistMap(
            Long deviceId
    ) throws ValidaException {
        Map<String, Object> result = calcService.currentDistMap(deviceId);
        return new FebsResponse().success().data(result);

    }



    @GetMapping("/generate/download")
    @ApiOperation(value = "生成并下载故障报文报告（不入库）")
    public void download(Long deviceId, HttpServletResponse response) throws IOException, ValidaException {
        XWPFTemplate template = generateWordReport(deviceId);
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

    private XWPFTemplate generateWordReport(Long deviceId) throws ValidaException, FileNotFoundException {
        DecimalFormat df2  = new DecimalFormat("###.000");
        FailureReportDto failureReport = new FailureReportDto();
        List<KeyValueResult> dataTable = calcService.analysisResult(deviceId);
        KeyValueResult 故障类型 = dataTable.stream().filter(o -> StringUtils.equals(o.getKey(), "故障类型")).findFirst().orElse(null);
        if (null != 故障类型) {
            failureReport.setFailuretype(故障类型.getValue().toString());
        }
        Device device = deviceService.getById(deviceId);
        List<KeyValueResult> keyValueResults = calcService.calcData(deviceId);
        failureReport.setSiteName(device.getSiteName());
        failureReport.setDeviceName(device.getDeviceName());
        failureReport.setCreateTime(device.getFailureTime());
        // 横联电流比法
        dataTable.stream().filter(o -> StringUtils.equals(o.getKey(), "横联电流比法距离（km）"))
                .findFirst()
                .ifPresent(横联电流比法 -> failureReport.setHldlbjl(df2.format(Double.parseDouble(横联电流比法.getValue().toString()))));
        // 上下行电流比法
        dataTable.stream().filter(o -> StringUtils.equals(o.getKey(), "上下行电流比法距离（km）"))
                .findFirst()
                .ifPresent(上下行电流比法距离 -> failureReport.setSxxdlbfcj(df2.format(Double.parseDouble(上下行电流比法距离.getValue().toString()))));
        // 吸上电流比法
        dataTable.stream().filter(o -> StringUtils.equals(o.getKey(), "吸上电流比法F相距离（km）"))
                .findFirst()
                .ifPresent(吸上电流比法F相距离 -> failureReport.setXsdlbfjl(df2.format(Double.parseDouble(吸上电流比法F相距离.getValue().toString()))));
        KeyValueResult 电流比法 = dataTable.stream().filter(o -> StringUtils.equals(o.getKey(), "横联电流比法距离（km）") ||
                StringUtils.equals(o.getKey(), "上下行电流比法距离（km）") ||
                StringUtils.equals(o.getKey(), "吸上电流比法F相距离（km）"))
                .max((o1, o2) ->
                        Double.parseDouble(o1.getValue().toString()) >
                                Double.parseDouble(o2.getValue().toString()) ? 1 : -1)
                .orElse(null);
        failureReport.setDlbf(电流比法.getKey());
        failureReport.setTjgzjl(df2.format(Double.parseDouble(电流比法.getValue().toString())));
        KeyValueResult 故障点公里标 = dataTable.stream().filter(o -> StringUtils.equals(o.getKey(), "故障点公里标（km）"))
                .findFirst()
                .orElse(null);
        failureReport.setGlb(MathUtils.base2scientific((Double.parseDouble(故障点公里标.getValue().toString()))));
        Map<String, Object> result = calcService.currentDistMap(deviceId);

        failureReport.setI0(df2.format(Double.parseDouble(((CurrentValue)result.get("I0")).getValue().toString())));
        failureReport.setImage("test");
        failureReport.setI1(df2.format(Double.parseDouble(((CurrentValue)result.get("I1")).getValue().toString())));
        // 故障区段
        dataTable.stream().filter(o -> StringUtils.equals(o.getKey(), "故障区段"))
                .findFirst()
                .ifPresent(故障区段 -> failureReport.setOnetwo(故障区段.getValue().toString()));
        failureReport.setI2(df2.format(Double.parseDouble(((CurrentValue)result.get("I2")).getValue().toString())));
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
        File file = ResourceUtils.getFile("classpath:word/test.docx");
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
                    null==keyValueResult.getValue()?"":keyValueResult.getValue().toString())
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
                        - Double.parseDouble(glb)*1000) < 500)
                .collect(Collectors.toList());
        Tables.TableBuilder tableBuilder = Tables.ofPercentWidth("100%").addRow(header);
        for (Bim bim : bims) {
            RowRenderData row = Rows.of(bim.getPillarCode(),
                    MathUtils.base2scientific(Double.parseDouble(bim.getMileage())/1000),
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
        RowRenderData header = Rows.of("行别",  "里程", "类别", "附加信息", "区间/车站").bgColor("F2F2F2").center()
                .textColor("7F7f7F").textFontFamily("Hei").textFontSize(9).create();
        Tables.TableBuilder tableBuilder = Tables.ofPercentWidth("100%").addRow(header);
        // 故障点最近的 上行
        Label labelUpLeft = list
                .stream()
                .filter(o -> StringUtils.equals(o.getLabel(), "上道口") &&
                        StringUtils.equals(o.getLine(), "上行") && Double.parseDouble(o.getMileage()) < glbDouble*1000)
                .min((o1, o2) -> {
                    double abs1 = Math.abs(Double.parseDouble(o1.getMileage()) - glbDouble);
                    double abs2 = Math.abs(Double.parseDouble(o2.getMileage()) - glbDouble);
                    return abs1 >= abs2 ? 1 : -1;
                }).orElse(null);
        if (null!=labelUpLeft) {
            tableBuilder.addRow(extracted(labelUpLeft));
        }

        // 故障点最近的 上行
        Label labelUpRight = list
                .stream()
                .filter(o -> StringUtils.equals(o.getLabel(), "上道口") &&
                        StringUtils.equals(o.getLine(), "上行") && Double.parseDouble(o.getMileage()) > glbDouble*1000)
                .min((o1, o2) -> {
                    double abs1 = Math.abs(Double.parseDouble(o1.getMileage()) - glbDouble);
                    double abs2 = Math.abs(Double.parseDouble(o2.getMileage()) - glbDouble);
                    return abs1 >= abs2 ? -1 : 1;
                }).orElse(null);
        if (null!=labelUpRight) {
            tableBuilder.addRow(extracted(labelUpRight));
        }
        // 故障点最近的 上行
        Label labelDownloadLeft = list
                .stream()
                .filter(o -> StringUtils.equals(o.getLabel(), "上道口") &&
                        StringUtils.equals(o.getLine(), "下行") && Double.parseDouble(o.getMileage()) < glbDouble*1000)
                .min((o1, o2) -> {
                    double abs1 = Math.abs(Double.parseDouble(o1.getMileage()) - glbDouble);
                    double abs2 = Math.abs(Double.parseDouble(o2.getMileage()) - glbDouble);
                    return abs1 >= abs2 ? 1 : -1;
                }).orElse(null);
        if (null!=labelDownloadLeft) {
            tableBuilder.addRow(extracted(labelDownloadLeft));
        }

        // 故障点最近的 上行
        Label labelDownRight = list
                .stream()
                .filter(o -> StringUtils.equals(o.getLabel(), "上道口") &&
                        StringUtils.equals(o.getLine(), "下行") && Double.parseDouble(o.getMileage()) > glbDouble*1000)
                .min((o1, o2) -> {
                    double abs1 = Math.abs(Double.parseDouble(o1.getMileage()) - glbDouble);
                    double abs2 = Math.abs(Double.parseDouble(o2.getMileage()) - glbDouble);
                    return abs1 >= abs2 ? -1 : 1;
                }).orElse(null);
        if (null!=labelDownRight) {
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
                MathUtils.base2scientific(Double.parseDouble(label.getMileage())/1000),
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
                        - Double.parseDouble(glb)*1000) < 500)
                .collect(Collectors.toList());
        Tables.TableBuilder tableBuilder = Tables.ofPercentWidth("100%").addRow(header);
        for (Label label : labels) {
            RowRenderData row = Rows.of(label.getLine(), label.getPillarNum(),
                    MathUtils.base2scientific(Double.parseDouble(label.getMileage())/1000),
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
