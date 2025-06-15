package org.example.controller_testing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelParser {
    private static boolean instanceState = false;//чтобы во время работы программы был только один объект
    private ObjectMapper mapper;

    private final String contentFileName = "data\\UniversityContents.xlsx";
    private final String groupFileName = "data\\Journal%d.xlsx";//нужно форматировать

    public static boolean isCreated(){
        return instanceState;
    }

    private List<Pair<String, String>> jsonsPairs;

    public ExcelParser(){

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
}
