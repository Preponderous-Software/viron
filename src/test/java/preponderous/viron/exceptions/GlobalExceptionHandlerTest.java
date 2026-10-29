package preponderous.viron.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import preponderous.viron.dto.ErrorResponse;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pins what each handler answers — status, body, and headers — and which handler Spring picks
 * for a given exception. The controller tests reach most handlers through MockMvc; this covers
 * the ones they do not, and the message-assembly rules that only show with several violations.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private final ExceptionHandlerMethodResolver resolver =
            new ExceptionHandlerMethodResolver(GlobalExceptionHandler.class);

    private static void assertError(ResponseEntity<ErrorResponse> response, HttpStatus status, String message) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(status.value());
        assertThat(response.getBody().getMessage()).isEqualTo(message);
    }

    // Dispatch

    private String resolvedHandlerFor(Exception ex) {
        Method method = resolver.resolveMethod(ex);
        assertThat(method).isNotNull();
        return method.getName();
    }

    @Test
    void subclassesOfServiceExceptionReachTheirOwnHandlerRatherThanTheParents() {
        assertThat(resolvedHandlerFor(new NotFoundException("missing")))
                .isEqualTo("handleNotFoundException");
        assertThat(resolvedHandlerFor(new EntityServiceException("entity failure")))
                .isEqualTo("handleEntityServiceException");
        assertThat(resolvedHandlerFor(new ServiceException("service failure")))
                .isEqualTo("handleServiceException");
    }

    @Test
    void anExceptionWithNoHandlerOfItsOwnFallsThroughToTheGenericHandler() {
        assertThat(resolvedHandlerFor(new IllegalStateException("boom")))
                .isEqualTo("handleGenericException");
    }

    @Test
    void aLockTimeoutReachesTheLockHandlerRatherThanTheGenericOne() {
        assertThat(resolvedHandlerFor(new CannotAcquireLockException("lock timeout")))
                .isEqualTo("handleCannotAcquireLockException");
    }

    @Test
    void aTypeMismatchReachesItsOwnHandlerRatherThanTheGenericOne() {
        assertThat(resolvedHandlerFor(typeMismatch("abc", int.class, "id")))
                .isEqualTo("handleMethodArgumentTypeMismatchException");
    }

    @Test
    void anUnreadableBodyReachesItsOwnHandlerRatherThanTheGenericOne() {
        assertThat(resolvedHandlerFor(unreadableBody()))
                .isEqualTo("handleHttpMessageNotReadableException");
    }

    // Client errors that quote the exception's message

    @Test
    void notFoundAnswers404WithTheExceptionsMessage() {
        assertError(handler.handleNotFoundException(new NotFoundException("Entity not found with id: 7")),
                HttpStatus.NOT_FOUND, "Entity not found with id: 7");
    }

    @Test
    void conflictAnswers409WithTheExceptionsMessage() {
        assertError(handler.handleConflictException(new ConflictException("Target location 3 is occupied")),
                HttpStatus.CONFLICT, "Target location 3 is occupied");
    }

    @Test
    void creationAndInvalidRequestFailuresAnswer400WithTheExceptionsMessage() {
        assertError(handler.handleEntityCreationException(new EntityCreationException("Failed to get next entity id")),
                HttpStatus.BAD_REQUEST, "Failed to get next entity id");
        assertError(handler.handleEnvironmentCreationException(new EnvironmentCreationException("Failed to create grid")),
                HttpStatus.BAD_REQUEST, "Failed to create grid");
        assertError(handler.handleInvalidRequestException(new InvalidRequestException("No grids found in environment: 2")),
                HttpStatus.BAD_REQUEST, "No grids found in environment: 2");
    }

    // Parameter type conversion

    private static MethodArgumentTypeMismatchException typeMismatch(Object value, Class<?> requiredType, String name) {
        return new MethodArgumentTypeMismatchException(value, requiredType, name, mock(MethodParameter.class), null);
    }

    @Test
    void anIntegerParameterThatIsNotANumberAnswers400NamingTheParameter() {
        assertError(handler.handleMethodArgumentTypeMismatchException(typeMismatch("abc", int.class, "id")),
                HttpStatus.BAD_REQUEST, "id: must be an integer");
        assertError(handler.handleMethodArgumentTypeMismatchException(typeMismatch("abc", Integer.class, "environmentId")),
                HttpStatus.BAD_REQUEST, "environmentId: must be an integer");
    }

    @Test
    void aTypeMismatchOnAnotherTypeNamesThatTypeWithoutQuotingTheValue() {
        assertError(handler.handleMethodArgumentTypeMismatchException(typeMismatch("<script>", Long.class, "since")),
                HttpStatus.BAD_REQUEST, "since: must be a valid Long");
        assertError(handler.handleMethodArgumentTypeMismatchException(typeMismatch("x", null, "since")),
                HttpStatus.BAD_REQUEST, "since: has an invalid value");
    }

    // Request bodies and framework rejections

    private static HttpMessageNotReadableException unreadableBody() {
        return new HttpMessageNotReadableException("JSON parse error: Unrecognized token 'nope'",
                new ServletServerHttpRequest(new MockHttpServletRequest()));
    }

    @Test
    void anUnreadableBodyAnswers400WithoutQuotingTheParsersMessage() {
        assertError(handler.handleHttpMessageNotReadableException(unreadableBody()),
                HttpStatus.BAD_REQUEST, "Request body is missing, is not valid JSON, or has a field of the wrong type");
    }

    @Test
    void aClientErrorSpringRaisesItselfAnswersItsOwnStatusAndHeaders() {
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(
                new HttpRequestMethodNotSupportedException("DELETE", List.of("GET")));

        assertError(response, HttpStatus.METHOD_NOT_ALLOWED, "Method 'DELETE' is not supported.");
        assertThat(response.getHeaders().getAllow()).containsExactly(org.springframework.http.HttpMethod.GET);
    }

    /**
     * Only a 4xx rejection is passed through: a 5xx one is still a fault of the service, answered
     * in the generic 500 manner.
     */
    @Test
    void aServerErrorSpringRaisesItselfIsStillAnswered500() {
        assertError(handler.handleGenericException(new AsyncRequestTimeoutException()),
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    // Server errors

    @Test
    void serviceFailuresAnswer500WithTheExceptionsMessage() {
        assertError(handler.handleEntityServiceException(new EntityServiceException("Error retrieving entities")),
                HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving entities");
        assertError(handler.handleServiceException(new ServiceException("Failed to update name for grid 4")),
                HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update name for grid 4");
    }

    @Test
    void anUnexpectedExceptionAnswers500WithoutQuotingItsMessage() {
        assertError(handler.handleGenericException(new IllegalStateException("connection string with a password")),
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    @Test
    void aLockTimeoutAnswers503WithRetryAfterAndWithoutQuotingTheDriverMessage() {
        ResponseEntity<ErrorResponse> response = handler.handleCannotAcquireLockException(
                new CannotAcquireLockException("ERROR: canceling statement due to lock timeout"));

        assertError(response, HttpStatus.SERVICE_UNAVAILABLE,
                "The requested resource is in use by another request; retry shortly");
        assertThat(response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER))
                .isEqualTo(GlobalExceptionHandler.RETRY_AFTER_SECONDS)
                .isEqualTo("1");
    }

    // Request-body validation

    private static MethodArgumentNotValidException bodyValidationFailure(FieldError... fieldErrors) {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        for (FieldError fieldError : fieldErrors) {
            bindingResult.addError(fieldError);
        }
        return new MethodArgumentNotValidException(null, bindingResult);
    }

    @Test
    void aBodyValidationFailureNamesTheFieldAndItsMessage() {
        MethodArgumentNotValidException ex = bodyValidationFailure(
                new FieldError("request", "name", "must not be blank"));

        assertError(handler.handleValidationException(ex), HttpStatus.BAD_REQUEST, "name: must not be blank");
    }

    @Test
    void severalBodyValidationFailuresAreJoinedInTheOrderTheyWereReported() {
        MethodArgumentNotValidException ex = bodyValidationFailure(
                new FieldError("request", "numGrids", "must be greater than or equal to 1"),
                new FieldError("request", "name", "must not be blank"));

        assertError(handler.handleValidationException(ex), HttpStatus.BAD_REQUEST,
                "numGrids: must be greater than or equal to 1; name: must not be blank");
    }

    @Test
    void aBodyValidationFailureWithNoFieldErrorsFallsBackToAGenericMessage() {
        assertError(handler.handleValidationException(bodyValidationFailure()),
                HttpStatus.BAD_REQUEST, "Validation failed");
    }

    @Test
    void aHandlerMethodValidationFailureAnswers400WithAFixedMessage() {
        HandlerMethodValidationException ex = mock(HandlerMethodValidationException.class);
        when(ex.getMessage()).thenReturn("400 BAD_REQUEST \"Validation failure\"");

        assertError(handler.handleHandlerMethodValidationException(ex), HttpStatus.BAD_REQUEST, "Validation failed");
    }

    // Method-parameter constraints

    private static Path pathOf(String... nodeNames) {
        List<Path.Node> nodes = new ArrayList<>();
        for (String nodeName : nodeNames) {
            Path.Node node = mock(Path.Node.class);
            when(node.getName()).thenReturn(nodeName);
            nodes.add(node);
        }
        Path path = mock(Path.class);
        when(path.iterator()).thenAnswer(invocation -> nodes.iterator());
        when(path.toString()).thenReturn(String.join(".", nodeNames));
        return path;
    }

    private static ConstraintViolation<?> violation(Path path, String message) {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn(message);
        return violation;
    }

    private static ConstraintViolationException constraintFailure(ConstraintViolation<?>... violations) {
        Set<ConstraintViolation<?>> set = new LinkedHashSet<>(List.of(violations));
        return new ConstraintViolationException(set);
    }

    @Test
    void aConstraintViolationQuotesOnlyTheParameterNotTheMethodThatReceivedIt() {
        ConstraintViolationException ex = constraintFailure(
                violation(pathOf("getEntityById", "id"), "must be greater than or equal to 1"));

        assertError(handler.handleConstraintViolationException(ex), HttpStatus.BAD_REQUEST,
                "id: must be greater than or equal to 1");
    }

    @Test
    void severalConstraintViolationsAreSortedSoTheMessageIsStable() {
        ConstraintViolationException ex = constraintFailure(
                violation(pathOf("moveEntityToLocation", "locationId"), "must be greater than or equal to 1"),
                violation(pathOf("moveEntityToLocation", "entityId"), "must be greater than or equal to 1"));

        assertError(handler.handleConstraintViolationException(ex), HttpStatus.BAD_REQUEST,
                "entityId: must be greater than or equal to 1; locationId: must be greater than or equal to 1");
    }

    @Test
    void aConstraintViolationWhosePathHasNoNamedNodeFallsBackToThePathsText() {
        Path unnamed = pathOf((String) null);
        when(unnamed.toString()).thenReturn("<return value>");
        ConstraintViolationException ex = constraintFailure(violation(unnamed, "must not be null"));

        assertError(handler.handleConstraintViolationException(ex), HttpStatus.BAD_REQUEST,
                "<return value>: must not be null");
    }

    @Test
    void aConstraintViolationExceptionWithNoViolationsFallsBackToAGenericMessage() {
        assertError(handler.handleConstraintViolationException(constraintFailure()),
                HttpStatus.BAD_REQUEST, "Validation failed");
    }
}
