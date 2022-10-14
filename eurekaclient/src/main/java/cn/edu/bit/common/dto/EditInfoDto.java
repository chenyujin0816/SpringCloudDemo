package cn.edu.bit.common.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import java.io.Serializable;

@Data
public class EditInfoDto implements Serializable {

    private String mobile;

    @Email(message="邮箱格式不正确")
    private String email;

    private String remark;
}
