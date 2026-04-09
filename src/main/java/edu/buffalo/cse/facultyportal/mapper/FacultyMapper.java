package edu.buffalo.cse.facultyportal.mapper;

import edu.buffalo.cse.facultyportal.dto.FacultyListItemDto;
import edu.buffalo.cse.facultyportal.entity.Faculty;
import org.springframework.stereotype.Component;

@Component
public class FacultyMapper {

    public FacultyListItemDto toListItemDto(Faculty faculty, String title, String officeAddress) {
        return FacultyListItemDto.builder()
                .personNumber(faculty.getPersonNumber())
                .fullName(faculty.getFullName())
                .title(title)
                .officeAddress(officeAddress)
                .build();
    }
}
