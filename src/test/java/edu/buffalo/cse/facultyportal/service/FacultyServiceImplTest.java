package edu.buffalo.cse.facultyportal.service;

import edu.buffalo.cse.facultyportal.dto.SaveTeachingPreferenceRequestItemDto;
import edu.buffalo.cse.facultyportal.dto.SaveTeachingPreferenceResultItemDto;
import edu.buffalo.cse.facultyportal.dto.SaveTeachingPreferencesRequestDto;
import edu.buffalo.cse.facultyportal.dto.SaveTeachingPreferencesResponseDto;
import edu.buffalo.cse.facultyportal.repository.DocumentRepository;
import edu.buffalo.cse.facultyportal.repository.FacultyDetailRepository;
import edu.buffalo.cse.facultyportal.repository.FacultyRepository;
import edu.buffalo.cse.facultyportal.repository.FacultyTeachingHistoryRepository;
import edu.buffalo.cse.facultyportal.repository.FacultyTeachingPreferenceRepository;
import edu.buffalo.cse.facultyportal.mapper.FacultyMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacultyServiceImplTest {

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private FacultyDetailRepository facultyDetailRepository;

    @Mock
    private FacultyTeachingPreferenceRepository facultyTeachingPreferenceRepository;

    @Mock
    private FacultyTeachingHistoryRepository facultyTeachingHistoryRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private FacultyMapper facultyMapper;

    @InjectMocks
    private FacultyServiceImpl facultyService;

    @Test
    void saveTeachingPreferencesProcessesSaveUpdateAndDeleteActions() {
        SaveTeachingPreferencesRequestDto request = SaveTeachingPreferencesRequestDto.builder()
                .facultyId("10000001")
                .preferences(List.of(
                        preference("CSE 521-Operating Systems", "preference1"),
                        preference("CSE 442-Software Engineering", "qualified"),
                        preference("CSE 331-Algorithms", "   ")))
                .build();

        when(facultyTeachingPreferenceRepository.findCoursesByCourseName("CSE 521", "Operating Systems"))
                .thenReturn(List.of(course("2001", "CSE 521", "Operating Systems")));
        when(facultyTeachingPreferenceRepository.findCoursesByCourseName("CSE 442", "Software Engineering"))
                .thenReturn(List.of(course("2002", "CSE 442", "Software Engineering")));
        when(facultyTeachingPreferenceRepository.findCoursesByCourseName("CSE 331", "Algorithms"))
                .thenReturn(List.of(course("2003", "CSE 331", "Algorithms")));
        when(facultyTeachingPreferenceRepository.countTeachingPreference("10000001", "2001"))
                .thenReturn(0L);
        when(facultyTeachingPreferenceRepository.countTeachingPreference("10000001", "2002"))
                .thenReturn(1L);

        SaveTeachingPreferencesResponseDto response =
                facultyService.saveTeachingPreferences("10000001", request);

        assertEquals("10000001", response.getFacultyId());
        assertEquals(3, response.getTotalRequested());
        assertEquals(3, response.getTotalProcessed());
        assertEquals(3, response.getProcessedPreferences().size());

        SaveTeachingPreferenceResultItemDto savedItem = response.getProcessedPreferences().get(0);
        assertEquals("2001", savedItem.getCourseId());
        assertEquals("CSE 521-Operating Systems", savedItem.getCourseName());
        assertEquals("preference1", savedItem.getCoursePref());
        assertEquals("SAVED", savedItem.getAction());

        SaveTeachingPreferenceResultItemDto updatedItem = response.getProcessedPreferences().get(1);
        assertEquals("2002", updatedItem.getCourseId());
        assertEquals("qualified", updatedItem.getCoursePref());
        assertEquals("UPDATED", updatedItem.getAction());

        SaveTeachingPreferenceResultItemDto deletedItem = response.getProcessedPreferences().get(2);
        assertEquals("2003", deletedItem.getCourseId());
        assertNull(deletedItem.getCoursePref());
        assertEquals("DELETED", deletedItem.getAction());

        verify(facultyTeachingPreferenceRepository)
                .insertTeachingPreference("10000001", "2001", 1, "10000001");
        verify(facultyTeachingPreferenceRepository)
                .updateTeachingPreference("10000001", "2002", 0, "10000001");
        verify(facultyTeachingPreferenceRepository)
                .deleteTeachingPreference("10000001", "2003");
    }

    @Test
    void saveTeachingPreferencesDeletesWhenCoursePrefIsMissing() {
        SaveTeachingPreferencesRequestDto request = SaveTeachingPreferencesRequestDto.builder()
                .facultyId("10000001")
                .preferences(List.of(preference("CSE 331-Algorithms", null)))
                .build();

        when(facultyTeachingPreferenceRepository.findCoursesByCourseName("CSE 331", "Algorithms"))
                .thenReturn(List.of(course("2003", "CSE 331", "Algorithms")));
        when(facultyTeachingPreferenceRepository.deleteTeachingPreference("10000001", "2003"))
                .thenReturn(0);

        SaveTeachingPreferencesResponseDto response =
                facultyService.saveTeachingPreferences("10000001", request);

        assertEquals(1, response.getTotalProcessed());
        SaveTeachingPreferenceResultItemDto deletedItem = response.getProcessedPreferences().getFirst();
        assertEquals("DELETED", deletedItem.getAction());
        assertNull(deletedItem.getCoursePref());

        verify(facultyTeachingPreferenceRepository).deleteTeachingPreference("10000001", "2003");
        verify(facultyTeachingPreferenceRepository, never())
                .countTeachingPreference("10000001", "2003");
    }

    @Test
    void saveTeachingPreferencesRejectsDuplicateCourseNamesIgnoringCaseAndWhitespace() {
        SaveTeachingPreferencesRequestDto request = SaveTeachingPreferencesRequestDto.builder()
                .facultyId("10000001")
                .preferences(List.of(
                        preference(" CSE 521-Operating Systems ", "preference1"),
                        preference("cse 521-operating systems", "")))
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> facultyService.saveTeachingPreferences("10000001", request));

        assertEquals("Duplicate courseName in request: cse 521-operating systems",
                exception.getMessage());
    }

    @Test
    void saveTeachingPreferencesRejectsInvalidNonBlankCoursePref() {
        SaveTeachingPreferencesRequestDto request = SaveTeachingPreferencesRequestDto.builder()
                .facultyId("10000001")
                .preferences(List.of(preference("CSE 521-Operating Systems", "preferred")))
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> facultyService.saveTeachingPreferences("10000001", request));

        assertEquals("Invalid coursePref: preferred", exception.getMessage());
    }

    @Test
    void getTeachingHistoryGroupsRowsByYearAndTermAndSortsCourses() {
        when(facultyTeachingHistoryRepository.findTeachingHistory("10000001"))
                .thenReturn(List.of(
                        historyRow("Jane Doe", "10000001", "30500", "2259", "Lecture", "UGRD",
                                "C1", "CSE 331", "Algorithms"),
                        historyRow("Jane Doe", "10000001", "104", "2251", "Seminar", "GRAD",
                                "C2", "CSE 601", "Advanced Topics"),
                        historyRow("Jane Doe", "10000001", "102", "2251", "Lecture", "UGRD",
                                "C3", "CSE 521", "Operating Systems"),
                        historyRow("Jane Doe", "10000001", "205", "2246", "Lecture", "PHD",
                                "C4", "CSE 701", "Research Methods")));

        var response = facultyService.getTeachingHistory("10000001");

        assertEquals("Jane Doe", response.getFaculty());
        assertEquals("10000001", response.getFacultySourceKey());
        assertEquals(2, response.getYears().size());
        assertEquals(2025, response.getYears().get(0).getYear());
        assertEquals(2024, response.getYears().get(1).getYear());

        assertEquals(2, response.getYears().get(0).getSpring().size());
        assertEquals("102", response.getYears().get(0).getSpring().get(0).getClassNumber());
        assertEquals("CSE 521-Operating Systems",
                response.getYears().get(0).getSpring().get(0).getCourseName());
        assertEquals("Undergraduate",
                response.getYears().get(0).getSpring().get(0).getCourseCareer());
        assertEquals("104", response.getYears().get(0).getSpring().get(1).getClassNumber());
        assertEquals("Graduate",
                response.getYears().get(0).getSpring().get(1).getCourseCareer());

        assertEquals(0, response.getYears().get(0).getSummer().size());
        assertEquals(1, response.getYears().get(0).getFall().size());
        assertEquals("30500", response.getYears().get(0).getFall().get(0).getClassNumber());

        assertEquals(1, response.getYears().get(1).getSummer().size());
        assertEquals("PHD", response.getYears().get(1).getSummer().get(0).getCourseCareer());
    }

    private static SaveTeachingPreferenceRequestItemDto preference(String courseName, String coursePref) {
        return SaveTeachingPreferenceRequestItemDto.builder()
                .courseName(courseName)
                .coursePref(coursePref)
                .build();
    }

    private static FacultyTeachingHistoryRepository.TeachingHistoryProjection historyRow(
            String faculty,
            String facultySourceKey,
            String classNumber,
            String termSourceKey,
            String courseType,
            String courseCareerSourceKey,
            String courseId,
            String primaryCatalogNumber,
            String courseTitleLong) {
        return new FacultyTeachingHistoryRepository.TeachingHistoryProjection() {
            @Override
            public String getFaculty() {
                return faculty;
            }

            @Override
            public String getFacultySourceKey() {
                return facultySourceKey;
            }

            @Override
            public String getClassNumber() {
                return classNumber;
            }

            @Override
            public String getTermSourceKey() {
                return termSourceKey;
            }

            @Override
            public String getCourseType() {
                return courseType;
            }

            @Override
            public String getCourseCareerSourceKey() {
                return courseCareerSourceKey;
            }

            @Override
            public String getCourseId() {
                return courseId;
            }

            @Override
            public String getPrimaryCatalogNumber() {
                return primaryCatalogNumber;
            }

            @Override
            public String getCourseTitleLong() {
                return courseTitleLong;
            }

            @Override
            public String getEffectiveStatus() {
                return "A";
            }

            @Override
            public java.time.LocalDate getEffectiveDate() {
                return java.time.LocalDate.of(2025, 1, 1);
            }
        };
    }

    private static FacultyTeachingPreferenceRepository.CourseCatalogProjection course(
            String courseId,
            String primaryCatalogNumber,
            String courseTitleLong) {
        return new FacultyTeachingPreferenceRepository.CourseCatalogProjection() {
            @Override
            public String getCourseId() {
                return courseId;
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
