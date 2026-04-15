package edu.buffalo.cse.facultyportal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class FacultyDetailDto {

    private String personNumber;
    private String fullName;
    private String pronouns;
    private String title;
    private String rank;
    private String profilePhotoUrl;
    private FacultyContactDto contact;
    private List<String> researchAreas;
    private List<TeachingReductionDto> teachingReductions;
    private List<FacultyLeaveSummaryItemDto> leaveSummary;
    private List<FacultyStudentSummaryDto> studentsUnderProfessor;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public static class TeachingReductionDto {

        private String termCode;
        private String reductionType;
        private BigDecimal reductionAmount;
        private String reason;
        private Long approvalDocumentId;
        private LocalDate createdAt;
    }
}
