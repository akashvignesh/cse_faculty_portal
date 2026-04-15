package edu.buffalo.cse.facultyportal.service;

import edu.buffalo.cse.facultyportal.dto.ActiveCourseDto;
import edu.buffalo.cse.facultyportal.repository.ActiveCourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final ActiveCourseRepository activeCourseRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ActiveCourseDto> getActiveCourses() {
        return activeCourseRepository.findActiveCourses().stream()
                .map(row -> ActiveCourseDto.builder()
                        .subject(trimToNull(row.getSubject()))
                        .courseName(buildCourseName(row.getPrimaryCatalogNumber(), row.getCourseTitleLong()))
                        .build())
                .toList();
    }

    private String buildCourseName(String primaryCatalogNumber, String courseTitleLong) {
        String catalogNumber = trimToEmpty(primaryCatalogNumber);
        String courseTitle = trimToEmpty(courseTitleLong);
        return catalogNumber + "-" + courseTitle;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String trimToEmpty(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}