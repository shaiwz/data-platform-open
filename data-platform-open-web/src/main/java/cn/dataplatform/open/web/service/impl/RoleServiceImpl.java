package cn.dataplatform.open.web.service.impl;

import cn.dataplatform.open.common.component.OrikaMapper;
import cn.dataplatform.open.common.exception.ApiException;
import cn.dataplatform.open.common.vo.base.PageBase;
import cn.dataplatform.open.common.vo.base.PageRequest;
import cn.dataplatform.open.common.vo.base.PageResult;
import cn.dataplatform.open.web.config.Context;
import cn.dataplatform.open.web.service.RoleService;
import cn.dataplatform.open.web.store.entity.Role;
import cn.dataplatform.open.web.store.mapper.RoleMapper;
import cn.dataplatform.open.web.vo.role.*;
import cn.dataplatform.open.web.vo.user.UserData;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Resource
    private OrikaMapper orikaMapper;

    /**
     * 列表
     *
     * @param pageRequest 分页请求
     * @return user
     */
    @Override
    public PageResult<RoleListResponse> list(PageRequest<RoleListRequest> pageRequest) {
        PageBase page = pageRequest.getPage();
        RoleListRequest query = Optional.ofNullable(pageRequest.getQuery()).orElse(new RoleListRequest());
        Page<Role> rolePage = this.lambdaQuery()
                .like(StrUtil.isNotBlank(query.getName()), Role::getName, query.getName())
                .like(StrUtil.isNotBlank(query.getCode()), Role::getCode, query.getCode())
                .eq(query.getStatus() != null, Role::getStatus, query.getStatus())
                .page(new Page<>(page.getCurrent(), page.getSize()));
        PageResult<RoleListResponse> pageResult = new PageResult<>();
        List<RoleListResponse> collect = rolePage.getRecords()
                .stream()
                .map(m -> {
                    RoleListResponse roleListResponse = new RoleListResponse();
                    this.orikaMapper.map(m, roleListResponse);
                    return roleListResponse;
                }).collect(Collectors.toList());
        pageResult.setData(collect, rolePage.getCurrent(), rolePage.getSize(), rolePage.getTotal());
        return pageResult;
    }

    /**
     * 详情
     *
     * @param id id
     * @return r
     */
    @Override
    public RoleDetailResponse detail(Long id) {
        Role role = this.getById(id);
        if (role == null) {
            throw new ApiException("角色不存在");
        }
        RoleDetailResponse roleDetailResponse = new RoleDetailResponse();
        this.orikaMapper.map(role, roleDetailResponse);
        return roleDetailResponse;
    }

    /**
     * 添加
     *
     * @param roleAddRequest 请求
     * @return r
     */
    @Override
    public Boolean add(RoleAddRequest roleAddRequest) {
        UserData user = Context.getUser();
        Role role = new Role();
        this.orikaMapper.map(roleAddRequest, role);
        role.setCode(UUID.fastUUID().toString(true));
        role.setCreateUserId(user.getId());
        this.save(role);
        return true;
    }

    /**
     * 更新
     *
     * @param roleUpdateRequest 请求
     * @return r
     */
    @Override
    public Boolean update(RoleUpdateRequest roleUpdateRequest) {
        Long id = roleUpdateRequest.getId();
        Role role = this.getById(id);
        if (role == null) {
            throw new ApiException("角色不存在");
        }
        this.orikaMapper.map(roleUpdateRequest, role);
        this.updateById(role);
        return true;
    }

    /**
     * 删除
     *
     * @param id id
     * @return r
     */
    @Override
    public Boolean delete(Long id) {
        Role role = this.getById(id);
        if (role == null) {
            throw new ApiException("角色不存在");
        }
        this.removeById(id);
        return true;
    }

}
