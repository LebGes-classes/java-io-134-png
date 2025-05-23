package org.example.university;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.example.controllers.Utility.*;

public class Schedule {

    private String finalScheduleForm;

    private HashMap<Integer, String> strMap;//таблица с названиями предметов
    private HashMap<Integer, Subject> subjects;//исходная таблица

    private List<List<String>> weekDays;//списки с предметами уже

    public static Schedule makeScheduleForNewGroup(HashMap<Integer, Subject> subjects){
        List<List<String>> table = new ArrayList<>();

        table.add(new ArrayList<>(Arrays.asList("Monday", "-1", "-1", "-1", "-1", "-1", "-1", "-1")));
        table.add(new ArrayList<>(Arrays.asList("Tuesday", "-1", "-1", "-1", "-1", "-1", "-1", "-1")));
        table.add(new ArrayList<>(Arrays.asList("Wednesday", "-1", "-1", "-1", "-1", "-1", "-1", "-1")));
        table.add(new ArrayList<>(Arrays.asList("Thursday", "-1", "-1", "-1", "-1", "-1", "-1", "-1")));
        table.add(new ArrayList<>(Arrays.asList("Friday", "-1", "-1", "-1", "-1", "-1", "-1", "-1")));
        table.add(new ArrayList<>(Arrays.asList("Saturday", "-1", "-1", "-1", "-1", "-1", "-1", "-1")));

        return new Schedule(table, subjects);

    }

    public Schedule(List<List<String>> weekDaysSchedule, HashMap<Integer, Subject> subjects){
        strMap = new HashMap<>();

        weekDays = weekDaysSchedule;

        finalScheduleForm = "";

        this.subjects = subjects;
        formStrMap();//формирует таблицу с названий предметов
        reformSubjects();//формирует таблицу расписания с названиями предметов
        formSchedule();//формирует само расписание

    }

    private void formStrMap(){
        int id;
        for(Subject subj: subjects.values()){

            strMap.put(subj.getId(), subj.getTitle());
        }
        strMap.put(-1, " Окно ");
    }

    private void reformSubjects(){//заменяем числа на названия предметов

        int id;
        for(List<String> l: weekDays){

            for(int i = 1; i < l.size(); i++){
                id = Double.valueOf(l.get(i)).intValue();

                l.set(i, strMap.get(id));
            }
        }

    }

    private void formSchedule(){

        List<List<String>> reversed = reverseList(weekDays);
        List<Integer> maxSizes = maxLength(reversed);

        for(List<String> day: weekDays){

            finalScheduleForm += day.get(0);

            for(int i = 0; i < (maxSizes.get(0) - day.get(0).length()); i++){
                finalScheduleForm += " ";
            }

            finalScheduleForm += "|";

            for(int i = 1; i < day.size(); i++){

                finalScheduleForm += day.get(i);

                for(int j = 0; j < (maxSizes.get(i) - day.get(i).length()); j++){
                    finalScheduleForm += " ";
                }
                finalScheduleForm += "|";

            }
            finalScheduleForm += "\n";
        }
    }

    //возвращаем List с числами обозначающими максимальную длину строки из одного столбца
    private List<Integer> maxLength(List<List<String>> matr){
        int max;
        List<Integer> maxSizes = new ArrayList<>();

        for(List<String> list: matr){
            max = 0;

            for(String str: list){
                if(max < str.length()){
                    max = str.length();
                }
            }

            maxSizes.add(max);
        }
        return maxSizes;
    }

    public String getSchedule(){
        return finalScheduleForm;
    }

    public void changeSchedule(String weekDay, int lessonNum, int subjId){
        for(List<String> day: weekDays){
            if(day.get(0).equals(weekDay)){

                day.set(lessonNum, strMap.get(subjId));
            }
        }
        finalScheduleForm = "";
        formSchedule();

    }

    public List<List<String>> getWeekDays(){
        return weekDays;
    }
}
