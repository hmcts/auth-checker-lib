package uk.gov.hmcts.auth.checker;


public interface SubjectResolver<T extends Subject> {
    T getTokenDetails(String bearerToken);
}
