package com.pardal.app.controllers;


import com.pardal.app.entity.dto.AppUserDto;
import com.pardal.app.entity.dto.UpdateUserRoleDto;
import com.pardal.app.service.appUser.AppUserService;
import com.pardal.app.service.export.CsvExportService;
import com.pardal.app.util.RequestExceptionHandler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
@Slf4j
public class AppUserController {
    private final AppUserService userService;
    private final CsvExportService csvExportService;

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
        return RequestExceptionHandler.handleRequest("Busca por Usuario", () -> {
            log.info("Busca pelo usuário com id: {}", id);
            return ResponseEntity.ok(userService.getUserById(id));
        });
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
            return RequestExceptionHandler.handleRequest("Busca por Email", () -> {
                log.info("Busca de usuário utilizando o email: {}", email);
                return ResponseEntity.ok(userService.convertUserToDto(userService.getUserByEmail(email)));
            });
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
        return RequestExceptionHandler.handleRequest("Buscar todos usuários", () -> {
            List<AppUserDto> users = userService.getAllUsers();
            log.info("Busca por todos os usuários");
            return ResponseEntity.ok(users);
        });
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
        return RequestExceptionHandler.handleRequest("Atualização de usuário", () -> {
            log.info("Atualização do usuário com o ID: {}", user.getId());
            return ResponseEntity.ok(userService.updateProfile(user));
        });
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
        return RequestExceptionHandler.handleRequest("Atualização da função do usuário", () -> {
            log.info("Atualização da role do usuário com id:{} para role: {}", user.getId(), user.getRole());
            return ResponseEntity.ok(
                    userService.updateUserRole(user));
        });
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
        return RequestExceptionHandler.handleRequest("Remoção de usuário", () -> {
            log.info("Remoção do usuário com o ID {}", id);
            return ResponseEntity.ok(userService.deleteUser(id));
        });
    }

    @Operation(summary = "Buscar informações relacionadas a tela de perfil do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca realizada com Successo."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "404", description = "Nenhum usuário encontrado."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar o local.")
    })
    @GetMapping(value = "/information/{userId}" )
    public ResponseEntity<?> getInfoAboutUserAndAuditById(@PathVariable Integer userId) {
        return RequestExceptionHandler.handleRequest("Buscar informações relacionadas a tela de perfil do usuário", () -> {
            log.info("Buscar informação do pefil do usuário com o id: {}", userId);
            return ResponseEntity.ok(userService.getAllInformationAboutUser(userId));
        });
    }

    @Operation(summary = "Buscar informações relacionadas a tela de perfil do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca realizada com Successo."),
            @ApiResponse(responseCode = "400", description = "Requisição mal formulada."),
            @ApiResponse(responseCode = "404", description = "Nenhum usuário encontrado."),
            @ApiResponse(responseCode = "408", description = "Tempo de resposta excedido."),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao tentar buscar o local.")
    })
    @GetMapping(value = "/information" )
    public ResponseEntity<?> getInfoAboutUserAndAuditByEmail(
            @Parameter(description = "email do usuario")
            String email) {
        return RequestExceptionHandler.handleRequest("Buscar informações relacionadas a tela de perfil do usuário", () -> {
            log.info("Buscar informação do pefil do usuário com o email: {}", email);
            return ResponseEntity.ok(userService.getAllInformationAboutUser(email));
        });
    }

    @Operation(summary = "Exportar dados do audit log para csv")
    @ApiResponses(value = {
                    @ApiResponse(responseCode = "200", description = "Arquivo ZIP com os CSVs gerado com sucesso."),
                    @ApiResponse(responseCode = "500", description = "Erro interno do servidor ao gerar o arquivo.")
    })
    @GetMapping(value = "/audit/csv" )
    public void getAuditLogCsv(HttpServletResponse response) throws IOException {
        String zipFileName = "audit_log_export" + System.currentTimeMillis() + ".zip";
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + zipFileName + "\"");
        var data = userService.getAllAuditLog();
        RequestExceptionHandler.handleExport("Exportar Audit Log CSV", response, () -> {
            log.info("Realizar o export para CSV do audit log");
            csvExportService.exportAuditLogZip(data, response.getOutputStream());
        });
    }

}
