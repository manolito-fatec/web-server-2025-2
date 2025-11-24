package com.pardal.app.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pardal.app.entity.dto.terms.NewTermDto;
import com.pardal.app.entity.dto.terms.RegisterAndUpdateCheckDto;
import com.pardal.app.service.terms.TermsOfUseService;
import com.pardal.app.util.RequestExceptionHandler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/term")
@RequiredArgsConstructor
@Slf4j
public class TermOfUserController
{
    private final TermsOfUseService termsOfUseService;

    @Operation(summary = "Criar um Termo de Uso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Termo de uso criado."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar o local.")
    })
    @PostMapping()
    public ResponseEntity<?> createTerm(@RequestBody NewTermDto term) {
        return RequestExceptionHandler.handleRequest("Criar uma novo termo de uso", () -> {
            log.info("Crir um novo termo de uso");
            termsOfUseService.createNewTerm(term);
            return ResponseEntity.ok("Termo de uso criado");
        });
    }

    @Operation(summary = "Buscar Termo de Uso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca realizada com Successo."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "404", description = "Nenhum Termo encontrado encontrado."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar o local.")
    })
    @GetMapping("/current")
    public ResponseEntity<?> getTerm() {
        return RequestExceptionHandler.handleRequest("Buscar o Atual Termo", () -> {
            log.info("Buscar informação sobre o atual Termo");
            return ResponseEntity.ok(termsOfUseService.getCurrentlyTerm());
        });
    }

    @Operation(summary = "Atualizar os checks do termo de uso para o usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Atualização realizada com Successo."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar o local.")
    })
    @PatchMapping()
    public ResponseEntity<?> updateTerm(@RequestBody RegisterAndUpdateCheckDto term ) {
        return RequestExceptionHandler.handleRequest("Atualizar check termo de uso", () -> {
            log.info("Atualizando os check do termo de uso");
            termsOfUseService.updateContract(term);
            return ResponseEntity.ok("Termo atualizado com sucesso");
        });
    }

    @Operation(summary = "Busca de Termo por id do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Termo encontrado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "404", description = "Termo não encontrado."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar o local.")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getTermByUser(@PathVariable Integer userId) {
            return RequestExceptionHandler.handleRequest("Busca Termo pelo Id do usuário", () -> {
                log.info("Busca de termo para o usuário com id: {}", userId);
                return ResponseEntity.ok(termsOfUseService.getTermByUser(userId));
            });
    }

    @Operation(summary = "Verificar se o usuário está pendente com o termo de uso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verificação feita com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "404", description = "Termo não encontrado."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar o local.")
    })
    @GetMapping("/user/pending/{userId}")
    public ResponseEntity<?> termsofUseVerification(@PathVariable Integer userId) {
            return RequestExceptionHandler.handleRequest("Verificar se o usuário esta pendente", () -> {
                log.info("Verificar se o usuário com o Id {}, esta pendente ", userId);
                return ResponseEntity.ok(termsOfUseService.contractIsActive(userId));
            });
    }

}
