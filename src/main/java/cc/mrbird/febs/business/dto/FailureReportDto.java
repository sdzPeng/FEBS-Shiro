package cc.mrbird.febs.business.dto;

import com.deepoove.poi.data.PictureRenderData;
import com.deepoove.poi.data.TableRenderData;
import lombok.Data;

import java.io.Serializable;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-09-25 3:48 上午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Data
public class FailureReportDto implements Serializable {

    private String createTime;

    private String siteName;

    private String deviceName;

    private String PictureRenderData;

    private String i0;

    private String i1;

    private String i2;

    private String onetwo;

    private String updown;

    private String failuretype;

    /**
     * 吸上电流比法
     */
    private String xsdlbfjl;

    /**
     * 横联电流比法
     */
    private String hldlbjl;

    /**
     * 上下行电流比法
     */
    private String sxxdlbfcj;

    /**
     * 电流比法
     */
    private String dlbf;

    /**
     * 推荐故障距离
     */
    private String tjgzjl;

    /**
     * 公里标
     */
    private String glb;

    /**
     * 截图
     */
    private com.deepoove.poi.data.PictureRenderData snapshot;

    /**
     * 接触网关键设施信息
     */
    private TableRenderData keyFacilities;

    /**
     * 前后两端最近上道口
     */
    private TableRenderData closedFacilities;

    /**
     * 接触网安装信息
     */
    private TableRenderData touchNetInfo;

    /**
     * 计算数据源
     */
    private TableRenderData calcData;
}
