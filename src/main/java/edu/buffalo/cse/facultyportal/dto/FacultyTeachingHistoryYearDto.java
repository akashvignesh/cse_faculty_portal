package edu.buffalo.cse.facultyportal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FacultyTeachingHistoryYearDto {

    private Integer year;
    private List<FacultyTeachingHistoryItemDto> spring;
    private List<FacultyTeachingHistoryItemDto> summer;
    private List<FacultyTeachingHistoryItemDto> fall;
}
