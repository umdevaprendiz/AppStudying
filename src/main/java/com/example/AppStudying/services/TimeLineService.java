package com.example.AppStudying.services;

import com.example.AppStudying.model.TimeLine;
import com.example.AppStudying.model.User;
import com.example.AppStudying.repository.TimeLineRepository;
import com.example.AppStudying.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TimeLineService {

   @Autowired
   private TimeLineRepository timeLineRepository;

   @Autowired
   private UserRepository userRepository;

   public TimeLine criarEvento(String description, Long userId) {
       User user = userRepository.findById(userId)
               .orElseThrow(() -> new IllegalStateException("Usuário não encontrado!"));

       TimeLine timeLine = new TimeLine();
       timeLine.setUser(user);
       timeLine.setDescription(description);
       timeLine.setEventDate(LocalDateTime.now());

       return timeLineRepository.save(timeLine);


   }

   public List<TimeLine> listarPorUsuario(Long userId){
      return timeLineRepository.findByUserId(userId);
   }

   @Cacheable
   public TimeLine buscarPorId(Long id){
       return timeLineRepository.findById(id)
               .orElseThrow(() -> new IllegalStateException("TimeLine não encontrado"));
   }

}



