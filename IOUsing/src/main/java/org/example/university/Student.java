package org.example.university;

public class Student implements UniversityElement{

    private int group;
    private String fullName;
    private int id;

    public Student(String fullName, int group, int id){

        this.fullName = fullName;
        this.id = id;
        this.group = group;

    }

    public int getGroup(){
        return group;
    }

    public int getId(){return id;}

    public String getFullName(){
        return fullName;
    }
    @Override
    public int compareTo(UniversityElement otherElement){
        Student otherStud = (Student) otherElement;
        return fullName.compareTo(otherStud.getFullName());
    }
    @Override
    public int hashCode(){

        return id;
    }

    @Override
    public boolean equals(Object obj){
        if(obj == null)
            return false;
        if(this == obj)
            return true;
        if(getClass() != obj.getClass())
            return false;

        Student other = (Student) obj;

        return other.getId() == id;
    }

    @Override
    public String toString(){
        return fullName + " | group: " + group + " | id: " + id;
    }
}
