/*
 * eiam-protocol-oidc - Employee Identity and Access Management Program
 * Copyright © 2020-2022 TopIAM (support@topiam.cn)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cn.topiam.employee.protocol.oidc.authentication.implicit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.temporal.ChronoUnit;
import java.util.*;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.log.LogMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationException;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.RedirectUrlBuilder;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.security.web.util.matcher.*;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponentsBuilder;

import cn.topiam.employee.common.constants.ProtocolConstants;
import cn.topiam.employee.core.context.ServerContextHelp;
import cn.topiam.employee.core.security.savedredirect.HttpSessionRedirectCache;
import cn.topiam.employee.core.security.savedredirect.RedirectCache;
import static org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames.TOKEN_TYPE;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.*;

import static cn.topiam.employee.common.constants.AuthorizeConstants.FE_LOGIN;
import static cn.topiam.employee.protocol.oidc.util.EiamOAuth2Utils.appendUrl;

/**
 * OAuth2 授权过滤器
 *
 * 用于支持 授权码模式和隐式授权模式
 *
 * @author TopIAM
 * Created by support@topiam.cn on  2022/10/28 23:26
 */
@SuppressWarnings({ "All" })
public final class EiamOAuth2AuthorizationImplicitAuthenticationEndpointFilter extends
                                                                               OncePerRequestFilter {
    /**
     * The default endpoint {@code URI} for authorization requests.
     */
    private static final String                                DEFAULT_AUTHORIZATION_ENDPOINT_URI = ProtocolConstants.OidcEndpointConstants.AUTHORIZATION_ENDPOINT;
    /**
     * 重定向缓存
     */
    private final RedirectCache                                redirectCache                      = new HttpSessionRedirectCache();
    private final AuthenticationManager                        authenticationManager;
    private final RequestMatcher                               authorizationEndpointMatcher;
    private final RedirectStrategy                             redirectStrategy                   = new DefaultRedirectStrategy();
    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource        = new WebAuthenticationDetailsSource();
    private AuthenticationConverter                            authenticationConverter;
    private AuthenticationSuccessHandler                       authenticationSuccessHandler       = this::sendAuthorizationResponse;
    private AuthenticationFailureHandler                       authenticationFailureHandler       = this::sendErrorResponse;
    private String                                             consentPage;

    /**
     * Constructs an {@code EiamOAuth2AuthorizationImplicitEndpointFilter} using the provided parameters.
     *
     * @param authenticationManager the authentication manager
     */
    public EiamOAuth2AuthorizationImplicitAuthenticationEndpointFilter(AuthenticationManager authenticationManager) {
        this(authenticationManager, DEFAULT_AUTHORIZATION_ENDPOINT_URI);
    }

    /**
     * Constructs an {@code EiamOAuth2AuthorizationImplicitEndpointFilter} using the provided parameters.
     *
     * @param authenticationManager the authentication manager
     * @param authorizationEndpointUri the endpoint {@code URI} for authorization requests
     */
    public EiamOAuth2AuthorizationImplicitAuthenticationEndpointFilter(AuthenticationManager authenticationManager,
                                                                       String authorizationEndpointUri) {
        Assert.notNull(authenticationManager, "authenticationManager cannot be null");
        Assert.hasText(authorizationEndpointUri, "authorizationEndpointUri cannot be empty");
        this.authenticationManager = authenticationManager;
        this.authorizationEndpointMatcher = createDefaultRequestMatcher(authorizationEndpointUri);
        this.authenticationConverter = new EiamOAuth2AuthenticationImplicitAuthenticationConverter();
    }

    private static RequestMatcher createDefaultRequestMatcher(String authorizationEndpointUri) {
        RequestMatcher authorizationRequestGetMatcher = new AntPathRequestMatcher(
            authorizationEndpointUri, HttpMethod.GET.name());
        RequestMatcher authorizationRequestPostMatcher = new AntPathRequestMatcher(
            authorizationEndpointUri, HttpMethod.POST.name());
        RequestMatcher openidScopeMatcher = request -> {
            String scope = request.getParameter(OAuth2ParameterNames.SCOPE);
            return StringUtils.hasText(scope) && scope.contains(OidcScopes.OPENID);
        };
        RequestMatcher responseTypeParameterMatcher = request -> request
            .getParameter(OAuth2ParameterNames.RESPONSE_TYPE) != null;

        RequestMatcher authorizationRequestMatcher = new OrRequestMatcher(
            authorizationRequestGetMatcher, new AndRequestMatcher(authorizationRequestPostMatcher,
                responseTypeParameterMatcher, openidScopeMatcher));
        RequestMatcher authorizationConsentMatcher = new AndRequestMatcher(
            authorizationRequestPostMatcher,
            new NegatedRequestMatcher(responseTypeParameterMatcher));

        return new OrRequestMatcher(authorizationRequestMatcher, authorizationConsentMatcher);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException,
                                                                      IOException {

        if (!this.authorizationEndpointMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            Authentication requestAuthentication = this.authenticationConverter.convert(request);
            if (Objects.isNull(requestAuthentication)) {
                filterChain.doFilter(request, response);
                return;
            }
            EiamOAuth2AuthorizationImplicitAuthenticationToken authorizationImplicitRequestAuthenticationToken = (EiamOAuth2AuthorizationImplicitAuthenticationToken) requestAuthentication;
            authorizationImplicitRequestAuthenticationToken
                .setDetails(this.authenticationDetailsSource.buildDetails(request));
            //进行认证处理
            Authentication authenticationResult = this.authenticationManager
                .authenticate(requestAuthentication);
            if (!authenticationResult.isAuthenticated()) {
                //Saved Redirect
                if (!CollectionUtils.isEmpty(request.getParameterMap())) {
                    redirectCache.saveRedirect(request, response,
                        RedirectCache.RedirectType.REQUEST);
                }
                //跳转登录
                response.sendRedirect(ServerContextHelp.getPortalPublicBaseUrl() + FE_LOGIN);
                return;
            }
            //颁发认证证书
            if (authenticationResult instanceof OAuth2AccessTokenAuthenticationToken) {
                OAuth2AccessTokenAuthenticationToken accessTokenAuthenticationToken = (OAuth2AccessTokenAuthenticationToken) authenticationResult;
                //认证成功
                this.authenticationSuccessHandler.onAuthenticationSuccess(request, response,
                    accessTokenAuthenticationToken);
            }
            //发送授权同意
            if (authenticationResult instanceof EiamOAuth2AuthorizationImplicitConsentAuthenticationToken) {
                EiamOAuth2AuthorizationImplicitConsentAuthenticationToken consentAuthenticationToken = (EiamOAuth2AuthorizationImplicitConsentAuthenticationToken) authenticationResult;
                sendAuthorizationImplicitConsent(request, response,
                    authorizationImplicitRequestAuthenticationToken, consentAuthenticationToken);
                return;
            }
        } catch (OAuth2AuthenticationException ex) {
            if (this.logger.isTraceEnabled()) {
                this.logger.trace(
                    LogMessage.format("Authorization request failed: %s", ex.getError()), ex);
            }
            this.authenticationFailureHandler.onAuthenticationFailure(request, response, ex);
        }
    }

    /**
     * 发送隐式授权同意
     *
     * @param request {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     * @param authorizationImplicitRequestAuthentication {@link EiamOAuth2AuthorizationImplicitAuthenticationToken}
     * @param authorizationImplicitRequestAuthenticationResult {@link EiamOAuth2AuthorizationImplicitAuthenticationToken}
     * @throws IOException IOException
     */
    private void sendAuthorizationImplicitConsent(HttpServletRequest request,
                                                  HttpServletResponse response,
                                                  EiamOAuth2AuthorizationImplicitAuthenticationToken authorizationImplicitRequestAuthentication,
                                                  EiamOAuth2AuthorizationImplicitConsentAuthenticationToken authorizationImplicitConsentAuthenticationToken) throws IOException {

        String clientId = authorizationImplicitConsentAuthenticationToken.getClientId();
        Authentication principal = (Authentication) authorizationImplicitConsentAuthenticationToken
            .getPrincipal();
        Set<String> requestedScopes = authorizationImplicitRequestAuthentication.getScopes();
        Set<String> authorizedScopes = authorizationImplicitConsentAuthenticationToken.getScopes();
        String state = authorizationImplicitConsentAuthenticationToken.getState();

        if (hasConsentUri()) {
            String redirectUri = UriComponentsBuilder.fromUriString(resolveConsentUri(request))
                .queryParam(OAuth2ParameterNames.SCOPE, String.join(" ", requestedScopes))
                .queryParam(OAuth2ParameterNames.CLIENT_ID, clientId)
                .queryParam(OAuth2ParameterNames.STATE, state).toUriString();
            this.redirectStrategy.sendRedirect(request, response, redirectUri);
        } else {
            if (this.logger.isTraceEnabled()) {
                this.logger.trace("Displaying generated consent screen");
            }
            DefaultConsentPage.displayConsent(request, response, clientId, principal,
                requestedScopes, authorizedScopes, state);
        }
    }

    private boolean hasConsentUri() {
        return StringUtils.hasText(this.consentPage);
    }

    private String resolveConsentUri(HttpServletRequest request) {
        if (UrlUtils.isAbsoluteUrl(this.consentPage)) {
            return this.consentPage;
        }
        RedirectUrlBuilder urlBuilder = new RedirectUrlBuilder();
        urlBuilder.setScheme(request.getScheme());
        urlBuilder.setServerName(request.getServerName());
        urlBuilder.setPort(request.getServerPort());
        urlBuilder.setContextPath(request.getContextPath());
        urlBuilder.setPathInfo(this.consentPage);
        return urlBuilder.getUrl();
    }

    /**
     * 发送成功响应
     *
     * @param request
     * @param response
     * @param authentication
     * @throws IOException
     */
    private void sendAuthorizationResponse(HttpServletRequest request, HttpServletResponse response,
                                           Authentication authentication) throws IOException {
        Map<String, Object> vars = new LinkedHashMap<String, Object>();
        Map<String, String> keys = new HashMap<String, String>();
        OAuth2AccessTokenAuthenticationToken accessTokenAuthenticationToken = (OAuth2AccessTokenAuthenticationToken) authentication;
        OAuth2AccessToken accessToken = accessTokenAuthenticationToken.getAccessToken();
        vars.put(TOKEN_TYPE, accessToken.getTokenType().getValue());
        vars.put(SCOPE, accessToken.getScopes());
        vars.put(ACCESS_TOKEN, accessToken.getTokenValue());
        if (accessToken.getIssuedAt() != null && accessToken.getExpiresAt() != null) {
            vars.put(EXPIRES_IN,
                ChronoUnit.SECONDS.between(accessToken.getIssuedAt(), accessToken.getExpiresAt()));
        }
        //state
        String state = (String) accessTokenAuthenticationToken.getAdditionalParameters().get(STATE);
        String redirectUri = (String) accessTokenAuthenticationToken.getAdditionalParameters()
            .get(REDIRECT_URI);
        if (StringUtils.hasText(state)) {
            vars.put(OAuth2ParameterNames.STATE, state);
        }
        String append = appendUrl(redirectUri, vars, keys, true);
        this.redirectStrategy.sendRedirect(request, response, append);
    }

    /**
     * 发送失败响应
     *
     * @param request
     * @param response
     * @param exception
     * @throws IOException
     */
    private void sendErrorResponse(HttpServletRequest request, HttpServletResponse response,
                                   AuthenticationException exception) throws IOException {
        if (exception instanceof OAuth2AuthenticationException) {
            OAuth2AuthenticationException authenticationException = (OAuth2AuthenticationException) exception;
            OAuth2Error error = authenticationException.getError();
            response.sendError(HttpStatus.BAD_REQUEST.value(), error.toString());
            return;
        }
        EiamOAuth2AuthorizationImplicitAuthenticationException authorizationCodeRequestAuthenticationException = (EiamOAuth2AuthorizationImplicitAuthenticationException) exception;
        OAuth2Error error = authorizationCodeRequestAuthenticationException.getError();
        EiamOAuth2AuthorizationImplicitAuthenticationToken authorizationImplicitRequestAuthenticationToken = authorizationCodeRequestAuthenticationException
            .getAuthorizationCodeRequestAuthentication();

        if (authorizationImplicitRequestAuthenticationToken == null || !StringUtils
            .hasText(authorizationImplicitRequestAuthenticationToken.getRedirectUri())) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), error.toString());
            return;
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Redirecting to client with error");
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
            .fromUriString(authorizationImplicitRequestAuthenticationToken.getRedirectUri())
            .queryParam(OAuth2ParameterNames.ERROR, error.getErrorCode());
        if (StringUtils.hasText(error.getDescription())) {
            uriBuilder.queryParam(OAuth2ParameterNames.ERROR_DESCRIPTION, error.getDescription());
        }
        if (StringUtils.hasText(error.getUri())) {
            uriBuilder.queryParam(OAuth2ParameterNames.ERROR_URI, error.getUri());
        }
        if (StringUtils.hasText(authorizationImplicitRequestAuthenticationToken.getState())) {
            uriBuilder.queryParam(OAuth2ParameterNames.STATE,
                authorizationImplicitRequestAuthenticationToken.getState());
        }
        this.redirectStrategy.sendRedirect(request, response, uriBuilder.toUriString());
    }

    /**
     * Sets the {@link AuthenticationConverter} used when attempting to extract an Authorization Request (or Consent) from {@link HttpServletRequest}
     * to an instance of {@link OAuth2AuthorizationCodeRequestAuthenticationToken} used for authenticating the request.
     *
     * @param authenticationConverter the {@link AuthenticationConverter} used when attempting to extract an Authorization Request (or Consent) from {@link HttpServletRequest}
     */
    public void setAuthenticationConverter(AuthenticationConverter authenticationConverter) {
        Assert.notNull(authenticationConverter, "authenticationConverter cannot be null");
        this.authenticationConverter = authenticationConverter;
    }

    /**
     * Sets the {@link AuthenticationSuccessHandler} used for handling an {@link OAuth2AuthorizationCodeRequestAuthenticationToken}
     * and returning the {@link OAuth2AuthorizationResponse Authorization Response}.
     *
     * @param authenticationSuccessHandler the {@link AuthenticationSuccessHandler} used for handling an {@link OAuth2AuthorizationCodeRequestAuthenticationToken}
     */
    public void setAuthenticationSuccessHandler(AuthenticationSuccessHandler authenticationSuccessHandler) {
        Assert.notNull(authenticationSuccessHandler, "authenticationSuccessHandler cannot be null");
        this.authenticationSuccessHandler = authenticationSuccessHandler;
    }

    /**
     * Sets the {@link AuthenticationFailureHandler} used for handling an {@link OAuth2AuthorizationCodeRequestAuthenticationException}
     * and returning the {@link OAuth2Error Error Response}.
     *
     * @param authenticationFailureHandler the {@link AuthenticationFailureHandler} used for handling an {@link OAuth2AuthorizationCodeRequestAuthenticationException}
     */
    public void setAuthenticationFailureHandler(AuthenticationFailureHandler authenticationFailureHandler) {
        Assert.notNull(authenticationFailureHandler, "authenticationFailureHandler cannot be null");
        this.authenticationFailureHandler = authenticationFailureHandler;
    }

    /**
     * Specify the URI to redirect Resource Owners to if consent is required. A default consent
     * page will be generated when this attribute is not specified.
     *
     * @param consentPage the URI of the custom consent page to redirect to if consent is required (e.g. "/oauth2/consent")
     */
    public void setConsentPage(String consentPage) {
        this.consentPage = consentPage;
    }

    /**
     * For internal use only.
     */
    @SuppressWarnings("AlibabaMethodTooLong")
    private static class DefaultConsentPage {
        private static final MediaType TEXT_HTML_UTF8 = new MediaType("text", "html",
            StandardCharsets.UTF_8);

        private static void displayConsent(HttpServletRequest request, HttpServletResponse response,
                                           String clientId, Authentication principal,
                                           Set<String> requestedScopes,
                                           Set<String> authorizedScopes,
                                           String state) throws IOException {

            String consentPage = generateConsentPage(request, clientId, principal, requestedScopes,
                authorizedScopes, state);
            response.setContentType(TEXT_HTML_UTF8.toString());
            response.setContentLength(consentPage.getBytes(StandardCharsets.UTF_8).length);
            response.getWriter().write(consentPage);
        }

        private static String generateConsentPage(HttpServletRequest request, String clientId,
                                                  Authentication principal,
                                                  Set<String> requestedScopes,
                                                  Set<String> authorizedScopes, String state) {
            Set<String> scopesToAuthorize = new HashSet<>();
            Set<String> scopesPreviouslyAuthorized = new HashSet<>();
            for (String scope : requestedScopes) {
                if (authorizedScopes.contains(scope)) {
                    scopesPreviouslyAuthorized.add(scope);
                } else if (!scope.equals(OidcScopes.OPENID)) { // openid scope does not require consent
                    scopesToAuthorize.add(scope);
                }
            }

            StringBuilder builder = new StringBuilder();

            builder.append("<!DOCTYPE html>");
            builder.append("<html lang=\"en\">");
            builder.append("<head>");
            builder.append("    <meta charset=\"utf-8\">");
            builder.append(
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">");
            builder.append(
                "    <link rel=\"stylesheet\" href=\"https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css\" integrity=\"sha384-JcKb8q3iqJ61gNV9KGb8thSsNjpSL0n8PARn9HuZOnIxN0hoP+VmmDGMN5t9UJ0Z\" crossorigin=\"anonymous\">");
            builder.append("    <title>Consent required</title>");
            builder.append("	<script>");
            builder.append("		function cancelConsent() {");
            builder.append("			document.consent_form.reset();");
            builder.append("			document.consent_form.submit();");
            builder.append("		}");
            builder.append("	</script>");
            builder.append("</head>");
            builder.append("<body>");
            builder.append("<div class=\"container\">");
            builder.append("    <div class=\"py-5\">");
            builder.append("        <h1 class=\"text-center\">Consent required</h1>");
            builder.append("    </div>");
            builder.append("    <div class=\"row\">");
            builder.append("        <div class=\"col text-center\">");
            builder
                .append("            <p><span class=\"font-weight-bold text-primary\">" + clientId
                        + "</span> wants to access your account <span class=\"font-weight-bold\">"
                        + principal.getName() + "</span></p>");
            builder.append("        </div>");
            builder.append("    </div>");
            builder.append("    <div class=\"row pb-3\">");
            builder.append("        <div class=\"col text-center\">");
            builder.append(
                "            <p>The following permissions are requested by the above app.<br/>Please review these and consent if you approve.</p>");
            builder.append("        </div>");
            builder.append("    </div>");
            builder.append("    <div class=\"row\">");
            builder.append("        <div class=\"col text-center\">");
            builder.append("            <form name=\"consent_form\" method=\"post\" action=\""
                           + request.getRequestURI() + "\">");
            builder.append("                <input type=\"hidden\" name=\"client_id\" value=\""
                           + clientId + "\">");
            builder.append(
                "                <input type=\"hidden\" name=\"state\" value=\"" + state + "\">");

            for (String scope : scopesToAuthorize) {
                builder.append("                <div class=\"form-group form-check py-1\">");
                builder.append(
                    "                    <input class=\"form-check-input\" type=\"checkbox\" name=\"scope\" value=\""
                               + scope + "\" id=\"" + scope + "\">");
                builder.append("                    <label class=\"form-check-label\" for=\""
                               + scope + "\">" + scope + "</label>");
                builder.append("                </div>");
            }

            if (!scopesPreviouslyAuthorized.isEmpty()) {
                builder.append(
                    "                <p>You have already granted the following permissions to the above app:</p>");
                for (String scope : scopesPreviouslyAuthorized) {
                    builder.append("                <div class=\"form-group form-check py-1\">");
                    builder.append(
                        "                    <input class=\"form-check-input\" type=\"checkbox\" name=\"scope\" id=\""
                                   + scope + "\" checked disabled>");
                    builder.append("                    <label class=\"form-check-label\" for=\""
                                   + scope + "\">" + scope + "</label>");
                    builder.append("                </div>");
                }
            }

            builder.append("                <div class=\"form-group pt-3\">");
            builder.append(
                "                    <button class=\"btn btn-primary btn-lg\" type=\"submit\" id=\"submit-consent\">Submit Consent</button>");
            builder.append("                </div>");
            builder.append("                <div class=\"form-group\">");
            builder.append(
                "                    <button class=\"btn btn-link regular\" type=\"button\" onclick=\"cancelConsent();\" id=\"cancel-consent\">Cancel</button>");
            builder.append("                </div>");
            builder.append("            </form>");
            builder.append("        </div>");
            builder.append("    </div>");
            builder.append("    <div class=\"row pt-4\">");
            builder.append("        <div class=\"col text-center\">");
            builder.append(
                "            <p><small>Your consent to provide access is required.<br/>If you do not approve, click Cancel, in which case no information will be shared with the app.</small></p>");
            builder.append("        </div>");
            builder.append("    </div>");
            builder.append("</div>");
            builder.append("</body>");
            builder.append("</html>");

            return builder.toString();
        }
    }
}
