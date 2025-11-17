package com.pardal.app.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

    @Operation(summary = "Atualizar termo de Uso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca realizada com Successo."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "404", description = "Nenhum Termo encontrado encontrado."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar o local.")
    })
    @PatchMapping()
    public ResponseEntity<?> updateTerm(@RequestBody RegisterAndUpdateCheckDto term ) {
        return RequestExceptionHandler.handleRequest("Buscar o Atual Termo", () -> {
            log.info("Buscar informação sobre o atual Termo");
            termsOfUseService.updateContract(term);
            return ResponseEntity.ok("Termo atualiuzado com sucesso");
        });
    }

}
