package com.jetbrains.remote_executor.repository;

import com.jetbrains.remote_executor.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Integer> {

}
