package edu.buffalo.cse.facultyportal.service;

import edu.buffalo.cse.facultyportal.dto.FacultyContactDto;
import edu.buffalo.cse.facultyportal.dto.FacultyDetailDto;
import edu.buffalo.cse.facultyportal.dto.FacultyLeaveSummaryItemDto;
import edu.buffalo.cse.facultyportal.dto.FacultyListItemDto;
import edu.buffalo.cse.facultyportal.dto.FacultyOfficeAddressDto;
import edu.buffalo.cse.facultyportal.dto.FacultyStudentSummaryDto;
import edu.buffalo.cse.facultyportal.dto.FacultyTeachingPreferenceItemDto;
import edu.buffalo.cse.facultyportal.dto.FacultyTeachingPreferencesResponseDto;
import edu.buffalo.cse.facultyportal.dto.FacultyTeachingHistoryItemDto;
import edu.buffalo.cse.facultyportal.dto.PaginatedResponseDto;
import edu.buffalo.cse.facultyportal.dto.ProfilePhotoUpdateResponseDto;
import edu.buffalo.cse.facultyportal.dto.SaveTeachingPreferenceRequestItemDto;
import edu.buffalo.cse.facultyportal.dto.SaveTeachingPreferencesRequestDto;
import edu.buffalo.cse.facultyportal.dto.SaveTeachingPreferencesResponseDto;
import edu.buffalo.cse.facultyportal.entity.Document;
import edu.buffalo.cse.facultyportal.entity.Faculty;
import edu.buffalo.cse.facultyportal.exception.ConflictException;
import edu.buffalo.cse.facultyportal.exception.InvalidFileException;
import edu.buffalo.cse.facultyportal.exception.ResourceNotFoundException;
import edu.buffalo.cse.facultyportal.mapper.FacultyMapper;
import edu.buffalo.cse.facultyportal.repository.FacultyDetailRepository;
import edu.buffalo.cse.facultyportal.repository.FacultyTeachingPreferenceRepository;
import edu.buffalo.cse.facultyportal.repository.DocumentRepository;
import edu.buffalo.cse.facultyportal.repository.FacultyRepository;
import edu.buffalo.cse.facultyportal.repository.FacultySpecifications;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class FacultyServiceImpl implements FacultyService {

    private static final long MAX_FILE_SIZE = 10_485_760L; // 10 MB
    private static final String FACULTY_PHOTO_URL_TEMPLATE = "/api/v1/faculty/%s/profile-photo";
    private static final Pattern FACULTY_ID_PATTERN = Pattern.compile("^[0-9]{8}$");
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp");
    private static final Map<Integer, String> PREF_VALUE_TO_LABEL = Map.of(
            1, "preference1",
            2, "preference2",
            3, "preference3",
            0, "qualified",
            -1, "not qualified");
    private static final Map<String, Integer> PREF_LABEL_TO_VALUE = Map.of(
            "preference1", 1,
            "preference2", 2,
            "preference3", 3,
            "qualified", 0,
            "not qualified", -1);

    private final FacultyRepository facultyRepository;
    private final FacultyDetailRepository facultyDetailRepository;
    private final FacultyTeachingPreferenceRepository facultyTeachingPreferenceRepository;
    private final DocumentRepository documentRepository;
    private final FacultyMapper facultyMapper;

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<FacultyListItemDto> listFaculty(int page, int size, String search) {
        Sort sort = Sort.by(
                Sort.Order.asc("fullName"),
                Sort.Order.asc("personNumber"));
        Pageable pageable = PageRequest.of(page, size, sort);

        List<String> tokens = parseSearchTokens(search);
        Specification<Faculty> spec = FacultySpecifications.fullNameContainsAllTokens(tokens);

        Page<Faculty> facultyPage = facultyRepository.findAll(spec, pageable);

        List<FacultyListItemDto> content = facultyPage.getContent().stream()
                .map(faculty -> {
                    FacultyDetailRepository.CurrentAppointmentProjection appointment =
                            facultyDetailRepository.findCurrentAppointment(faculty.getPersonNumber())
                                    .orElse(null);
                    String officeAddress = facultyDetailRepository.findOfficeAddress(faculty.getPersonNumber())
                            .map(this::formatOfficeAddress)
                            .orElse(null);

                    return facultyMapper.toListItemDto(
                            faculty,
                            appointment != null ? appointment.getTitle() : null,
                            officeAddress);
                })
                .toList();

        return PaginatedResponseDto.<FacultyListItemDto>builder()
                .content(content)
                .page(facultyPage.getNumber())
                .size(facultyPage.getSize())
                .totalElements(facultyPage.getTotalElements())
                .totalPages(facultyPage.getTotalPages())
                .hasNext(facultyPage.hasNext())
                .hasPrevious(facultyPage.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public FacultyDetailDto getFacultyDetails(String personNumber) {
        Faculty faculty = findFacultyOrThrow(personNumber);

        FacultyDetailRepository.CurrentAppointmentProjection appointment =
                facultyDetailRepository.findCurrentAppointment(personNumber).orElse(null);
        String email = facultyDetailRepository.findPrimaryWorkEmail(personNumber)
                .map(FacultyDetailRepository.PrimaryEmailProjection::getEmailAddress)
                .orElse(null);
        String phone = facultyDetailRepository.findPrimaryOfficePhone(personNumber)
                .map(FacultyDetailRepository.PrimaryPhoneProjection::getPhoneNumber)
                .orElse(null);
        FacultyOfficeAddressDto officeAddress = facultyDetailRepository.findOfficeAddress(personNumber)
                .map(this::toOfficeAddressDto)
                .orElse(null);

        return FacultyDetailDto.builder()
                .personNumber(faculty.getPersonNumber())
                .fullName(faculty.getFullName())
                .pronouns(faculty.getPronouns())
                .title(appointment != null ? appointment.getTitle() : null)
                .rank(appointment != null ? appointment.getRankName() : null)
                .profilePhotoUrl(buildPhotoUrl(faculty))
                .contact(FacultyContactDto.builder()
                        .email(email)
                        .phone(phone)
                        .officeAddress(officeAddress)
                        .build())
                .researchAreas(facultyDetailRepository.findResearchAreas(personNumber).stream()
                        .map(FacultyDetailRepository.ResearchAreaProjection::getAreaName)
                        .toList())
                .teachingHistory(facultyDetailRepository.findTeachingHistory(personNumber).stream()
                        .map(this::toTeachingHistoryItemDto)
                        .toList())
                .leaveSummary(facultyDetailRepository.findLeaveSummary(personNumber).stream()
                        .map(this::toLeaveSummaryItemDto)
                        .toList())
                .studentsUnderProfessor(
                        facultyDetailRepository.findStudentsUnderProfessor(personNumber).stream()
                                .map(this::toStudentSummaryDto)
                                .toList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public FacultyTeachingPreferencesResponseDto getTeachingPreferences(String facultyId) {
        validateFacultyId(facultyId);

        List<FacultyTeachingPreferenceItemDto> teachingPreferences =
                facultyTeachingPreferenceRepository.findTeachingPreferences(facultyId).stream()
                        .map(row -> FacultyTeachingPreferenceItemDto.builder()
                                .courseId(row.getCourseId())
                                .courseName(buildTeachingPreferenceCourseName(
                                        facultyId,
                                        row.getCourseId(),
                                        row.getPrimaryCatalogNumber(),
                                        row.getCourseTitleLong()))
                                .coursePref(toCanonicalPrefLabel(row.getPref()))
                                .build())
                        .toList();

        return FacultyTeachingPreferencesResponseDto.builder()
                .facultyId(facultyId)
                .teachingPreferences(teachingPreferences)
                .build();
    }

    @Override
    @Transactional
    public SaveTeachingPreferencesResponseDto saveTeachingPreferences(
            String facultyId,
            SaveTeachingPreferencesRequestDto request) {
        validateFacultyId(facultyId);
        validateSaveTeachingPreferencesRequest(facultyId, request);

        List<FacultyTeachingPreferenceItemDto> savedPreferences = new ArrayList<>();
        for (SaveTeachingPreferenceRequestItemDto item : request.getPreferences()) {
            String courseName = normalizeCourseName(item.getCourseName());
            String coursePref = normalizeCoursePref(item.getCoursePref());
            FacultyTeachingPreferenceRepository.CourseCatalogProjection catalogCourse =
                    resolveCatalogCourse(courseName);

            String courseId = catalogCourse.getCourseId();
            int prefValue = toPrefValue(coursePref);

            if (facultyTeachingPreferenceRepository.countTeachingPreference(facultyId, courseId) > 0) {
                facultyTeachingPreferenceRepository.updateTeachingPreference(
                        facultyId,
                        courseId,
                        prefValue,
                        facultyId);
            } else {
                facultyTeachingPreferenceRepository.insertTeachingPreference(
                        facultyId,
                        courseId,
                        prefValue,
                        facultyId);
            }

            savedPreferences.add(FacultyTeachingPreferenceItemDto.builder()
                    .courseId(courseId)
                    .courseName(buildCatalogCourseName(
                            catalogCourse.getPrimaryCatalogNumber(),
                            catalogCourse.getCourseTitleLong()))
                    .coursePref(coursePref)
                    .build());
        }

        return SaveTeachingPreferencesResponseDto.builder()
                .facultyId(facultyId)
                .totalRequested(request.getPreferences().size())
                .totalSaved(savedPreferences.size())
                .savedPreferences(savedPreferences)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Document getProfilePhoto(String personNumber) {
        Faculty faculty = findFacultyOrThrow(personNumber);

        // Accessing the proxy object itself is safe (not null check), but we need to
        // get the FK value without triggering lazy initialization of the full proxy.
        // getDocumentId() on a Hibernate proxy is safe — Hibernate knows the PK without
        // issuing a SELECT. We then fetch the real Document row inside this transaction.
        Document photoProxy = faculty.getProfilePhotoDocument();
        if (photoProxy == null) {
            throw new ResourceNotFoundException(
                    "No profile photo linked for faculty: " + personNumber);
        }

        Long documentId = photoProxy.getDocumentId();
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profile photo document not found for faculty: " + personNumber));
    }

    @Override
    @Transactional
    public ProfilePhotoUpdateResponseDto updateProfilePhoto(String personNumber,
                                                            MultipartFile profilePhoto) {
        if (profilePhoto == null || profilePhoto.isEmpty()) {
            throw new InvalidFileException("Profile photo file must be provided");
        }

        validateFile(profilePhoto, ALLOWED_IMAGE_TYPES, "Profile photo");

        Faculty faculty = findFacultyOrThrow(personNumber);

        Document photoDoc = upsertDocument(
                faculty.getProfilePhotoDocument(),
                profilePhoto,
                "PROFILE_PHOTO");
        faculty.setProfilePhotoDocument(photoDoc);
        facultyRepository.save(faculty);

        return ProfilePhotoUpdateResponseDto.builder()
                .personNumber(faculty.getPersonNumber())
                .profilePhotoDocumentId(photoDoc.getDocumentId())
                .build();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Faculty findFacultyOrThrow(String personNumber) {
        return facultyRepository.findByPersonNumber(personNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Faculty not found with person number: " + personNumber));
    }

    private List<String> parseSearchTokens(String search) {
        if (search == null || search.isBlank()) {
            return List.of();
        }
        return Arrays.stream(search.trim().split("\\s+"))
                .filter(s -> !s.isBlank())
                .toList();
    }

    private void validateFacultyId(String facultyId) {
        if (facultyId == null || !FACULTY_ID_PATTERN.matcher(facultyId).matches()) {
            throw new IllegalArgumentException("facultyId must be exactly 8 digits");
        }
    }

    private void validateSaveTeachingPreferencesRequest(
            String facultyId,
            SaveTeachingPreferencesRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        validateFacultyId(request.getFacultyId());
        if (!facultyId.equals(request.getFacultyId())) {
            throw new IllegalArgumentException("Path facultyId and body facultyId must match");
        }
        if (request.getPreferences() == null || request.getPreferences().isEmpty()) {
            throw new IllegalArgumentException("preferences list must not be null or empty");
        }

        Set<String> normalizedCourseNames = new LinkedHashSet<>();
        for (SaveTeachingPreferenceRequestItemDto item : request.getPreferences()) {
            if (item == null) {
                throw new IllegalArgumentException("preferences items must not be null");
            }

            String courseName = normalizeCourseName(item.getCourseName());
            String coursePref = normalizeCoursePref(item.getCoursePref());

            if (!normalizedCourseNames.add(courseName.toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException(
                        "Duplicate courseName in request: " + courseName);
            }

            toPrefValue(coursePref);
        }
    }

    private void validateFile(MultipartFile file, Set<String> allowedTypes, String label) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException(
                    label + " exceeds maximum allowed size of 10 MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType.toLowerCase())) {
            throw new InvalidFileException(
                    label + " has invalid file type: " + contentType
                            + ". Allowed types: " + allowedTypes);
        }
    }

    /**
     * If an existing document row is linked, overwrite it in place.
     * Otherwise, create a new document row.
     */
    private Document upsertDocument(Document existing, MultipartFile file, String documentType) {
        try {
            byte[] data = file.getBytes();
            String fileName = file.getOriginalFilename() != null
                    ? file.getOriginalFilename()
                    : documentType.toLowerCase() + "_upload";

            if (existing != null) {
                existing.setDocumentName(fileName);
                existing.setDocumentType(documentType);
                existing.setMimeType(file.getContentType());
                existing.setFileSizeBytes(file.getSize());
                existing.setFileData(data);
                return documentRepository.save(existing);
            }

            Document doc = Document.builder()
                    .documentName(fileName)
                    .documentType(documentType)
                    .mimeType(file.getContentType())
                    .fileSizeBytes(file.getSize())
                    .fileData(data)
                    .build();
            return documentRepository.save(doc);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }
    }

    private String buildPhotoUrl(Faculty faculty) {
        if (faculty.getProfilePhotoDocument() == null) {
            return null;
        }
        return String.format(FACULTY_PHOTO_URL_TEMPLATE, faculty.getPersonNumber());
    }

    private FacultyTeachingPreferenceRepository.CourseCatalogProjection resolveCatalogCourse(
            String courseName) {
        CourseNameParts courseNameParts = parseCourseName(courseName);
        List<FacultyTeachingPreferenceRepository.CourseCatalogProjection> matches =
                facultyTeachingPreferenceRepository.findCoursesByCourseName(
                        courseNameParts.primaryCatalogNumber(),
                        courseNameParts.courseTitleLong());

        if (matches.isEmpty()) {
            throw new ResourceNotFoundException("Course not found in catalog: " + courseName);
        }
        if (matches.size() > 1) {
            throw new ConflictException(
                    "Multiple course catalog matches found for course name: " + courseName);
        }
        return matches.getFirst();
    }

    private String buildTeachingPreferenceCourseName(
            String facultyId,
            String courseId,
            String primaryCatalogNumber,
            String courseTitleLong) {
        if (isBlank(primaryCatalogNumber) || isBlank(courseTitleLong)) {
            log.warn(
                    "Course catalog row missing for courseId={} while loading teaching preferences for facultyId={}",
                    courseId,
                    facultyId);
            return courseId + "-UNKNOWN COURSE";
        }
        return buildCatalogCourseName(primaryCatalogNumber, courseTitleLong);
    }

    private String buildCatalogCourseName(String primaryCatalogNumber, String courseTitleLong) {
        return primaryCatalogNumber.trim() + "-" + courseTitleLong.trim();
    }

    private String normalizeCourseName(String courseName) {
        if (courseName == null || courseName.isBlank()) {
            throw new IllegalArgumentException("courseName is required");
        }
        return courseName.trim();
    }

    private CourseNameParts parseCourseName(String courseName) {
        int separatorIndex = courseName.indexOf('-');
        if (separatorIndex <= 0 || separatorIndex == courseName.length() - 1) {
            throw new IllegalArgumentException(
                    "courseName must be in the format <catalogNumber>-<courseTitle>");
        }

        String primaryCatalogNumber = courseName.substring(0, separatorIndex).trim();
        String courseTitleLong = courseName.substring(separatorIndex + 1).trim();
        if (primaryCatalogNumber.isEmpty() || courseTitleLong.isEmpty()) {
            throw new IllegalArgumentException(
                    "courseName must be in the format <catalogNumber>-<courseTitle>");
        }

        return new CourseNameParts(primaryCatalogNumber, courseTitleLong);
    }

    private String normalizeCoursePref(String coursePref) {
        if (coursePref == null || coursePref.isBlank()) {
            throw new IllegalArgumentException("coursePref is required");
        }

        String normalized = coursePref.trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
        if (!PREF_LABEL_TO_VALUE.containsKey(normalized)) {
            throw new IllegalArgumentException("Invalid coursePref: " + coursePref);
        }
        return normalized;
    }

    private int toPrefValue(String coursePref) {
        Integer prefValue = PREF_LABEL_TO_VALUE.get(coursePref);
        if (prefValue == null) {
            throw new IllegalArgumentException("Invalid coursePref: " + coursePref);
        }
        return prefValue;
    }

    private String toCanonicalPrefLabel(Integer prefValue) {
        String coursePref = prefValue != null ? PREF_VALUE_TO_LABEL.get(prefValue) : null;
        if (coursePref == null) {
            throw new IllegalStateException("Unsupported teaching preference value: " + prefValue);
        }
        return coursePref;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record CourseNameParts(String primaryCatalogNumber, String courseTitleLong) {
    }

    private FacultyOfficeAddressDto toOfficeAddressDto(
            FacultyDetailRepository.OfficeAddressProjection projection) {
        return FacultyOfficeAddressDto.builder()
                .line1(projection.getLine1())
                .city(projection.getCity())
                .state(projection.getState())
                .postalCode(projection.getPostalCode())
                .country(projection.getCountry())
                .build();
    }

    private String formatOfficeAddress(FacultyDetailRepository.OfficeAddressProjection projection) {
        List<String> parts = new ArrayList<>();
        addIfPresent(parts, projection.getLine1());
        addIfPresent(parts, projection.getCity());

        String statePostal = joinWithSpace(projection.getState(), projection.getPostalCode());
        addIfPresent(parts, statePostal);
        addIfPresent(parts, projection.getCountry());

        return parts.isEmpty() ? null : String.join(", ", parts);
    }

    private void addIfPresent(List<String> parts, String value) {
        if (value != null && !value.isBlank()) {
            parts.add(value.trim());
        }
    }

    private String joinWithSpace(String left, String right) {
        boolean hasLeft = left != null && !left.isBlank();
        boolean hasRight = right != null && !right.isBlank();

        if (hasLeft && hasRight) {
            return left.trim() + " " + right.trim();
        }
        if (hasLeft) {
            return left.trim();
        }
        if (hasRight) {
            return right.trim();
        }
        return null;
    }

    private FacultyTeachingHistoryItemDto toTeachingHistoryItemDto(
            FacultyDetailRepository.TeachingHistoryProjection projection) {
        return FacultyTeachingHistoryItemDto.builder()
                .termCode(projection.getTermCode())
                .courseCode(projection.getCourseCode())
                .courseName(projection.getCourseName())
                .sectionCode(projection.getSectionCode())
                .role(projection.getRoleName())
                .days(projection.getDays())
                .timeRange(projection.getTimeRange())
                .location(projection.getLocation())
                .enrollment(projection.getEnrollment())
                .build();
    }

    private FacultyLeaveSummaryItemDto toLeaveSummaryItemDto(
            FacultyDetailRepository.LeaveSummaryProjection projection) {
        return FacultyLeaveSummaryItemDto.builder()
                .leaveType(projection.getLeaveType())
                .startDate(projection.getStartDate())
                .endDate(projection.getEndDate())
                .location(projection.getLocation())
                .reason(projection.getReason())
                .backupFacultyPersonNumber(projection.getBackupFacultyPersonNumber())
                .build();
    }

    private FacultyStudentSummaryDto toStudentSummaryDto(
            FacultyDetailRepository.StudentProjection projection) {
        return FacultyStudentSummaryDto.builder()
                .studentPersonNumber(projection.getStudentPersonNumber())
                .fullName(projection.getFullName())
                .program(projection.getProgram())
                .build();
    }
}
