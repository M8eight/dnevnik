package com.rusobr.user.controller;

import com.rusobr.common.context.CurrentStudentContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public abstract class ControllerTestBase {

    @MockitoBean
    protected CurrentStudentContext currentStudentContext;
}