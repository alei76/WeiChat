package net.chat.service.impl;

import java.util.ArrayList;
import java.util.List;

import net.chat.dao.WxAccountDao;
import net.chat.dao.WxAccountGameDao;
import net.chat.dao.WxGameDao;
import net.chat.dao.WxMessageDao;
import net.chat.dao.WxMsgTypeDao;
import net.chat.domain.WxAccount;
import net.chat.domain.WxAccountGame;
import net.chat.domain.WxGame;
import net.chat.domain.WxMessage;
import net.chat.domain.WxMsgType;
import net.chat.service.AccountService;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("accountSerivce")
@Transactional
public class AccountServiceImpl implements AccountService {

	@Autowired
	private WxAccountDao accountDao;

	@Autowired
	private WxMessageDao messageDao;

	@Autowired
	private WxGameDao gameDao;

	@Autowired
	private WxAccountGameDao accountGameDao;

	@Autowired
	private WxMsgTypeDao messageTypeDao;

	public void saveAccount(WxAccount account) {
		if (account.getId() == null) {
			accountDao.save(account);
			Long accountId = account.getId();
			WxMessage message = new WxMessage();
			message.setAccountId(accountId);
			message.setMsgType("text");
			message.setMsgName("��ӭ��");
			message.setContent("лл��ע���˺ţ�");
			// ��ȡ���������
			WxGame defaultGame = gameDao.findByUrlAndGameType("autoreply.jsp",
					"program");
			WxAccountGame accountGame = new WxAccountGame();
			accountGame.setGameId(defaultGame.getId());
			accountGame.setAccountId(accountId);
			accountGameDao.save(accountGame);
			List<WxMsgType> messageTypeList = new ArrayList<WxMsgType>(10);
			messageTypeList.add(new WxMsgType(accountId, "text", "program",
					message.getId(), "�ı�"));
			messageTypeList.add(new WxMsgType(accountId, "image", "direct",
					message.getId(), "ͼƬ"));
			messageTypeList.add(new WxMsgType(accountId, "voice", "direct",
					message.getId(), "����"));
			messageTypeList.add(new WxMsgType(accountId, "subscribe", "direct",
					message.getId(), "��ע"));
			messageTypeList.add(new WxMsgType(accountId, "video", "direct",
					message.getId(), "��Ƶ"));
			messageTypeList.add(new WxMsgType(accountId, "unsubscribe",
					"direct", message.getId(), "ȡ����ע"));
			messageTypeDao.save(messageTypeList);
		}

	}

	public void editAccount(WxAccount account) {
		if (account.getId() != null) {
			WxAccount accountEntity = accountDao.findOne(account.getId());
			BeanUtils.copyProperties(account, accountEntity);
		} else
			this.saveAccount(account);
	}

	public void deleteAccount(Long accountId) {
		accountDao.delete(accountId);
	}

	public void ListAllAcount() {
		accountDao.findAll(new Pageable() {

			public int getPageNumber() {
				// TODO Auto-generated method stub
				return 0;
			}

			public int getPageSize() {
				// TODO Auto-generated method stub
				return 0;
			}

			public int getOffset() {
				// TODO Auto-generated method stub
				return 0;
			}

			public Sort getSort() {
				// TODO Auto-generated method stub
				return null;
			}

		});

	}

	public WxAccount findAcountById(Long accountId) {
		// TODO Auto-generated method stub
		return null;
	}

}
