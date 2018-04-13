package uk.gov.hmcts.reform.auth.parser.idam.core.user.token;

public interface UserTokenParser<T> {
    T parse(String jwt);
}

