package cn.edu.bit.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data //生成Getter,Setter,equals,canEqual,hasCode,toString等方法
@AllArgsConstructor //添加一个构造函数，该构造函数含有所有已声明字段属性参数
@NoArgsConstructor //创建一个无参构造函数
public class Student {
    private long id;
    private String name;
    private int age;
}