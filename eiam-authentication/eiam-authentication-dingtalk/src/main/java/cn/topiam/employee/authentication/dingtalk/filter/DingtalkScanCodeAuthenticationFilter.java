/*
 * eiam-authentication-dingtalk - Employee Identity and Access Management Program
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
package cn.topiam.employee.authentication.dingtalk.filter;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.alibaba.fastjson2.JSONObject;
import com.aliyun.dingtalkoauth2_1_0.Client;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenRequest;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenResponseBody;
import com.aliyun.teaopenapi.models.Config;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiSnsGetuserinfoBycodeRequest;
import com.dingtalk.api.request.OapiUserGetUseridByUnionidRequest;
import com.dingtalk.api.request.OapiV2UserGetRequest;
import com.dingtalk.api.response.OapiSnsGetuserinfoBycodeResponse;
import com.dingtalk.api.response.OapiUserGetUseridByUnionidResponse;
import com.dingtalk.api.response.OapiV2UserGetResponse;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.taobao.api.ApiException;

import cn.topiam.employee.authentication.common.filter.AbstractIdpAuthenticationProcessingFilter;
import cn.topiam.employee.authentication.common.modal.IdpUser;
import cn.topiam.employee.authentication.common.service.UserIdpService;
import cn.topiam.employee.authentication.dingtalk.DingTalkIdpScanCodeConfig;
import cn.topiam.employee.common.entity.authentication.IdentityProviderEntity;
import cn.topiam.employee.common.enums.IdentityProviderType;
import cn.topiam.employee.common.repository.authentication.IdentityProviderRepository;
import cn.topiam.employee.core.context.ServerContextHelp;
import cn.topiam.employee.support.exception.TopIamException;
import cn.topiam.employee.support.trace.TraceUtils;
import cn.topiam.employee.support.util.HttpUrlUtils;
import static cn.topiam.employee.authentication.dingtalk.constant.DingTalkAuthenticationConstants.*;
import static cn.topiam.employee.authentication.dingtalk.filter.DingtalkScanCodeAuthorizationRequestGetFilter.PROVIDER_ID;
import static cn.topiam.employee.common.enums.IdentityProviderType.DINGTALK_SCAN_CODE;

/**
 * 钉钉认证过滤器
 * <p>
 * https://open.dingtalk.com/document/orgapp-server/scan-qr-code-to-log-on-to-third-party-websites
 *
 * @author TopIAM
 * Created by support@topiam.cn on  2021/12/8 21:11
 */
@SuppressWarnings("DuplicatedCode")
public class DingtalkScanCodeAuthenticationFilter extends
                                                  AbstractIdpAuthenticationProcessingFilter {
    public final static String                DEFAULT_FILTER_PROCESSES_URI = DINGTALK_SCAN_CODE
        .getLoginPathPrefix() + "/*";
    /**
     * AntPathRequestMatcher
     */
    public static final AntPathRequestMatcher REQUEST_MATCHER              = new AntPathRequestMatcher(
        DINGTALK_SCAN_CODE.getLoginPathPrefix() + "/" + "{" + PROVIDER_ID + "}",
        HttpMethod.GET.name());

    /**
     * Creates a new instance
     *
     * @param identityProviderRepository the {@link IdentityProviderRepository}
     * @param authenticationUserDetails  {@link  UserIdpService}
     */
    public DingtalkScanCodeAuthenticationFilter(IdentityProviderRepository identityProviderRepository,
                                                UserIdpService authenticationUserDetails) {
        super(DEFAULT_FILTER_PROCESSES_URI, authenticationUserDetails, identityProviderRepository);
    }

    /**
     * 钉钉认证
     *
     * @param request  {@link  HttpServletRequest}
     * @param response {@link  HttpServletRequest}
     * @return {@link  HttpServletRequest}
     * @throws AuthenticationException {@link  AuthenticationException} AuthenticationException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException,
                                                                              IOException {
        OAuth2AuthorizationRequest authorizationRequest = getOAuth2AuthorizationRequest(request,
            response);
        TraceUtils.put(UUID.randomUUID().toString());
        RequestMatcher.MatchResult matcher = REQUEST_MATCHER.matcher(request);
        Map<String, String> variables = matcher.getVariables();
        String providerId = variables.get(PROVIDER_ID);
        //code
        String code = request.getParameter(OAuth2ParameterNames.CODE);
        if (StringUtils.isEmpty(code)) {
            OAuth2Error oauth2Error = new OAuth2Error(INVALID_CODE_PARAMETER_ERROR_CODE);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
        }
        // state
        String state = request.getParameter(OAuth2ParameterNames.STATE);
        if (StringUtils.isEmpty(state)) {
            OAuth2Error oauth2Error = new OAuth2Error(INVALID_STATE_PARAMETER_ERROR_CODE);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
        }
        if (!authorizationRequest.getState().equals(state)) {
            OAuth2Error oauth2Error = new OAuth2Error(INVALID_STATE_PARAMETER_ERROR_CODE);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
        }
        //获取身份提供商
        IdentityProviderEntity provider = getIdentityProviderEntity(providerId);
        DingTalkIdpScanCodeConfig config = JSONObject.parseObject(provider.getConfig(),
            DingTalkIdpScanCodeConfig.class);
        if (Objects.isNull(config)) {
            logger.error("未查询到钉钉扫码登录配置");
            //无效身份提供商
            OAuth2Error oauth2Error = new OAuth2Error(INVALID_IDP_CONFIG);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
        }
        String accessToken = getToken(config);
        // 1、通过临时授权码获取授权用户的个人信息
        DefaultDingTalkClient client = new DefaultDingTalkClient(GET_USERINFO_BY_CODE);
        OapiSnsGetuserinfoBycodeRequest reqByCodeRequest = new OapiSnsGetuserinfoBycodeRequest();
        // 通过扫描二维码，跳转指定的redirect_uri后，向url中追加的code临时授权码
        reqByCodeRequest.setTmpAuthCode(code);
        // 修改appid和appSecret为步骤三创建扫码登录时创建的appid和appSecret
        OapiSnsGetuserinfoBycodeResponse userinfoByCodeResp;
        try {
            userinfoByCodeResp = client.execute(reqByCodeRequest, config.getAppKey(),
                config.getAppSecret());
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        if (!userinfoByCodeResp.isSuccess()) {
            logger.error("钉钉扫码认证获取用户信息失败: [" + userinfoByCodeResp.getBody() + "]");
            throw new TopIamException(userinfoByCodeResp.getErrmsg());
        }
        // 2、根据unionid获取userid
        client = new DefaultDingTalkClient(GET_USERID_BY_UNIONID);
        OapiUserGetUseridByUnionidRequest req = new OapiUserGetUseridByUnionidRequest();
        req.setUnionid(userinfoByCodeResp.getUserInfo().getUnionid());
        OapiUserGetUseridByUnionidResponse rsp;
        try {
            rsp = client.execute(req, accessToken);
        } catch (ApiException e) {
            logger.error("钉钉扫码认证获取用户ID失败: [" + e.getErrMsg() + "]");
            throw new TopIamException("钉钉扫码认证获取用户ID失败", e);
        }
        if (!rsp.isSuccess()) {
            logger.error("钉钉扫码认证获取用户ID失败: [" + rsp.getBody() + "]");
            throw new TopIamException(rsp.getErrmsg());
        }
        // 3、根据userId获取用户信息
        DingTalkClient v2UserGetResponse = new DefaultDingTalkClient(GET_USERINFO_BY_USERID);
        OapiV2UserGetRequest reqGetRequest = new OapiV2UserGetRequest();
        reqGetRequest.setUserid(rsp.getUserid());
        OapiV2UserGetResponse rspGetResponse;
        try {
            rspGetResponse = v2UserGetResponse.execute(reqGetRequest, accessToken);
        } catch (ApiException e) {
            logger.error("钉钉扫码认证获取用户信息失败: [" + e.getErrMsg() + "]");
            throw new TopIamException("钉钉扫码认证获取用户信息失败", e);
        }
        if (!rspGetResponse.isSuccess()) {
            logger.error("钉钉扫码认证获取用户信息失败: [" + rspGetResponse.getBody() + "]");
            throw new TopIamException(rspGetResponse.getErrmsg());
        }
        //4、执行逻辑
        OapiV2UserGetResponse.UserGetResponse result = rspGetResponse.getResult();
        IdpUser idpUser = IdpUser.builder().openId(result.getUserid()).build();
        return attemptAuthentication(request, response, IdentityProviderType.DINGTALK_SCAN_CODE,
            providerId, idpUser);
    }

    /**
     * 获取token
     *
     * @return {@link String}
     */
    public String getToken(DingTalkIdpScanCodeConfig config) {
        if (!Objects.isNull(cache)) {
            cache.getIfPresent(OAuth2ParameterNames.ACCESS_TOKEN);
        }
        Config clientConfig = new Config();
        clientConfig.protocol = "https";
        clientConfig.regionId = "central";
        try {
            Client client = new Client(clientConfig);
            GetAccessTokenRequest accessTokenRequest = new GetAccessTokenRequest();
            accessTokenRequest.setAppKey(config.getAppKey());
            accessTokenRequest.setAppSecret(config.getAppSecret());
            GetAccessTokenResponseBody body = client.getAccessToken(accessTokenRequest).getBody();
            //放入缓存
            cache = CacheBuilder.newBuilder()
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .expireAfterWrite(body.getExpireIn(), TimeUnit.SECONDS).build();
            cache.put(OAuth2ParameterNames.ACCESS_TOKEN, body.getAccessToken());
            return cache.getIfPresent(OAuth2ParameterNames.ACCESS_TOKEN);
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    /**
     * 缓存
     */
    private Cache<String, String> cache;

    public static String getLoginUrl(String providerId) {
        String url = ServerContextHelp.getPortalPublicBaseUrl()
                     + DINGTALK_SCAN_CODE.getLoginPathPrefix() + "/" + providerId;
        return HttpUrlUtils.format(url);
    }

    public static RequestMatcher getRequestMatcher() {
        return REQUEST_MATCHER;
    }
}
