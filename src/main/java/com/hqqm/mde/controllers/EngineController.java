package com.hqqm.mde.controllers;

import com.hqqm.mde.models.*;
import com.hqqm.mde.services.engine.impl.EngineFacade;
import com.hqqm.mde.services.engine.EngineService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
public class EngineController {
    private final EngineService engineService;
    private final EngineFacade engineFacade;

    @GetMapping("/engines")
    public List<EngineDTO> getEngines(RequestParamsForEngineFiltration engineParams) {
        return engineService.getEngines(engineParams);
    }

    @GetMapping("/engines/{id}")
    public List<EngineInfoTable> getEngine(@PathVariable Long id) {
        return engineService.getEngine(id);
    }

    @PostMapping("/engines")
    public Long saveEngine(SaveEngineRequestData saveEngineRequestData) {
        return engineFacade.saveEngine(saveEngineRequestData);
    }

    @PutMapping("/engines")
    public void updateEngine(UpdateEngineDTO updateEngineDTO) {
        engineService.updateEngine(updateEngineDTO);
    }

    @DeleteMapping("/engines/{id}")
    public void deleteEngine(@PathVariable Long id) {
        engineFacade.deleteEngine(id);
    }

    @GetMapping("/editEngine/{id}")
    public UpdateEngineDTO getEngineDataForUpdate(@PathVariable Long id) {
        return engineService.getEngineDataForUpdate(id);
    }
}