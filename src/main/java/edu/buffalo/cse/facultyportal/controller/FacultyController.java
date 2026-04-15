package edu.buffalo.cse.facultyportal.controller;

import edu.buffalo.cse.facultyportal.dto.ApiResponseDto;
import edu.buffalo.cse.facultyportal.dto.FacultyDetailDto;
import edu.buffalo.cse.facultyportal.dto.FacultyListItemDto;
import edu.buffalo.cse.facultyportal.dto.FacultyTeachingHistoryResponseDto;
import edu.buffalo.cse.facultyportal.dto.FacultyTeachingPreferencesResponseDto;
import edu.buffalo.cse.facultyportal.dto.PaginatedResponseDto;
import edu.buffalo.cse.facultyportal.dto.ProfilePhotoUpdateResponseDto;
import edu.buffalo.cse.facultyportal.dto.SaveTeachingPreferencesRequestDto;
import edu.buffalo.cse.facultyportal.dto.SaveTeachingPreferencesResponseDto;
import edu.buffalo.cse.facultyportal.entity.Document;
import edu.buffalo.cse.facultyportal.service.FacultyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/faculty")
@RequiredArgsConstructor
public class FacultyController {

    private final FacultyService facultyService;

    /**
     * GET /api/v1/faculty?page=0&size=10&search=
     */
    @GetMapping
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<FacultyListItemDto>>> listFaculty(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search) {

        PaginatedResponseDto<FacultyListItemDto> result = facultyService.listFaculty(page, size, search);
        return ResponseEntity.ok(ApiResponseDto.success("Faculty list fetched successfully", result));
    }

    /**
     * GET /api/v1/faculty/{personNumber}
     */
    @GetMapping("/{personNumber}")
    public ResponseEntity<ApiResponseDto<FacultyDetailDto>> getFacultyDetails(
            @PathVariable String personNumber) {

        FacultyDetailDto result = facultyService.getFacultyDetails(personNumber);
        return ResponseEntity.ok(
                ApiResponseDto.success("Faculty details fetched successfully", result));
    }

    /**
     * GET /api/v1/faculty/{facultySourceKey}/teaching-history
     */
    @GetMapping("/{facultySourceKey}/teaching-history")
    public ResponseEntity<ApiResponseDto<FacultyTeachingHistoryResponseDto>> getTeachingHistory(
            @PathVariable String facultySourceKey) {

        FacultyTeachingHistoryResponseDto result =
                facultyService.getTeachingHistory(facultySourceKey);
        return ResponseEntity.ok(
                ApiResponseDto.success("Teaching history fetched successfully", result));
    }

    /**
     * GET /api/v1/faculty/{facultyId}/teaching-preferences
     */
    @GetMapping("/{facultyId}/teaching-preferences")
    public ResponseEntity<ApiResponseDto<FacultyTeachingPreferencesResponseDto>> getTeachingPreferences(
            @PathVariable String facultyId) {

        FacultyTeachingPreferencesResponseDto result = facultyService.getTeachingPreferences(facultyId);
        return ResponseEntity.ok(
                ApiResponseDto.success("Teaching preferences fetched successfully", result));
    }

    /**
     * POST /api/v1/faculty/{facultyId}/teaching-preferences
     */
    @PostMapping("/{facultyId}/teaching-preferences")
    public ResponseEntity<ApiResponseDto<SaveTeachingPreferencesResponseDto>> saveTeachingPreferences(
            @PathVariable String facultyId,
            @RequestBody SaveTeachingPreferencesRequestDto request) {

        SaveTeachingPreferencesResponseDto result =
                facultyService.saveTeachingPreferences(facultyId, request);
        return ResponseEntity.ok(
                ApiResponseDto.success("Teaching preferences saved successfully", result));
    }

    /**
     * GET /api/v1/faculty/{personNumber}/profile-photo
     */
    @GetMapping("/{personNumber}/profile-photo")
    public ResponseEntity<byte[]> getProfilePhoto(@PathVariable String personNumber) {
        Document photo = facultyService.getProfilePhoto(personNumber);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(photo.getMimeType()));
        headers.setContentLength(photo.getFileSizeBytes());

        return new ResponseEntity<>(photo.getFileData(), headers, HttpStatus.OK);
    }

    /**
     * PUT /api/v1/faculty/{personNumber}/profile-photo
     * multipart/form-data with "profilePhoto" field
     */
    @PutMapping("/{personNumber}/profile-photo")
    public ResponseEntity<ApiResponseDto<ProfilePhotoUpdateResponseDto>> updateProfilePhoto(
            @PathVariable String personNumber,
            @RequestParam(value = "profilePhoto") MultipartFile profilePhoto) {

        ProfilePhotoUpdateResponseDto result =
                facultyService.updateProfilePhoto(personNumber, profilePhoto);
        return ResponseEntity.ok(
                ApiResponseDto.success("Faculty profile photo updated successfully", result));
    }
}
