package net.chat.dao;

import net.chat.domain.WxCmd;
import net.chat.util.CreateDomainUtil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/spring/spring-jpa.xml" })
public class WxCmdDaoTest {

	@Autowired
	private WxCmdDao dao;

	@Test
	@Transactional
	public void testCreate() throws IllegalArgumentException, IllegalAccessException {
		WxCmd domain = new WxCmd();
		CreateDomainUtil.createDomain(domain);
		dao.save(domain);

	}

}
