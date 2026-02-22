package io.github.sam42r.semver.release;

import io.github.sam24r.semver.release.AbstractRestApiPublisher;
import io.github.sam42r.semver.model.release.ProviderSpec;
import io.github.sam42r.semver.model.release.ReleaseInfo;
import io.github.sam42r.semver.release.model.ApiError;
import io.github.sam42r.semver.release.model.CreateReleaseOption;

import java.io.IOException;

public class CodebergPublisher extends AbstractRestApiPublisher {

    private final String token;

    public CodebergPublisher(String baseUrl, String token) {
        super(baseUrl);
        this.token = token;
    }

    @Override
    protected Object generatePayload(ReleaseInfo releaseInfo) {
        return CreateReleaseOption.builder()
                .tagName(releaseInfo.tagName())
                .name(releaseInfo.name())
                .body(releaseInfo.description())
                .build();
    }

    @Override
    protected String[] headers() {
        return new String[]{
                "Content-Type", "application/json",
                "Accept", "application/json",
                "Authorization", token
        };
    }

    @Override
    protected String getErrorMessage(String responseBody) throws IOException {
        var apiError = getObjectMapper().readValue(responseBody, ApiError.class);
        return apiError.getMessage();
    }

    @Override
    public ProviderSpec providerSpec() {
        return new ProviderSpec(
                "%s://%s/%s/%s/issues/%s",
                "%s://%s/%s/%s/commit/%s",
                "%s://%s/%s/%s/compare/%s...%s"
        );
    }
}
