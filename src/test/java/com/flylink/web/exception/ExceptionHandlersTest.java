package com.flylink.web.exception;

import com.flylink.domain.exception.CodeAlreadyExistsException;
import com.flylink.domain.exception.EmailAlreadyExistsException;
import com.flylink.domain.exception.UrlExpiredException;
import com.flylink.domain.exception.UrlNotFoundException;
import com.flylink.web.dto.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ExceptionHandlersTest {

        private final AuthExceptionHandler authHandler = new AuthExceptionHandler();
        private final DatabaseExceptionHandler dbHandler = new DatabaseExceptionHandler();
        private final DomainExceptionHandler domainHandler = new DomainExceptionHandler();
        private final ValidationExceptionHandler validationHandler = new ValidationExceptionHandler();
        private final ResourceNotFoundExceptionHandler notFoundHandler = new ResourceNotFoundExceptionHandler();

        // GlobalHandler test wrapper to access protected method
        private static class TestGlobalExceptionHandler extends GlobalExceptionHandler {
                public ResponseEntity<Object> callHandleExceptionInternal(Exception ex, HttpStatusCode statusCode,
                                WebRequest request) {
                        return super.handleExceptionInternal(ex, null, new HttpHeaders(), statusCode, request);
                }
        }

        private final TestGlobalExceptionHandler globalHandler = new TestGlobalExceptionHandler();

        @Test
        @DisplayName("Deve tratar EmailAlreadyExistsException")
        void handleEmailAlreadyExists() {
                MockHttpServletRequest request = new MockHttpServletRequest();
                request.setRequestURI("/test");
                EmailAlreadyExistsException ex = new EmailAlreadyExistsException("test@test.com");

                ResponseEntity<ErrorResponse> response = authHandler.handleEmailAlreadyExists(ex, request);

                assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals("Já existe um usuário cadastrado com o email: test@test.com",
                                response.getBody().getMessage());
        }

        @Test
        @DisplayName("Deve tratar BadCredentialsException")
        void handleBadCredentials() {
                MockHttpServletRequest request = new MockHttpServletRequest();
                request.setRequestURI("/login");
                BadCredentialsException ex = new BadCredentialsException("Bad");

                ResponseEntity<ErrorResponse> response = authHandler.handleBadCredentials(ex, request);

                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals("Credenciais inválidas", response.getBody().getMessage());
        }

        @Test
        @DisplayName("Deve tratar AccessDeniedException")
        void handleAccessDenied() {
                MockHttpServletRequest request = new MockHttpServletRequest();
                request.setRequestURI("/secure");
                AccessDeniedException ex = new AccessDeniedException("Denied");

                ResponseEntity<ErrorResponse> response = authHandler.handleAccessDenied(ex, request);

                assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals("Acesso negado", response.getBody().getMessage());
        }

        @Test
        @DisplayName("Deve tratar os cenários de DataIntegrityViolationException")
        void handleDataIntegrityViolationBranches() {
                MockHttpServletRequest request = new MockHttpServletRequest();
                request.setRequestURI("/db");

                DataIntegrityViolationException exUnique = new DataIntegrityViolationException("Violation",
                                new RuntimeException("duplicate key value"));
                assertEquals("Já existe um registro com esse valor",
                                dbHandler.handleDataIntegrityViolation(exUnique, request).getBody().getMessage());
                DataIntegrityViolationException exUnique2 = new DataIntegrityViolationException("Violation",
                                new RuntimeException("unique constraint"));
                assertEquals("Já existe um registro com esse valor",
                                dbHandler.handleDataIntegrityViolation(exUnique2, request).getBody().getMessage());

                DataIntegrityViolationException exTooLong = new DataIntegrityViolationException("Violation",
                                new RuntimeException("value too long for type"));
                assertEquals("Valor excede o tamanho máximo permitido",
                                dbHandler.handleDataIntegrityViolation(exTooLong, request).getBody().getMessage());

                DataIntegrityViolationException exNotNull = new DataIntegrityViolationException("Violation",
                                new RuntimeException("null value in column violates not-null constraint"));
                assertEquals("Campo obrigatório não informado",
                                dbHandler.handleDataIntegrityViolation(exNotNull, request).getBody().getMessage());
                DataIntegrityViolationException exCannotBeNull = new DataIntegrityViolationException("Violation",
                                new RuntimeException("cannot be null"));
                assertEquals("Campo obrigatório não informado",
                                dbHandler.handleDataIntegrityViolation(exCannotBeNull, request).getBody().getMessage());

                DataIntegrityViolationException exFk = new DataIntegrityViolationException("Violation",
                                new RuntimeException("violates foreign key constraint"));
                assertEquals("Registro referenciado não existe ou não pode ser removido",
                                dbHandler.handleDataIntegrityViolation(exFk, request).getBody().getMessage());
                DataIntegrityViolationException exFk2 = new DataIntegrityViolationException("Violation",
                                new RuntimeException("constraint fk_test violated"));
                assertEquals("Registro referenciado não existe ou não pode ser removido",
                                dbHandler.handleDataIntegrityViolation(exFk2, request).getBody().getMessage());

                DataIntegrityViolationException exGeneric = new DataIntegrityViolationException("Violation",
                                new RuntimeException("Generic Error"));
                assertEquals("Erro ao salvar os dados. Verifique as informações e tente novamente",
                                dbHandler.handleDataIntegrityViolation(exGeneric, request).getBody().getMessage());
        }

        @Test
        @DisplayName("Deve tratar CodeAlreadyExistsException")
        void handleCodeAlreadyExists() {
                MockHttpServletRequest request = new MockHttpServletRequest();
                request.setRequestURI("/domain");
                CodeAlreadyExistsException ex = new CodeAlreadyExistsException("code123");

                ResponseEntity<ErrorResponse> response = domainHandler.handleCodeAlreadyExists(ex, request);

                assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals("Já existe uma URL com o código: code123", response.getBody().getMessage());
        }

        @Test
        @DisplayName("Deve tratar UrlExpiredException")
        void handleUrlExpired() {
                MockHttpServletRequest request = new MockHttpServletRequest();
                request.setRequestURI("/domain/expired");
                UrlExpiredException ex = new UrlExpiredException("code123");

                ResponseEntity<ErrorResponse> response = domainHandler.handleUrlExpired(ex, request);

                assertEquals(HttpStatus.GONE, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals("A URL com o código 'code123' expirou e não está mais disponível.",
                                response.getBody().getMessage());
        }

        @Test
        @DisplayName("Deve tratar UrlNotFoundException")
        void handleUrlNotFound() {
                MockHttpServletRequest request = new MockHttpServletRequest();
                request.setRequestURI("/notfound");
                UrlNotFoundException ex = new UrlNotFoundException("code123");

                ResponseEntity<ErrorResponse> response = domainHandler.handleUrlNotFound(ex, request);

                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals("URL não encontrada com o código: code123", response.getBody().getMessage());
        }

        @Test
        @DisplayName("Deve tratar os cenários de NoResourceFoundException")
        void handleNoResourceFoundBranches() {
                MockHttpServletRequest request = new MockHttpServletRequest();
                request.setRequestURI("/static/image.png");
                NoResourceFoundException ex = new NoResourceFoundException(org.springframework.http.HttpMethod.GET,
                                "/static/image.png");

                ResponseEntity<ErrorResponse> response = notFoundHandler.handleNoResourceFound(ex, request);
                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

                MockHttpServletRequest reqFavicon = new MockHttpServletRequest();
                reqFavicon.setRequestURI("/favicon.ico");
                ResponseEntity<ErrorResponse> favResponse = notFoundHandler.handleNoResourceFound(ex, reqFavicon);
                assertEquals(HttpStatus.NOT_FOUND, favResponse.getStatusCode());
        }

        @Test
        @DisplayName("Deve tratar exceções genéricas (Erro 500)")
        void handleGenericException() {
                MockHttpServletRequest request = new MockHttpServletRequest();
                request.setRequestURI("/error");
                Exception ex = new Exception("Generic error");

                ResponseEntity<ErrorResponse> response = globalHandler.handleGenericException(ex, request);

                assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals("Erro interno do servidor", response.getBody().getMessage());
                assertEquals(500, response.getBody().getStatus());
        }

        @Test
        @DisplayName("Deve tratar exceções internas do framework")
        void handleFrameworkExceptions() {
                MockHttpServletRequest servletRequest = new MockHttpServletRequest();
                servletRequest.setRequestURI("/global");
                WebRequest request = new ServletWebRequest(servletRequest);

                assertEquals("Requisição inválida: verifique os parâmetros enviados",
                                ((ErrorResponse) globalHandler
                                                .callHandleExceptionInternal(new Exception("test"),
                                                                HttpStatus.BAD_REQUEST, request)
                                                .getBody())
                                                .getMessage());

                assertEquals("Método HTTP não permitido para este endpoint",
                                ((ErrorResponse) globalHandler
                                                .callHandleExceptionInternal(new Exception("test"),
                                                                HttpStatus.METHOD_NOT_ALLOWED, request)
                                                .getBody()).getMessage());

                assertEquals("Formato de resposta não suportado",
                                ((ErrorResponse) globalHandler
                                                .callHandleExceptionInternal(new Exception("test"),
                                                                HttpStatus.NOT_ACCEPTABLE, request)
                                                .getBody()).getMessage());

                assertEquals("Tipo de conteúdo não suportado. Use application/json",
                                ((ErrorResponse) globalHandler
                                                .callHandleExceptionInternal(new Exception("test"),
                                                                HttpStatus.UNSUPPORTED_MEDIA_TYPE, request)
                                                .getBody()).getMessage());

                assertEquals("Unknown error",
                                ((ErrorResponse) globalHandler
                                                .callHandleExceptionInternal(new Exception("Unknown error"),
                                                                HttpStatus.I_AM_A_TEAPOT, request)
                                                .getBody()).getMessage());
        }

        @Test
        @DisplayName("Deve tratar requisições web que não são Servlets")
        void handleNonServletWebRequest() {
                WebRequest request = mock(WebRequest.class);
                ResponseEntity<Object> response = globalHandler.callHandleExceptionInternal(new Exception("test"),
                                HttpStatus.BAD_REQUEST, request);
                assertEquals("", ((ErrorResponse) response.getBody()).getPath());
        }

        @Test
        @DisplayName("Deve tratar erros de validação (MethodArgumentNotValidException)")
        void handleValidationErrors() throws Exception {
                MockHttpServletRequest request = new MockHttpServletRequest();
                request.setRequestURI("/validate");

                BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
                bindingResult.addError(new FieldError("target", "email", "must be a well-formed email address"));

                java.lang.reflect.Method method = this.getClass().getDeclaredMethod("handleValidationErrors");
                org.springframework.core.MethodParameter param = new org.springframework.core.MethodParameter(method,
                                -1);

                MethodArgumentNotValidException ex = new MethodArgumentNotValidException(param, bindingResult);

                ResponseEntity<ErrorResponse> response = validationHandler.handleValidationErrors(ex, request);

                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals("Erro de validação", response.getBody().getMessage());
                List<String> errors = response.getBody().getErrors();
                assertTrue(errors.contains("email: must be a well-formed email address"));
        }
}
