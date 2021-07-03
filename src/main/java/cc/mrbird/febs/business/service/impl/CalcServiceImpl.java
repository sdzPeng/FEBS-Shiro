package cc.mrbird.febs.business.service.impl;

import cc.mrbird.febs.business.constants.DeviceFailureConstants;
import cc.mrbird.febs.business.constants.FixedValueConstants;
import cc.mrbird.febs.business.dto.CurrentValue;
import cc.mrbird.febs.business.dto.DeviceDataDto;
import cc.mrbird.febs.business.dto.KeyValueResult;
import cc.mrbird.febs.business.entity.FixedValue;
import cc.mrbird.febs.business.entity.FixedValueVersion;
import cc.mrbird.febs.business.service.ICalcService;
import cc.mrbird.febs.business.service.IFixedValueService;
import cc.mrbird.febs.business.service.IFixedValueVersionService;
import cc.mrbird.febs.business.util.MathUtils;
import cc.mrbird.febs.common.exception.ValidaException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.linear.RealVector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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

    public static ThreadLocal<Map<String, Object>> THREAD_LOCAL = new ThreadLocal<>();




    private static int compare(DeviceDataDto o1, DeviceDataDto o2) {
        return Double.parseDouble(o1.getDeviceValue()) > Double.parseDouble(o2.getDeviceValue()) ? 1 : -1;
    }

    @Override
    public List<KeyValueResult> analysisResult(Long deviceId) throws ValidaException {
        Map<String, Object> paramsMap = new HashMap<>();
        THREAD_LOCAL.set(paramsMap);
        List<KeyValueResult> keyValueResults = new ArrayList<>();
        // 故障类型
        // 规则：
        // TF故障：【变电所吸上电流】、【AT所吸上电流】、【分区所吸上电流】最小值小于【定值表】中【TF短路故障判别】
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
        extracted(deviceId, keyValueResults);
        // 变电所吸上电流(A)
        List<DeviceDataDto> bdDeviceData = this.fixedValueService.findByFixedValueVersionIdAndDimension(deviceId,
                Collections.singletonList(DeviceFailureConstants.DIMENSION.变电所吸上电流));
        keyValueResults.add(new KeyValueResult("变电所吸上电流（A）", Double.parseDouble(bdDeviceData.get(0).getDeviceValue())));
        // AT所吸上电流(A)
        List<DeviceDataDto> atDeviceData = this.fixedValueService.findByFixedValueVersionIdAndDimension(deviceId,
               Collections.singletonList(DeviceFailureConstants.DIMENSION.AT所吸上电流));
        keyValueResults.add(new KeyValueResult("AT所吸上电流（A）", Double.parseDouble(atDeviceData.get(0).getDeviceValue())));
        // 分区所吸上电流(A)
        List<DeviceDataDto> fqDeviceData = this.fixedValueService.findByFixedValueVersionIdAndDimension(deviceId,
                Collections.singletonList(DeviceFailureConstants.DIMENSION.分区所吸上电流));
        keyValueResults.add(new KeyValueResult("分区所吸上电流（A）", Double.parseDouble(fqDeviceData.get(0).getDeviceValue())));

        // 吸上电流比法T相距离（km）
        if (null!=THREAD_LOCAL.get().get("故障类型")&&StringUtils.equals(THREAD_LOCAL.get().get("故障类型").toString(), "TR故障")) {
            if (StringUtils.equals(THREAD_LOCAL.get().get("故障区段").toString(), "第一AT区段")) {
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
        if (null!=THREAD_LOCAL.get().get("故障类型")&&StringUtils.equals(THREAD_LOCAL.get().get("故障类型").toString(), "FR故障")) {
            if (StringUtils.equals(THREAD_LOCAL.get().get("故障区段").toString(), "第一AT区段")) {
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
        if (StringUtils.equals(THREAD_LOCAL.get().get("故障区段").toString(), "第一AT区段")) {
            // 横联电流比法（第一AT段故障）
            /**
             * 第一AT段长度*AT所横联电流Ihl2/(AT所横联电流Ihl2+变电所横联电流Ihl0)
             */
            FixedValue 第一AT段长度 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.区间1长度);
            KeyValueResult keyValueResult = AT所横联电流Ihl1(deviceId);
            KeyValueResult bdsKeyValueResult = 变电所横联电流Ihl0(deviceId);
            Double result = Double.parseDouble(第一AT段长度.getSummonValue())*Double.parseDouble(keyValueResult.getValue().toString())
                    /(Double.parseDouble(keyValueResult.getValue().toString())+Double.parseDouble(bdsKeyValueResult.getValue().toString()));
            keyValueResults.add(new KeyValueResult("横联电流比法距离（km）", result));
            THREAD_LOCAL.get().put("横联电流比法距离（km）", result);
        }else {
            // 横联电流比法（第二AT段故障）
            /**
             * 第一AT段长度-AT所供电线、电缆长度*2+第二AT段长度*分区所横联电流Ihl2/AT所横联电流Ihl1+分区所横联电流Ihl2
             */
            FixedValue 第一AT段长度 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.区间1长度);
            // 第二AT段长度
            FixedValue 第二AT段长度 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.区间2长度);
            KeyValueResult keyValueResult = AT所横联电流Ihl1(deviceId);
            KeyValueResult fqsKeyValueResult = 分区所横联电流Ihl2(deviceId);
            // AT所供电线、电缆长度
            FixedValue AT1供电线长度 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.AT1供电线长度);
            Double result = Double.parseDouble(第一AT段长度.getSummonValue())-Double.parseDouble(AT1供电线长度.getSummonValue())*2
                    +Double.parseDouble(第二AT段长度.getSummonValue())*Double.parseDouble(fqsKeyValueResult.getValue().toString())
                    /Double.parseDouble(keyValueResult.getValue().toString())+Double.parseDouble(fqsKeyValueResult.getValue().toString());
            keyValueResults.add(new KeyValueResult("横联电流比法距离（km）", result));
            THREAD_LOCAL.get().put("横联电流比法距离（km）", result);
        }
        // 上下行电流比法距离（km）
        if (StringUtils.equals(THREAD_LOCAL.get().get("故障区段").toString(), "第一AT区段")) {
            // 上下行电流比法（第一AT段）
            /**
             * 2*第一AT段长度*MIN(AT所横联电流Ihl1,分区所横联电流Ihl2)/(AT所横联电流Ihl1+分区所横联电流Ihl2)
             *
             */
            FixedValue 第一AT段长度 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.区间1长度);
            KeyValueResult keyValueResult = 变电所上行电流Ihl0(deviceId, null);
            KeyValueResult fqsKeyValueResult = 变电所下行电流Ihl0(deviceId, null);
            Double result = 2*Double.parseDouble(第一AT段长度.getSummonValue())
                    *Math.min(Double.parseDouble(keyValueResult.getValue().toString()),
                    Double.parseDouble(fqsKeyValueResult.getValue().toString()))/(Double.parseDouble(keyValueResult.getValue().toString())+
            Double.parseDouble(fqsKeyValueResult.getValue().toString()));
            keyValueResults.add(new KeyValueResult("上下行电流比法距离（km）", result));
            THREAD_LOCAL.get().put("上下行电流比法距离（km）", result);
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
            THREAD_LOCAL.get().put("上下行电流比法距离（km）", result);
        }
        // 推荐故障距离（km）
        keyValueResults.add(new KeyValueResult("推荐故障距离（km）", THREAD_LOCAL.get().get("横联电流比法距离（km）")));
        // 故障点公里标（km）
        /**
         * 变电所上网点公里标+(temp.get("上下行电流比法距离（km）")-)
         */
        FixedValue 起点公里标 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.起点公里标);
        FixedValue 变电所供电线长度 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.变电所供电线长度);
        Double sxdlbf = (Double) THREAD_LOCAL.get().get("上下行电流比法距离（km）");
        FixedValueVersion fixedValueVersionInfo = fixedValueVersionService.getById(起点公里标.getFixedValueVersionId());
        Double 故障点公里标 = Double.parseDouble(起点公里标.getSummonValue())
                +(sxdlbf-Double.parseDouble(变电所供电线长度.getSummonValue()))
                *Double.parseDouble(fixedValueVersionInfo.getDirection());
        keyValueResults.add(new KeyValueResult("故障点公里标（km）", 故障点公里标));
        THREAD_LOCAL.remove();
        return keyValueResults;
    }

    private void extracted(Long deviceId, List<KeyValueResult> keyValueResults) throws ValidaException {
        List<DeviceFailureConstants.DIMENSION> params = new ArrayList<>();
        params.add(DeviceFailureConstants.DIMENSION.变电所吸上电流);
        params.add(DeviceFailureConstants.DIMENSION.AT所吸上电流);
        params.add(DeviceFailureConstants.DIMENSION.分区所吸上电流);
        List<DeviceDataDto> deviceDataGroups = this.fixedValueService.findByFixedValueVersionIdAndDimension(deviceId,params);
        DeviceDataDto minDevice = deviceDataGroups
                .stream()
                .min(Comparator.comparing(DeviceDataDto::getDeviceValue))
                .get();
        FixedValue TF短路故障判别 = fixedValueService.getOneByDeviceIdAndFixedValueName(deviceId, FixedValueConstants.TF短路故障判别);
        FixedValue 吸上电流流互变比 = fixedValueService.getOneByDeviceIdAndFixedValueName(deviceId, FixedValueConstants.吸上电流流互变比);
        // 变电所横联电流Ihl0(A)
//        MathUtils.toRealVector()
        // 变电所电流向量
        KeyValueResult keyValueResult1 = 变电所横联电流Ihl0(deviceId);
        // AT所横联电流Ihl1(A)
        KeyValueResult keyValueResult2 = AT所横联电流Ihl1(deviceId);
        // 分区所横联电流Ihl2(A)
        KeyValueResult keyValueResult3 = 分区所横联电流Ihl2(deviceId);
        DeviceDataDto otherMaxDevice;
        // 故障区段
        if (Double.parseDouble(keyValueResult3.getValue().toString())<Double.parseDouble(keyValueResult2.getValue().toString())
                &&Double.parseDouble(keyValueResult3.getValue().toString())<Double.parseDouble(keyValueResult1.getValue().toString())) {
            keyValueResults.add(new KeyValueResult("故障区段", "第一AT区段"));
            if (null == THREAD_LOCAL.get()) {
                Map<String, Object> result = new HashMap<>();
                result.put("故障区段", "第一AT区段");
                THREAD_LOCAL.set(result);
            }else {
                THREAD_LOCAL.get().put("故障区段", "第一AT区段");
            }
            List<DeviceFailureConstants.DIMENSION> otherParams = new ArrayList<>();
            otherParams.add(DeviceFailureConstants.DIMENSION.变电所上行T电流);
            otherParams.add(DeviceFailureConstants.DIMENSION.变电所上行F电流);
            otherParams.add(DeviceFailureConstants.DIMENSION.变电所下行T电流);
            otherParams.add(DeviceFailureConstants.DIMENSION.变电所下行F电流);
            otherParams.add(DeviceFailureConstants.DIMENSION.AT所上行T电流);
            otherParams.add(DeviceFailureConstants.DIMENSION.AT所上行F电流);
            otherParams.add(DeviceFailureConstants.DIMENSION.AT所下行T电流);
            otherParams.add(DeviceFailureConstants.DIMENSION.AT所下行F电流);
            List<DeviceDataDto> otherDeviceDataGroups = this.fixedValueService.findByFixedValueVersionIdAndDimension(deviceId,
                    otherParams);
            otherMaxDevice = otherDeviceDataGroups
                    .stream()
                    .max(CalcServiceImpl::compare)
                    .get();
            // 故障行别
            THREAD_LOCAL.get().put("故障行别", otherMaxDevice.getDirection());
            keyValueResults.add(new KeyValueResult("故障行别", otherMaxDevice.getDirection()));
        }else {
            List<DeviceFailureConstants.DIMENSION> otherParams = new ArrayList<>();
            otherParams.add(DeviceFailureConstants.DIMENSION.AT所上行T电流);
            otherParams.add(DeviceFailureConstants.DIMENSION.AT所上行F电流);
            otherParams.add(DeviceFailureConstants.DIMENSION.AT所下行T电流);
            otherParams.add(DeviceFailureConstants.DIMENSION.AT所下行F电流);
            otherParams.add(DeviceFailureConstants.DIMENSION.分区所上行T电流);
            otherParams.add(DeviceFailureConstants.DIMENSION.分区所上行F电流);
            otherParams.add(DeviceFailureConstants.DIMENSION.分区所下行T电流);
            otherParams.add(DeviceFailureConstants.DIMENSION.分区所下行F电流);
            List<DeviceDataDto> otherDeviceDataGroups = this.fixedValueService.findByFixedValueVersionIdAndDimension(deviceId,
                    otherParams);
            otherMaxDevice = otherDeviceDataGroups
                    .stream()
                    .max(CalcServiceImpl::compare)
                    .get();
            keyValueResults.add(new KeyValueResult("故障区段", "第二AT区段"));
            if (null == THREAD_LOCAL.get()) {
                Map<String, Object> result = new HashMap<>();
                result.put("故障区段", "第二AT区段");
                THREAD_LOCAL.set(result);
            }else {
                THREAD_LOCAL.get().put("故障区段", "第二AT区段");
            }
            // 故障行别
            THREAD_LOCAL.get().put("故障行别", otherMaxDevice.getDirection());
            keyValueResults.add(new KeyValueResult("故障行别", otherMaxDevice.getDirection()));
        }
        if (Double.parseDouble(minDevice.getDeviceValue().replaceAll("[a-zA-Z]*", ""))-
                Double.parseDouble(TF短路故障判别.getSummonValue())*Double.parseDouble(吸上电流流互变比.getSummonValue())<0) {
            THREAD_LOCAL.get().put("故障类型", "TF故障");
            keyValueResults.add(new KeyValueResult("故障类型", "TF故障"));
        }else {
            THREAD_LOCAL.get().put("故障类型",otherMaxDevice.getDesc());
            keyValueResults.add(new KeyValueResult("故障类型", otherMaxDevice.getDesc()));
        }
        keyValueResults.add(keyValueResult1);
        keyValueResults.add(keyValueResult2);
        keyValueResults.add(keyValueResult3);
    }

    @Override
    public List<KeyValueResult> calcData(Long deviceId) throws ValidaException {
        List<KeyValueResult> keyValueResults = new ArrayList<>();
        FixedValue 起点公里标 = fixedValueService.getOneByDeviceIdAndFixedValueName(deviceId, FixedValueConstants.DIMENSION.起点公里标.getName());
        Double tfValue = Double.parseDouble(起点公里标.getSummonValue());
        keyValueResults.add(new KeyValueResult("变电所上网点公里标", tfValue));
        FixedValue 变电所供电线长度 = fixedValueService.getOneByDeviceIdAndFixedValueName(deviceId, FixedValueConstants.DIMENSION.变电所供电线长度.getName());
        Double bdValue = Double.parseDouble(变电所供电线长度.getSummonValue());
        keyValueResults.add(new KeyValueResult("变电所供电线电缆长度", bdValue));
        // todo unknown
        keyValueResults.add(new KeyValueResult("AT所上网点公里标", null));
        FixedValue AT1供电线长度 = fixedValueService.getOneByDeviceIdAndFixedValueName(deviceId, FixedValueConstants.DIMENSION.AT1供电线长度.getName());
        Double at1Value = Double.parseDouble(AT1供电线长度.getSummonValue());
        keyValueResults.add(new KeyValueResult("AT所供电线电缆长度", at1Value));
        // todo
        keyValueResults.add(new KeyValueResult("分区所上网点公里标", null));
        FixedValue AT2供电线长度 = fixedValueService.getOneByDeviceIdAndFixedValueName(deviceId, FixedValueConstants.DIMENSION.AT2供电线长度.getName());
        Double at2Value = Double.parseDouble(AT2供电线长度.getSummonValue());
        keyValueResults.add(new KeyValueResult("分区所供电线电缆长度", at2Value));
        FixedValue 区间1长度 = fixedValueService.getOneByDeviceIdAndFixedValueName(deviceId, FixedValueConstants.DIMENSION.区间1长度.getName());
        Double qj1Value = Double.parseDouble(区间1长度.getSummonValue());
        keyValueResults.add(new KeyValueResult("第一AT段长度", qj1Value));
        FixedValue 区间2长度 = fixedValueService.getOneByDeviceIdAndFixedValueName(deviceId, FixedValueConstants.DIMENSION.区间2长度.getName());
        Double qj2Value = Double.parseDouble(区间2长度.getSummonValue());
        keyValueResults.add(new KeyValueResult("第二AT段长度", qj2Value));
        FixedValue 变电所的QT值 = fixedValueService.getOneByDeviceIdAndFixedValueName(deviceId, FixedValueConstants.DIMENSION.变电所的QT值.getName());
        Double bdsQtValue = Double.parseDouble(变电所的QT值.getSummonValue());
        keyValueResults.add(new KeyValueResult("变电所Q值", bdsQtValue));
        FixedValue AT1的QT1值 = fixedValueService.getOneByDeviceIdAndFixedValueName(deviceId, FixedValueConstants.DIMENSION.AT1的QT1值.getName());
        Double at1QtValue = Double.parseDouble(AT1的QT1值.getSummonValue());
        keyValueResults.add(new KeyValueResult("AT所QT1", at1QtValue));
        FixedValue AT1的QT2值 = fixedValueService.getOneByDeviceIdAndFixedValueName(deviceId, FixedValueConstants.DIMENSION.AT1的QT2值.getName());
        Double at2QtValue = Double.parseDouble(AT1的QT2值.getSummonValue());
        keyValueResults.add(new KeyValueResult("AT所QT2", at2QtValue));
        FixedValue AT2的QT1值 = fixedValueService.getOneByDeviceIdAndFixedValueName(deviceId, FixedValueConstants.DIMENSION.AT2的QT1值.getName());
        Double at2QfValue = Double.parseDouble(AT2的QT1值.getSummonValue());
        keyValueResults.add(new KeyValueResult("分区所QT1", at2QfValue));
        FixedValue AT2的QT2值 = fixedValueService.getOneByDeviceIdAndFixedValueName(deviceId, FixedValueConstants.DIMENSION.AT2的QT2值.getName());
        Double at2Qt2Value = Double.parseDouble(AT2的QT2值.getSummonValue());
        keyValueResults.add(new KeyValueResult("分区所QT2", at2Qt2Value));
        FixedValue AT1的QF1值 = fixedValueService.getOneByDeviceIdAndFixedValueName(deviceId, FixedValueConstants.DIMENSION.AT1的QF1值.getName());
        Double at1Qf1Value = Double.parseDouble(AT1的QF1值.getSummonValue());
        keyValueResults.add(new KeyValueResult("AT所QF1", at1Qf1Value));
        FixedValue AT1的QF2值 = fixedValueService.getOneByDeviceIdAndFixedValueName(deviceId, FixedValueConstants.DIMENSION.AT1的QF2值.getName());
        Double at1Qf2Value = Double.parseDouble(AT1的QF2值.getSummonValue());
        keyValueResults.add(new KeyValueResult("AT所QF2", at1Qf2Value));
        FixedValue AT2的QF1值 = fixedValueService.getOneByDeviceIdAndFixedValueName(deviceId, FixedValueConstants.DIMENSION.AT1的QF2值.getName());
        Double at2Qf1Value = Double.parseDouble(AT2的QF1值.getSummonValue());
        keyValueResults.add(new KeyValueResult("分区所QF1", at2Qf1Value));
        FixedValue AT2的QF2值 = fixedValueService.getOneByDeviceIdAndFixedValueName(deviceId, FixedValueConstants.DIMENSION.AT2的QF2值.getName());
        Double at2Qf2Value = Double.parseDouble(AT2的QF2值.getSummonValue());
        keyValueResults.add(new KeyValueResult("分区所QF2", at2Qf2Value));
        List<DeviceFailureConstants.DIMENSION> otherParams = new ArrayList<>();
        otherParams.add(DeviceFailureConstants.DIMENSION.变电所吸上电流);
        otherParams.add(DeviceFailureConstants.DIMENSION.变电所上行T电流);
        otherParams.add(DeviceFailureConstants.DIMENSION.变电所上行T电流角度);
        otherParams.add(DeviceFailureConstants.DIMENSION.变电所上行F电流);
        otherParams.add(DeviceFailureConstants.DIMENSION.变电所上行F电流角度);
        otherParams.add(DeviceFailureConstants.DIMENSION.变电所下行T电流);
        otherParams.add(DeviceFailureConstants.DIMENSION.变电所下行T电流角度);
        otherParams.add(DeviceFailureConstants.DIMENSION.变电所下行F电流);
        otherParams.add(DeviceFailureConstants.DIMENSION.变电所下行F电流角度);
        otherParams.add(DeviceFailureConstants.DIMENSION.变电所上行F电流);
        otherParams.add(DeviceFailureConstants.DIMENSION.变电所上行F电流角度);
        otherParams.add(DeviceFailureConstants.DIMENSION.AT所吸上电流);
        otherParams.add(DeviceFailureConstants.DIMENSION.AT所上行T电流);
        otherParams.add(DeviceFailureConstants.DIMENSION.AT所上行T电流角度);
        otherParams.add(DeviceFailureConstants.DIMENSION.AT所上行F电流);
        otherParams.add(DeviceFailureConstants.DIMENSION.AT所上行F电流角度);
        otherParams.add(DeviceFailureConstants.DIMENSION.AT所下行T电流);
        otherParams.add(DeviceFailureConstants.DIMENSION.AT所下行T电流角度);
        otherParams.add(DeviceFailureConstants.DIMENSION.AT所下行F电流);
        otherParams.add(DeviceFailureConstants.DIMENSION.AT所下行F电流角度);
        otherParams.add(DeviceFailureConstants.DIMENSION.分区所吸上电流);
        otherParams.add(DeviceFailureConstants.DIMENSION.分区所上行T电流);
        otherParams.add(DeviceFailureConstants.DIMENSION.分区所上行T电流角度);
        otherParams.add(DeviceFailureConstants.DIMENSION.分区所上行F电流);
        otherParams.add(DeviceFailureConstants.DIMENSION.分区所上行F电流角度);
        otherParams.add(DeviceFailureConstants.DIMENSION.分区所下行T电流);
        otherParams.add(DeviceFailureConstants.DIMENSION.分区所下行T电流角度);
        otherParams.add(DeviceFailureConstants.DIMENSION.分区所下行F电流);
        otherParams.add(DeviceFailureConstants.DIMENSION.分区所下行F电流角度);
        otherParams.add(DeviceFailureConstants.DIMENSION.分区所上行F电流);
        otherParams.add(DeviceFailureConstants.DIMENSION.分区所上行F电流角度);
        List<DeviceDataDto> otherDeviceDataGroups = this.fixedValueService.findByFixedValueVersionIdAndDimension(deviceId,
                otherParams);
        Map<String, List<DeviceDataDto>> deviceDataMap = otherDeviceDataGroups.stream().collect(Collectors.groupingBy(o->o.getDeviceResourceName()+"_"+o.getDeviceKey()));
        List<KeyValueResult> 电流 = otherDeviceDataGroups.stream()
                .filter(o -> o.getDeviceKey().endsWith("电流"))
                .map(o -> {
                    if (o.getDeviceKey().contains("吸上电流")) return new KeyValueResult(o.getDeviceKey(), o.getDeviceValue()+"安");
                    DeviceDataDto deviceDataDto = deviceDataMap.get(o.getDeviceResourceName()+"_"+o.getDeviceKey() + "角度").get(0);
                    return new KeyValueResult(o.getDeviceKey(), o.getDeviceValue() + "安," + deviceDataDto.getDeviceValue() + "度");
                }).collect(Collectors.toList());
        keyValueResults.addAll(电流);
        FixedValue 吸上电流流互变比 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.吸上电流流互变比);
        FixedValue TF短路故障判别 = fixedValueService.findByDeviceIdAndFixedName(deviceId, FixedValueConstants.DIMENSION.TF短路故障判别);
        keyValueResults.add(new KeyValueResult("Kp", Float.parseFloat(吸上电流流互变比.getSummonValue())));
        keyValueResults.add(new KeyValueResult("In TF短路故障判别系数", Float.parseFloat(TF短路故障判别.getSummonValue())));
        return keyValueResults;
    }

    @Override
    public Map<String, CurrentValue> currentDistMap(Long deviceId) throws ValidaException {
        Map<String, CurrentValue> currentMap = new HashMap<>();
//        I0st=上行T线电流（变电所测距数据）
        RealVector I0stVector = buildDimension(deviceId, DeviceFailureConstants.DIMENSION.变电所上行T电流);
//        I0xt=下行T线电流（变电所测距数据）
        RealVector I0xtVector = buildDimension(deviceId, DeviceFailureConstants.DIMENSION.变电所下行T电流);
        RealVector 标准零度 = I0stVector.add(I0xtVector);
        currentMap.put("I0st", analysisCurrentValue(I0xtVector, 标准零度));
//        I0sf=上行F线电流（变电所测距数据）
        RealVector I0sfVector = buildDimension(deviceId, DeviceFailureConstants.DIMENSION.变电所上行F电流);
        currentMap.put("I0sf", analysisCurrentValue(I0sfVector, 标准零度));
//        I0xf=下行F线电流（变电所测距数据）
        RealVector I0xfVector = buildDimension(deviceId, DeviceFailureConstants.DIMENSION.变电所下行F电流);
        currentMap.put("I0xf", analysisCurrentValue(I0xfVector, 标准零度));
//        I1st=上行T线电流（子站1测距数据）
        RealVector I1stVector = buildDimension(deviceId, DeviceFailureConstants.DIMENSION.AT所上行T电流);
        currentMap.put("I1st", analysisCurrentValue(I1stVector, 标准零度));
//        I1xt=下行T线电流（子站1测距数据）
        RealVector I1xtVector = buildDimension(deviceId, DeviceFailureConstants.DIMENSION.AT所下行T电流);
        currentMap.put("I1xt", analysisCurrentValue(I1xtVector, 标准零度));
//        I1sf=上行F线电流（子站1测距数据）
        RealVector I1sfVector = buildDimension(deviceId, DeviceFailureConstants.DIMENSION.AT所上行F电流);
        currentMap.put("I1sf", analysisCurrentValue(I1sfVector, 标准零度));
//        I1xf=下行F线电流（子站1测距数据）
        RealVector I1xfVector = buildDimension(deviceId, DeviceFailureConstants.DIMENSION.AT所下行F电流);
        currentMap.put("I1xf", analysisCurrentValue(I1xfVector, 标准零度));
//        I2st=上行T线电流（子站2测距数据）
        RealVector I2stVector = buildDimension(deviceId, DeviceFailureConstants.DIMENSION.分区所上行T电流);
        currentMap.put("I2st", analysisCurrentValue(I2stVector, 标准零度));
//        I2xt=下行T线电流（子站2测距数据）
        RealVector I2xtVector = buildDimension(deviceId, DeviceFailureConstants.DIMENSION.分区所下行T电流);
        currentMap.put("I2xt", analysisCurrentValue(I2xtVector, 标准零度));
//        I2sf=上行F线电流（子站2测距数据）
        RealVector I2sfVector = buildDimension(deviceId, DeviceFailureConstants.DIMENSION.分区所上行F电流);
        currentMap.put("I2sf", analysisCurrentValue(I2sfVector, 标准零度));
//        I2xf=下行F线电流（子站2测距数据）
        RealVector I2xfVector = buildDimension(deviceId, DeviceFailureConstants.DIMENSION.分区所下行F电流);
        currentMap.put("I2xf", analysisCurrentValue(I2xfVector, 标准零度));
        // I1f=I1sf+I1xf
        RealVector I1fVactor = I1sfVector.add(I1xfVector);
        currentMap.put("I1f", analysisCurrentValue(I1fVactor, 标准零度));
        // I1t=I1st+I1xt
        RealVector I1tVector = I1stVector.add(I1xtVector);
        currentMap.put("I1tV", analysisCurrentValue(I1tVector, 标准零度));
        // I2f=I2sf+I2xf todo why wrong????
        RealVector I2fVector = I2sfVector.add(I2xfVector);
        currentMap.put("I2f", analysisCurrentValue(I2fVector, 标准零度));
        // I2t=I2st+I2xt
        RealVector I2tVector = I2stVector.add(I2xtVector);
        currentMap.put("I2t", analysisCurrentValue(I2tVector, 标准零度));
        // I0=I0st+I0xt+I0sf+I0xf
        RealVector I0Vector = I0stVector.add(I0xtVector).add(I0sfVector).add(I0xfVector);
        currentMap.put("I0", analysisCurrentValue(I0Vector, 标准零度));
        // I1=I1st+I1xt+I1sf+I1xf
        RealVector I1Vector = I1stVector.add(I1xtVector).add(I1sfVector).add(I1xfVector);
        currentMap.put("I1", analysisCurrentValue(I1Vector, 标准零度));
        // I2=I2t+I2f
        RealVector I2Vector = I2tVector.add(I2fVector);
        currentMap.put("I2", analysisCurrentValue(I2Vector, 标准零度));
        // I短路 六种情况
        extracted(deviceId, new ArrayList<>());
        RealVector I短路;
        if (StringUtils.equals(THREAD_LOCAL.get().get("故障类型").toString(), "TF故障")
                &&StringUtils.equals(THREAD_LOCAL.get().get("故障行别").toString(), "上行")) {
//        第一/二AT段上行TF故障：I短路=I0st-I1st-I2st
            I短路 = I0stVector.subtract(I1stVector).subtract(I2stVector);
        }else if (StringUtils.equals(THREAD_LOCAL.get().get("故障类型").toString(), "TF故障")
                &&StringUtils.equals(THREAD_LOCAL.get().get("故障行别").toString(), "下行")) {
//        第一/二AT段下行TF故障：I短路=I0xt-I1xt-I2xt
            I短路 = I0xtVector.subtract(I1xtVector).subtract(I2xtVector);
        }else if (StringUtils.equals(THREAD_LOCAL.get().get("故障类型").toString(), "TR故障")
                &&StringUtils.equals(THREAD_LOCAL.get().get("故障行别").toString(), "上行")) {
//        第一/二AT段上行TR故障：I短路=I0st-I1st-I2st
            I短路 = I0stVector.subtract(I1stVector).subtract(I2stVector);
        }else if (StringUtils.equals(THREAD_LOCAL.get().get("故障类型").toString(), "TR故障")
                &&StringUtils.equals(THREAD_LOCAL.get().get("故障行别").toString(), "下行")) {
//        第一/二AT段下行TR故障：I短路=I0xt-I1xt-I2xt
            I短路 = I0xtVector.subtract(I1xtVector).subtract(I2xtVector);
        }else if (StringUtils.equals(THREAD_LOCAL.get().get("故障类型").toString(), "FR故障")
                &&StringUtils.equals(THREAD_LOCAL.get().get("故障行别").toString(), "上行")) {
//        第一/二AT段上行FR故障：I短路=I1st+I2st-I0st
            I短路 = I1stVector.add(I2stVector).subtract(I0stVector);
        }else if (StringUtils.equals(THREAD_LOCAL.get().get("故障类型").toString(), "FR故障")
                &&StringUtils.equals(THREAD_LOCAL.get().get("故障行别").toString(), "上行")) {
//        第一/二AT段下行FR故障：I短路=I1xt+I2xt-I0xt
            I短路 = I1xtVector.add(I2xtVector).subtract(I0xtVector);
        }else {
            I短路 = MathUtils.toRealVector(0d,0d);
        }
        currentMap.put("I短路", analysisCurrentValue(I短路, 标准零度));
        // Ilast 四种情况
        RealVector IlastVector = null;
//        第一AT段上/下行FR故障：Ilast=I短路+I0
        if (StringUtils.equals(THREAD_LOCAL.get().get("故障类型").toString(), "FR故障")
                &&StringUtils.equals(THREAD_LOCAL.get().get("故障区段").toString(), "第一AT区段")) {
            IlastVector = I短路.add(I0Vector);
        }else if (StringUtils.equals(THREAD_LOCAL.get().get("故障类型").toString(), "FR故障")
                &&StringUtils.equals(THREAD_LOCAL.get().get("故障区段").toString(), "第二AT区段")) {
//        第二AT段上/下行FR故障：Ilast=I2-I短路
            IlastVector = I2Vector.subtract(I短路);
        }else if (StringUtils.equals(THREAD_LOCAL.get().get("故障类型").toString(), "TR故障")
                &&StringUtils.equals(THREAD_LOCAL.get().get("故障区段").toString(), "第一AT区段")){
//        第一AT段上/下行TR故障：Ilast=I短路-I0
            IlastVector = I短路.subtract(I0Vector);
        }else if (StringUtils.equals(THREAD_LOCAL.get().get("故障类型").toString(), "TR故障")
                &&StringUtils.equals(THREAD_LOCAL.get().get("故障区段").toString(), "第二AT区段")) {
//        第二AT段上/下行TR故障：Ilast=I2+I短路
            IlastVector = I2Vector.add(I短路);
        }
        currentMap.put("Ilast", analysisCurrentValue(IlastVector, 标准零度));
        return currentMap;
    }

    private CurrentValue analysisCurrentValue(RealVector vector, RealVector base) {
        CurrentValue currentValue = new CurrentValue();
        Double sub = MathUtils.toAngle(vector) - MathUtils.toAngle(base);
        currentValue.setValue(vector.getNorm());
        currentValue.setDirection(Math.cos(sub)>0?1:-1);
        return currentValue;
    }

    private RealVector buildDimension(Long deviceId, DeviceFailureConstants.DIMENSION dimension) throws ValidaException {
        List<DeviceFailureConstants.DIMENSION> bdsdlxl = new ArrayList<>();
        bdsdlxl.add(dimension);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.getByNameAndResource(dimension.getName()+"角度", dimension.getResource()));
        List<DeviceDataDto> bdsdlxlDeviceData = this.fixedValueService.findByFixedValueVersionIdAndDimension(deviceId,
                bdsdlxl);
        return MathUtils.toRealVector(Double.parseDouble(bdsdlxlDeviceData.get(0).getDeviceValue()),
                Double.parseDouble(bdsdlxlDeviceData.get(1).getDeviceValue()));
    }

    private Double 下行(Long deviceId) throws ValidaException {
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

    private Double 上行(Long deviceId) throws ValidaException {
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

    private KeyValueResult 分区所横联电流Ihl2(Long deviceId) throws ValidaException {
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
        return new KeyValueResult("分区所横联电流Ihl2(A)", bdsResult.getNorm());
    }

    private KeyValueResult 变电所横联电流Ihl0(Long deviceId) throws ValidaException {
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
        return new KeyValueResult("变电所横联电流Ihl0(A)", bdsResult.getNorm() / 2);
    }

    private KeyValueResult 变电所上行电流Ihl0(Long deviceId, List<KeyValueResult> keyValueResults) throws ValidaException {
        List<DeviceFailureConstants.DIMENSION> bdsdlxl = new ArrayList<>();
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.变电所上行T电流);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.变电所上行T电流角度);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.变电所上行F电流);
        bdsdlxl.add(DeviceFailureConstants.DIMENSION.变电所上行F电流角度);
        List<DeviceDataDto> bdsdlxlDeviceData = this.fixedValueService.findByFixedValueVersionIdAndDimension(deviceId,
                bdsdlxl);
        RealVector number1 = MathUtils.toRealVector(Double.parseDouble(bdsdlxlDeviceData.get(0).getDeviceValue()),
                Double.parseDouble(bdsdlxlDeviceData.get(1).getDeviceValue()));
        RealVector number2 = MathUtils.toRealVector(Double.parseDouble(bdsdlxlDeviceData.get(2).getDeviceValue()),
                Double.parseDouble(bdsdlxlDeviceData.get(3).getDeviceValue()));
        RealVector bdsResult = number1.subtract(number2);
        KeyValueResult keyValueResult = new KeyValueResult("变电所上行电流Ihl0(A)", bdsResult.getNorm() / 2);
        if (null != keyValueResults) {
            keyValueResults.add(keyValueResult);
        }
        return keyValueResult;
    }

    private KeyValueResult 变电所下行电流Ihl0(Long deviceId, List<KeyValueResult> keyValueResults) throws ValidaException {
        List<DeviceFailureConstants.DIMENSION> bdsdlxl = new ArrayList<>();
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
        RealVector bdsResult = number1.subtract(number2);
        KeyValueResult keyValueResult = new KeyValueResult("变电所下行电流Ihl0(A)", bdsResult.getNorm() / 2);
        if (null != keyValueResults) {
            keyValueResults.add(keyValueResult);
        }
        return keyValueResult;
    }

    private KeyValueResult AT所横联电流Ihl1(Long deviceId) throws ValidaException {
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
        return new KeyValueResult("AT所横联电流Ihl1(A)", bdsResult.getNorm());
    }
}
