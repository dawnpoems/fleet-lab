package kr.dawnpoem.fleetlab.navigation.adapter.web

import kr.dawnpoem.fleetlab.navigation.application.MapNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice(basePackageClasses = [NavigationMapController::class])
class NavigationApiErrors {
    @ExceptionHandler(ArithmeticException::class)
    fun distanceOverflow(): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(422), "Reachable paths exceed the finite distance range")

    @ExceptionHandler(IllegalArgumentException::class)
    fun invalidInput(exception: IllegalArgumentException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.message ?: "Invalid navigation input")

    @ExceptionHandler(HttpMessageNotReadableException::class, MethodArgumentTypeMismatchException::class)
    fun malformedInput(): ProblemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Malformed request body or identifier")

    @ExceptionHandler(MapNotFoundException::class)
    fun missingMap(exception: MapNotFoundException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.message ?: "Map not found")
}
