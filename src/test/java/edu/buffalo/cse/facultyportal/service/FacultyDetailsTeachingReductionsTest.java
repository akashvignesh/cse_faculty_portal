package edu.buffalo.cse.facultyportal.service;

import edu.buffalo.cse.facultyportal.dto.FacultyDetailDto;
import edu.buffalo.cse.facultyportal.entity.Faculty;
import edu.buffalo.cse.facultyportal.mapper.FacultyMapper;
import edu.buffalo.cse.facultyportal.repository.DocumentRepository;
import edu.buffalo.cse.facultyportal.repository.FacultyDetailRepository;
import edu.buffalo.cse.facultyportal.repository.FacultyRepository;
import edu.buffalo.cse.facultyportal.repository.FacultyTeachingHistoryRepository;
import edu.buffalo.cse.facultyportal.repository.FacultyTeachingPreferenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacultyDetailsTeachingReductionsTest {

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
    void getFacultyDetailsIncludesTeachingReductions() {
        Faculty faculty = Faculty.builder()
                .personNumber("10000001")
                .fullName("Jane Doe")
                .build();

        when(facultyRepository.findByPersonNumber("10000001")).thenReturn(java.util.Optional.of(faculty));
        when(facultyDetailRepository.findCurrentAppointment("10000001")).thenReturn(java.util.Optional.empty());
        when(facultyDetailRepository.findPrimaryWorkEmail("10000001")).thenReturn(java.util.Optional.empty());
        when(facultyDetailRepository.findPrimaryOfficePhone("10000001")).thenReturn(java.util.Optional.empty());
        when(facultyDetailRepository.findOfficeAddress("10000001")).thenReturn(java.util.Optional.empty());
        when(facultyDetailRepository.findResearchAreas("10000001")).thenReturn(List.of());
        when(facultyDetailRepository.findTeachingReductions("10000001")).thenReturn(List.of(
                reduction("2025FA", "COURSE REDUCTION", new BigDecimal("3.00"), "Research leave", 55L)));
        when(facultyDetailRepository.findLeaveSummary("10000001")).thenReturn(List.of());
        when(facultyDetailRepository.findStudentsUnderProfessor("10000001")).thenReturn(List.of());

        FacultyDetailDto result = facultyService.getFacultyDetails("10000001");

        assertEquals(1, result.getTeachingReductions().size());
        assertEquals("2025FA", result.getTeachingReductions().getFirst().getTermCode());
        assertEquals("COURSE REDUCTION", result.getTeachingReductions().getFirst().getReductionType());
        assertEquals(new BigDecimal("3.00"), result.getTeachingReductions().getFirst().getReductionAmount());
        assertEquals("Research leave", result.getTeachingReductions().getFirst().getReason());
        assertEquals(55L, result.getTeachingReductions().getFirst().getApprovalDocumentId());
    }

    private static FacultyDetailRepository.TeachingReductionProjection reduction(
            String termCode,
            String reductionType,
            BigDecimal reductionAmount,
            String reason,
            Long approvalDocumentId) {
        return new FacultyDetailRepository.TeachingReductionProjection() {
            @Override
            public String getTermCode() {
                return termCode;
            }

            @Override
            public String getReductionType() {
                return reductionType;
            }

            @Override
            public BigDecimal getReductionAmount() {
                return reductionAmount;
            }

            @Override
            public String getReason() {
                return reason;
            }

            @Override
            public Long getApprovalDocumentId() {
                return approvalDocumentId;
            }

            @Override
            public LocalDateTime getCreatedAt() {
                return LocalDateTime.of(2026, 4, 15, 10, 30);
            }
        };
    }
}
