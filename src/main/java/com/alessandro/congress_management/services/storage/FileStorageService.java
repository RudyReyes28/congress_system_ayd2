package com.alessandro.congress_management.services.storage;

import com.alessandro.congress_management.exceptions.FileStorageException;
import org.springframework.web.multipart.MultipartFile;


public interface FileStorageService {


    String uploadFile(MultipartFile file, String folder) throws FileStorageException;


    void deleteFile(String fileUrl) throws FileStorageException;
}