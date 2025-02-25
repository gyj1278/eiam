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
package cn.topiam.employee.console.security.listener;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;

import cn.topiam.employee.audit.entity.Actor;
import cn.topiam.employee.audit.enums.EventStatus;
import cn.topiam.employee.audit.enums.EventType;
import cn.topiam.employee.audit.event.AuditEventPublish;
import cn.topiam.employee.common.entity.setting.AdministratorEntity;
import cn.topiam.employee.common.enums.UserType;
import cn.topiam.employee.common.repository.setting.AdministratorRepository;
import cn.topiam.employee.core.security.userdetails.UserDetails;
import cn.topiam.employee.support.context.ApplicationContextHelp;
import static cn.topiam.employee.core.security.util.SecurityUtils.getFailureMessage;

/**
 * 认证失败
 *
 * @author TopIAM
 * Created by support@topiam.cn on 2020/9/3 22:42
 */
public class ConsoleAuthenticationFailureEventListener implements
                                                       ApplicationListener<AbstractAuthenticationFailureEvent> {
    private final Logger logger = LoggerFactory
        .getLogger(ConsoleAuthenticationFailureEventListener.class);

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(@NonNull AbstractAuthenticationFailureEvent event) {
        //@formatter:off
        AuditEventPublish publish = ApplicationContextHelp.getBean(AuditEventPublish.class);
        String content = getFailureMessage(event);
        logger.error("认证失败 [{}]",content);
        String principal = (String) event.getAuthentication().getPrincipal();
        if (event.getAuthentication().getPrincipal() instanceof String){
            principal = (String) event.getAuthentication().getPrincipal();
        }
        if (event.getAuthentication().getPrincipal() instanceof UserDetails || event.getAuthentication().getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails){
            principal = ((UserDetails) event.getAuthentication().getPrincipal()).getUsername();
        }
        if (StringUtils.isNotBlank(principal)){
            Optional<AdministratorEntity> optional = getAdministratorRepository().findByUsername(principal);
            if (optional.isEmpty()) {
                // 手机号
                optional = getAdministratorRepository().findByPhone(principal);
                if (optional.isEmpty()) {
                    // 邮箱
                    optional = getAdministratorRepository().findByEmail(principal);
                }
            }
            if (optional.isEmpty()) {
                Actor actor = Actor.builder().type(UserType.ADMIN).build();
                publish.publish(EventType.LOGIN_CONSOLE,"账户不存在："+principal, actor, EventStatus.FAIL);
                return;
            }
            AdministratorEntity administrator = optional.get();
            Actor actor = Actor.builder().id(administrator.getId().toString()).type(UserType.ADMIN).build();
            publish.publish(EventType.LOGIN_CONSOLE,content+"："+administrator.getUsername(), actor,EventStatus.FAIL);
        }
        //@formatter:on
    }

    private AdministratorRepository getAdministratorRepository() {
        return ApplicationContextHelp.getBean(AdministratorRepository.class);
    }

}
