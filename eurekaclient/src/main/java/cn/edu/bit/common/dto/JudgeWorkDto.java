package cn.edu.bit.common.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class JudgeWorkDto {
    @NotNull(message="评分不能为空")
    private int score;//评分
}
