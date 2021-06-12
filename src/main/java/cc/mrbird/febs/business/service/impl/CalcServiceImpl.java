package cc.mrbird.febs.business.service.impl;

import cc.mrbird.febs.business.constants.DeviceFailureConstants;
import cc.mrbird.febs.business.constants.FixedValueConstants;
import cc.mrbird.febs.business.dto.DeviceDataDto;
import cc.mrbird.febs.business.dto.KeyValueResult;
import cc.mrbird.febs.business.entity.FixedValue;
import cc.mrbird.febs.business.entity.FixedValueVersion;
import cc.mrbird.febs.business.service.ICalcService;
import cc.mrbird.febs.business.service.IFixedValueService;
import cc.mrbird.febs.business.service.IFixedValueVersionService;
import cc.mrbird.febs.business.util.MathUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.linear.RealVector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-05-30 1:12 上午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class CalcServiceImpl implements ICalcService {

    @Autowired private IFixedValueService fixedValueService;
    @Autowired private IFixedValueVersionService fixedValueVersionService;

    private static int compare(DeviceDataDto o1, DeviceDataDto o2) {
        return Double.parseDouble(o1.getDeviceValue()) > Double.parseDouble(o2.getDeviceValue()) ? 1 : -1;
    }

    @Override
    public List<KeyValueResult> analysisResult(Long deviceId) {
        Map<String, Object> temp = new HashMap<>();
        List<KeyValueResult> keyValueResults = new ArrayList<>();
        // 故障类型
        // 规则：
        // FR故障：【变电所吸上电流】、【AT所吸上电流】、【分区所吸上电流】最大值小于【定值表】中【TF短路故障判别】
        // 如果不符合上述，则取最大值：
        // 【变电所上行T电流】、【变电所上行F电流】、【变电所下行T电流】、【变电所下行F电流】、
        // 【AT所上行T电流】、【AT所上行F电流】、【AT所下行T电流】、
        // 【分区所上行T电流】、【分区所上行F电流】、【分区所下行T电流】、【分区所下行F电流】，满足以下映射关系：
        //  变电所上行T电流：TR故障
        //  变电所上行F电流：FR故障
        //  变电所下行T电流：TR故障
        //  变电所下行F电流：FR故障
        //  AT所上行T电流：TR故障
        //  AT所上行F电流：FR故障
        //  AT所下行T电流：TR故障
        //  分区所上行T电流：FR故障
        //  分区所上行F电流：TR故障
        //  分区所下行T电流：FR故障
        //  分区所下行F电流：TR故障
        FixedValue TF短路故障判别 = fixedValueService.getOneByDeviceIdAndFixedValueName(deviceId, FixedValueConstants.TF短路故障判别);
        List<DeviceFailureConstants.DIMENSION> params = new ArrayList<>();
        params.add(DeviceFailureConstants.DIMENSION.变电所吸上电流);
        params.add(DeviceFailureConstants.DIMENSION.AT所吸上电流);
        params.add(DeviceFailureConstants.DIMENSION.分区所吸上电流);

        List<DeviceFailureConstants.DIMENSION> otherParams = new ArrayList<>();
        otherParams.add(DeviceFailureConstants.DIMENSION.变电所上行T电流);
        otherParams.add(DeviceFailureConstants.DIMENSION.变电所上行F电流);
        otherParams.add(DeviceFailureConstants.DIMENSION.变电所下行T电流);
        otherParams.add(DeviceFailureConstants.DIMENSION.变电所下行F电流);
        otherParams.add(DeviceFailureConstants.DIMENSION.AT所上行T电流);
        otherParams.add(DeviceFailureConstants.DIMENSION.AT所上行F电流);
        otherParams.add(DeviceFailureConstants.DIMENSION.AT所下行T电流);
        otherParams.add(DeviceFailureConstants.DIMENSION.AT所下行F电流);
        otherParams.add(DeviceFailureConstants.DIMENSION.分区所上行T电流);
        otherParams.add(DeviceFailureConstants.DIMENSION.分区所上行F电流);
        otherParams.add(DeviceFailureConstants.DIMENSION.分区所下行T电流);
        otherParams.add(DeviceFailureConstants.DIMENSION.分区所下行F电流);
        List<DeviceDataDto> deviceDataGroups = this.fixedValueService.findByFixedValueVersionIdAndDimension(deviceId,params);
        DeviceDataDto maxDevice = deviceDataGroups
                .stream()
                .max(Comparator.comparing(DeviceDataDto::getDeviceValue))
                .get();
        List<DeviceDataDto> otherDeviceDataGroups = this.fixedValueService.findByFixedValueVersionIdAndDimension(deviceId,
                otherParams);
        DeviceDataDto otherMaxDevice = otherDeviceDataGroups
                .stream()
                .max(CalcServiceImpl::compare)
                .get();
        if (Double.parseDouble(maxDevice.getDeviceValue().replaceAll("[a-zA-Z]*", ""))-
                Double.parseDouble(TF短路故障判别.getSummonValue())<0) {
            temp.put("故障类型", "FR故障");
            keyValueResults.add(new KeyValueResult("故障类型", "FR故障"));
        }else {
            temp.put("故障类型",otherMaxDevice.getDesc());
            keyValueResults.add(new KeyValueResult("故障类型", otherMaxDevice.getDesc()));
        }
        // 故障行别
        keyValueResults.add(new KeyValueResult("故障行别", otherMaxDevice.getDirection()));
        // 变电所吸上电流(A)
        List<DeviceDataDto> bdDeviceData = this.fixedValueService.findByFixedValueVersionIdAndDimension(deviceId,
                Collections.singletonList(DeviceFailureConstants.DIMENSION.变电所吸上电流));
        keyValueResults.add(new KeyValueResult("变电所吸上电路（A）", Double.parseDouble(bdDeviceData.get(0).getDeviceValue())));
        // AT所吸上电流(A)
        List<DeviceDataDto> atDeviceData = this.fixedValueService.findByFixedValueVersionIdAndDimension(deviceId,
               Collections.singletonList(DeviceFailureConstants.DIMENSION.AT所吸上电流));
        keyValueResults.add(new KeyValueResult("AT所吸上电流（A）", Double.parseDouble(atDeviceData.get(0).getDeviceValue())));
        // 分区所吸上电流(A)
        List<DeviceDataDto> fqDeviceData = this.fixedValueService.findByFixedValueVersionIdAndDimension(deviceId,
                Collections.singletonList(DeviceFailureConstants.DIMENSION.分区所吸上电流));
        keyValueResults.add(new KeyValueResult("分区所吸上电路（A）", Double.parseDouble(fqDeviceData.get(0).getDeviceValue())));
        // 变电所横联电流Ihl0(A)
//        MathUtils.toRealVector()
        // 变电所电流向量
        KeyValueResult keyValueResult1 = 变电所横联电流Ihl0(deviceId, keyValueResults);
        // AT所横联电流Ihl1(A)
        KeyValueResult keyValueResult2 = AT所横联电流Ihl1(deviceId, keyValueResults);
        // 分区所横联电流Ihl2(A)
        KeyValueResult keyValueResult3 = 分区所横联电流Ihl2(deviceId, keyValueResults);
        // 故障区段
        if (Double.parseDouble(keyValueResult3.getValue().toString())<Double.parseDouble(keyValueResult2.getValue().toString())
        &&Double.parseDouble(keyValueResult3.getValue().toString())<Double.parseDouble(keyValueResult1.getValue().toString())) {
            keyValueResults.add(new KeyValueResult("故障区段", "第一AT区段"));
            temp.put("故障区段", "第一AT区段");
        }else {
            keyValueResults.add(new KeyValueResult("故障区段", "第二AT区段"));
            temp.put("故障区段", "第二AT区段");
        }
        // 吸上电流比法T相距离（km）
        if (null!=temp.get("故障类型")&&StringUtils.equals(temp.get("故障类型").toString(), "TR故障")) {
            if (StringUtils.equals(temp.get("故障区段").toString(), "第一AT区段")) {
                // 吸上电流比法（第一AT段故障）T相
                /**
                 * 1、 第一AT段长度/(100-变电所Q值-AT所QT1)*(100*(AT所吸上电流（子站1)/(AT所吸上电流（子站1)+变电所吸上电流))-变电所Q值)
                 *    =B7/(100-B9-B10)*(100*(D7/(D7+D2))-B9)
                 */
                //1、第一AT段长度（定值表：起点公里标）
                FixedValue 第一AT段长度 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.区间1长度);
                Double b7 = Double.parseDouble(第一AT段长度.getSummonValue());
                //2、变电所Q值
                FixedValue 变电所的QT值 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.变电所的QT值);
                Double b9 = Double.parseDouble(变电所的QT值.getSummonValue());
                //3、AT所QT1
                FixedValue AT1的QT1值 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.AT1的QT1值);
                Double b10 = Double.parseDouble(AT1的QT1值.getSummonValue());
                //4、AT所吸上电流（子站1)
                List<DeviceDataDto> xsdlatDeviceData = this.fixedValueService.findByFixedValueVersionIdAndDimension(deviceId,
                        Collections.singletonList(DeviceFailureConstants.DIMENSION.AT所吸上电流));
                String xsdlatDeviceDataValue = xsdlatDeviceData.get(0).getDeviceValue();
                Double d7 = Double.parseDouble(xsdlatDeviceDataValue);
                //5、变电所吸上电流
                List<DeviceDataDto> xsdlbdDeviceData = this.fixedValueService.findByFixedValueVersionIdAndDimension(deviceId,
                        Collections.singletonList(DeviceFailureConstants.DIMENSION.变电所吸上电流));
                String xsdlbdDeviceDataValue = xsdlbdDeviceData.get(0).getDeviceValue();
                Double d2 = Double.parseDouble(xsdlbdDeviceDataValue);
                Double xsdlResult = b7 /(100-b9-b10) *(100*d7 /(d7 +d2)-b9);
                keyValueResults.add(new KeyValueResult("吸上电流比法T相距离（km）", xsdlResult));
            }else {
                // 吸上电流比法（第二AT段故障）T相
                /**
                 *  第一AT段长度+第二AT段长度/(100-AT所QT2-分区所QT1)*(100*分区所吸上电流(子站2)/(分区所吸上电流(子站2)+AT所吸上电流)-AT所QT2)-2*AT所供电线、电缆长度
                 */
                // 第一AT段长度
                FixedValue 第一AT段长度 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.区间1长度);
                // 第二AT段长度
                FixedValue 区间4长度 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.区间4长度);
                // AT所QT2
                FixedValue AT2的QT2值 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.AT2的QT2值);
                // 分区所QT1
                FixedValue AT3的QT1值 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.AT3的QT1值);
                // 分区所吸上电流(子站2)
                List<DeviceDataDto> fqsDeviceData = this.fixedValueService.findByFixedValueVersionIdAndDimension(deviceId,
                        Collections.singletonList(DeviceFailureConstants.DIMENSION.分区所吸上电流));
                String xsdlatDeviceDataValue = fqsDeviceData.get(0).getDeviceValue();
                // AT所吸上电流（子站1）
                List<DeviceDataDto> atsDeviceData = this.fixedValueService.findByFixedValueVersionIdAndDimension(deviceId,
                        Collections.singletonList(DeviceFailureConstants.DIMENSION.AT所吸上电流));
                String atsDeviceDataValue = atsDeviceData.get(0).getDeviceValue();
                // AT所供电线、电缆长度
                FixedValue AT1供电线长度 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.AT1供电线长度);
                Double xsdlResult = Double.parseDouble(第一AT段长度.getSummonValue())
                        +Double.parseDouble(区间4长度.getSummonValue())/(100-Double.parseDouble(AT2的QT2值.getSummonValue())-Double.parseDouble(AT3的QT1值.getSummonValue()))
                        *(100*Double.parseDouble(xsdlatDeviceDataValue)/(Double.parseDouble(xsdlatDeviceDataValue)+Double.parseDouble(atsDeviceDataValue))-Double.parseDouble(AT2的QT2值.getSummonValue()))
                        -2*Double.parseDouble(AT1供电线长度.getSummonValue());
                keyValueResults.add(new KeyValueResult("吸上电流比法T相距离（km）", xsdlResult));
            }
        }else {
            keyValueResults.add(new KeyValueResult("吸上电流比法T相距离（km）", null));
        }
        // 吸上电流比法F相距离（km）
        if (null!=temp.get("故障类型")&&StringUtils.equals(temp.get("故障类型").toString(), "FR故障")) {
            if (StringUtils.equals(temp.get("故障区段").toString(), "第一AT区段")) {
                // 吸上电流比法（第一AT段故障）F相
                /**
                 * 第一AT段长度/(100-变电所Q值-AT所QF1)*(100*AT所吸上电流（子站1）/(AT所吸上电流（子站1）+变电所吸上电流)-变电所Q值)
                 */
                // 第一AT段长度
                FixedValue 第一AT段长度 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.区间1长度);
                //2、变电所Q值
                FixedValue 变电所的QT值 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.变电所的QT值);
                //3、AT所QF1
                FixedValue AT1的QF1值 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.AT1的QF1值);
                //4、 AT所吸上电流（子站1）
                List<DeviceDataDto> atsDeviceData = this.fixedValueService.findByFixedValueVersionIdAndDimension(deviceId,
                        Collections.singletonList(DeviceFailureConstants.DIMENSION.AT所吸上电流));
                String atsDeviceDataValue = atsDeviceData.get(0).getDeviceValue();
                //5、变电所吸上电流
                List<DeviceDataDto> xsdlbdDeviceData = this.fixedValueService.findByFixedValueVersionIdAndDimension(deviceId,
                        Collections.singletonList(DeviceFailureConstants.DIMENSION.变电所吸上电流));
                String xsdlbdDeviceDataValue = xsdlbdDeviceData.get(0).getDeviceValue();

                Double result = Double.parseDouble(第一AT段长度.getSummonValue())/(100-Double.parseDouble(变电所的QT值.getSummonValue())-Double.parseDouble(AT1的QF1值.getSummonValue()))
                        *(100*Double.parseDouble(atsDeviceDataValue)/(Double.parseDouble(atsDeviceDataValue)+Double.parseDouble(xsdlbdDeviceDataValue))-Double.parseDouble(变电所的QT值.getSummonValue()));
                keyValueResults.add(new KeyValueResult("吸上电流比法F相距离（km）", result));
            }else {
                // 吸上电流比法（第二AT段故障）F
                /**
                 *  第一AT段长度+(第二AT段长度/(100-AT所QF2-分区所QF1)*(100*分区所吸上电流（子站2）/(分区所吸上电流（子站2）+AT所吸上电流（子站1）)-AT所QF2))-2*AT所供电线、电缆长度
                 */
                // 第一AT段长度
                FixedValue 第一AT段长度 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.区间1长度);
                // 第二AT段长度
                FixedValue 第二AT段长度 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.区间2长度);
                // AT所QF2
                FixedValue AT2的QT2值 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.AT2的QT2值);
                // 分区所QF1
                FixedValue AT1的QF1值 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.AT1的QF1值);
                // 分区所吸上电流（子站2）
                List<DeviceDataDto> fqsDeviceData = this.fixedValueService.findByFixedValueVersionIdAndDimension(deviceId,
                        Collections.singletonList(DeviceFailureConstants.DIMENSION.分区所吸上电流));
                String xsdlatDeviceDataValue = fqsDeviceData.get(0).getDeviceValue();
                // AT所吸上电流（子站1）
                List<DeviceDataDto> atsDeviceData = this.fixedValueService.findByFixedValueVersionIdAndDimension(deviceId,
                        Collections.singletonList(DeviceFailureConstants.DIMENSION.AT所吸上电流));
                String atsDeviceDataValue = atsDeviceData.get(0).getDeviceValue();
                // AT所供电线、电缆长度
                FixedValue AT1供电线长度 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.AT1供电线长度);
                Double result = Double.parseDouble(第一AT段长度.getSummonValue())
                        +(
                                Double.parseDouble(第二AT段长度.getSummonValue())
                                        /
                                        (100-Double.parseDouble(AT2的QT2值.getSummonValue())-Double.parseDouble(AT1的QF1值.getSummonValue()))
                                        *
                                        (100*Double.parseDouble(xsdlatDeviceDataValue)
                                                /
                                                (Double.parseDouble(xsdlatDeviceDataValue)
                                                        +Double.parseDouble(atsDeviceDataValue))
                                                -Double.parseDouble(AT2的QT2值.getSummonValue()))
                        -2*Double.parseDouble(AT1供电线长度.getSummonValue())
                );
                keyValueResults.add(new KeyValueResult("吸上电流比法F相距离（km）", result));
            }
        }else {
            keyValueResults.add(new KeyValueResult("吸上电流比法F相距离（km）", null));
        }
        // 横联电流比法距离（km）
        if (StringUtils.equals(temp.get("故障区段").toString(), "第一AT区段")) {
            // 横联电流比法（第一AT段故障）
            /**
             * 第一AT段长度*AT所横联电流Ihl2/(AT所横联电流Ihl2+变电所横联电流Ihl0)
             */
            FixedValue 第一AT段长度 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.区间1长度);
            KeyValueResult keyValueResult = AT所横联电流Ihl1(deviceId, null);
            KeyValueResult bdsKeyValueResult = 变电所横联电流Ihl0(deviceId, null);
            Double result = Double.parseDouble(第一AT段长度.getSummonValue())*Double.parseDouble(keyValueResult.getValue().toString())
                    /(Double.parseDouble(keyValueResult.getValue().toString())+Double.parseDouble(bdsKeyValueResult.getValue().toString()));
            keyValueResults.add(new KeyValueResult("横联电流比法距离（km）", result));
            temp.put("横联电流比法距离（km）", result);
        }else {
            // 横联电流比法（第二AT段故障）
            /**
             * 第一AT段长度-AT所供电线、电缆长度*2+第二AT段长度*分区所横联电流Ihl2/AT所横联电流Ihl1+分区所横联电流Ihl2
             */
            FixedValue 第一AT段长度 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.区间1长度);
            // 第二AT段长度
            FixedValue 第二AT段长度 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.区间2长度);
            KeyValueResult keyValueResult = AT所横联电流Ihl1(deviceId, null);
            KeyValueResult fqsKeyValueResult = 分区所横联电流Ihl2(deviceId, null);
            // AT所供电线、电缆长度
            FixedValue AT1供电线长度 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.AT1供电线长度);
            Double result = Double.parseDouble(第一AT段长度.getSummonValue())-Double.parseDouble(AT1供电线长度.getSummonValue())*2
                    +Double.parseDouble(第二AT段长度.getSummonValue())*Double.parseDouble(fqsKeyValueResult.getValue().toString())
                    /Double.parseDouble(keyValueResult.getValue().toString())+Double.parseDouble(fqsKeyValueResult.getValue().toString());
            keyValueResults.add(new KeyValueResult("横联电流比法距离（km）", result));
            temp.put("横联电流比法距离（km）", result);
        }
        // 上下行电流比法距离（km）
        if (StringUtils.equals(temp.get("故障区段").toString(), "第一AT区段")) {
            // 上下行电流比法（第一AT段）
            /**
             * 2*第一AT段长度*MIN(AT所横联电流Ihl1,分区所横联电流Ihl2)/(AT所横联电流Ihl1+分区所横联电流Ihl2)
             */
            FixedValue 第一AT段长度 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.区间1长度);
            KeyValueResult keyValueResult = AT所横联电流Ihl1(deviceId, null);
            KeyValueResult fqsKeyValueResult = 分区所横联电流Ihl2(deviceId, null);
            Double result = 2*Double.parseDouble(第一AT段长度.getSummonValue())
                    *Math.min(Double.parseDouble(keyValueResult.getValue().toString()),
                    Double.parseDouble(fqsKeyValueResult.getValue().toString()))/(Double.parseDouble(keyValueResult.getValue().toString())+
            Double.parseDouble(fqsKeyValueResult.getValue().toString()));
            keyValueResults.add(new KeyValueResult("上下行电流比法距离（km）", result));
            temp.put("上下行电流比法距离（km）", result);
        }else {
            // 上下行电流比法（第二AT段）
            /**
             * 第一AT段长度+2*第二AT段长度*min(上行, 下行)/(上行+下行)-2*AT所供电线、电缆长度
             */
            FixedValue 第一AT段长度 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.区间1长度);
            FixedValue 第二AT段长度 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.区间2长度);
            // AT所供电线、电缆长度
            FixedValue AT1供电线长度 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.AT1供电线长度);
            Double result = Double.parseDouble(第一AT段长度.getSummonValue())
                    +2*Double.parseDouble(第二AT段长度.getSummonValue())
                    *Math.min(上行(deviceId),下行(deviceId))/(上行(deviceId)+下行(deviceId))
                    -2*Double.parseDouble(AT1供电线长度.getSummonValue());
            keyValueResults.add(new KeyValueResult("上下行电流比法距离（km）", result));
            temp.put("上下行电流比法距离（km）", result);
        }
        // 推荐故障距离（km）
        keyValueResults.add(new KeyValueResult("推荐故障距离（km）", temp.get("横联电流比法距离（km）")));
        // 故障点公里标（km）
        /**
         * 变电所上网点公里标+(temp.get("上下行电流比法距离（km）")-)
         */
        FixedValue 起点公里标 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.起点公里标);
        FixedValue 变电所供电线长度 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.变电所供电线长度);
        Double sxdlbf = (Double) temp.get("上下行电流比法距离（km）");
        FixedValueVersion fixedValueVersionInfo = fixedValueVersionService.getById(起点公里标.getFixedValueVersionId());
        Double 故障点公里标 = Double.parseDouble(起点公里标.getSummonValue())
                +(sxdlbf-Double.parseDouble(变电所供电线长度.getSummonValue()))
                *Double.parseDouble(fixedValueVersionInfo.getDirection());
        keyValueResults.add(new KeyValueResult("故障点公里标（km）", 故障点公里标));
        return keyValueResults;
    }

    private Double 下行(Long deviceId) {
        List<DeviceFailureConstants.DIMENSION> bdsdlxl = new ArrayList<>();
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.变电所下行T电流);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.变电所下行T电流角度);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.变电所下行F电流);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.变电所下行F电流角度);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.AT所下行T电流);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.AT所下行T电流角度);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.AT所下行F电流);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.AT所下行F电流角度);
        List<DeviceDataDto> bdsdlxlDeviceData = this.fixedValueService.findByFixedValueVersionIdAndDimension(deviceId,
                bdsdlxl);
        RealVector number1 = MathUtils.toRealVector(Double.parseDouble(bdsdlxlDeviceData.get(0).getDeviceValue()),
                Double.parseDouble(bdsdlxlDeviceData.get(1).getDeviceValue()));
        RealVector number2 = MathUtils.toRealVector(Double.parseDouble(bdsdlxlDeviceData.get(2).getDeviceValue()),
                Double.parseDouble(bdsdlxlDeviceData.get(3).getDeviceValue()));
        RealVector number3 = MathUtils.toRealVector(Double.parseDouble(bdsdlxlDeviceData.get(4).getDeviceValue()),
                Double.parseDouble(bdsdlxlDeviceData.get(5).getDeviceValue()));
        RealVector number4 = MathUtils.toRealVector(Double.parseDouble(bdsdlxlDeviceData.get(6).getDeviceValue()),
                Double.parseDouble(bdsdlxlDeviceData.get(7).getDeviceValue()));
        RealVector bdsResult = number1.subtract(number2).add(number3).subtract(number4);
        return bdsResult.getNorm();
    }

    private Double 上行(Long deviceId) {
        List<DeviceFailureConstants.DIMENSION> bdsdlxl = new ArrayList<>();
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.变电所上行T电流);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.变电所上行T电流角度);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.变电所上行F电流);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.变电所上行F电流角度);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.AT所上行T电流);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.AT所上行T电流角度);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.AT所上行F电流);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.AT所上行F电流角度);
        List<DeviceDataDto> bdsdlxlDeviceData = this.fixedValueService.findByFixedValueVersionIdAndDimension(deviceId,
                bdsdlxl);
        RealVector number1 = MathUtils.toRealVector(Double.parseDouble(bdsdlxlDeviceData.get(0).getDeviceValue()),
                Double.parseDouble(bdsdlxlDeviceData.get(1).getDeviceValue()));
        RealVector number2 = MathUtils.toRealVector(Double.parseDouble(bdsdlxlDeviceData.get(2).getDeviceValue()),
                Double.parseDouble(bdsdlxlDeviceData.get(3).getDeviceValue()));
        RealVector number3 = MathUtils.toRealVector(Double.parseDouble(bdsdlxlDeviceData.get(4).getDeviceValue()),
                Double.parseDouble(bdsdlxlDeviceData.get(5).getDeviceValue()));
        RealVector number4 = MathUtils.toRealVector(Double.parseDouble(bdsdlxlDeviceData.get(6).getDeviceValue()),
                Double.parseDouble(bdsdlxlDeviceData.get(7).getDeviceValue()));
        RealVector bdsResult = number1.subtract(number2).add(number3).subtract(number4);
        return bdsResult.getNorm();
    }

    private KeyValueResult 分区所横联电流Ihl2(Long deviceId, List<KeyValueResult> keyValueResults) {
        List<DeviceFailureConstants.DIMENSION> bdsdlxl = new ArrayList<>();
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.分区所上行T电流);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.分区所上行T电流角度);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.分区所上行F电流);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.分区所上行F电流角度);
        List<DeviceDataDto> bdsdlxlDeviceData = this.fixedValueService.findByFixedValueVersionIdAndDimension(deviceId,
                bdsdlxl);
        RealVector number1 = MathUtils.toRealVector(Double.parseDouble(bdsdlxlDeviceData.get(0).getDeviceValue()),
                Double.parseDouble(bdsdlxlDeviceData.get(1).getDeviceValue()));
        RealVector number2 = MathUtils.toRealVector(Double.parseDouble(bdsdlxlDeviceData.get(2).getDeviceValue()),
                Double.parseDouble(bdsdlxlDeviceData.get(3).getDeviceValue()));
        RealVector bdsResult = number1.subtract(number2);
        KeyValueResult keyValueResult = new KeyValueResult("分区所横联电流Ihl2(A)", bdsResult.getNorm());
        if (null != keyValueResults) {
            keyValueResults.add(keyValueResult);
        }
        return keyValueResult;
    }

    private KeyValueResult 变电所横联电流Ihl0(Long deviceId, List<KeyValueResult> keyValueResults) {
        List<DeviceFailureConstants.DIMENSION> bdsdlxl = new ArrayList<>();
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.变电所上行T电流);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.变电所上行T电流角度);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.变电所上行F电流);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.变电所上行F电流角度);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.变电所下行T电流);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.变电所下行T电流角度);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.变电所下行F电流);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.变电所下行F电流角度);
        List<DeviceDataDto> bdsdlxlDeviceData = this.fixedValueService.findByFixedValueVersionIdAndDimension(deviceId,
                bdsdlxl);
        RealVector number1 = MathUtils.toRealVector(Double.parseDouble(bdsdlxlDeviceData.get(0).getDeviceValue()),
                Double.parseDouble(bdsdlxlDeviceData.get(1).getDeviceValue()));
        RealVector number2 = MathUtils.toRealVector(Double.parseDouble(bdsdlxlDeviceData.get(2).getDeviceValue()),
                Double.parseDouble(bdsdlxlDeviceData.get(3).getDeviceValue()));
        RealVector number3 = MathUtils.toRealVector(Double.parseDouble(bdsdlxlDeviceData.get(4).getDeviceValue()),
                Double.parseDouble(bdsdlxlDeviceData.get(5).getDeviceValue()));
        RealVector number4 = MathUtils.toRealVector(Double.parseDouble(bdsdlxlDeviceData.get(6).getDeviceValue()),
                Double.parseDouble(bdsdlxlDeviceData.get(7).getDeviceValue()));
        RealVector bdsResult = number1.subtract(number2).subtract(number3).add(number4);
        KeyValueResult keyValueResult = new KeyValueResult("变电所横联电流Ihl0(A)", bdsResult.getNorm() / 2);
        if (null != keyValueResults) {
            keyValueResults.add(keyValueResult);
        }
        return keyValueResult;
    }

    private KeyValueResult AT所横联电流Ihl1(Long deviceId, List<KeyValueResult> keyValueResults) {
        List<DeviceFailureConstants.DIMENSION> bdsdlxl = new ArrayList<>();
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.AT所上行T电流);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.AT所上行T电流角度);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.AT所上行F电流);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.AT所上行F电流角度);
        List<DeviceDataDto> bdsdlxlDeviceData = this.fixedValueService.findByFixedValueVersionIdAndDimension(deviceId,
                bdsdlxl);
        RealVector number1 = MathUtils.toRealVector(Double.parseDouble(bdsdlxlDeviceData.get(0).getDeviceValue()),
                Double.parseDouble(bdsdlxlDeviceData.get(1).getDeviceValue()));
        RealVector number2 = MathUtils.toRealVector(Double.parseDouble(bdsdlxlDeviceData.get(2).getDeviceValue()),
                Double.parseDouble(bdsdlxlDeviceData.get(3).getDeviceValue()));
        RealVector bdsResult = number1.subtract(number2);
        KeyValueResult keyValueResult = new KeyValueResult("AT所横联电流Ihl1(A)", bdsResult.getNorm());
        if (null != keyValueResults) {
            keyValueResults.add(keyValueResult);
        }
        return keyValueResult;
    }
}
