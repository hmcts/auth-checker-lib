# auth-checker-lib
[![](https://jitpack.io/v/hmcts/auth-checker-lib.svg)](https://jitpack.io/#hmcts/auth-checker-lib)
[![](https://github.com/hmcts/auth-checker-lib/actions/workflows/ci.yml/badge.svg)](https://github.com/hmcts/auth-checker-lib/actions/workflows/ci.yml/)

A library for verifying user/service "Bearer" tokens and enforcing coarse grained authentication/authorization.   

## Securing a spring-boot application

*auth-checker-spring* provides a set of filters for securing an application in a spring-security friendly way:
- *AuthCheckerUserOnlyFilter* - for user only authentication/authorization
- *AuthCheckerServiceOnlyFilter* - for service only authentication/authorization
- *AuthCheckerServiceAndUserFilter* - for user & service authentication/authorization

### Configuring user authentication/authorization

1. Initialize *AuthCheckerUserOnlyFilter* OR *AuthCheckerServiceAndUserFilter* bean in your application
2. Configure spring security to use the filter, e.g.
   ```java
   @Configuration
   @EnableWebSecurity
   public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
   
       @Autowired
       private AuthCheckerUserOnlyFilter filter;
   
       @Override
       protected void configure(HttpSecurity http) throws Exception {
           http
               .addFilter(filter)
               .authorizeRequests().anyRequest().authenticated();
       }
   }
   ```
3. Initialize bean for returning a list of roles allowed to execute a given request. If necessary it can be coarse-grained at 
   this level and more fine-grained using standard spring-security approach (e.g. @Secured annotation at class/method level)
   ```java
   
   @Bean
   public Function<HttpServletRequest, Collection<String>> authorizedRolesExtractor() {
       return (anyRequest) -> Collections.singletonList("citizen");
   }
   ```
4. Initialize bean for returning an id of a user that is allowed to execute given request. Useful, if your API is structured in a following way /users/{userId}/appeals/{appealId}. If any user can execute given request, return Optional.empty()
   ```java
   @Bean
   public Function<HttpServletRequest, Optional<String>> userIdExtractor() {
       Pattern pattern = Pattern.compile("^/users/([^/]+)/.+$");

       return (request) -> {
           Matcher matcher = pattern.matcher(request.getRequestURI());
           boolean matched = matcher.find();
           return Optional.ofNullable(matched ? matcher.group(1) : null);
       };
   }
   ```

### Configuring service authentication/authorization
1. Initialize *AuthCheckerServiceOnlyFilter* OR *AuthCheckerServiceAndUserFilter* bean in your application
2. Configure spring security to use the filter, e.g.
   ```java
   @Configuration
   @EnableWebSecurity
   public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    
       @Autowired
       private AuthCheckerServiceOnlyFilter filter;
    
       @Override
       protected void configure(HttpSecurity http) throws Exception {
           http
               .addFilter(filter)
               .authorizeRequests().anyRequest().authenticated();
       }
   }
   ```
3. Initialize bean for returning a list of services allowed to execute a given request. It is most likely to be a static
   list based on some application property
   ```java
   
   @Bean
   public Function<HttpServletRequest, Collection<String>> authorizedServicesExtractor() {
       return (request) -> Collections.singletonList("divorce");
   }
   ```

### Accessing user/service details
Given user/service authentication was successful you can access authentication details using any of spring-security approaches. e.g.
```java
ServiceAndUserDetails serviceAndUser = (ServiceAndUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
serviceAndUser.getUsername();       // returns user's id
serviceAndUser.getPassword();       // returns user's Bearer token (only if Spring's eraseCredentialsAfterAuthentication==false)
serviceAndUser.getServicename();    // returns service's name
```

**NOTE**: the example above is only valid if AuthCheckerServiceAndUserFilter is used. If you are using AuthCheckerUserOnlyFilter the principal 
is going to be UserDetails and ServiceDetails in case of AuthCheckerServiceOnlyFilter.

### Configuring token cache
auth-checker-lib provides a basic caching solution aimed at minimizing latency and number of HTTP requests being sent to IDAM and 
service-auth-provider. The following properties can be used to tweak it: 
- auth.checker.cache.service.ttlInSeconds
- auth.checker.cache.service.maximumSize
- auth.checker.cache.user.ttlInSeconds
- auth.checker.cache.user.maximumSize

Caching can be disabled by setting ttl to 0 or defining your own "serviceResolver"/"userResolver" beans.

### Spring integration testing

For spring integration testing, you can mock the authenticated user/service by defining SubjectResolver<Service> and SubjectResolver<User> 
in your spring's test context.

For an example, please check 
https://github.com/hmcts/document-management-store-app
