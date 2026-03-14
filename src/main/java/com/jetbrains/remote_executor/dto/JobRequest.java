package com.jetbrains.remote_executor.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record JobRequest (
        @NotBlank(message = "Command cannot be empty")
        String command,

        @Min(value = 1, message = "CPU count must be at least 1")
        int cpuCount
){}
