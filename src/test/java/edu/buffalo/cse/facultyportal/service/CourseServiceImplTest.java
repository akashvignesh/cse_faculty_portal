package edu.buffalo.cse.facultyportal.service;

import edu.buffalo.cse.facultyportal.dto.ActiveCourseDto;
import edu.buffalo.cse.facultyportal.repository.ActiveCourseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock
    private ActiveCourseRepository activeCourseRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    @Test
    void getActiveCoursesMapsAndTrimsResults() {
        when(activeCourseRepository.findActiveCourses()).thenReturn(List.of(
                course(" CSE ", " 521 ", " Operating Systems "),
                course("CSE", "442", null)));

        List<ActiveCourseDto> result = courseService.getActiveCourses();

        assertEquals(2, result.size());
        assertEquals("CSE", result.get(0).getSubject());
        assertEquals("521-Operating Systems", result.get(0).getCourseName());
        assertEquals("CSE", result.get(1).getSubject());
        assertEquals("442-", result.get(1).getCourseName());
    }

    @Test
    void getActiveCoursesReturnsNullSubjectWhenBlank() {
        when(activeCourseRepository.findActiveCourses()).thenReturn(List.of(course("   ", "101", "Intro")));

        List<ActiveCourseDto> result = courseService.getActiveCourses();

        assertNull(result.getFirst().getSubject());
    }

    private static ActiveCourseRepository.ActiveCourseProjection course(
            String subject,
            String primaryCatalogNumber,
            String courseTitleLong) {
        return new ActiveCourseRepository.ActiveCourseProjection() {
            @Override
            public String getSubject() {
                return subject;
            }

            @Override
            public String getPrimaryCatalogNumber() {
                return primaryCatalogNumber;
            }

            @Override
            public String getCourseTitleLong() {
                return courseTitleLong;
            }
        };
    }
}