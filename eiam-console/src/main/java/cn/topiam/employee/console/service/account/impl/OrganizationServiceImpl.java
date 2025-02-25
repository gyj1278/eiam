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

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.Lists;

import cn.topiam.employee.audit.context.AuditContext;
import cn.topiam.employee.audit.entity.Target;
import cn.topiam.employee.audit.enums.TargetType;
import cn.topiam.employee.common.entity.account.OrganizationEntity;
import cn.topiam.employee.common.enums.DataOrigin;
import cn.topiam.employee.common.repository.account.OrganizationRepository;
import cn.topiam.employee.console.converter.account.OrganizationConverter;
import cn.topiam.employee.console.pojo.result.account.OrganizationChildResult;
import cn.topiam.employee.console.pojo.result.account.OrganizationResult;
import cn.topiam.employee.console.pojo.result.account.OrganizationRootResult;
import cn.topiam.employee.console.pojo.result.account.OrganizationTreeResult;
import cn.topiam.employee.console.pojo.save.account.OrganizationCreateParam;
import cn.topiam.employee.console.pojo.update.account.OrganizationUpdateParam;
import cn.topiam.employee.console.service.account.OrganizationService;
import cn.topiam.employee.console.service.account.UserService;
import cn.topiam.employee.support.context.ApplicationContextHelp;
import cn.topiam.employee.support.util.BeanUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static cn.topiam.employee.support.constant.EiamConstants.ROOT_NODE;
import static cn.topiam.employee.support.repository.domain.BaseEntity.LAST_MODIFIED_BY;
import static cn.topiam.employee.support.repository.domain.BaseEntity.LAST_MODIFIED_TIME;

/**
 * <p>
 * 组织架构 服务实现类
 * </p>
 *
 * @author TopIAM
 * Created by support@topiam.cn on  2020-08-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {
    public static final String SEPARATE = "/";

    /**
     * 创建组织架构
     *
     * @param param {@link OrganizationCreateParam}
     * @return {@link Boolean}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createOrg(OrganizationCreateParam param) {
        //保存
        OrganizationEntity entity = orgDataConverter.orgCreateParamConvertToEntity(param);
        //查询父节点
        Optional<OrganizationEntity> parent = organizationRepository.findById(param.getParentId());
        //新建
        organizationRepository.save(entity);
        //展示路径枚举
        parent.ifPresent(org -> entity
            .setDisplayPath(StringUtils.isEmpty(org.getPath()) ? SEPARATE + entity.getName()
                : org.getDisplayPath() + SEPARATE + entity.getName()));
        //设置路径枚举
        parent.ifPresent(
            org -> entity.setPath(StringUtils.isEmpty(org.getPath()) ? SEPARATE + entity.getId()
                : org.getPath() + SEPARATE + entity.getId()));
        //存在父节点，更改为非叶子节点
        if (parent.isPresent() && parent.get().getLeaf()) {
            organizationRepository.updateIsLeaf(parent.get().getId(), false);
        }
        AuditContext
            .setTarget(Target.builder().id(entity.getId()).type(TargetType.ORGANIZATION).build());
        return true;
    }

    /**
     * 修改组织架构
     *
     * @param param {@link OrganizationCreateParam}
     * @return {@link Boolean}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateOrg(OrganizationUpdateParam param) {
        OrganizationEntity organization = orgDataConverter.orgUpdateParamConvertToEntity(param);
        Optional<OrganizationEntity> optional = this.organizationRepository.findById(param.getId());
        if (optional.isPresent()) {
            OrganizationEntity entity = optional.get();
            //如果修改了名字，递归修改和该组织有关所有节点信息的展示路径
            if (!optional.get().getName().equals(param.getName())) {
                //修改名称
                organization.setDisplayPath(getNewDisplayPath(param.getId(), param.getName(),
                    entity.getPath(), entity.getDisplayPath()));
                //递归更改下级名称
                if (!entity.getLeaf()) {
                    recursiveUpdateDisplayPath(entity.getId(), entity.getId(), param.getName());
                }
            }
            //修改
            BeanUtils.merge(organization, entity, LAST_MODIFIED_BY, LAST_MODIFIED_TIME);
            organizationRepository.save(entity);
            AuditContext.setTarget(
                Target.builder().id(entity.getId()).type(TargetType.ORGANIZATION).build());
            return true;
        }
        return false;
    }

    /**
     * 递归修改显示名称
     *
     * @param parentId {@link  String} 上级ID
     * @param id       {@link  String} ID 要更改名称的节点ID
     * @param name     {@link  String} 名称
     */
    protected void recursiveUpdateDisplayPath(String parentId, String id, String name) {
        List<OrganizationEntity> childNodes = organizationRepository.findByParentId(parentId);
        for (OrganizationEntity org : childNodes) {
            org.setDisplayPath(getNewDisplayPath(id, name, org.getPath(), org.getDisplayPath()));
            organizationRepository.save(org);
            //存在下级节点
            if (!org.getLeaf()) {
                //递归处理
                recursiveUpdateDisplayPath(org.getId(), id, name);
            }
        }
    }

    /**
     * 获取新的显示路径
     *
     * @param id          {@link  String} 要更改的ID
     * @param name        {@link  String} 要更改的新名称
     * @param path        {@link  String} 路径
     * @param displayPath {@link  String} 显示路径
     * @return {@link  String} 新显示名称
     */
    private String getNewDisplayPath(String id, String name, String path, String displayPath) {
        // 修改名称有个问题，如果名称一致，使用替换就会出问题，所以使用KEY=VALUE更改
        String[] pathIds = path.split(SEPARATE);
        String[] pathNames = displayPath.split(SEPARATE);
        //路径数据
        Map<String, String> pathData = new LinkedHashMap<>();
        if (pathIds.length == pathNames.length) {
            //i=1起步，以为切割数组，第0位为""
            for (int i = 1; i < pathNames.length; i++) {
                pathData.put(pathIds[i], pathNames[i]);
            }
        }
        pathData.put(id, name);
        //封装 displayPath
        StringBuilder newDisplayPath = new StringBuilder();

        for (Map.Entry<String, String> next : pathData.entrySet()) {
            newDisplayPath.append(SEPARATE).append(next.getValue());
        }
        return newDisplayPath.toString();
    }

    /**
     * 启用/禁用
     *
     * @param id      {@link String}
     * @param enabled {@link Boolean}
     * @return {@link Boolean}
     */
    @Override
    public Boolean updateStatus(String id, boolean enabled) {
        return organizationRepository.updateStatus(id, enabled) > 0;
    }

    /**
     * 删除组织架构
     *
     * @param id {@link List}
     * @return {@link Boolean}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteOrg(String id) {
        Optional<OrganizationEntity> optional = organizationRepository.findById(id);
        if (optional.isPresent()) {
            //是否存在子节点
            List<OrganizationEntity> list = organizationRepository.findByParentId(id);
            if (CollectionUtils.isEmpty(list)) {
                //查询当前机构和当前机构下子机构下是否存在用户，不存在删除，存在抛出异常
                Long count = ApplicationContextHelp.getBean(UserService.class)
                    .getOrgMemberCount(id);
                if (count > 0) {
                    throw new RuntimeException("删除机构失败，当前机构下存在用户");
                }
                //删除
                organizationRepository.deleteById(id);
                AuditContext
                    .setTarget(Target.builder().id(id).type(TargetType.ORGANIZATION).build());
                //查询该子节点上级组织机构是否存在子节点，如果不存在，更改 leaf=true
                list = organizationRepository.findByParentId(optional.get().getParentId());
                //不存在子部门，且父节点非根节点，执行更改 leaf=true 操作
                if (CollectionUtils.isEmpty(list)
                    && !StringUtils.equals(ROOT_NODE, optional.get().getParentId())) {
                    organizationRepository.updateIsLeaf(optional.get().getParentId(), true);
                }
                return true;
            }
            throw new RuntimeException("删除机构失败，当前机构下存在子机构");
        }
        return false;
    }

    /**
     * 组织机构详情
     *
     * @param id {@link String}
     * @return {@link OrganizationResult}
     */
    @Override
    public OrganizationResult getOrganization(String id) {
        Optional<OrganizationEntity> entity = organizationRepository.findById(id);
        return entity.map(orgDataConverter::entityConvertToOrgDetailResult).orElse(null);
    }

    /**
     * 移动组织机构
     *
     * @param id       {@link String}
     * @param parentId {@link String}
     * @return {@link Boolean}
     */
    @Override
    public Boolean moveOrganization(String id, String parentId) {
        AuditContext.setTarget(Target.builder().type(TargetType.ORGANIZATION).id(id).build(),
            Target.builder().type(TargetType.ORGANIZATION).id(parentId).build());
        Optional<OrganizationEntity> organization = organizationRepository.findById(id);
        if (organization.isPresent()) {
            OrganizationEntity entity = organization.get();
            Optional<OrganizationEntity> parent = organizationRepository.findById(parentId);
            if (parent.isPresent()) {
                if (parent.get().getLeaf()) {
                    parent.get().setLeaf(false);
                    organizationRepository.save(parent.get());
                }
                entity.setParentId(parentId);
                //父级路径
                entity.setPath(
                    StringUtils.defaultString(parent.get().getPath()) + SEPARATE + entity.getId());
                //父级展示路径
                entity.setDisplayPath(StringUtils.defaultString(parent.get().getDisplayPath())
                                      + SEPARATE + entity.getName());
            }
            // 判断当前节点下是否还存在子节点，不存在更改此节点为叶子节点
            List<OrganizationEntity> childList = organizationRepository.findByParentId(id);
            if (CollectionUtils.isEmpty(childList)) {
                entity.setLeaf(true);
            }
            organizationRepository.save(entity);
            //存在子组织，递归更改子组织 path 和 displayPath
            recursiveUpdateChildNodePathAndDisplayPath(entity.getId());
            return true;
        }
        return false;
    }

    /**
     * 递归修改子节点 path 和 displayPath
     *
     * @param id {@link  String } 当前节点ID
     */
    private void recursiveUpdateChildNodePathAndDisplayPath(String id) {
        Optional<OrganizationEntity> organization = organizationRepository.findById(id);
        if (organization.isPresent()) {
            OrganizationEntity entity = organization.get();
            List<OrganizationEntity> childList = organizationRepository.findByParentId(id);
            for (OrganizationEntity e : childList) {
                e.setPath(entity.getPath() + SEPARATE + entity.getId());
                e.setDisplayPath(entity.getDisplayPath() + SEPARATE + entity.getName());
                organizationRepository.save(e);
                //存在下级节点
                if (!e.getLeaf()) {
                    //递归处理
                    recursiveUpdateChildNodePathAndDisplayPath(e.getId());
                }
            }
        }
    }

    /**
     * 查询根组织
     *
     * @return {@link OrganizationResult}
     */
    @Override
    public OrganizationRootResult getRootOrganization() {
        OrganizationEntity entity = organizationRepository.findById(ROOT_NODE).orElse(null);
        return orgDataConverter.entityConvertToRootOrgListResult(entity);
    }

    /**
     * 查询子组织
     *
     * @param parentId {@link String}
     * @return {@link OrganizationResult}
     */
    @Override
    public List<OrganizationChildResult> getChildOrganization(String parentId) {
        List<OrganizationEntity> entityList = organizationRepository
            .findByParentIdOrderByOrderAsc(parentId);
        return orgDataConverter.entityConvertToChildOrgListResult(entityList);
    }

    /**
     * 查询子组织
     *
     * @param parentId         {@link String}
     * @param dataOrigin       {@link DataOrigin}
     * @param identitySourceId {@link Long}
     * @return {@link OrganizationEntity}
     */
    @Override
    public List<OrganizationEntity> getChildOrgList(String parentId, DataOrigin dataOrigin,
                                                    Long identitySourceId) {
        return organizationRepository.findByParentIdAndDataOriginAndIdentitySourceId(parentId,
            dataOrigin, identitySourceId);
    }

    /**
     * 过滤组织树
     *
     * @param keyWord {@link String} 关键字 name | code
     * @return {@link List}
     */
    @Override
    public List<OrganizationTreeResult> filterOrganizationTree(String keyWord) {
        List<OrganizationEntity> list = organizationRepository.findByNameLikeOrCodeLike(keyWord);
        if (!CollectionUtils.isEmpty(list)) {
            List<String> parentIds = Lists.newArrayList();
            for (OrganizationEntity entity : list) {
                parentIds.addAll(Lists.newArrayList(entity.getPath().split(SEPARATE)));
            }
            if (!CollectionUtils.isEmpty(parentIds)) {
                List<OrganizationEntity> entityList = organizationRepository
                    .findByIdInOrderByOrderAsc(parentIds);
                return orgDataConverter.entityConvertToChildOrgTreeListResult(null, entityList);
            }
        }
        return Lists.newArrayList();
    }

    @Override
    public OrganizationEntity getById(String id) {
        return organizationRepository.findById(id).orElse(null);
    }

    /**
     * 根据外部ID查询组织架构
     *
     * @param id               {@link String}
     * @param identitySourceId {@link Long}
     * @return {@link OrganizationEntity}
     */
    @Override
    public OrganizationEntity getOrganizationByExternalId(String id, Long identitySourceId) {
        return organizationRepository.findByExternalIdAndIdentitySourceId(id, identitySourceId);
    }

    /**
     * 批量删除组织
     *
     * @param ids {@link String}
     * @return {@link Boolean}
     */
    @Override
    public Boolean batchDeleteOrg(String[] ids) {
        organizationRepository.deleteAllById(Arrays.asList(ids));
        return true;
    }

    /**
     * 组织架构数据映射器
     */
    private final OrganizationConverter  orgDataConverter;
    /**
     * OrganizationRepository
     */
    private final OrganizationRepository organizationRepository;
}
