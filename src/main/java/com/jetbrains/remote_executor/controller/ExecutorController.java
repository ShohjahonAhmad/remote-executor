package com.jetbrains.remote_executor.controller;

import com.jetbrains.remote_executor.service.ExecutorService;
import com.jetbrains.remote_executor.model.Job;
import com.jetbrains.remote_executor.exception.JobNotFoundException;
import com.jetbrains.remote_executor.dto.JobRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/execute")
public class ExecutorController {
    private final ExecutorService executorService;

    public ExecutorController(ExecutorService executorService){
        this.executorService = executorService;
    }

    @PostMapping
    public Job submit(@Valid @RequestBody JobRequest request) {
        return executorService.submit(request.command(), request.cpuCount());
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<?> getStatus(@PathVariable int id){
        try {
            return ResponseEntity.ok(executorService.getStatus(id));
        } catch (JobNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getJob(@PathVariable int id){
        try {
            return ResponseEntity.ok(executorService.getJob(id));
        } catch(JobNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
