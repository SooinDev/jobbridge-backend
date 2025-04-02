package com.jobbridge.jobbridge_backend.service;

import com.jobbridge.jobbridge_backend.dto.ResumeDto;
import com.jobbridge.jobbridge_backend.entity.Resume;
import com.jobbridge.jobbridge_backend.entity.User;
import com.jobbridge.jobbridge_backend.repository.ResumeRepository;
import com.jobbridge.jobbridge_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Transactional
    public ResumeDto.Response createResume(String email, ResumeDto.Request request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (user.getUserType() != User.UserType.INDIVIDUAL) {
            throw new IllegalArgumentException("개인 회원만 이력서를 등록할 수 있습니다.");
        }

        Resume resume = Resume.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .user(user)
                .build();

        Resume savedResume = resumeRepository.save(resume);
        return convertToDto(savedResume);
    }

    @Transactional(readOnly = true)
    public List<ResumeDto.Response> getUserResumes(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return resumeRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ResumeDto.Response getResume(Long id) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다."));

        return convertToDto(resume);
    }

    @Transactional
    public ResumeDto.Response updateResume(Long id, String email, ResumeDto.Request request) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다."));

        if (!resume.getUser().getEmail().equals(email)) {
            throw new IllegalArgumentException("자신의 이력서만 수정할 수 있습니다.");
        }

        resume.setTitle(request.getTitle());
        resume.setContent(request.getContent());

        Resume updatedResume = resumeRepository.save(resume);
        return convertToDto(updatedResume);
    }

    @Transactional
    public void deleteResume(Long id, String email) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다."));

        if (!resume.getUser().getEmail().equals(email)) {
            throw new IllegalArgumentException("자신의 이력서만 삭제할 수 있습니다.");
        }

        resumeRepository.delete(resume);
    }

    private ResumeDto.Response convertToDto(Resume resume) {
        ResumeDto.Response response = new ResumeDto.Response();
        response.setId(resume.getId());
        response.setTitle(resume.getTitle());
        response.setContent(resume.getContent());
        response.setUserName(resume.getUser().getName());
        response.setCreatedAt(resume.getCreatedAt().format(formatter));
        response.setUpdatedAt(resume.getUpdatedAt().format(formatter));
        return response;
    }
}