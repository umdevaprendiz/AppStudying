package com.example.AppStudying.services;

import com.example.AppStudying.repository.TimeLineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TimeLineService {

   @Autowired
    private TimeLineRepository timeLineRepository;

}
