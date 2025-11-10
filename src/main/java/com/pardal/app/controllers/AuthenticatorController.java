package com.pardal.app.controllers;

import com.pardal.app.entity.dto.auth.JwtAuthenticationResponseDto;
import com.pardal.app.entity.dto.auth.LoginRequestDto;
import com.pardal.app.entity.dto.auth.ResponseUserCreatedDto;
import com.pardal.app.entity.dto.auth.SignupRequestDto;
import com.pardal.app.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.NoSuchElementException;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticatorController {

    private final  AuthService authService;
    private final String TITLE = "title";

    @Operation(summary = "Cadastro de Usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor no cadastro de usuário.")
    })
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequestDto request) {
        MDC.put(TITLE,"Registro de usuário");
        try {
            ResponseUserCreatedDto response = authService.signup(request);
            log.info("Usuário com email: {} foi registrado com sucesso", response.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: " + runtimeException.getMessage());
        }finally {
            MDC.remove(TITLE);
        }
    };

    @Operation(summary = "Valida o usuário criado via o token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário validado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor na validação de usuário.")
    })
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody String token) {
        MDC.put(TITLE,"Verificação de token");
        try{
            ResponseUserCreatedDto response = authService.verify(token.substring(0, token.length() - 1));
            log.info("Verification feita com sucesso");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: " + runtimeException.getMessage());
        }finally {
            MDC.remove(TITLE);
        }
    }
    @Operation(summary = "Login de Usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar login.")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
        MDC.put(TITLE,"Login");
        try {
            JwtAuthenticationResponseDto response = authService.login(request);
            log.info("Login  do usuário {} feito com sucesso", request.getEmail());
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: " + runtimeException.getMessage());
        }finally {
            MDC.remove(TITLE);
        }
    }
}
