package com.jetbrains.remote_executor.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.jetbrains.remote_executor.model.Job;
import com.jetbrains.remote_executor.repository.JobRepository;
import com.jetbrains.remote_executor.model.JobStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AsyncExecutor {
    
    private final JobRepository jobRepository ;
    
    public AsyncExecutor(JobRepository jobRepository){
        this.jobRepository = jobRepository;
        
    }

    @Async
    public void executeAsync(Job job, DockerClient dockerClient) {
        try {
            job.setStatus(JobStatus.IN_PROGRESS);
            jobRepository.save(job);

            CreateContainerResponse container = dockerClient.createContainerCmd("ubuntu")
                    .withCmd("sh", "-c", job.getCommand())
                    .withHostConfig(HostConfig.newHostConfig()
                            .withNanoCPUs((long) (job.getCpuCount() * 1e9)))
                    .exec();

            String containerId = container.getId();

            dockerClient.startContainerCmd(containerId).exec();

            StringBuilder output = new StringBuilder();

            dockerClient.logContainerCmd(containerId) //writing logs to output
                            .withStdOut(true)
                            .withStdErr(true)
                            .withFollowStream(true)
                            .exec(new ResultCallback.Adapter<Frame>(){
                                @Override
                                public void onNext(Frame frame){
                                    output.append(new String(frame.getPayload()));
                                }
                            })
                            .awaitCompletion();

            dockerClient.waitContainerCmd(container.getId())
                    .start()
                    .awaitCompletion();

            dockerClient.removeContainerCmd(container.getId()).exec();
            job.setOutput(output.toString());
            job.setStatus(JobStatus.FINISHED);
            jobRepository.save(job);
        } catch (Exception e){
            job.setStatus(JobStatus.FAILED);
            jobRepository.save(job);
            throw new RuntimeException("Execution failed", e);
        }
    }
}
