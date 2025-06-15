package org.example.controllers;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.example.controller_testing.ExcelChanger;
import org.example.controller_testing.ExcelParser;
import org.example.controller_testing.Pair;
import org.example.university.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class MainController {

    private ObjectMapper mapper = new ObjectMapper();
    private ExcelParser parser;
    private ExcelChanger changer;
    private HashMap<Integer, GroupController> groups; //номер группы - группа
    private ContentController contentContr;

    private ArrayList<Integer> removedGroups;

    public MainController(){
        parser = new ExcelParser();
        changer = new ExcelChanger();

        try {

            String json = parser.getUniversityContents();
            List<Pair<String, String>> list = mapper.readValue(json, new TypeReference<List<Pair<String, String>>>(){});

            contentContr = new ContentController(list);
            groups = new HashMap<>();
            removedGroups = new ArrayList<>();

        }
        catch(IOException exc){
            exc.printStackTrace();
        }
    }

    public void prepareGroupInfo(int group){
        try {

            String json = parser.getGroupInfo(group);
            List<Pair<String, String>> pairs = mapper.readValue(json, new TypeReference<List<Pair<String, String>>>() {});

            GroupController groupContr = new GroupController(group, contentContr, pairs);
            groups.put(group, groupContr);
        }
        catch(IOException exc){
            exc.printStackTrace();
        }
    }

    public List<Integer> getGroupNums(){
        return contentContr.getGroupNums();
    }
    public boolean isGroupExists(int num){
        if(contentContr.getGroupNums().contains(num))
            return true;

        return false;
    }
    public void makeNewGroup(int num, BufferedReader reader)throws IOException{
        contentContr.getGroupNums().add(num);
        GroupController newGroup = new GroupController(num, contentContr, reader);
        groups.put(num, newGroup);
    }
    public void makeDefaultGroup(int num){

        contentContr.getGroupNums().add(num);
        GroupController newGroup = new GroupController(num, contentContr);
        groups.put(num, newGroup);
    }
    public boolean isGroupPrepared(int group){
        if(groups.get(group) == null)
            return false;

        return true;
    }

    public Collection<Student> getStudents(){
        return contentContr.getStudents();
    }
    public List<Student> getStudentsFrom(int group){
        return groups.get(group).getStudentsFromGroup();
    }
    public Student getStudent(int id){
        return contentContr.getStudent(id);
    }
    public double getStudentPointsFrom(int group, int id, int subjId){
        return groups.get(group).getStudentPoints(id, subjId);
    }
    public void setStudentPoints(int group, int studId, int subjId, double newPoints){
        groups.get(group).setStudentPoints(studId, subjId, newPoints);
    }
    public void changeStudentPoints(int group, int studId, int subjId, double newPoints){
        groups.get(group).changeStudentPoints(studId, subjId, newPoints);
    }
    public boolean isStudentInGroup(int id, int group){
        return groups.get(group).isStudentHere(id);
    }
    public void deductStudent(int group, int id){
        groups.get(group).deductStudent(id);
        contentContr.deductStudent(id);
    }
    public void addNewStudent(int group, String fullName){

        Student newStudent = contentContr.addNewStudent(group, fullName);
        GroupController groupController = groups.get(group);

        groupController.addNewStudent(newStudent);

    }

    public Collection<Teacher> getTeachers(){

        return contentContr.getTeachers();
    }
    public List<String> getTeachersFrom(int group){//в каком формате возвращает читать в классе GroupController
        return groups.get(group).getSubjTeachers();
    }

    public Collection<Subject> getSubjects(){
        return contentContr.getSubjects().values();
    }
    public Subject getSubject(int id){
        return contentContr.getSubject(id);
    }

    public String getSchedule(int group){

        return groups.get(group).getSchedule();
    }
    public void changeSchedule(int group, String weekday, int lessonNum, int subjId){

        groups.get(group).changeSchedule(weekday, lessonNum, subjId);
    }

    public void saveChanges()throws IOException, InvalidFormatException {
        for(Integer groupNum: removedGroups){
            changer.removeGroup(groupNum);
        }

        List<Pair<String, String>> jsons = new ArrayList<>();
        List<List<String>> sheetData;
        List<String> row;

        if(contentContr.isStudentsChanged()){
            sheetData = new ArrayList<>();

            sheetData.add(new ArrayList<>(Arrays.asList("Full Name", "Id", "Group")));
            List<Student> students = contentContr.getStudents().stream().sorted((Student stud1, Student stud2)->{
                return stud1.getId() - stud2.getId();
            }).toList();

            for(Student stud: students){
                row = new ArrayList<>();

                row.add(stud.getFullName());
                row.add(stud.getId() + "");
                row.add(stud.getGroup() + "");

                sheetData.add(row);
            }

            String json = mapper.writeValueAsString(sheetData);
            jsons.add(new Pair<>("Students", json));

        }

        for(GroupController group: groups.values()){

            if(group.isNewGroup()){
                jsons.add(new Pair(group.getGroup() + "", jsonForNewGroup(group)));

            }
            else{
                if(group.isPointsChanged()){
                    List<Integer> subjIds = contentContr.getSubjects().keySet().stream().sorted().toList();

                    sheetData = new ArrayList<>();
                    row = new ArrayList<>();

                    row.add("StudentID и SubjectID");
                    for(Integer subjId: subjIds){
                        row.add(subjId + "");
                    }

                    sheetData.add(row);

                    for(StudentPoints student: group.getAllStudentsAndPoints()){
                        row = new ArrayList<>();
                        row.add(student.getStudent().getId() + "");

                        for(Integer subjId: subjIds){

                            row.add(student.getPoints(subjId) + "");
                        }

                        sheetData.add(row);
                    }

                    jsons.add(new Pair<>("CommonPoints!" + group.getGroup(), mapper.writeValueAsString(sheetData)));
                }
                if(group.isScheduleChanged()){
                    sheetData = new ArrayList<>();
                    row = new ArrayList<>();
                    row.add("Week day");

                    for(int i = 1; i < 8; i++){
                        row.add(i + "");
                    }

                    sheetData.add(row);
                    List<List<String>> weekDays = group.getScheduleTable();
                    HashMap<String, Integer> reversedSubjectMap = contentContr.reversedSubjectMap();

                    for(List<String> day: weekDays){

                        row = new ArrayList<>();
                        row.add(day.get(0));

                        for(int i = 1; i < day.size(); i++){
                            row.add(reversedSubjectMap.get(day.get(i)) + "");
                        }

                        sheetData.add(row);
                    }

                    jsons.add(new Pair<>("Schedule!" + group.getGroup(), mapper.writeValueAsString(sheetData)));
                }
            }

        }

        String finalJson = mapper.writeValueAsString(jsons);
        changer.writeChanges(finalJson);
    }

    private String jsonForNewGroup(GroupController group) throws IOException{
        List<List<String>> sheetsData = new ArrayList<>();
        List<Integer> subjIds = contentContr.getSubjects().keySet().stream().sorted().toList();

        List<StudentPoints> points = group.getAllStudentsAndPoints();
        ArrayList<String> row = new ArrayList<>();
        //оценки
        row.add("CommonPointsStudentID и SubjectID");

        for (Integer subjId : subjIds) {
            row.add(subjId + "");
        }
        sheetsData.add(row);

        for (StudentPoints student : points) {
            row = new ArrayList<>();

            row.add(student.getStudent().getId() + "");

            for (Integer subjId : subjIds) {
                row.add(student.getPoints(subjId) + "");
            }

            sheetsData.add(row);
        }
        //расписание
        row = new ArrayList<>();
        row.add("ScheduleWeek day");

        for(int i = 1; i < 8; i++){
            row.add(i + "");
        }

        sheetsData.add(row);
        List<List<String>> weekDays = group.getScheduleTable();
        HashMap<String, Integer> reversedSubjectMap = contentContr.reversedSubjectMap();

        for(List<String> day: weekDays){

            row = new ArrayList<>();
            row.add(day.get(0));

            for(int i = 1; i < day.size(); i++){
                row.add(reversedSubjectMap.get(day.get(i)) + "");
            }

            sheetsData.add(row);
        }

        //преподы и их предметы
        row = new ArrayList<>();
        row.add("TeachersSubjectID");
        row.add("TeacherID");

        sheetsData.add(row);

        for(Integer subjId: subjIds){
            row = new ArrayList<>();

            row.add(subjId + "");
            row.add(group.getTeacher(subjId).getId() + "");
            sheetsData.add(row);
        }

        return mapper.writeValueAsString(sheetsData);
    }

    //группа удаляется только если у нее стоит флаг, а флаг может быть поставлен только если группа пустая
    //возвращает true если группа удалена, false если в ней есть студенты
    public boolean removeGroup(int group){
        GroupController deletedGroup = groups.get(group);

        if(!deletedGroup.isGroupEmpty())
            return false;

        List<Integer> nums = contentContr.getGroupNums();
        if(nums.contains(group))
            nums.remove((Object) Integer.valueOf(group));

        groups.remove(group);
        removedGroups.add(deletedGroup.getGroup());
        return true;
    }
}
