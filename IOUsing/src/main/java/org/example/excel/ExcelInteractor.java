package org.example.excel;

//класс готов думаю
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class ExcelInteractor {

    private static boolean instanceState = false;//чтобы во время работы программы был только один объект
    private ObjectMapper mapper;

    private final String contentFileName = "data\\UniversityContents.xlsx";
    private final String groupFileName = "data\\Journal%d.xlsx";//нужно форматировать

    public static boolean isCreated(){
        return instanceState;
    }

    private List<Pair<String, String>> jsonsPairs;

    public ExcelInteractor(){

        if(isCreated())
            throw new IllegalCallerException("Interactor has been created already");

        instanceState = true;

        mapper = new ObjectMapper();

        try(InputStream input = new FileInputStream(contentFileName)) {
            Workbook wb = new XSSFWorkbook(input);
            parseContents(wb);

        }
        catch(JsonProcessingException exc){
            exc.printStackTrace();
        }
        catch(IOException exc){
            exc.printStackTrace();
        }

    }
    //создает список пар - "имя листа - json содержимого" для эксельки с "контентом" университета
    private void parseContents(Workbook wb)throws JsonProcessingException {

        jsonsPairs = new ArrayList<>();
        Pair<String, String> currentPair;

        List<List<String>>  strSheet;
        List<String> strRow;

        for(Sheet sheet: wb){
            strSheet = new ArrayList<>();

            for(int i = 1; i < sheet.getPhysicalNumberOfRows(); i++){
                strRow = new ArrayList<>();

                for(Cell cell: sheet.getRow(i)){
                    if(cell.getCellType() == CellType.STRING)
                        strRow.add(cell.getStringCellValue());

                    else
                        strRow.add(String.valueOf(cell.getNumericCellValue()));
                }

                strSheet.add(strRow);
            }

            String json = mapper.writeValueAsString(strSheet);
            jsonsPairs.add(new Pair<String, String>(sheet.getSheetName(), json));

        }

        this.jsonsPairs = jsonsPairs;

    }
    //возвращаем конечный json всей эксельки
    public String getUniversityContents()throws JsonProcessingException{
        String str = mapper.writeValueAsString(jsonsPairs);
        jsonsPairs = null;
        return str;
    }

    //создает список пар - "имя листа - json содержимого" для эксельки с информацией каждой группы
    public void parseGroup(int group){//у всех листов пропускает заголовочную строку
        jsonsPairs = new ArrayList<>();
        try(InputStream input = new FileInputStream(groupFileName.formatted(group))){

            Workbook wb = new XSSFWorkbook(input);

            List<List<String>> strSheet;
            List<String> strRow;
            int i;

            for(Sheet sheet: wb){
                strSheet = new ArrayList<>();

                if(sheet.getSheetName().equals("CommonPoints"))
                    i = 0;
                else
                    i = 1;

                for(; i < sheet.getPhysicalNumberOfRows(); i++){
                    strRow = new ArrayList<>();

                    for(Cell cell: sheet.getRow(i)){

                        if(cell.getCellType() == CellType.STRING)
                            strRow.add(cell.getStringCellValue());

                        else
                            strRow.add(String.valueOf(cell.getNumericCellValue()));

                    }

                    strSheet.add(strRow);

                }

                String sheetJson = mapper.writeValueAsString(strSheet);
                jsonsPairs.add(new Pair(sheet.getSheetName(), sheetJson));
            }
        }
        catch(IOException exc){
            exc.printStackTrace();
        }
    }

    public String getGroupInfo(int group)throws JsonProcessingException{
        parseGroup(group);
        String str = mapper.writeValueAsString(jsonsPairs);
        jsonsPairs = null;
        return str;
    }
    //если информация о группах то название таблички будет заканчиваться на номер группы.
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
}
