package edu.buffalo.cse.facultyportal.controller;

import edu.buffalo.cse.facultyportal.exception.GlobalExceptionHandler;
import edu.buffalo.cse.facultyportal.mapper.FacultyMapper;
import edu.buffalo.cse.facultyportal.repository.DocumentRepository;
import edu.buffalo.cse.facultyportal.repository.FacultyDetailRepository;
import edu.buffalo.cse.facultyportal.repository.FacultyRepository;
import edu.buffalo.cse.facultyportal.repository.FacultyTeachingHistoryRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FacultyControllerIntegrationTest {

    private FacultyTeachingPreferenceRepository facultyTeachingPreferenceRepository;
    private FacultyTeachingHistoryRepository facultyTeachingHistoryRepository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        FacultyRepository facultyRepository = mock(FacultyRepository.class);
        FacultyDetailRepository facultyDetailRepository = mock(FacultyDetailRepository.class);
        facultyTeachingPreferenceRepository = mock(FacultyTeachingPreferenceRepository.class);
        facultyTeachingHistoryRepository = mock(FacultyTeachingHistoryRepository.class);
        DocumentRepository documentRepository = mock(DocumentRepository.class);
        FacultyMapper facultyMapper = mock(FacultyMapper.class);

        FacultyServiceImpl facultyService = new FacultyServiceImpl(
                facultyRepository,
                facultyDetailRepository,
                facultyTeachingPreferenceRepository,
                facultyTeachingHistoryRepository,
                documentRepository,
                facultyMapper);

        mockMvc = MockMvcBuilders.standaloneSetup(new FacultyController(facultyService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        reset(facultyTeachingPreferenceRepository);
        reset(facultyTeachingHistoryRepository);
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

    @Test
    void getTeachingHistoryReturnsWrappedGroupedResponse() throws Exception {
        when(facultyTeachingHistoryRepository.findTeachingHistory("10000001"))
                .thenReturn(List.of(
                        historyRow("Jane Doe", "10000001", "104", "2251", "Seminar", "GRAD",
                                "2001", "CSE 601", "Advanced Topics"),
                        historyRow("Jane Doe", "10000001", "102", "2251", "Lecture", "UGRD",
                                "2002", "CSE 521", "Operating Systems")));

        mockMvc.perform(get("/api/v1/faculty/10000001/teaching-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Teaching history fetched successfully"))
                .andExpect(jsonPath("$.data.faculty").value("Jane Doe"))
                .andExpect(jsonPath("$.data.facultySourceKey").value("10000001"))
                .andExpect(jsonPath("$.data.years[0].year").value(2025))
                .andExpect(jsonPath("$.data.years[0].spring[0].classNumber").value("102"))
                .andExpect(jsonPath("$.data.years[0].spring[0].courseName")
                        .value("CSE 521-Operating Systems"))
                .andExpect(jsonPath("$.data.years[0].spring[0].courseCareer")
                        .value("Undergraduate"))
                .andExpect(jsonPath("$.data.years[0].spring[1].classNumber").value("104"))
                .andExpect(jsonPath("$.data.years[0].summer").isArray())
                .andExpect(jsonPath("$.data.years[0].fall").isArray());
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
}
