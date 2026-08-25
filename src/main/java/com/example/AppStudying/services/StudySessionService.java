package com.example.AppStudying.services;

import com.example.AppStudying.repository.StudySessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudySessionService {

    @Autowired
    private StudySessionRepository studySessionRepository;


}
