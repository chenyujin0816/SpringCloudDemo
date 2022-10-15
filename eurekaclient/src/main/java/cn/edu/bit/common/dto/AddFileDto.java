package cn.edu.bit.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AddFileDto {
    @NotBlank(message="文件地址不能为空")
    private String url;//在线文件地址
}
