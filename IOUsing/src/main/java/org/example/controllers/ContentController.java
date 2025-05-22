package org.example.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.excel.Pair;
import org.example.university.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class ContentController {

    private ObjectMapper mapper;
    private boolean studentsChanged = false;
    private HashMap<Integer, Student> students; //id и объект студента

    private HashMap<Integer, Teacher> teachers; //id и объект препода
    private HashMap<Integer, Subject> subjects;// id и учебный предмет
    private List<Integer> groupNums;
    private int newStudentId;

    {
        mapper = new ObjectMapper();
        students = new HashMap<>();
        teachers = new HashMap<>();
        subjects = new HashMap<>();
        groupNums = new ArrayList<>();
    }

    public ContentController(List<Pair<String, String>> pairs){

        try {
            for(Pair<String, String> pair: pairs){

                switch(pair.getFirstEl()){

                    case "Subjects":
                        parseSubjects(pair.getSecondEl());
                        break;
                    case "Students":
                        parseStudents(pair.getSecondEl());
                        break;
                    case "Teachers":
                        parseTeachers(pair.getSecondEl());
                }
            }

        }
        catch(IOException exc){
            exc.printStackTrace();
        }

        File[] files = new File(".\\data").listFiles();
        String[] strs = Arrays.stream(files).map(File::getName).toArray(String[]::new);

        for(String str: strs){

            if(Pattern.matches("[a-zA-Z]+\\d+[a-zA-Z.]+", str)){
                groupNums.add(Integer.valueOf(str.substring(str.length() - 8, str.length() - 5)));
            }
        }


    }

    private void parseSubjects(String json)throws IOException {
        List<List<String>> matr = mapper.readValue(json, new TypeReference<List<List<String>>>(){});
        int id;

        for(List<String> list: matr){
            id = (int) Double.parseDouble(list.get(1));

            Subject su = new Subject(list.get(0), id);
            subjects.put(id, su);
        }
    }
    private void parseStudents(String json)throws IOException {
        List<List<String>> matr = mapper.readValue(json, new TypeReference<List<List<String>>>(){});

        int id;
        int group;

        for(List<String> list: matr){
            id = (int) Double.parseDouble(list.get(1));
            group = (int) Double.parseDouble(list.get(2));

            students.put(id, new Student(list.get(0), group, id));
        }

        Set<Integer> ids = students.keySet();
        newStudentId = ids.stream().sorted().toList().get(ids.size() - 1) + 1;

    }
    private void parseTeachers(String json)throws IOException {
        List<List<String>> matr = mapper.readValue(json, new TypeReference<List<List<String>>>(){});
        int id;

        for(List<String> list: matr){
            id = (int) Double.parseDouble(list.get(1));

            teachers.put(id, new Teacher(list.get(0), id));
        }
    }

    public Collection<Student> getStudents(){
        return students.values();
    }
    public Student getStudent(int id){

        if(students.get(id) == null)
            throw new IllegalArgumentException("Студента с таким id не существует");

        return students.get(id);
    }
    public void deductStudent(int id){
        studentsChanged = true;
        students.remove(id);
    }
    public int getNewStudentId(){
        newStudentId++;
        return newStudentId - 1;
    }
    public Student addNewStudent(int group, String fullName){
        studentsChanged = true;
        Student newStudent = new Student(fullName, group, getNewStudentId());
        students.put(newStudent.getId(), newStudent);

        return newStudent;
    }
    public boolean isStudentsChanged(){
        return studentsChanged;
    }

    public HashMap<Integer, Subject> getSubjects(){
        return subjects;
    }
    public Subject getSubject(int id){
        return subjects.get(id);
    }

    public Collection<Teacher> getTeachers(){
        return teachers.values();
    }
    public Teacher getTeacher(int id){
        return teachers.get(id);
    }

    public List<Integer> getGroupNums(){
        return groupNums;
    }

    public HashMap<String, Integer> reversedSubjectMap(){
        HashMap<String, Integer> resMap = new HashMap<>();

        for(Subject subj: subjects.values()){

            resMap.put(subj.getTitle(), subj.getId());
        }

        resMap.put(" Окно ", -1);
        return resMap;
    }
}
