/*
 * eiam-identity-source-core - Employee Identity and Access Management Program
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
package cn.topiam.employee.identitysource.core.exception;

import cn.topiam.employee.support.exception.TopIamException;

/**
 * 身份源不存在异常
 *
 * @author TopIAM
 * Created by support@topiam.cn on  2022/7/8 22:23
 */
public class IdentitySourceNotExistException extends TopIamException {
    public IdentitySourceNotExistException() {
        super("identity_source_not_exist", "身份源不存在", DEFAULT_STATUS);
    }
}
