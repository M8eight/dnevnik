package com.rusobr.user.web.controller;

import com.rusobr.user.infrastructure.service.parent.ParentService;
import com.rusobr.user.web.dto.parent.ParentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/parents")
public class ParentController {

    private final ParentService parentService;

    @GetMapping("/{id}")
    public ParentResponse getParent(@PathVariable Long id) {
        return parentService.getParent(id);
    }

}
