package org.example.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.excel.Pair;
import org.example.university.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class GroupController {
    private boolean isCreated = false;

    private int group;

    private ObjectMapper mapper;

    private ContentController contentController;//нужен для получения данных о предметах, студентах и преподах
    private boolean pointsChanged = false;
    private HashMap<Integer, StudentPoints> studsPoints;//id студента - его оценки
    private HashMap<Subject, Teacher> teachers;// какой учитель какой предмет ведет у данной группы

    private boolean scheduleChanged = false;
    private Schedule schedule;

    {
        mapper = new ObjectMapper();
        studsPoints = new HashMap<>();
        teachers = new HashMap<>();
    }

    //для новой группы
    public GroupController(int group, ContentController contentController, BufferedReader reader)throws IOException{
        isCreated = true;
        this.group = group;
        this.contentController = contentController;
        schedule = Schedule.makeScheduleForNewGroup(contentController.getSubjects());

        List<Teacher> teachers = contentController.getTeachers().stream().toList();
        System.out.println("Выбери для каждого предмета преподавателя для данной группы");
        for(int i = 1; i <= teachers.size(); i++){

            System.out.println(i + ". " + teachers.get(i - 1));
        }
        System.out.println("По очереди для каждого предмета введи id преподавателя");

        String answer;
        for(Subject subj: contentController.getSubjects().values()){

            System.out.println("Для предмета " + subj.getTitle());
            answer = reader.readLine();

            while(!Pattern.matches("\\d{1}", answer) || !isTeacherIdCorrect(Integer.valueOf(answer))){
                if(answer.equals("back"))
                    System.out.println("В этот раз придется пройти до конца");
                else
                    System.out.println("Неверно введены данные");
                answer = reader.readLine();
            }

            this.teachers.put(subj, contentController.getTeacher(Integer.valueOf(answer)));
        }
    }

    public GroupController(int group, ContentController contentController, List<Pair<String, String>> pairs)throws IOException{
        this.group = group;
        this.contentController = contentController;

        for(Pair<String, String> pair:pairs){

            switch(pair.getFirstEl()){

                case "CommonPoints":
                    parseStudents(pair.getSecondEl());
                    break;
                case "Schedule":
                    parseSchedule(pair.getSecondEl());
                    break;
                case "Teachers":
                    parseTeachers(pair.getSecondEl());
            }
        }
    }

    public boolean isTeacherIdCorrect(int id){
        if(contentController.getTeacher(id) == null){
            System.out.println("badId");
            return false;
        }

        return true;
    }

    private void parseStudents(String json)throws IOException{
        List<List<String>> matr = mapper.readValue(json, new TypeReference<List<List<String>>>(){});

        matr.get(0).remove(0);

        List<Integer> subjIds = matr.get(0).stream().map((String str)->{
            return Double.valueOf(str).intValue();
        }).toList();//id предметов

        matr.remove(0);

        List<List<Double>> points = matr.stream().map((List<String> l)->{
            List<Double> newList = new ArrayList<>();

            for(String str: l){
                newList.add(Double.valueOf(str));
            }

            return newList;
        }).toList();

        Student currentStud;
        HashMap<Integer, Double> subjPoints;

        for(List<Double> list: points){

            currentStud = contentController.getStudent(list.get(0).intValue());
            subjPoints = new HashMap<>();

            for(int i = 1; i < list.size(); i++){
                subjPoints.put(subjIds.get(i - 1), list.get(i));

            }
            studsPoints.put(currentStud.getId(), new StudentPoints(currentStud, subjPoints));
        }

    }
    private void parseSchedule(String json)throws IOException{
        List<List<String>> matr = mapper.readValue(json, new TypeReference<List<List<String>>>(){});

        schedule = new Schedule(matr, contentController.getSubjects());

    }
    private void parseTeachers(String json)throws IOException{
        List<List<String>> matr = mapper.readValue(json, new TypeReference<List<List<String>>>(){});

        List<List<Integer>> intMatr = matr.stream().map((List<String> l1)->{

            List<Integer> l2 = new ArrayList<>();
            for(String str: l1){
                l2.add(Double.valueOf(str).intValue());
            }
            return l2;
        }).toList();

        for(List<Integer> teacher: intMatr){
            teachers.put(contentController.getSubject(teacher.get(0)), contentController.getTeacher(teacher.get(1)));
        }
    }

    public int getGroup(){
        return group;
    }

    public List<Student> getStudentsFromGroup(){
        return studsPoints.values().stream().toList().stream().map(StudentPoints::getStudent).toList();
    }
    public boolean isStudentHere(int id){
        if(studsPoints.get(id) == null)
            return false;
        return true;
    }
    public double getStudentPoints(int id, int subjId){//получить баллы у студента по какому-то предмету
        return studsPoints.get(id).getPoints(subjId);
    }
    public void setStudentPoints(int id, int subjId, double newPoints){
        pointsChanged = true;
        studsPoints.get(id).setPoints(subjId, newPoints);
    }
    public List<StudentPoints> getAllStudentsAndPoints(){
        return studsPoints.values().stream().sorted().toList();
    }
    public void changeStudentPoints(int id, int subjId, double newPoints){
        pointsChanged = true;
        studsPoints.get(id).changePoints(subjId, newPoints);
    }
    public void deductStudent(int studId){
        pointsChanged = true;
        studsPoints.remove(studId);
    }
    public void addNewStudent(Student newStudent){
        pointsChanged = true;
        studsPoints.put(newStudent.getId(), new StudentPoints(newStudent, fillPoints()));
    }
    public boolean isGroupEmpty(){
        if(studsPoints.isEmpty())
            return true;

        return false;
    }

    public String getSchedule(){
        return schedule.getSchedule();
    }
    public void changeSchedule(String weekDay, int lessonNum, int subjId){
        scheduleChanged = true;
        schedule.changeSchedule(weekDay, lessonNum, subjId);
    }
    public List<List<String>> getScheduleTable(){
        return schedule.getWeekDays();
    }

    public List<String> getSubjTeachers(){//возвращает отсортированный список преподов и предметов которые они ведут у группы
        List<String> list = new ArrayList<>();

        for(Subject subj: teachers.keySet()){

            list.add(subj.getTitle() + " : " + teachers.get(subj).getFullName());
        }
        list = list.stream().sorted().toList();

        return list;
    }
    public Teacher getTeacher(int subjId){
        return teachers.get(contentController.getSubject(subjId));
    }

    public HashMap<Integer, Double> fillPoints(){
        HashMap<Integer, Double> map = new HashMap<>();
        List<Integer> subjectsIds = contentController.getSubjects().keySet().stream().toList();

        for(Integer id: subjectsIds){
            map.put(id, 0.0);
        }

        return map;
    }

    public boolean isNewGroup(){
        return isCreated;
    }
    public boolean isScheduleChanged(){
        return scheduleChanged;
    }
    public boolean isPointsChanged(){
        return pointsChanged;
    }
}
