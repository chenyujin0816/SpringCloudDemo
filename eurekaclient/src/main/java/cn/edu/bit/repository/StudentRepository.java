package cn.edu.bit.repository;

import cn.edu.bit.entity.Student;

import java.util.Collection;

public interface StudentRepository {
    public Collection<Student> findAll();
    public Student findById(long id);
    public void saveOrUpdate(Student s);
    public void deleteById(long id);
}