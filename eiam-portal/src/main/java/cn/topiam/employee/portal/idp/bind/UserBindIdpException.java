/*
 * eiam-portal - Employee Identity and Access Management Program
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
package cn.topiam.employee.portal.idp.bind;

import org.springframework.http.HttpStatus;

import cn.topiam.employee.support.exception.TopIamException;

/**
 *
 * @author TopIAM
 * Created by support@topiam.cn on  2022/8/13 23:58
 */
public class UserBindIdpException extends TopIamException {
    public UserBindIdpException() {
        super("user_bind_idp_error", "用户绑定 IDP 错误", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public UserBindIdpException(String error, String description) {
        super(error, description, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
