package org.example.menu;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.example.controllers.MainController;
import org.example.excel.Pair;
import org.example.university.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MainUI{
    private MainController controller;
    private BufferedReader reader;

    public MainUI(){
        try {
            controller = new MainController();
            reader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
            start();
        }
        catch(InvalidFormatException exc){
            exc.printStackTrace();
        }
        catch(IOException exc){
            exc.printStackTrace();
        }
        catch(InterruptedException exc){
            exc.printStackTrace();
        }

    }
    public void clearConsole(){

        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }
    public String[] removeDummy(String[] arr){

        for(int i = 0; i < arr.length; i++){
            if(arr[i].equals(""))
                arr[i] = null;
        }

        ArrayList<String> list = new ArrayList<>();
        for(String str: arr){
            if(str != null){
                list.add(str);
            }
        }

        return list.toArray(String[]::new);
    }

    private boolean checkPointsChangeInput(String input){
        String[] answers = input.split("\\s+");

        int actionChoice = Integer.valueOf(answers[0]);
        int subjectChoice = Integer.valueOf(answers[1]);

        if(actionChoice < 1 || actionChoice > 3 || subjectChoice < 1 || subjectChoice > controller.getSubjects().size())
            return false;
        if((actionChoice == 1 || actionChoice == 2) && answers.length == 2)
            return false;

        return true;
    }
    private boolean isGoodWeekDay(String str){

        switch(str){
            case "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday":
                return true;
            default:
                return false;
        }
    }

    private void addStudent(int group)throws IOException{

        System.out.println("Введите фамилию и имя нового студента(ну или только имя если фамилии нет)");
        String answer = reader.readLine();

        while(!Pattern.matches("\\s*[а-яА-Я]+(\\s*|\\s+[а-яА-Я]+\\s*)", answer)){
            System.out.println("Нормально введи!!");
            answer = reader.readLine();
        }

        String[] answers = removeDummy(answer.split("\\s+"));

        String fullName = "";
        if(answers.length == 2)
            fullName = answers[0] + " " + answers[1];
        else
            fullName = answers[0];

        controller.addNewStudent(group, fullName);


    }
    private void makeNewGroup()throws IOException{
        System.out.println("Введи 3-значный номер для новой группы");
        String answer = reader.readLine();

        int group;

        boolean badNumber = true;
        do {

            while (!Pattern.matches("\\d{3}", answer)){
                if (answer.equals("back"))
                    return;
                System.out.println("Такое нельзя вводить!");
                answer = reader.readLine();

            }
            group = Integer.valueOf(answer);

            if(!controller.isGroupExists(group))
                badNumber = false;
        }while(badNumber);

        controller.makeNewGroup(group, reader);
    }

    private void showStandartMenu(Pair<String, String> pair) throws IOException{

        System.out.println("\n=== ЖУРНАЛ Ь CHIK ===");
        System.out.println("1. Показать список групп");
        System.out.println("2. Показать список всех студентов");
        System.out.println("3. Показать список всех преподавателей");
        System.out.println("4. Показать список всех предметов");
        System.out.println("5. Сформировать новую группу");
        System.out.println("6. Выход");
        System.out.println("Выберите действие(пишите back если хотите вернуться в прошлое меню): ");
        String answer = reader.readLine();

        while(!Pattern.matches("\\b\\d{1}\\b", answer)) {
            if (answer.equals("back"))
                return;
            System.out.println("Такое нельзя вводить!!");
            answer = reader.readLine();
        }

        switch(answer){
            case "1":
                pair.setFirstEl("groupMenu");
                pair.setSecondEl("showGroups");
                break;
            case "2":
                pair.setFirstEl("contentMenu");
                pair.setSecondEl("showStudents");
                break;
            case "3":
                pair.setFirstEl("contentMenu");
                pair.setSecondEl("showTeachers");
                break;
            case "4":
                pair.setFirstEl("contentMenu");
                pair.setSecondEl("showSubjects");
                break;
            case "5":
                makeNewGroup();
                break;
            case "6":
                pair.setFirstEl("finishProg");
                break;
            default:
                System.out.println("Такое нельзя вводить");
        }

    }

    private void showGroupMenu(Pair<String, String> pair)throws IOException{

        if(pair.getSecondEl().equals("showGroups")){
            List<Integer> list = controller.getGroupNums().stream().sorted().toList();
            System.out.println("\n===========");
            for(int i = 1; i <= list.size(); i++){

                System.out.println(i + ". " + list.get(i-1));
            }
            System.out.println("===========");
            System.out.println("Для получения или изменения информации о группе,напишите номер группы\n");

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
            String answer = reader.readLine();
            int group;

            while(!Pattern.matches("\\b\\d{3}\\b", answer)) {

                if (answer.equals("back")) {
                    pair.setFirstEl("standartMenu");
                    pair.setSecondEl(null);
                    return;
                }
                System.out.println("Такое нельзя вводить!!");
                answer = reader.readLine();
            }

            group = Integer.valueOf(answer);

            if(!controller.getGroupNums().contains(group)){
                System.out.println("Нет такой группы");
                return;
            }

            if(!controller.isGroupPrepared(group))
                controller.prepareGroupInfo(group);
            pair.setSecondEl("showConcreteGroup!" + group);

        }
        else if(pair.getSecondEl().contains("showConcreteGroup")){//showConcreteGroup!Номергруппы

            String[] strs = pair.getSecondEl().split("!");
            int group = Integer.valueOf(strs[1]);

            System.out.println("==========");
            System.out.println("1. Показать список студентов данной группы");
            System.out.println("2. Показать расписание группы");
            System.out.println("3. Преподы у группы");
            System.out.println("4. Добавить нового студента в группу");
            System.out.println("==========");
            System.out.println("Выберите действие");

            String answer = reader.readLine();

            while(!Pattern.matches("\\s*\\d{1}\\s*", answer)) {
                if (answer.equals("back")){
                    pair.setSecondEl("showGroups");
                    return;
                }

                System.out.println("Такое нельзя вводить!!");
                answer = reader.readLine();
            }

            switch(answer){
                case "1":
                    pair.setSecondEl("showStudentsAndPoints!" + group);
                    break;
                case "2":
                    pair.setSecondEl("showSchedule!" + group);
                    break;
                case "3":
                    pair.setSecondEl("showTeachers!" + group);
                    break;
                case "4":
                    addStudent(group);
                    break;
                default:
                    System.out.println("Такое нельзя вводить!!");
                    break;
            }
        }
        else if(pair.getSecondEl().contains("showStudentsAndPoints")){//showStudentsAndPoints!Номергруппы
            String[] strs = pair.getSecondEl().split("!");
            int group = Integer.valueOf(strs[1]);

            List<Student> students = controller.getStudentsFrom(group).stream().sorted().toList();
            System.out.println("===========");

            if(students.size() == 0){
                System.out.println("Группа пока пустая");
            }
            else {

                for (int i = 1; i <= students.size(); i++) {

                    System.out.println(i + ". " + students.get(i - 1));
                }

            }

            System.out.println("============");
            System.out.println("Чтобы просмотреть оценки у студента введите его id");

            boolean studentState;
            int id;
            do {
                String answer = reader.readLine();

                while (!Pattern.matches("\\b\\d+\\b", answer)) {
                    if (answer.equals("back")) {
                        pair.setSecondEl("showConcreteGroup!" + group);
                        return;
                    }

                    System.out.println("Такое нельзя вводить!!");
                    answer = reader.readLine();
                }

                id = Integer.valueOf(answer);
                studentState = controller.isStudentInGroup(id, group);

                if (!studentState) {
                    System.out.println("Студента с таким id нет в группе ");
                }
            }while(!studentState);

            pair.setSecondEl("showStudentData!" + id + "!" + group);
        }
        else if(pair.getSecondEl().contains("showStudentData")){
            String[] strs = pair.getSecondEl().split("!");
            int studId = Integer.valueOf(strs[1]);
            int group = Integer.valueOf(strs[2]);

            System.out.println("========");
            System.out.println(controller.getStudent(studId));
            System.out.println("========");

            List<Subject> subjects = controller.getSubjects().stream().sorted().toList();

            for(int i = 1; i <= subjects.size(); i++){
                System.out.println(i + ". " + subjects.get(i - 1).toString() + ", : " + controller.getStudentPointsFrom(group, studId, subjects.get(i - 1).getId()));

            }
            System.out.println("=========");
            System.out.println("Выберите действие:\n" +
                    "1. Увеличить балл на сколько-нибудь\n" +
                    "2. Уменьшить балл на сколько-нибудь\n" +
                    "3. Установить баллы(если балл не указан, то ставится 0)\n" +
                    "4. Отчислить...\n" +
                    "==========\n" +
                    "Пишешь номер действия, id предмета и балл положительным вещественным или целым числом через пробелы, пример команды:\n" +
                    "\"1 3 4.3\" - увеличиваем балл на 4.3 в предмете algebra and geometry\n" +
                    "В случае отчисления введи просто 4");

            String answer = reader.readLine();

            if(Pattern.matches("\\s*4\\s*", answer)){
                controller.deductStudent(group, studId);
                pair.setSecondEl("showStudentsAndPoints!" + group);
                return;
            }

            while(!Pattern.matches("\\d\\s+\\d(\\s*|\\s+(\\d{1,2}\\.\\d|\\d{1,2}|))\\s*", answer) || !checkPointsChangeInput(answer)){
                if(answer.equals("back")){
                    pair.setSecondEl("showStudentsAndPoints!" + group);
                    return;
                }
                System.out.println("Некорректный ввод!!");

                answer = reader.readLine();
            }

            String[] answers = answer.split("\\s+");
            answers = removeDummy(answers);

            int subjChoice = Integer.valueOf(answers[1]);

            if(answers.length == 2){
                controller.setStudentPoints(group, studId, subjChoice, 0);
            }
            else {

                int actionChoice = Integer.valueOf(answers[0]);
                double newPoints = Double.valueOf(answers[2]);

                switch (actionChoice) {
                    case 1:
                        controller.changeStudentPoints(group, studId, subjChoice, newPoints);
                        break;
                    case 2:
                        controller.changeStudentPoints(group, studId, subjChoice, (-1) * newPoints);
                        break;
                    case 3:
                        controller.setStudentPoints(group, studId, subjChoice, newPoints);
                }
            }
        }
        else if(pair.getSecondEl().contains("showSchedule")){
            String[] strs = pair.getSecondEl().split("!");
            int group = Integer.valueOf(strs[1]);

            System.out.println("==========");
            System.out.println(controller.getSchedule(group));
            System.out.println("==========");
            System.out.println("Чтобы изменить расписание введи день нужный день недели и какую по счету пару хочешь изменить через запятую\n" +
                    "К примеру: \"Monday 3\"");

            String answer = reader.readLine();
            String weekDay;
            int lessonNum;
            boolean badInput = true;

            do {
                while (!Pattern.matches("\\s*[a-zA-Z]+\\s+\\d{1}\\s*", answer)) {
                    if (answer.equals("back")) {
                        pair.setSecondEl("showConcreteGroup!" + group);
                        return;
                    }
                    System.out.println("Такое нельзя вводить");
                    answer = reader.readLine();
                }

                String[] answers = removeDummy(answer.split("\\s+"));

                weekDay = answers[0];
                lessonNum = Integer.valueOf(answers[1]);

                if(isGoodWeekDay(weekDay) && lessonNum >= 1 && lessonNum <= 7)
                    badInput = false;
                else
                    System.out.println("Некорректно ввел!!");
            }while(badInput);
            System.out.println("Какой предмет поставить в эту ячейку");

            List<Subject> subjects = controller.getSubjects().stream().toList();

            for(int i = 1; i <= subjects.size(); i++){
                System.out.println(i + ". " + subjects.get(i-1));
            }
            System.out.println("Напиши id предмета который хочешь поставить в ячейку");
            answer = reader.readLine();

            while (!Pattern.matches("\\d{1}", answer)) {
                if (answer.equals("back")) {
                    pair.setSecondEl("showConcreteGroup!" + group);
                    return;
                }
                System.out.println("Такое нельзя вводить(вводи без пробелов)");
                answer = reader.readLine();
            }

            int subjId = Integer.valueOf(removeDummy(answer.split("\\s+"))[0]);
            controller.changeSchedule(group, weekDay, lessonNum, subjId);
        }
        else if(pair.getSecondEl().contains("showTeachers")){
            String[] strs = pair.getSecondEl().split("!");
            int group = Integer.valueOf(strs[1]);

            System.out.println("============");
            List<String> list = controller.getTeachersFrom(group);
            for(int i = 1; i <= list.size(); i++){

                System.out.println(i + ". " + list.get(i - 1));
            }
            System.out.println("============");
            System.out.println("Введи что угодно чтобы вернуться назад");
            reader.readLine();
            pair.setSecondEl("showConcreteGroup!" + group);
        }



    }

    private void showContentMenu(Pair<String, String>  pair)throws IOException{

        List<? extends UniversityElement> elements;

        if(pair.getSecondEl().equals("showStudents"))
            elements = controller.getStudents().stream().toList();
        else if(pair.getSecondEl().equals("showTeachers"))
            elements = controller.getTeachers().stream().toList();
        else if(pair.getSecondEl().equals("showSubjects"))
            elements = controller.getSubjects().stream().toList();
        else //просто чтобы было а то компилятор жалуется
            elements = new ArrayList<>(10);

        elements = elements.stream().sorted().toList();
        System.out.println("============");
        for(int i = 1; i <= elements.size(); i++){
            System.out.println(i + ". " + elements.get(i - 1));
        }
        System.out.println("============");
        System.out.println("Чтобы выйти в главное меню введи что-нибудь");
        reader.readLine();

        pair.setFirstEl("standartMenu");
        pair.setSecondEl(null);

    }

    public void start()throws InterruptedException, IOException, InvalidFormatException{//для определения какое меню вызывать и что делать
                        //используется объект Pair хранящий название меню в первом элементе пары и выбор из меню или данные для изменения во втором элементе пары
        Pair<String, String> operation = new Pair<>("standartMenu", null);
        boolean working = true;

        while(working){
            clearConsole();
            try {
                switch (operation.getFirstEl()) {
                    case "standartMenu":
                        showStandartMenu(operation);
                        break;
                    case "groupMenu":
                        showGroupMenu(operation);
                        break;
                    case "contentMenu":
                        showContentMenu(operation);
                        break;
                    default:
                        working = false;
                        break;
                }
            }
            catch(IOException exc){
                System.out.println("========Что-то пошло не так========");
                Thread.sleep(1000);
                System.out.println("А именно......");
                Thread.sleep(1000);
                exc.printStackTrace();
            }
        }

        controller.saveChanges();

    }


    public static void main(String[] args){
        new MainUI();

    }


}
