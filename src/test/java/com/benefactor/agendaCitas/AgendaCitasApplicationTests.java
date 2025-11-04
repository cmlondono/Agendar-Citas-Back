package com.benefactor.agendaCitas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootTest
@SpringBootApplication
@EnableScheduling
class AgendaCitasApplicationTests {
    public static void main(String[] args) {
        SpringApplication.run(AgendaCitasApplication.class, args);
    }


}
