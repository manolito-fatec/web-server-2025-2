package com.pardal.app.controllers;

import com.pardal.app.entity.dto.ResponseUserCreatedDto;
import com.pardal.app.entity.dto.SignupRequestDto;
import com.pardal.app.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticatorController {

    private final  AuthService authService;

    @Operation(summary = "Cadastro de Usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor no cadastro de usuário.")
    })
    @PostMapping("/signup")
    public ResponseEntity<ResponseUserCreatedDto> signup(@RequestBody SignupRequestDto request) {
        ResponseUserCreatedDto response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    };

    @Operation(summary = "Valida o usuário criado via o token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário validado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor na validação de usuário.")
    })
    @PostMapping("/verify")
    public ResponseEntity<ResponseUserCreatedDto> verify(@RequestBody String token) {
        ResponseUserCreatedDto response = authService.verify(token);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
