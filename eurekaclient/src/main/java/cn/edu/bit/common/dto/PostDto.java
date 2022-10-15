package cn.edu.bit.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class PostDto implements Serializable {

    //发帖
    @NotBlank(message="标题不能为空")
    private String title;//标题
    @NotBlank(message="内容不能为空")
    private String description;//内容

    private char visibility;//可见性

}
