package org.example.university;

public class Teacher implements UniversityElement{

    private String fullName;
    private int id;

    public Teacher(String fullName, int id){

        this.fullName = fullName;
        this.id = id;
    }

    public String getFullName(){return fullName;}

    public int getId(){
        return id;
    }

    @Override
    public int compareTo(UniversityElement otherElement){
        Teacher otherTeacher = (Teacher) otherElement;
        return fullName.compareTo(otherTeacher.fullName);
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

        Teacher other = (Teacher) obj;

        return other.getId() == id;
    }

    @Override
    public String toString(){
        return fullName + " | id: " + id;
    }

}
