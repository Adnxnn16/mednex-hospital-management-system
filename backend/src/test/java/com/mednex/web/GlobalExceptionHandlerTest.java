package com.mednex.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mednex.service.ConflictException;
import com.mednex.service.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@Import({ RestExceptionHandler.class, MethodValidationPostProcessor.class })
class GlobalExceptionHandlerTest {

	@Autowired MockMvc mvc;
	@Autowired ObjectMapper mapper;

	@MockBean
	GlobalExceptionHandlerTest.TestController testController;

	@Test
	@WithMockUser(authorities = "ROLE_ADMIN")
	void notFound_returns_404_and_body_shape() throws Exception {
		when(testController.triggerNotFound()).thenThrow(new NotFoundException("Patient not found"));

		MvcResult result = mvc.perform(get("/test/not-found").with(csrf()))
			.andExpect(status().isNotFound())
			.andReturn();

		Map<String, Object> body = mapper.readValue(result.getResponse().getContentAsString(), Map.class);
		assertThat(body).containsKeys("timestamp", "status", "error", "message", "path");
		assertThat(body.get("status")).isEqualTo(404);
		assertThat(body.get("error")).isEqualTo("Not Found");
		assertThat(body.get("message")).isEqualTo("Patient not found");
	}

	@Test
	@WithMockUser(authorities = "ROLE_ADMIN")
	void conflict_returns_409_and_body_shape() throws Exception {
		when(testController.triggerConflict()).thenThrow(new ConflictException("Time slot unavailable"));

		MvcResult result = mvc.perform(get("/test/conflict").with(csrf()))
			.andExpect(status().isConflict())
			.andReturn();

		Map<String, Object> body = mapper.readValue(result.getResponse().getContentAsString(), Map.class);
		assertThat(body).containsKeys("timestamp", "status", "error", "message", "path");
		assertThat(body.get("status")).isEqualTo(409);
		assertThat(body.get("error")).isEqualTo("Conflict");
		assertThat(body.get("message")).isEqualTo("Time slot unavailable");
	}

	@Test
	@WithMockUser(authorities = "ROLE_USER")
	void accessDenied_returns_403() throws Exception {
		when(testController.triggerAccessDenied()).thenThrow(new AccessDeniedException("Access denied"));

		MvcResult result = mvc.perform(get("/test/access-denied").with(csrf()))
			.andExpect(status().isForbidden())
			.andReturn();

		Map<String, Object> body = mapper.readValue(result.getResponse().getContentAsString(), Map.class);
		assertThat(body.get("status")).isEqualTo(403);
		assertThat(body.get("error")).isEqualTo("Forbidden");
	}

	@Test
	@WithMockUser(authorities = "ROLE_ADMIN")
	void validation_returns_400_with_fieldErrors() throws Exception {
		mvc.perform(post("/test/validate")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}")
				.with(csrf()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.error").value("Bad Request"))
			.andExpect(jsonPath("$.fieldErrors").isArray())
			.andExpect(jsonPath("$.fieldErrors[0].field").exists())
			.andExpect(jsonPath("$.fieldErrors[0].message").exists());
	}

	@RestController
	@RequestMapping("/test")
	static class TestController {
		@GetMapping("/not-found")
		String triggerNotFound() { return "ok"; }

		@GetMapping("/conflict")
		String triggerConflict() { return "ok"; }

		@GetMapping("/access-denied")
		String triggerAccessDenied() { return "ok"; }

		@PostMapping("/validate")
		String validate(@jakarta.validation.Valid @RequestBody ValidRequest req) { return "ok"; }
	}

	record ValidRequest(@jakarta.validation.constraints.NotBlank String name) {}
}
