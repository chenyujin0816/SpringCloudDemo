package cn.edu.bit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 小组实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("\"group\"")
public class Group implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id",type= IdType.AUTO)
    private Long id;//ID

    @NotBlank(message = "人数不能为空")
    private int memberCount;//人数

    @NotBlank(message = "组名不能为空")
    private String groupName;//小组名称

    @NotBlank(message = "隶属课程不能为空")
    private Long courseId;//隶属课程id

    @NotBlank(message = "指导教师不能为空")
    private Long teacherId;//指导教师id

    private Timestamp createTime;//发布时间

}