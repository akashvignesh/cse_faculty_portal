package edu.buffalo.cse.facultyportal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class FacultyTeachingHistoryItemDto {

    private String termCode;
    private String courseCode;
    private String courseName;
    private String sectionCode;
    private String role;
    private String days;
    private String timeRange;
    private String location;
    private Integer enrollment;
}
