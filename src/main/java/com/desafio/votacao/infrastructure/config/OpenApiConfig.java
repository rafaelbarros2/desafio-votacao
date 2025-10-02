package com.desafio.votacao.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:" + serverPort);
        server.setDescription("Servidor de Desenvolvimento");

        Contact contact = new Contact();
        contact.setName("DBServer");
        contact.setUrl("https://github.com/rafaelbarros2/desafio-votacao");

        License license = new License();
        license.setName("MIT License");
        license.setUrl("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("API de Votação Cooperativa")
                .version("1.0.0")
                .description("API REST para gerenciamento de sessões de votação em cooperativas. " +
                        "Permite criar pautas, abrir sessões de votação com tempo determinado, " +
                        "registrar votos de associados e contabilizar resultados.")
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }
}
