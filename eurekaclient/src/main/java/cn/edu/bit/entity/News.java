package cn.edu.bit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class News implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id",type= IdType.AUTO)
    private Integer id;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "编辑人不能为空")
    private String editor;

    @NotBlank(message = "审核人不能为空")
    private String reviewer;

    @NotBlank(message = "撰稿人不能为空")
    private String contributor;

    @NotBlank(message = "内容不能为空")
    private String content;

    @NotBlank(message = "类别不能为空")
    private String type;

    private LocalDateTime releaseDate;


}
