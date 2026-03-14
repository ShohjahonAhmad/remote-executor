package com.jetbrains.remote_executor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Job {
    @Id @GeneratedValue
    private int id;

    @Column(columnDefinition = "TEXT")
    private String command;

    private int cpuCount;

    private JobStatus status;

    @Column(columnDefinition = "TEXT")
    private String output;

    public Job(String command, int cpuCount) {
        this.command = command;
        this.cpuCount = cpuCount;
        this.status = JobStatus.QUEUED;
    }

}

