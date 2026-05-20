package com.glasses.service;

import com.glasses.dto.DataExportDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface DataService {
    DataExportDTO exportAllData();

    int importData(MultipartFile file, String mode) throws IOException;

    int resetAllData();
}
