package edu.buffalo.cse.facultyportal.service;

import edu.buffalo.cse.facultyportal.dto.CommitteeMembershipDto;

import java.util.List;

public interface CommitteeService {

    List<CommitteeMembershipDto> getCommitteesByUserId(String userId);
}
