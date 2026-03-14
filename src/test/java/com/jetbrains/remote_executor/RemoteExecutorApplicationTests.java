package com.jetbrains.remote_executor;

import com.jetbrains.remote_executor.model.Job;
import com.jetbrains.remote_executor.repository.JobRepository;
import com.jetbrains.remote_executor.service.AsyncExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class RemoteExecutorApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AsyncExecutor asyncExecutor;

	private final JobRepository jobRepository;

	@Autowired
	public RemoteExecutorApplicationTests(JobRepository jobRepository){
		this.jobRepository = jobRepository;
	}

	@Test
	void testThatsubmitJobReturnsQueuedJob() throws Exception {
		String requestJson = "{\"command\": \"echo hello\", \"cpuCount\": 1}";
		mockMvc.perform(post("/execute")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("QUEUED"))
				.andExpect(jsonPath("$.cpuCount").value(1))
				.andExpect(jsonPath("$.command").value("echo hello"));
	}

	@Test
	void testThatsubmitJobReturnsBadRequest() throws Exception {
		String requestJson = "{\"command\": \"\", \"cpuCount\": -1}";
		mockMvc.perform(post("/execute")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isBadRequest());
	}

	@Test
	void testThatgetStatusReturnsStatus() throws Exception {
		Job job = jobRepository.save(new Job("echo hello", 1));
		mockMvc.perform(get("/execute/" + job.getId() + "/status"))
				.andExpect(status().isOk())
				.andExpect(content().string("\"QUEUED\""));
	}

	@Test
	void testThatgetStatusReturnsStatusCode404() throws Exception {
		mockMvc.perform(get("/execute/999/status"))
				.andExpect(status().isNotFound());
	}

	@Test
	void testThatgetJobReturnsJobDetails() throws Exception {
		Job job = jobRepository.save(new Job("echo hello", 1));
		mockMvc.perform(get("/execute/" + job.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(job.getId()))
				.andExpect(jsonPath("$.command").value(job.getCommand()))
				.andExpect(jsonPath("$.output").doesNotExist())
				.andExpect(jsonPath("$.cpuCount").value(job.getCpuCount()));
	}

	@Test
	void testThatgetJobReturnsStatusCode404() throws Exception {
		mockMvc.perform(get("/execute/999"))
				.andExpect(status().isNotFound());
	}
}
