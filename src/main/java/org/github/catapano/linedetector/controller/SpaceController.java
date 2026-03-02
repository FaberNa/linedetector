package org.github.catapano.linedetector.controller;

import jakarta.validation.Valid;
import org.github.catapano.linedetector.dto.LineDto;
import org.github.catapano.linedetector.dto.PointDto;
import org.github.catapano.linedetector.dto.PointRequest;
import org.github.catapano.linedetector.model.Point;
import org.springframework.web.bind.annotation.*;
import org.github.catapano.linedetector.service.SpaceService;

import java.util.List;

@RestController
public class SpaceController {
    private final SpaceService service;

    public SpaceController(SpaceService service) {
        this.service = service;
    }

    @PostMapping("/point")
    public PointDto addPoint(@Valid @RequestBody PointRequest req) {
        Point p = service.add(req.x(), req.y());
        return new PointDto(p.id(), p.x(), p.y());
    }

    @GetMapping("/space")
    public List<PointDto> space() {
        return service.allPoints().stream()
                .map(p -> new PointDto(p.id(), p.x(), p.y()))
                .toList();
    }

    @GetMapping("/lines/{n}")
    public List<LineDto> lines(@PathVariable int n) {
        return service.linesAtLeast(n);
    }

    @DeleteMapping("/space")
    public void clear() {
        service.clear();
    }
}