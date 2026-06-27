package com.rusobr.user.web.controller;

import com.rusobr.user.application.service.parent.ParentService;
import com.rusobr.user.web.dto.parent.ParentDetails;
import com.rusobr.user.web.dto.parent.ParentInfoResponse;
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
    public ParentResponse getWithUserById(@PathVariable Long id) {
        return parentService.getWithUserById(id);
    }

    @GetMapping("/{id}/details")
    public ParentDetails getDetailsById(@PathVariable Long id) {
        return parentService.getDetailsById(id);
    }

    @GetMapping("/{id}/info")
    public ParentInfoResponse getInfoById(@PathVariable Long id) {
        return parentService.getInfoById(id);
    }

}
