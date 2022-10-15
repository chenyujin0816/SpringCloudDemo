package cn.edu.bit.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CreateGroupDto {

    //发布作业
    @NotBlank(message="组名不能为空")
    private String groupName;//标题
    private Long courseId;//课程编号
}
