package net.chat.dao.mall;

import java.util.List;

import net.chat.domain.mall.WxProductPic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WxProductPicDao extends JpaRepository<WxProductPic, Long>,
		JpaSpecificationExecutor<WxProductPic> {

	@Query("from WxProductPic  where productId = :productId")
	List<WxProductPic> findPicByProductId(@Param("productId") long productId);

	@Query("from WxProductPic  where productId = :productId and flag=1")
	List<WxProductPic> findPicByProductIdWherenotDefault(
			@Param("productId") long productId);

	@Query("from WxProductPic  where productId = :productId and flag=0")
	WxProductPic findDefaultPicByProductId(@Param("productId") long productId);

	@Modifying
	@Query("update WxProductPic  set flag=0 where id = :id")
	void setDaulftPic(@Param("id") Long id);

	@Modifying
	@Query("update WxProductPic set flag=1 where productId = :productId")
	void resetPic(@Param("productId") Long prductId);

	@Query("from WxProductPic  where productId = :productId and flag=1")
	List<WxProductPic> findExtentionPicByProductId(
			@Param("productId") long productId);

	@Modifying
	@Query("delete from  WxProductPic where productId = :productId and flag=1")
	void deletePic(@Param("productId") long productId);

}
