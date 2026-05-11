package com.finsightai.web.service.statement;

import com.finsightai.web.dto.statement.StoredFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface FileStorageService {

    StoredFile save(MultipartFile file, UUID userId, UUID statementId);
    void delete(String filePath);
}
