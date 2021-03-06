/**
 * 
 */
package net.chat.service;

import java.util.List;

import net.chat.domain.WxCustomMenu;
import net.chat.integration.vo.Menu;

/**
 * @author bo.chen
 * 
 */
public interface CustomMenuService {

	WxCustomMenu findCustomMenuById(Long id);

	List<WxCustomMenu> findCustomMenuByAccountId(Long accountId);

	WxCustomMenu save(WxCustomMenu bean);

	void delete(Long id);

	List<WxCustomMenu> findParentMenuByAccountId(Long accountId);

	String checkCustomMenu(WxCustomMenu bean);

	List<WxCustomMenu> findChildMenuByAccountId(Long accountId);

	Menu createMenu(Long accountId);
}
