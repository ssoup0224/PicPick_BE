package com.picpick.services;

import com.picpick.api.aws.S3Service;
import com.picpick.entities.Mart;
import com.picpick.entities.MartItem;
import com.picpick.repositories.MartRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MartRegisterService {
    private final S3Service s3Service;
    private final MartRepository martRepository;

    public Mart registerMartWithFile(String name,
            String address,
            String brn,
            MultipartFile file) throws Exception {

        // 1. 파일을 S3에 업로드
        String s3Key = s3Service.uploadFile(file, "mart-documents");

        // 2. Mart 엔티티 생성
        Mart mart = Mart.builder()
                .name(name)
                .address(address)
                .brn(brn)
                .documentFile(s3Key)
                .build();

        // 3. 엑셀에서 MartItem 리스트 추출
        List<MartItem> items = parseExcel(file.getInputStream(), mart);
        mart.setMartItems(items);

        // 4. 저장 (cascade = ALL 로 MartItem까지 저장)
        return martRepository.save(mart);
    }

    private List<MartItem> parseExcel(InputStream is, Mart mart) throws Exception {
        List<MartItem> items = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0); // 첫 번째 시트

            boolean first = true;
            for (Row row : sheet) {
                if (first) { // 헤더 스킵
                    first = false;
                    continue;
                }

                if (row == null || row.getCell(0) == null) {
                    continue;
                }

                String itemName = getString(row.getCell(0));
                if (itemName.isBlank())
                    continue;

                int price = (int) row.getCell(1).getNumericCellValue();
                LocalDate startDate = LocalDate.parse(getString(row.getCell(2)));
                LocalDate endDate = LocalDate.parse(getString(row.getCell(3)));

                Integer discount = null;
                Cell discountCell = row.getCell(5);
                if (discountCell != null && discountCell.getCellType() != CellType.BLANK) {
                    double d = discountCell.getNumericCellValue(); // 예: 0.202
                    discount = (int) Math.round(d * 100); // 20.2 → 20 으로 저장 등 정책은 자유
                }

                MartItem item = MartItem.builder()
                        .itemName(itemName)
                        .itemPrice(price)
                        .startDate(startDate)
                        .endDate(endDate)
                        .discountPercentage(discount)
                        .mart(mart)
                        .build();

                items.add(item);
            }
        }

        return items;
    }

    private String getString(Cell cell) {
        if (cell == null)
            return "";
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate().toString();
            }
            // 숫자를 그냥 문자열로
            long l = (long) cell.getNumericCellValue();
            return String.valueOf(l);
        } else {
            return cell.toString().trim();
        }
    }
}
