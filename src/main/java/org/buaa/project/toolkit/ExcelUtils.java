package org.buaa.project.toolkit;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Slf4j
@Component
public class ExcelUtils {

    private final Set<String> studentIds = new HashSet<>();

    private final String filePath = "/义工.xlsx";

    @PostConstruct
    public void init() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                return;
            }

            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter dataFormatter = new DataFormatter();

            Iterator<Row> rowIterator = sheet.iterator();

            // 跳过标题行
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Cell cell = row.getCell(0);

                if (cell != null) {
                    String studentId = dataFormatter.formatCellValue(cell).trim();
                    if (!studentId.isEmpty()) {
                        studentIds.add(studentId);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("读取义工文件时发生错误", e);
        }
    }

    public boolean isVolunteer(String id) {
        return studentIds.contains(id);
    }


}