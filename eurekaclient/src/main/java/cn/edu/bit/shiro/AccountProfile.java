package cn.edu.bit.shiro;

import lombok.Data;

import java.io.Serializable;

@Data
public class AccountProfile implements Serializable {
    private Integer id;//用户id
    private String username;//学工号
    private String type;//用户类型
    private String name;//姓名
}
