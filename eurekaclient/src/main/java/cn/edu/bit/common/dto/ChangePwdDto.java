package cn.edu.bit.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class ChangePwdDto implements Serializable {

    @NotBlank(message = "当前密码不能为空")
    private String curPassword;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    private String confirmNewPwd;
}
