package org.example.university;

public class Subject implements UniversityElement{

    private String title;
    private int id;

    public Subject(String title, int id){
        this.title = title;
        this.id = id;

    }

    public int getId(){ return id; }

    public String getTitle(){ return title; }
    @Override
    public int compareTo(UniversityElement otherElement){
        Subject otherSubj = (Subject) otherElement;
        return title.compareTo(otherSubj.title);
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

        Subject other = (Subject) obj;
        return other.getId() == id;
    }
    @Override
    public String toString(){

        return title + ", id = " + id;
    }
}
