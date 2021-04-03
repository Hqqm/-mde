package com.hqqm.mde.services;

import com.hqqm.mde.config.FileUploadProperties;
import com.hqqm.mde.exceptions.FileNotFoundException;
import com.hqqm.mde.exceptions.FileStorageException;
import com.hqqm.mde.lib.MultipartFileToFileMapper;
import com.hqqm.mde.models.FileEntity;
import com.hqqm.mde.repositories.FileRepository;
import lombok.Getter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileSystemStorageService {

    @Getter
    private final Path dirLocation;
    private final FileRepository fileRepository;

    public FileSystemStorageService(FileUploadProperties fileUploadProperties, FileRepository fileRepository) {
        this.fileRepository = fileRepository;
        dirLocation = Paths.get(fileUploadProperties.getLocation())
                           .toAbsolutePath()
                           .normalize();
        try {
            Files.createDirectories(this.dirLocation);
        }
        catch (Exception ex) {
            throw new FileStorageException("Could not create upload dir!");
        }
    }

    public void saveFilesMetadataInDB(List<MultipartFile> files, Long engineId) {
        List<FileEntity> f = files.stream()
                .map(file -> MultipartFileToFileMapper.mapper(file, engineId, this.dirLocation))
                .collect(Collectors.toList());

        fileRepository.saveFiles(f);
    }

    public void saveFilesInFileSystem(List<MultipartFile> files) {
        try {
            for (MultipartFile file : files) {
                String filename = file.getOriginalFilename();
                Path dFile = this.dirLocation.resolve(filename);

                Files.copy(file.getInputStream(), dFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not upload file " + e.getMessage());
        }
    }

    public Resource loadFile(String fileName) {
        try {
            Path file = this.dirLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists())
                return resource;
            else
                throw new FileNotFoundException("Could not find file" + fileName);

        } catch (MalformedURLException e) {
            throw new FileNotFoundException("Could not download file " + fileName);
        }
    }
}
