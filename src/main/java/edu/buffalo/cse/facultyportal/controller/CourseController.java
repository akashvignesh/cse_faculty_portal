package edu.buffalo.cse.facultyportal.controller;

import edu.buffalo.cse.facultyportal.dto.ActiveCourseDto;
import edu.buffalo.cse.facultyportal.dto.ApiResponseDto;
import edu.buffalo.cse.facultyportal.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping("/active")
    public ResponseEntity<ApiResponseDto<List<ActiveCourseDto>>> getActiveCourses() {
        List<ActiveCourseDto> result = courseService.getActiveCourses();
        return ResponseEntity.ok(ApiResponseDto.success("Active courses fetched successfully", result));
    }
}