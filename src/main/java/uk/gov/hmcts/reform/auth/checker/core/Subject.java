package uk.gov.hmcts.reform.auth.checker.core;


public abstract class Subject {

    private final String principal;

    public Subject(String principal) {
        this.principal = principal;
    }

    public String getPrincipal() {
        return principal;
    }
}
