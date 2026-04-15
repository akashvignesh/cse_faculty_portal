package edu.buffalo.cse.facultyportal.service;

import edu.buffalo.cse.facultyportal.dto.ActiveCourseDto;

import java.util.List;

public interface CourseService {

    List<ActiveCourseDto> getActiveCourses();
}