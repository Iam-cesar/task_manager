package com.example.task_manager.infra;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI taskManagerOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Task Manager API")
                .description("""
                    API para gerenciar usuários, projetos, tarefas e etiquetas.
                    Tarefas atrasadas podem ser consultadas com `overdue=true`.
                    O resumo de tarefas por status não inclui tarefas arquivadas.
                    """)
                .version("1.0.0"));
    }
}
