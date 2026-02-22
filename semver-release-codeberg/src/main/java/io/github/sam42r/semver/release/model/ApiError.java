package io.github.sam42r.semver.release.model;

import lombok.Data;

import java.util.List;

/**
 * Generic API error used for:
 * <ul>
 *     <li>404: APINotFound is a not found error response</li>
 *     <li>409: APIError is error format response</li>
 *     <li>422: APIValidationError is error format response related to input validation</li>
 * </ul>
 */
@Data
public class ApiError {

    private String message;
    private String url;
    private List<String> errors;
}
