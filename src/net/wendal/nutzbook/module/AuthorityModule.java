package net.wendal.nutzbook.module;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.wendal.nutzbook.bean.Permission;
import net.wendal.nutzbook.bean.Role;
import net.wendal.nutzbook.bean.User;
import oracle.jdbc.proxy.annotation.Post;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.dao.FieldFilter;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.util.Daos;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;

@At("/admin/authority")
@IocBean
public class AuthorityModule extends BaseModule {
	
	//---------------------------------------------
	// 查询类
	
	@RequiresPermissions("authority:user:query")
	@At
	@Ok("json:{locked:'password|salt',ignoreNull:true}")
	public Object users(@Param("query")String query, @Param("..")Pager pager) {
		return ajaxOk(query(User.class, null, pager, null));
	}
	
	@RequiresPermissions("authority:role:query")
	@At
	public Object roles(@Param("query")String query, @Param("..")Pager pager) {
		return ajaxOk(query(Role.class, null, pager, null));
	}
	
	@RequiresPermissions("authority:permission:query")
	@At
	public Object permissions(@Param("query")String query, @Param("..")Pager pager) {
		return ajaxOk(query(Permission.class, null, pager, null));
	}
	

	//---------------------------------------------
	// 用户操作

	@Post
	@AdaptBy(type=JsonAdaptor.class)
	@RequiresPermissions("authority:user:update")
	@At("/user/update")
	public void updateUser(@Param("userId")long userId,
						   @Param("roles")List<String> roles, 
						   @Param("permissions")List<String> permissions) {
		User user = dao.fetch(User.class, userId);
		if (user == null)
			return;
		if (roles != null) {
			List<Role> rs = new ArrayList<Role>(roles.size());
			for (String role : roles) {
				if (role == null)
					continue;
				Role r = dao.fetch(Role.class, role);
				if (r != null) {
					rs.add(r);
				}
			}
			dao.fetchLinks(user, "roles");
			if (user.getRoles().size() > 0) {
				dao.clearLinks(user, "roles");
			}
			user.setRoles(rs);
			dao.updateLinks(user, "roles");
		}
		if (permissions != null) {
			List<Permission> ps = new ArrayList<Permission>();
			for (String permission : permissions) {
				Permission p = dao.fetch(Permission.class, permission);
				if (p != null)
					ps.add(p);
			}
			dao.fetchLinks(user, "permissions");
			if (user.getPermissions().size() > 0) {
				dao.clearLinks(user, "permissions");
			}
			user.setPermissions(ps);
			dao.updateLinks(user, "permissions");
		}
	}

	//---------------------------------------------
	// Role操作
	
	@Post
	@AdaptBy(type=JsonAdaptor.class)
	@RequiresPermissions("authority:role:add")
	@At("/role/add")
	public void addRole(@Param("..")Role role) {
		if (role == null)
			return;
		dao.insert(role);
	}
	
	@POST
	@AdaptBy(type=JsonAdaptor.class)
	@RequiresPermissions("authority:role:delete")
	@At("/role/delete")
	public void delRole(@Param("..")Role role) {
		if (role == null)
			return;
		role = dao.fetch(Role.class, role.getId());
		if (role == null)
			return;
		if ("admin".equals(role.getName()))
			return;
		dao.delete(Role.class, role.getId());
	}
	
	@Post
	@AdaptBy(type=JsonAdaptor.class)
	@RequiresPermissions("authority:role:update")
	@At("/role/update")
	public void updateRole(@Param("role")Role role, @Param("permissions")List<String> permissions) {
		if (role == null)
			return;
		if (dao.fetch(Role.class, role.getId()) == null)
			return;
		dao.update(role);
		if (permissions != null) {
			List<Permission> ps = new ArrayList<Permission>();
			for (String permission : permissions) {
				Permission p = dao.fetch(Permission.class, permission);
				if (p != null)
					ps.add(p);
			}
			dao.fetchLinks(role, "permissions");
			if (role.getPermissions().size() > 0) {
				dao.clearLinks(role, "permissions");
			}
			role.setPermissions(ps);
			dao.updateLinks(role, "permissions");
		}
	}
	
	//--------------------------------------------------------------------
	// Permission操作
	
	@Post
	@AdaptBy(type=JsonAdaptor.class)
	@RequiresPermissions("authority:permission:add")
	@At("/permission/add")
	public void addPermission(@Param("..")Permission permission) {
		if (permission == null)
			return;
		dao.insert(permission);
	}
	
	@POST
	@AdaptBy(type=JsonAdaptor.class)
	@RequiresPermissions("authority:permission:delete")
	@At("/permission/delete")
	public void delPermission(@Param("..")Permission permission) {
		if (permission == null)
			return;
		dao.delete(Permission.class, permission.getId());
	}
	
	@Post
	@AdaptBy(type=JsonAdaptor.class)
	@RequiresPermissions("authority:permission:update")
	@At("/permission/update")
	public void updateRole(@Param("..")Permission permission) {
		if (permission == null)
			return;
		if (dao.fetch(Permission.class, permission.getId()) == null)
			return;
		permission.setUpdateTime(new Date());
		permission.setCreateTime(null);
		Daos.ext(dao, FieldFilter.create(Permission.class, null, "name", true)).update(permission);
	}
}