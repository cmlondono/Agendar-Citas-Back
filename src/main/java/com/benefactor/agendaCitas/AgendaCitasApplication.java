package com.benefactor.agendaCitas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
class AgendaCitasApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgendaCitasApplication.class, args);
    }


}
