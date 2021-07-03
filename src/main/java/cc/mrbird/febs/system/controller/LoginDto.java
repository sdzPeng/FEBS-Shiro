package cc.mrbird.febs.system.controller;

import lombok.Data;

import java.io.Serializable;

/**
 * @company: 上海数慧系统技术有限公司
 * @department: 数据中心
 * @date: 2021-06-29 10:31 下午
 * @author: zhangyp
 * @email: zhangyp@dist.com.cn
 * @desc：
 */
@Data
public class LoginDto implements Serializable {

    private String username;

    private String password;

    private String verifyCode;

    private String rememberMe;
}
