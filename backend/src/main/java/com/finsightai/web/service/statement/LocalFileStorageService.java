package com.finsightai.web.service.statement;

import com.finsightai.web.dto.statement.StoredFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    private static final String BASE_DIR = "uploads/statements";

    @Override
    public StoredFile save(MultipartFile file, UUID userId, UUID statementId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл выписки не передан или пустой");
        }

        String originalFileName = file.getOriginalFilename();
        String storedFileName = statementId + ".pdf";

        Path userDirectory = Paths.get(BASE_DIR, userId.toString());
        Path targetPath = userDirectory.resolve(storedFileName);

        try {
            Files.createDirectories(userDirectory);

            Files.copy(
                    file.getInputStream(),
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return new StoredFile(
                    originalFileName,
                    storedFileName,
                    targetPath.toString(),
                    file.getContentType(),
                    file.getSize()
            );

        } catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить файл выписки", e);
        }

    }

    @Override
    public void delete(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return;
        }

        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            throw new RuntimeException("Не удалось удалить файл выписки", e);
        }
    }
}
