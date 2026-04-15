package edu.buffalo.cse.facultyportal.controller;

import edu.buffalo.cse.facultyportal.dto.ActiveCourseDto;
import edu.buffalo.cse.facultyportal.exception.GlobalExceptionHandler;
import edu.buffalo.cse.facultyportal.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CourseControllerIntegrationTest {

        private CourseService courseService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
                courseService = mock(CourseService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new CourseController(courseService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getActiveCoursesReturnsWrappedResponse() throws Exception {
        when(courseService.getActiveCourses()).thenReturn(List.of(
                ActiveCourseDto.builder().subject("CSE").courseName("521-Operating Systems").build(),
                ActiveCourseDto.builder().subject("CSE").courseName("442-Software Engineering").build()));

        mockMvc.perform(get("/api/v1/courses/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Active courses fetched successfully"))
                .andExpect(jsonPath("$.data[0].subject").value("CSE"))
                .andExpect(jsonPath("$.data[0].courseName").value("521-Operating Systems"))
                .andExpect(jsonPath("$.data[1].subject").value("CSE"))
                .andExpect(jsonPath("$.data[1].courseName").value("442-Software Engineering"));
    }
}