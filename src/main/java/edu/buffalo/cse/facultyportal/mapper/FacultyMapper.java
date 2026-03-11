package edu.buffalo.cse.facultyportal.mapper;

import edu.buffalo.cse.facultyportal.dto.FacultyListItemDto;
import edu.buffalo.cse.facultyportal.entity.Faculty;
import org.springframework.stereotype.Component;

@Component
public class FacultyMapper {

    private static final String PROFILE_PHOTO_ENDPOINT_TEMPLATE = "/api/v1/faculty/%s/profile-photo";

    public FacultyListItemDto toListItemDto(Faculty faculty) {
        boolean hasPhoto = faculty.getProfilePhotoDocument() != null;

        return FacultyListItemDto.builder()
                .personNumber(faculty.getPersonNumber())
                .fullName(faculty.getFullName())
                .hasProfilePhoto(hasPhoto)
                .profilePhotoEndpoint(hasPhoto
                        ? String.format(PROFILE_PHOTO_ENDPOINT_TEMPLATE, faculty.getPersonNumber())
                        : null)
                .build();
    }
}
