package cn.edu.bit.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class EditNewsDto {
    @NotBlank(message="标题不能为空")
    private String title;//标题
    @NotBlank(message="内容不能为空")
    private String content;//内容
}
