package com.hqqm.mde.services.engine;

import com.hqqm.mde.lib.AfterUpdateEngineTransactionFilesResolver;
import com.hqqm.mde.lib.AfterUpdateEngineTransactionImageResolver;
import com.hqqm.mde.lib.CleanupTransactionListener;
import com.hqqm.mde.lib.FromRequestParamsMappers.FromSaveEngineReqToEngineMapper;
import com.hqqm.mde.models.Engine;
import com.hqqm.mde.models.SaveEngineRequestData;
import com.hqqm.mde.services.FileStorageService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EngineFacade {
    private final EngineService engineService;
    private final FileStorageService fileStorageService;

    @Transactional(rollbackFor = Exception.class)
    public Long saveEngine(SaveEngineRequestData saveEngineRequestData) {
        Engine engine = FromSaveEngineReqToEngineMapper.map(saveEngineRequestData);
        Path pathToEngineImage = fileStorageService.saveEngineImageInFileSystem(saveEngineRequestData.getImage());
        engine.setPathToImage(pathToEngineImage.toString());
        Long engineId = engineService.saveEngine(engine);

        List<MultipartFile> files = saveEngineRequestData.getFiles();
        if (files != null) {
            Path mainDir = fileStorageService.getDirLocation();
            List<Path> filePaths = files.stream()
                    .map(file -> mainDir.resolve(file.getOriginalFilename()))
                    .collect(Collectors.toList());
            filePaths.add(pathToEngineImage);

            var cleanupTransactionListener = new CleanupTransactionListener(filePaths);
            TransactionSynchronizationManager.registerSynchronization(cleanupTransactionListener);
            fileStorageService.saveFilesMetadataInDB(files, engineId);
            fileStorageService.saveFilesInFileSystem(files);
        }

        return engineId;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateEngine(SaveEngineRequestData saveEngineRequestData) {
        Engine engine = FromSaveEngineReqToEngineMapper.map(saveEngineRequestData);
        MultipartFile engineImage = saveEngineRequestData.getImage();
        Path pathToEngineImage = fileStorageService.updateEngineImageInFileSystem(engineImage);
        engine.setPathToImage(pathToEngineImage.toString());
        engineService.updateEngine(engine);
        var imageResolver = new AfterUpdateEngineTransactionImageResolver(
                fileStorageService.getTmpLocation(),
                fileStorageService.getImagesLocation(),
                engineImage.getOriginalFilename()
        );
        TransactionSynchronizationManager.registerSynchronization(imageResolver);

        List<MultipartFile> files = saveEngineRequestData.getFiles();
        if (files != null) {
            List<String> filenames = files.stream()
                    .map(MultipartFile::getOriginalFilename)
                    .collect(Collectors.toList());

            var filesResolver = new AfterUpdateEngineTransactionFilesResolver(
                    fileStorageService.getTmpLocation(),
                    fileStorageService.getDirLocation(),
                    filenames
            );
            TransactionSynchronizationManager.registerSynchronization(filesResolver);
            fileStorageService.updateFilesInFileSystem(files);
            fileStorageService.deleteEngineFiles(saveEngineRequestData.getEngineId());
            fileStorageService.saveFilesMetadataInDB(files, saveEngineRequestData.getEngineId());
        }
    }
}
