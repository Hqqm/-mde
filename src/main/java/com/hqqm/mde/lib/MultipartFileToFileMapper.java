package com.hqqm.mde.lib;

import com.hqqm.mde.models.FileEntity;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public class MultipartFileToFileMapper {
    public static FileEntity mapper(MultipartFile file, Long engineId, Path location) {
        FileEntity f = new FileEntity();
        String filename = file.getOriginalFilename();

        f.setEngineId(engineId);
        f.setName(filename);
        f.setLocation(location.resolve(filename).toString());
        f.setContentType(file.getContentType());

        return f;
    }
}
