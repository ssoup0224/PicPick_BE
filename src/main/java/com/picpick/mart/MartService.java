package com.picpick.mart;

import com.picpick.api.s3.S3Service;
import com.picpick.martItem.MartItem;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted;

@Service
@AllArgsConstructor
@Slf4j
public class MartService {
    private final S3Service s3Service;
    private final MartRepository martRepository;
    private final MartMapper martMapper;

    public Long registerMart(SignupRequest request) {
        Mart mart = martMapper.toEntity(request);
        Mart savedMart = martRepository.save(mart);
        return savedMart.getId();
    }

    public void uploadExcelFile(MultipartFile file, Long martId) {
        Mart mart = martRepository.findById(martId)
                .orElseThrow(() -> new RuntimeException("Mart not found with ID: " + martId));

        try (InputStream is = file.getInputStream()) {
            byte[] fileBytes = file.getBytes();

            String s3Key = s3Service.uploadFile(file, "mart-documents");
            mart.setDocumentFile(s3Key);

            // 2. Parse Excel
            try (InputStream parseStream = new ByteArrayInputStream(fileBytes)) {
                List<MartItem> items = parseExcel(parseStream, mart);
                mart.getMartItems().addAll(items);

                martRepository.save(mart);
                log.info("Parsed {} items from Excel file for Mart ID: {}", items.size(), martId);
            }

        } catch (Exception e) {
            log.error("Error processing Excel file", e);
            throw new RuntimeException("Error processing Excel file", e);
        }
    }

    private List<MartItem> parseExcel(InputStream is, Mart mart)
            throws Exception {
        List<MartItem> items = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || row.getCell(0) == null)
                    continue;

                String itemName = formatter.formatCellValue(row.getCell(0)).trim();
                if (itemName.isEmpty())
                    continue;

                // Price (Column B)
                int price = 0;
                Cell priceCell = row.getCell(1);
                if (priceCell != null && priceCell.getCellType() == CellType.NUMERIC) {
                    price = (int) priceCell.getNumericCellValue();
                }

                // Start Date (Column C) & End Date (Column D)
                LocalDate startDate = LocalDate.now();
                LocalDate endDate = LocalDate.now().plusMonths(1);

                Cell startCell = row.getCell(2);
                if (startCell != null && startCell.getCellType() == CellType.NUMERIC
                        && isCellDateFormatted(startCell)) {
                    startDate = startCell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault())
                            .toLocalDate();
                }

                Cell endCell = row.getCell(3);
                if (endCell != null && endCell.getCellType() == CellType.NUMERIC
                        && isCellDateFormatted(endCell)) {
                    endDate = endCell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault())
                            .toLocalDate();
                }

                items.add(MartItem.builder()
                        .name(itemName)
                        .price(price)
                        .startDate(startDate)
                        .endDate(endDate)
                        .mart(mart)
                        .build());
            }
        }
        return items;
    }

    public void deleteUploadedFile(Long id) {
        log.info("Deleting file/content for ID: {}", id);
        if (martRepository.existsById(id)) {
            martRepository.deleteById(id);
        }
    }

    public MartDto viewMartInfo(Long id) {
        return martRepository.findById(id)
                .map(martMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Mart not found with ID: " + id));
    }

    public void updateLocation(com.picpick.user.UpdateLocationRequest request) {
        Mart mart = martRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Mart not found with ID: " + request.getUserId()));

        mart.setLatitude(request.getLatitude());
        mart.setLongitude(request.getLongitude());
        martRepository.save(mart);
    }

    public void deleteUploadedFileFromMart(Long martId) {
        Mart mart = martRepository.findById(martId)
                .orElseThrow(() -> new RuntimeException("Mart not found with ID: " + martId));

        // Clear the uploaded file reference
        if (mart.getDocumentFile() != null) {
            s3Service.deleteFile(mart.getDocumentFile());
            mart.setDocumentFile(null);
        }

        // Clear all mart items
        if (mart.getMartItems() != null) {
            mart.getMartItems().clear();
        }

        martRepository.save(mart);
        log.info("Deleted uploaded file and items for Mart ID: {}", martId);
    }

    public MartDto martLogin(MartLoginRequest request) {
        BigInteger registrationNumber = request.getRegistrationNumber();

        Mart mart = martRepository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(
                        () -> new RuntimeException("Mart not found with registration number: " + registrationNumber));

        log.info("Mart login successful: {}", mart.getName());
        return martMapper.toDto(mart);
    }
}
