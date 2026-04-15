package edu.buffalo.cse.facultyportal.controller;

import edu.buffalo.cse.facultyportal.exception.GlobalExceptionHandler;
import edu.buffalo.cse.facultyportal.mapper.FacultyMapper;
import edu.buffalo.cse.facultyportal.repository.DocumentRepository;
import edu.buffalo.cse.facultyportal.repository.FacultyDetailRepository;
import edu.buffalo.cse.facultyportal.repository.FacultyRepository;
import edu.buffalo.cse.facultyportal.repository.FacultyTeachingPreferenceRepository;
import edu.buffalo.cse.facultyportal.service.FacultyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FacultyControllerIntegrationTest {

    private FacultyTeachingPreferenceRepository facultyTeachingPreferenceRepository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        FacultyRepository facultyRepository = mock(FacultyRepository.class);
        FacultyDetailRepository facultyDetailRepository = mock(FacultyDetailRepository.class);
        facultyTeachingPreferenceRepository = mock(FacultyTeachingPreferenceRepository.class);
        DocumentRepository documentRepository = mock(DocumentRepository.class);
        FacultyMapper facultyMapper = mock(FacultyMapper.class);

        FacultyServiceImpl facultyService = new FacultyServiceImpl(
                facultyRepository,
                facultyDetailRepository,
                facultyTeachingPreferenceRepository,
                documentRepository,
                facultyMapper);

        mockMvc = MockMvcBuilders.standaloneSetup(new FacultyController(facultyService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        reset(facultyTeachingPreferenceRepository);
    }

    @Test
    void postTeachingPreferencesSavesAndDeletesInOneRequest() throws Exception {
        when(facultyTeachingPreferenceRepository.findCoursesByCourseName("CSE 521", "Operating Systems"))
                .thenReturn(List.of(course("2001", "CSE 521", "Operating Systems")));
        when(facultyTeachingPreferenceRepository.findCoursesByCourseName("CSE 442", "Software Engineering"))
                .thenReturn(List.of(course("2002", "CSE 442", "Software Engineering")));
        when(facultyTeachingPreferenceRepository.countTeachingPreference("10000001", "2001"))
                .thenReturn(0L);
        when(facultyTeachingPreferenceRepository.deleteTeachingPreference("10000001", "2002"))
                .thenReturn(0);

        mockMvc.perform(post("/api/v1/faculty/10000001/teaching-preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "facultyId": "10000001",
                                  "preferences": [
                                    {
                                      "courseName": "CSE 521-Operating Systems",
                                      "coursePref": "preference1"
                                    },
                                    {
                                      "courseName": "CSE 442-Software Engineering",
                                      "coursePref": "   "
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalRequested").value(2))
                .andExpect(jsonPath("$.data.totalProcessed").value(2))
                .andExpect(jsonPath("$.data.processedPreferences[0].courseId").value("2001"))
                .andExpect(jsonPath("$.data.processedPreferences[0].action").value("SAVED"))
                .andExpect(jsonPath("$.data.processedPreferences[0].coursePref").value("preference1"))
                .andExpect(jsonPath("$.data.processedPreferences[1].courseId").value("2002"))
                .andExpect(jsonPath("$.data.processedPreferences[1].action").value("DELETED"))
                .andExpect(jsonPath("$.data.processedPreferences[1].coursePref").value(nullValue()));
    }

    @Test
    void postTeachingPreferencesReturnsBadRequestWhenPathAndBodyFacultyIdsDiffer() throws Exception {
        mockMvc.perform(post("/api/v1/faculty/10000001/teaching-preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "facultyId": "10000002",
                                  "preferences": [
                                    {
                                      "courseName": "CSE 521-Operating Systems",
                                      "coursePref": "preference1"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("Path facultyId and body facultyId must match"));
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
