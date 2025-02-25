/*
 * eiam-console - Employee Identity and Access Management Program
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
package cn.topiam.employee.console.service.account.impl;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import cn.topiam.employee.audit.context.AuditContext;
import cn.topiam.employee.audit.entity.AuditElasticSearchEntity;
import cn.topiam.employee.audit.entity.Target;
import cn.topiam.employee.audit.enums.TargetType;
import cn.topiam.employee.common.entity.account.*;
import cn.topiam.employee.common.entity.account.po.UserPO;
import cn.topiam.employee.common.entity.account.query.UserListNotInGroupQuery;
import cn.topiam.employee.common.entity.account.query.UserListQuery;
import cn.topiam.employee.common.enums.*;
import cn.topiam.employee.common.repository.account.*;
import cn.topiam.employee.console.converter.account.UserConverter;
import cn.topiam.employee.console.pojo.result.account.UserListResult;
import cn.topiam.employee.console.pojo.result.account.UserLoginAuditListResult;
import cn.topiam.employee.console.pojo.result.account.UserResult;
import cn.topiam.employee.console.pojo.save.account.UserCreateParam;
import cn.topiam.employee.console.pojo.update.account.ResetPasswordParam;
import cn.topiam.employee.console.pojo.update.account.UserUpdateParam;
import cn.topiam.employee.console.service.account.UserService;
import cn.topiam.employee.core.configuration.EiamSupportProperties;
import cn.topiam.employee.core.message.MsgVariable;
import cn.topiam.employee.core.message.mail.MailMsgEventPublish;
import cn.topiam.employee.core.message.sms.SmsMsgEventPublish;
import cn.topiam.employee.support.exception.BadParamsException;
import cn.topiam.employee.support.exception.InfoValidityFailException;
import cn.topiam.employee.support.exception.TopIamException;
import cn.topiam.employee.support.repository.page.domain.Page;
import cn.topiam.employee.support.repository.page.domain.PageModel;
import cn.topiam.employee.support.util.BeanUtils;
import cn.topiam.employee.support.validation.annotation.ValidationPhone;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static cn.topiam.employee.audit.enums.TargetType.*;
import static cn.topiam.employee.common.constants.AuditConstants.getAuditIndexPrefix;
import static cn.topiam.employee.core.message.sms.SmsMsgEventPublish.PASSWORD;
import static cn.topiam.employee.core.message.sms.SmsMsgEventPublish.USERNAME;
import static cn.topiam.employee.support.repository.domain.BaseEntity.LAST_MODIFIED_BY;
import static cn.topiam.employee.support.repository.domain.BaseEntity.LAST_MODIFIED_TIME;
import static cn.topiam.employee.support.util.PhoneUtils.getPhoneNumber;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author TopIAM
 * Created by support@topiam.cn on  2020-07-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    /**
     * 获取用户（分页）
     *
     * @param pageModel {@link PageModel}
     * @return {@link UserListQuery}
     */
    @Override
    public Page<UserListResult> getUserList(PageModel pageModel, UserListQuery query) {
        org.springframework.data.domain.Page<UserPO> list = userRepository.getUserList(query,
            PageRequest.of(pageModel.getCurrent(), pageModel.getPageSize()));
        return userConverter.userPoConvertToUserListResult(list);
    }

    /**
     * 获取用户列表不在当前组
     *
     * @param model {@link  PageModel}
     * @param query {@link UserListNotInGroupQuery }
     * @return {@link  }
     */
    @Override
    public Page<UserListResult> getUserListNotInGroup(PageModel model,
                                                      UserListNotInGroupQuery query) {
        org.springframework.data.domain.Page<UserPO> list = userRepository.getUserListNotInGroupId(
            query, PageRequest.of(model.getCurrent(), model.getPageSize()));
        return userConverter.userPoConvertToUserListResult(list);
    }

    /**
     * 重置密码
     *
     * @param param       {@link ResetPasswordParam} 用户修改入参
     * @return {@link Boolean}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean resetUserPassword(ResetPasswordParam param) {
        //additionalContent
        Optional<UserEntity> optional = userRepository.findById(Long.valueOf(param.getId()));
        //用户不存在
        if (optional.isEmpty()) {
            AuditContext.setContent("操作失败，用户不存在");
            log.warn(AuditContext.getContent());
            throw new TopIamException(AuditContext.getContent());
        }
        UserEntity userEntity = optional.get();
        // 重置用户密码
        String password = new String(
            Base64.getUrlDecoder().decode(param.getPassword().getBytes(StandardCharsets.UTF_8)),
            StandardCharsets.UTF_8);
        password = passwordEncoder.encode(password);
        userRepository.updateUserPassword(Long.valueOf(param.getId()), password,
            LocalDateTime.now());
        //保存历史密码
        UserHistoryPasswordEntity userHistoryPassword = new UserHistoryPasswordEntity();
        userHistoryPassword.setUserId(String.valueOf(param.getId()));
        userHistoryPassword.setPassword(password);
        userHistoryPassword.setChangeTime(LocalDateTime.now());
        userHistoryPasswordRepository.save(userHistoryPassword);
        AuditContext.setTarget(Target.builder().id(param.getId()).type(TargetType.USER).build());

        ResetPasswordParam.PasswordResetConfig passwordResetConfig = param.getPasswordResetConfig();
        if (Objects.nonNull(passwordResetConfig) && passwordResetConfig.getEnableNotice()
            && org.apache.commons.collections4.CollectionUtils
                .isNotEmpty(passwordResetConfig.getNoticeChannels())) {
            // 重置密码成功通知
            if (passwordResetConfig.getNoticeChannels().contains(MessageNoticeChannel.MAIL)) {
                if (StringUtils.isNotEmpty(userEntity.getEmail())) {
                    log.warn("不存在用户邮箱信息，未发送用户重置密码邮件。");
                } else {
                    Map<String, Object> parameter = new HashMap<>(16);
                    parameter.put(PASSWORD, password);
                    mailMsgEventPublish.publish(MailType.RESET_PASSWORD_CONFIRM,
                        userEntity.getEmail(), parameter);
                }
            }
            if (passwordResetConfig.getNoticeChannels().contains(MessageNoticeChannel.SMS)) {
                LinkedHashMap<String, String> parameter = new LinkedHashMap<>();
                parameter.put(USERNAME, userEntity.getUsername());
                parameter.put(PASSWORD, password);
                smsMsgEventPublish.publish(SmsType.RESET_PASSWORD_SUCCESS, userEntity.getPhone(),
                    parameter);
            }
        }

        return true;
    }

    /**
     * 更改用户状态
     *
     * @param id     {@link Long}
     * @param status {@link UserStatus}
     * @return {@link Boolean}
     */
    @Override
    public boolean changeUserStatus(Long id, UserStatus status) {
        Optional<UserEntity> optional = userRepository.findById(id);
        //用户不存在
        if (optional.isEmpty()) {
            AuditContext.setContent("操作失败，用户不存在");
            log.warn(AuditContext.getContent());
            throw new TopIamException(AuditContext.getContent());
        }
        AuditContext.setTarget(Target.builder().id(id.toString()).type(TargetType.USER).build());
        return userRepository.updateUserStatus(id, status) > 0;
    }

    /**
     * 创建用户
     *
     * @param param {@link UserCreateParam}
     * @return {@link Boolean}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createUser(UserCreateParam param) {
        if (!ObjectUtils.isEmpty(param.getPhone()) && StringUtils.isNotEmpty(param.getPhone())) {
            if (!getPhoneNumber(param.getPhone()).matches(ValidationPhone.PHONE_REGEXP)) {
                throw new InfoValidityFailException("手机号格式错误");
            }
            Boolean validityPhone = userParamCheck(CheckValidityType.PHONE, param.getPhone(), null);
            if (!validityPhone) {
                throw new InfoValidityFailException("手机号已存在");
            }
        }
        Boolean validityEmail = userParamCheck(CheckValidityType.EMAIL, param.getEmail(), null);
        if (!validityEmail) {
            throw new InfoValidityFailException("邮箱已存在");
        }
        Boolean validityUsername = userParamCheck(CheckValidityType.USERNAME, param.getUsername(),
            null);
        if (!validityUsername) {
            throw new InfoValidityFailException("用户名已存在");
        }
        //用户信息
        UserEntity user = userConverter.userCreateParamConvertToUserEntity(param);
        userRepository.save(user);
        //用户详情
        UserDetailEntity detail = userConverter.userCreateParamConvertToUserDetailEntity(param);
        detail.setUserId(user.getId());
        userDetailsRepository.save(detail);
        //用户组织关联关系
        OrganizationMemberEntity member = new OrganizationMemberEntity(param.getOrganizationId(),
            user.getId());
        organizationMemberRepository.save(member);
        AuditContext.setTarget(Target.builder().type(USER).id(user.getId().toString()).build(),
            Target.builder().type(USER_DETAIL).id(detail.getId().toString()).build());

        // 发送短信和邮件的欢迎信息（密码通知）
        UserCreateParam.PasswordInitializeConfig passwordInitializeConfig = param
            .getPasswordInitializeConfig();
        if (Objects.nonNull(passwordInitializeConfig) && passwordInitializeConfig.getEnableNotice()
            && org.apache.commons.collections4.CollectionUtils
                .isNotEmpty(passwordInitializeConfig.getNoticeChannels())) {
            List<MessageNoticeChannel> channels = passwordInitializeConfig.getNoticeChannels();
            if (channels.contains(MessageNoticeChannel.MAIL)) {
                Map<String, Object> parameter = new HashMap<>(16);
                parameter.put(MsgVariable.CLIENT_DESCRIPTION,
                    "您的 TopIAM 账户的初始密码是【" + param.getPassword() + "】。");
                mailMsgEventPublish.publish(MailType.WELCOME_MAIL, user.getEmail(), parameter);
            }
            if (channels.contains(MessageNoticeChannel.SMS)) {
                LinkedHashMap<String, String> parameter = new LinkedHashMap<>();
                parameter.put(USERNAME, user.getUsername());
                parameter.put(PASSWORD, user.getPassword());
                smsMsgEventPublish.publish(SmsType.WELCOME_SMS, user.getPhone(), parameter);
            }
        }
        return true;
    }

    /**
     * 通过外部ID获取用户
     *
     * @param id {@link String}
     * @return {@link UserEntity}
     */
    @Override
    public UserEntity getByExternalId(String id) {
        return userRepository.findByExternalId(id).orElse(null);
    }

    /**
     * 根据ID查询用户
     *
     * @param id {@link String}
     * @return {@link UserResult}
     */
    @Override
    public UserResult getUser(String id) {
        //查询
        Optional<UserEntity> user = userRepository.findById(Long.valueOf(id));
        Optional<UserDetailEntity> detail = userDetailsRepository.findByUserId(Long.valueOf(id));
        //映射
        UserEntity userEntity = user.orElse(null);
        UserResult userResult = userConverter.entityConvertToUserResult(userEntity,
            detail.orElse(null));
        if (Objects.nonNull(userEntity) && StringUtils.isNotEmpty(userEntity.getPhone())) {
            userResult.setPhone(userEntity.getPhoneAreaCode() + userEntity.getPhone());
        }
        return userResult;
    }

    /**
     * 更新用户
     *
     * @param param {@link UserUpdateParam}
     * @return {@link Boolean}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(UserUpdateParam param) {
        if (StringUtils.isNotBlank(param.getPhone())) {
            String phoneNumber = getPhoneNumber(param.getPhone());
            if (!phoneNumber.matches(ValidationPhone.PHONE_REGEXP)) {
                throw new InfoValidityFailException("手机号格式错误");
            }
        }
        //用户信息
        UserEntity toUserEntity = userConverter.userUpdateParamConvertToUserEntity(param);
        UserEntity user = getUser(Long.valueOf(param.getId()));
        BeanUtils.merge(toUserEntity, user, LAST_MODIFIED_BY, LAST_MODIFIED_TIME);
        //如果更改密码到期时间，修改为启用
        if (user.getStatus().equals(UserStatus.EXPIRED_LOCKED)) {
            if (toUserEntity.getExpireDate().isAfter(LocalDate.now())) {
                user.setStatus(UserStatus.ENABLE);
            }
        }
        userRepository.save(user);
        //用户详情
        UserDetailEntity detail = userDetailsRepository.findByUserId(Long.valueOf(param.getId()))
            .orElse(new UserDetailEntity());
        UserDetailEntity toUserDetailsEntity = userConverter
            .userUpdateParamConvertToUserDetailsEntity(param);
        toUserDetailsEntity.setId(detail.getId());
        BeanUtils.merge(toUserDetailsEntity, detail, LAST_MODIFIED_BY, LAST_MODIFIED_TIME);
        userDetailsRepository.save(detail);
        AuditContext.setTarget(Target.builder().type(USER).id(user.getId().toString()).build(),
            Target.builder().type(USER_DETAIL).id(detail.getId().toString()).build());
        return true;
    }

    /**
     * 删除用户
     *
     * @param id {@link Serializable}
     * @return {@link Boolean}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(String id) {
        Optional<UserEntity> optional = userRepository.findById(Long.valueOf(id));
        //管理员不存在
        if (optional.isEmpty()) {
            AuditContext.setContent("删除失败，用户不存在");
            log.warn(AuditContext.getContent());
            throw new TopIamException(AuditContext.getContent());
        }
        //删除
        userRepository.deleteById(Long.valueOf(id));
        //删除用户详情
        userDetailsRepository.deleteByUserId(Long.valueOf(id));
        //删除组织用户关联关系
        organizationMemberRepository.deleteByUserId(Long.valueOf(id));
        //删除用户组用户详情
        userGroupMemberRepository.deleteByUserId(Long.valueOf(id));
        AuditContext.setTarget(Target.builder().id(id).type(TargetType.USER).build());
        return true;
    }

    /**
     * 用户转岗
     *
     * @param userId {@link String}
     * @param orgId  {@link String}
     * @return {@link  Boolean}
     */
    @Override
    public Boolean userTransfer(String userId, String orgId) {
        Optional<OrganizationEntity> entity = organizationRepository.findById(orgId);
        //additionalContent
        if (entity.isEmpty()) {
            AuditContext.setContent("操作失败，组织不存在");
            log.warn(AuditContext.getContent());
            throw new TopIamException(AuditContext.getContent());
        }
        organizationMemberRepository.deleteByOrgIdAndUserId(orgId, Long.valueOf(userId));
        userRepository.save(null);
        AuditContext.setTarget(Target.builder().id(userId).type(TargetType.USER).build());
        return true;
    }

    /**
     * 批量删除用户
     *
     * @param ids {@link String}
     * @return {@link Boolean}
     */
    @Override
    public Boolean batchDeleteUser(String[] ids) {
        //删除用户
        userRepository
            .deleteAllById(Arrays.stream(ids).map(s -> Long.parseLong(s.trim())).toList());
        //删除用户详情
        userDetailsRepository
            .deleteAllByUserId(Arrays.stream(ids).map(s -> Long.parseLong(s.trim())).toList());
        //删除组织用户关系
        organizationMemberRepository
            .deleteAllByUserId(Arrays.stream(ids).map(s -> Long.parseLong(s.trim())).toList());
        //删除用户组关系
        userGroupMemberRepository
            .deleteAllByUserId(Arrays.stream(ids).map(s -> Long.parseLong(s.trim())).toList());
        return true;
    }

    /**
     * 参数有效性验证
     *
     * @param type  {@link CheckValidityType}
     * @param value {@link String}
     * @param id    {@link Long}
     * @return {@link Boolean}
     */
    @Override
    public Boolean userParamCheck(CheckValidityType type, String value, Long id) {
        if (StringUtils.isEmpty(value)) {
            return true;
        }
        QUserEntity user = QUserEntity.userEntity;
        UserEntity entity = new UserEntity();
        boolean result = false;
        // ID存在说明是修改操作，查询一下当前数据
        if (Objects.nonNull(id)) {
            entity = userRepository.findById(id).orElse(new UserEntity());
        }
        //手机号
        if (CheckValidityType.PHONE.equals(type)) {
            try {
                //手机号未修改
                if (StringUtils.equals(value, entity.getPhoneAreaCode() + entity.getPhone())) {
                    return true;
                }
                Phonenumber.PhoneNumber phoneNumber = PhoneNumberUtil.getInstance().parse(value,
                    "CN");
                BooleanExpression eq = user.phone
                    .eq(String.valueOf(phoneNumber.getNationalNumber()))
                    .and(user.phoneAreaCode.eq(String.valueOf(phoneNumber.getCountryCode())));
                result = !userRepository.exists(eq);
            } catch (NumberParseException e) {
                log.error("校验手机号发生异常", e);
                throw new TopIamException("校验手机号发生异常");
            }
        }
        //邮箱
        if (CheckValidityType.EMAIL.equals(type)) {
            if (StringUtils.equals(entity.getEmail(), value)) {
                return true;
            }
            BooleanExpression eq = user.email.eq(value);
            result = !userRepository.exists(eq);
        }
        //用户名
        if (CheckValidityType.USERNAME.equals(type)) {
            if (StringUtils.equals(entity.getUsername(), value)) {
                return true;
            }
            BooleanExpression eq = user.username.eq(value);
            result = !userRepository.exists(eq);
        }
        return result;
    }

    /**
     * 查询组织成员数量
     *
     * @param orgId {@link  String}
     * @return {@link  Long}
     */
    @Override
    public Long getOrgMemberCount(String orgId) {
        //条件
        QUserEntity user = QUserEntity.userEntity;
        QOrganizationEntity qOrganization = QOrganizationEntity.organizationEntity;
        Predicate predicate = user.isNotNull();
        //FIND_IN_SET函数
        BooleanExpression template = Expressions.booleanTemplate(
            "FIND_IN_SET({0}, replace({1}, '/', ','))> 0", orgId, qOrganization.path);
        predicate = StringUtils.isBlank(orgId) ? predicate
            : ExpressionUtils.and(predicate, qOrganization.id.eq(orgId).or(template));
        //构造查询
        JPAQuery<Long> jpaQuery = jpaQueryFactory.selectFrom(user).select(user.count())
            .innerJoin(QOrganizationMemberEntity.organizationMemberEntity)
            .on(user.id.eq(QOrganizationMemberEntity.organizationMemberEntity.userId))
            .innerJoin(qOrganization)
            .on(qOrganization.id.eq(QOrganizationMemberEntity.organizationMemberEntity.orgId))
            .where(predicate);
        return jpaQuery.fetch().get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatchUser(List<Long> removeIds) {
        // 删除
        if (!CollectionUtils.isEmpty(removeIds)) {
            userRepository.deleteAllById(removeIds);
        }
    }

    /**
     * 查看用户登录日志
     *
     * @param id {@link Long}
     * @param pageModel {@link PageModel}
     * @return {@link   List}
     */
    @Override
    public Page<UserLoginAuditListResult> findUserLoginAuditList(Long id, PageModel pageModel) {
        //查询入参转查询条件
        NativeSearchQuery nsq = userConverter.auditListRequestConvertToNativeSearchQuery(id,
            pageModel);
        //查询列表
        SearchHits<AuditElasticSearchEntity> search = elasticsearchRestTemplate.search(nsq,
            AuditElasticSearchEntity.class, IndexCoordinates
                .of(getAuditIndexPrefix(eiamSupportProperties.getDemo().isOpen()) + "*"));
        //结果转返回结果
        return userConverter.searchHitsConvertToAuditListResult(search, pageModel);
    }

    /**
     * 获取用户信息
     *
     * @param id {@link String}
     * @return {@link UserEntity}
     */
    private UserEntity getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new BadParamsException("用户不存在"));
    }

    /**
     * 用户数据映射器
     */
    private final UserConverter                 userConverter;

    /**
     * UserRepository
     */
    private final UserRepository                userRepository;

    /**
     * password encoder
     */
    private final PasswordEncoder               passwordEncoder;

    /**
     * 组织
     */
    private final OrganizationRepository        organizationRepository;

    /**
     * 组织成员
     */
    private final OrganizationMemberRepository  organizationMemberRepository;

    /**
     * 部门成员
     */
    private final UserGroupMemberRepository     userGroupMemberRepository;

    /**
     * 用户详情Repository
     */
    private final UserDetailRepository          userDetailsRepository;

    /**
     * JPAQueryFactory
     */
    private final JPAQueryFactory               jpaQueryFactory;

    /**
     * 修改密码历史Repository
     */
    private final UserHistoryPasswordRepository userHistoryPasswordRepository;

    /**
     * ElasticsearchRestTemplate
     */
    private final ElasticsearchRestTemplate     elasticsearchRestTemplate;

    /**
     * 邮件消息发布
     */
    private final MailMsgEventPublish           mailMsgEventPublish;

    /**
     * 短信消息发送
     */
    private final SmsMsgEventPublish            smsMsgEventPublish;

    /**
     * EiamSupportProperties
     */
    private final EiamSupportProperties         eiamSupportProperties;
}
