package tuneandmanner.wiselydiarybackend.rag.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tuneandmanner.wiselydiarybackend.common.exception.dto.response.ExceptionResponse;
import tuneandmanner.wiselydiarybackend.rag.service.VectorStoreService;
import tuneandmanner.wiselydiarybackend.common.exception.DocumentUploadException;
import tuneandmanner.wiselydiarybackend.common.exception.type.ExceptionCode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/rag")
public class RAGController {

    private final VectorStoreService vectorStoreService;
    private static final List<String> VALID_STORE_TYPES = Arrays.asList("letter", "image");

    public RAGController(VectorStoreService vectorStoreService) {
        this.vectorStoreService = vectorStoreService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file, @RequestParam("storeType") String storeType) {
        if (!VALID_STORE_TYPES.contains(storeType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid store type. Valid types are: letter, image");
        }

        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            String fileName = file.getOriginalFilename();

            String result = vectorStoreService.addDocumentFromText(content, fileName, storeType.toLowerCase());
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            throw new DocumentUploadException(ExceptionCode.DOCUMENT_UPLOAD_ERROR);
        }
    }

    @ExceptionHandler(DocumentUploadException.class)
    public ResponseEntity<ExceptionResponse> handleDocumentUploadException(DocumentUploadException e) {
        final ExceptionResponse exceptionResponse = ExceptionResponse.of(e.getCode(), e.getMessage());
        return ResponseEntity.badRequest().body(exceptionResponse);
    }
}