package uk.gov.hmcts.reform.auth.checker.core;


public interface SubjectResolver<T extends Subject> {
    T getTokenDetails(String bearerToken);
}
