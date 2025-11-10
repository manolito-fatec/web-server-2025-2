package com.pardal.app.controllers;


import com.pardal.app.entity.dto.AppUserDto;
import com.pardal.app.entity.dto.UpdateUserRoleDto;
import com.pardal.app.service.appUser.AppUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
@Slf4j
public class AppUserController {
    private final AppUserService userService;
    private final String TITLE = "title";
    private final String SERVER_ERROR = "Internal Server Error:";

    @Operation(summary = "Busca de Usuário por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar o local.")
    })
    @GetMapping("/id/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Integer id) {
        MDC.put(TITLE,"Busca por Usuario");
        try
        {
            log.info("Busca pelo usuário com id: {}", id);
            return ResponseEntity.ok(userService.getUserById(id));
        } catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SERVER_ERROR + runtimeException.getMessage());
        }finally {
            MDC.remove(TITLE);
        }
    }

    @Operation(summary = "Busca de Usuário por email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar o local.")
    })
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        MDC.put(TITLE,"Busca por Email");
        try {
            log.info("Busca de usuário utilizando o email: {}",email);
            return ResponseEntity.ok(userService.convertUserToDto(userService.getUserByEmail(email)));
        }
        catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SERVER_ERROR + runtimeException.getMessage());
        }finally {
            MDC.remove(TITLE);
        }
    }

    @Operation(summary = "Busca de todos os Usuários")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuários encontrados com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "404", description = "Nenhum usuário encontrado."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar o local.")
    })
    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        MDC.put(TITLE,"Buscar todos usuários");
        try {
        List<AppUserDto> users = userService.getAllUsers();
        log.info("Busca por todos os usuários");
        return ResponseEntity.ok(users);
        }catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SERVER_ERROR + runtimeException.getMessage());
        }finally {
            MDC.remove(TITLE);
        }
    }

    @Operation(summary = "Atualizar dados de um usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "404", description = "Nenhum usuário encontrado."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar o local.")
    })
    @PostMapping()
    public ResponseEntity<?> updateUser(@RequestBody AppUserDto user) {
        MDC.put(TITLE,"Atualização de usuário");
        try {
        log.info("Atualização do usuário com o ID: {}", user.getId());
        return ResponseEntity.ok(
                userService.updateUser(
                        userService.getUserByEmail(user.getEmail())));
        }catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SERVER_ERROR + runtimeException.getMessage());
        }finally {
            MDC.remove(TITLE);
        }
    }

    @Operation(summary = "Atualizar a role de um usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "404", description = "Nenhum usuário encontrado."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar o local.")
    })
    @PostMapping("/role")
    public ResponseEntity<?> updateUserRole(@RequestBody UpdateUserRoleDto user) {
        MDC.put(TITLE,"Atualização da função do usuário");
        try {
            log.info("Atualização da role do usuário com id:{} para role: {}", user.getId(), user.getRole());
            return ResponseEntity.ok(
                    userService.updateUserRole(user));
        }catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SERVER_ERROR + runtimeException.getMessage());
        }finally {
            MDC.remove(TITLE);
        }
    }

    @Operation(summary = "Deleção de um Usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário deletado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "404", description = "Nenhum usuário encontrado."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar o local.")
    })

    @DeleteMapping()
    public ResponseEntity<?> deleteUser(@RequestParam Integer id) {
        MDC.put(TITLE,"Atualização de usuário");
        try {
            log.info("Remoção do usuário com o ID {}", id);
            return ResponseEntity.ok(userService.deleteUser(id));
        }catch (NoSuchElementException noSuchElementException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException runtimeException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SERVER_ERROR + runtimeException.getMessage());
        }finally {
            MDC.remove(TITLE);
        }
    }

}
