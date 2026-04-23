package edu.buffalo.cse.facultyportal.service;

import edu.buffalo.cse.facultyportal.dto.CommitteeMembershipDto;
import edu.buffalo.cse.facultyportal.repository.CommitteeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommitteeServiceImpl implements CommitteeService {

    private final CommitteeRepository committeeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CommitteeMembershipDto> getCommitteesByUserId(String userId) {
        return committeeRepository.findCommitteesByUserId(userId).stream()
                .map(row -> CommitteeMembershipDto.builder()
                        .committeeName(row.getCommitteeName())
                        .role(row.getRole())
                        .build())
                .toList();
    }
}
