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
package cn.topiam.employee.console.controller.app;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.topiam.employee.common.enums.app.AppType;
import cn.topiam.employee.console.pojo.result.app.AppTemplateResult;
import cn.topiam.employee.console.service.app.AppTemplateService;
import cn.topiam.employee.support.result.ApiRestResult;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import static cn.topiam.employee.common.constants.AppConstants.APP_PATH;

/**
 * 应用模板
 *
 * @author TopIAM
 * Created by support@topiam.cn on 2020/9/27 19:28
 */
@Validated
@Tag(name = "应用模板")
@RestController
@RequiredArgsConstructor
@RequestMapping(value = APP_PATH + "/template", produces = MediaType.APPLICATION_JSON_VALUE)
public class AppTemplateController {

    /**
     * 获取模板列表
     *
     * @return {@link AppTemplateResult}
     */
    @Operation(summary = "模板列表")
    @GetMapping(value = "/list")
    @PreAuthorize(value = "authenticated and hasAuthority(T(cn.topiam.employee.core.security.authorization.Roles).ADMIN)")
    public ApiRestResult<List<AppTemplateResult>> getAppTemplateList(@Parameter(description = "模板类型") @RequestParam(value = "type", required = false) String type,
                                                                     @RequestParam(value = "name", required = false) String name) {
        return ApiRestResult.ok(templateService.getAppTemplateList(AppType.getType(type), name));
    }

    /**
     * 模板表单架构
     *
     * @return {@link AppTemplateResult}
     */
    @Operation(summary = "模板表单架构")
    @GetMapping(value = "/form_schema")
    @PreAuthorize(value = "authenticated and hasAuthority(T(cn.topiam.employee.core.security.authorization.Roles).ADMIN)")
    public ApiRestResult<List<Map>> getAppTemplateFormSchema(@Validated @Parameter(description = "模板编码") @NotEmpty(message = "模板编码不能为空") @RequestParam(value = "code", required = false) String code) {
        return ApiRestResult.ok(templateService.getAppTemplateFormSchema(code));
    }

    /**
     * ApplicationTemplateService
     */
    private final AppTemplateService templateService;
}
