package cn.edu.bit.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class LoginDto implements Serializable {

    //登录
    @NotBlank(message="学号不能为空")
    private String username;
    @NotBlank(message="密码不能为空")
    private String password;
//    private char status;
//    private Long id;
}
