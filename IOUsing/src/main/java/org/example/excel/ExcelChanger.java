package org.example.excel;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class ExcelChanger {
    //работает только в конце программы
    private static boolean instanceState = false;//чтобы во время работы программы был только один объект
    private ObjectMapper mapper;

    private final String contentFileName = "data\\UniversityContents.xlsx";
    private final String groupFileName = "data\\Journal%d.xlsx";//нужно форматировать

    public static boolean isCreated(){
        return instanceState;
    }

    private List<Pair<String, String>> jsonsPairs;

    public ExcelChanger(){
        if(isCreated())
            throw new IllegalCallerException("Interactor has been created already");

        instanceState = true;

        mapper = new ObjectMapper();
    }

    //если информация о группах то название таблички и номер группы.
    //метод вызывается в конце работы журнала
    public void pushChanges(String json)throws IOException, InvalidFormatException {
        HashMap<Integer, Workbook> workbooks = new HashMap<>();
        String data;//для хранения значений ячейки

        List<Pair<String, String>> jsonPairs = mapper.readValue(json, new TypeReference<List<Pair<String, String>>>(){});
        //второй элемент пары представляет собой json-ованный
        //List<List<String>>
        OutputStream fileOutput;
        File file;

        List<List<String>> sheetData;

        for(Pair<String, String> pair: jsonPairs){
            String str = pair.getFirstEl();//номерГруппы, если мы формируем новую группу
            //названиеЛиста!НомерГруппы, если мы изменили данные у существующей группы
            //students, если мы изменили "контент" университета в данном случае только список студентов
            if(Pattern.matches("[a-zA-Z]+!\\d{3}", str)){//изменения у существующей группы
                Workbook wb;
                String[] splitted = str.split("!");

                String sheetName = splitted[0];
                int group = Integer.valueOf(splitted[1]);

                if(workbooks.get(group) == null){

                    workbooks.put(group, new XSSFWorkbook(new FileInputStream(groupFileName.formatted(group))));
                }

                wb = workbooks.get(group);
                wb.removeSheetAt(wb.getSheetIndex(sheetName));

                Sheet sheet = wb.createSheet(sheetName);
                sheetData = mapper.readValue(pair.getSecondEl(), new TypeReference<List<List<String>>>(){});

                for(int i = 0; i < sheetData.size(); i++){
                    Row currRow;

                    currRow = sheet.createRow(i);

                    for(int j = 0; j < sheetData.get(i).size(); j++){
                        data = sheetData.get(i).get(j);

                        if(Pattern.matches("\\d+|\\d+\\.\\d+|-1", data))
                            currRow.createCell(j).setCellValue(Double.parseDouble(data));
                        else
                            currRow.createCell(j).setCellValue(sheetData.get(i).get(j));
                    }
                }

                try(FileOutputStream output = new FileOutputStream(groupFileName.formatted(group))) {
                    workbooks.get(group).write(output);
                }

            }
            else if(Pattern.matches("\\d{3}", str)){//создание новой группы
                int group = Integer.valueOf(str);
                fileOutput = new FileOutputStream(groupFileName.formatted(group));

                makeNewGroupJournal(pair.getSecondEl(), fileOutput);

            }
            else {
                File contentFile = new File(contentFileName);
                Workbook wb = new XSSFWorkbook(new FileInputStream(contentFile));

                String sheetName = pair.getFirstEl();
                wb.removeSheetAt(wb.getSheetIndex(sheetName));
                Sheet sheet = wb.createSheet(sheetName);

                sheetData = mapper.readValue(pair.getSecondEl(), new TypeReference<List<List<String>>>(){});

                for(int i = 0; i < sheetData.size(); i++){
                    Row currRow;

                    currRow = sheet.createRow(i);
                    for(int j = 0;j < sheetData.get(i).size(); j++){
                        data = sheetData.get(i).get(j);

                        if(Pattern.matches("\\d+|\\d+\\.\\d+|-1", data))
                            currRow.createCell(j).setCellValue(Double.parseDouble(data));
                        else
                            currRow.createCell(j).setCellValue(data);
                    }
                }

                try(FileOutputStream outputStream = new FileOutputStream(contentFile)){
                    wb.write(outputStream);
                }

                wb.close();
            }

        }

        Collection<Workbook> wbCollection = workbooks.values();

        for(Workbook wb: wbCollection)
            wb.close();

    }

    private void makeNewGroupJournal(String json, OutputStream excelOutput)throws IOException{
        Workbook wb = new XSSFWorkbook();
        List<List<String>> wbData = mapper.readValue(json, new TypeReference<List<List<String>>>(){});
        /*данные будут храниться так:
         *каждая "строка" List - ряд в листе
         * чтобы понять где начинается новый лист
         * в первом элементе вложенного List будет дополнительно лежать
         * подстрока - названиеЛиста - в начале строки
         * а потом содержимое листа
         *
         *CommonPoints
         *Schedule
         *Teachers
         */

        Sheet currentSheet = wb.createSheet("dummy");
        Row currentRow;
        int indexImportant = 0;

        for(int j = 0; j < wbData.size(); j++){
            List<String> list = wbData.get(j);

            if(list.get(0).contains("CommonPoints")){
                indexImportant = 0;
                currentSheet = wb.createSheet("CommonPoints");
                list.set(0, list.get(0).replace("CommonPoints", ""));
            }
            else if(list.get(0).contains("Schedule")){
                indexImportant = 0;
                currentSheet = wb.createSheet("Schedule");
                list.set(0, list.get(0).replace("Schedule", ""));
            }
            else if(list.get(0).contains("Teachers")){
                indexImportant = 0;
                currentSheet = wb.createSheet("Teachers");
                list.set(0, list.get(0).replace("Teachers", ""));
            }

            currentRow = currentSheet.createRow(indexImportant);

            for(int i = 0; i < list.size(); i++){
                String str = list.get(i);

                if(Pattern.matches("\\d+|\\d+\\.\\d+|-1", str))
                    currentRow.createCell(i).setCellValue(Double.parseDouble(str));
                else
                    currentRow.createCell(i).setCellValue(str);
            }

            indexImportant++;
        }

        wb.removeSheetAt(wb.getSheetIndex("dummy"));
        wb.write(excelOutput);
        wb.close();
        excelOutput.close();

    }

    public void removeGroup(int group){
        File f = new File(groupFileName.formatted(group));
        f.delete();
    }
}
