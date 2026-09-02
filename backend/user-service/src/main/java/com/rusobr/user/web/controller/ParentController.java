package com.rusobr.user.web.controller;

import com.rusobr.user.application.service.parent.ParentService;
import com.rusobr.user.web.dto.parent.ParentDetails;
import com.rusobr.user.web.dto.parent.ParentInfoResponse;
import com.rusobr.user.web.dto.parent.ParentResponse;
import com.rusobr.user.web.dto.user.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/unasigned-to-student")
    public Page<UserResponse> getUnassignedToStudentParents(Pageable pageable,
                                                            @RequestParam(required = false) String search) {
        return parentService.getUnassignedToStudent(pageable, search);
    }

}
