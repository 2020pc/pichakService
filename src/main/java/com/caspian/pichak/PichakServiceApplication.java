package com.caspian.pichak;

import com.caspian.pichak.utility.LoadOverrideProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
//@ImportResource({"classpath:applicationContext-gateway-spi.xml"})
public class PichakServiceApplication {


    public static void main(String[] args) {
        new LoadOverrideProperties().loadOverrideProperties();
        SpringApplication.run(PichakServiceApplication.class, args);
    }

}
