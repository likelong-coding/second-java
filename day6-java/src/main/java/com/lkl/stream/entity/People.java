package com.lkl.stream.entity;

public class People {
    private String name;
    private Integer age;

    public People(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Override
    public java.lang.String toString() {
        return "stream.People{" +
                "name=" + name +
                ", age=" + age +
                '}';
    }
}
