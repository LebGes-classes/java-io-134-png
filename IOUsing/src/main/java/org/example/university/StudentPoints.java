package org.example.university;

import java.util.Collection;
import java.util.HashMap;

public class StudentPoints implements Comparable<StudentPoints>{

    private Student student;
    private HashMap<Integer, Double> points;// id предмета - нынешний балл

    public StudentPoints(Student student, HashMap<Integer, Double> points){
        this.student = student;
        this.points = points;
    }

    public Student getStudent(){return student;}
    public double getPoints(int id){return points.get(id);}

    public void setPoints(int subjId, double newPoints){
        points.put(subjId, newPoints);
    }

    public void changePoints(int subjId, double newPoints){

        points.merge(subjId, newPoints, (Double src, Double newVal) ->{
            if((src + newVal) < 0)
                return 0.0;
            if((src + newVal) > 100)
                return 100.0;

            return src + newVal;
        });
    }

    @Override
    public int compareTo(StudentPoints otherStud){
        return getStudent().getId() - otherStud.getStudent().getId();
    }
}
