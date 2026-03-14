package com.jetbrains.remote_executor.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.jetbrains.remote_executor.exception.JobNotFoundException;
import com.jetbrains.remote_executor.model.Job;
import com.jetbrains.remote_executor.model.JobStatus;
import com.jetbrains.remote_executor.repository.JobRepository;
import org.springframework.stereotype.Service;

@Service
public class ExecutorService {

    private final JobRepository jobRepository;
    private final DockerClient dockerClient;
    private final AsyncExecutor asyncExecutor;

    public ExecutorService(JobRepository jobRepository, AsyncExecutor asyncExecutor){
        this.jobRepository = jobRepository;
        this.asyncExecutor = asyncExecutor;

        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        ApacheDockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .build();
        this.dockerClient = DockerClientImpl.getInstance(config, httpClient);
    }

    public Job submit(String command, int cpuCount){
        Job job = jobRepository.save(new Job(command, cpuCount));
        asyncExecutor.executeAsync(job, dockerClient);
        return job;
    }

    public JobStatus getStatus(int id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException(id)).getStatus();
    }


    public Job getJob(int id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException(id));
    }
}
