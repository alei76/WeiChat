package net.chat.service.mall.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import net.chat.dao.mall.WxMallDao;
import net.chat.dao.mall.WxPrdtCategoryDao;
import net.chat.dao.mall.WxPrdtSubCategoryDao;
import net.chat.dao.mall.WxProductCategoryDao;
import net.chat.dao.mall.WxProductDao;
import net.chat.dao.mall.WxProductPicDao;
import net.chat.dao.mall.WxProductPriceDao;
import net.chat.domain.mall.WxPrdtCategory;
import net.chat.domain.mall.WxProduct;
import net.chat.domain.mall.WxProductPic;
import net.chat.domain.mall.WxProductPrice;
import net.chat.formbean.MallProductForm;
import net.chat.service.mall.MallProductService;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service("mallProductService")
@Transactional
public class MallProductServiceImpl implements MallProductService {

	@Value("${ssweb.root}")
	protected File rootFolder;

	@Autowired
	WxMallDao mallDao;

	@Autowired
	WxProductCategoryDao productCategoryDao;

	@Autowired
	WxPrdtSubCategoryDao prdtSubCategoryDao;

	@Autowired
	WxPrdtCategoryDao prdtCategoryDao;

	@Autowired
	WxProductDao productDao;

	@Autowired
	WxProductPriceDao priceDao;

	@Autowired
	WxProductPicDao picDao;

	@Override
	public Page<MallProductForm> findAllProductBySubcategory(
			final Long subcategoryId, int pageNo) {

		Pageable pageable = new PageRequest(pageNo - 1, 5, new Sort(new Order(
				Direction.DESC, "createDate")));
		Specification<WxPrdtCategory> spec = new Specification<WxPrdtCategory>() {
			public Predicate toPredicate(Root<WxPrdtCategory> root,
					CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb
						.equal(root.<Long> get("subCategoryId"), subcategoryId);
			}
		};

		Page<WxPrdtCategory> pagePrdtCategory = prdtCategoryDao.findAll(spec,
				pageable);
		List<WxPrdtCategory> prdtCategorys = pagePrdtCategory.getContent();
		List<MallProductForm> mallProductForms = new ArrayList<MallProductForm>();
		for (WxPrdtCategory prodtCategory : prdtCategorys) {
			WxProduct product = productDao
					.findOne(prodtCategory.getProductId());
			if (product != null) {
				MallProductForm _mallProductForm = new MallProductForm();
				WxProductPrice productPrice = priceDao
						.findPriceByProductId(prodtCategory.getProductId());
				WxProductPic productDefaultPic = picDao
						.findDefaultPicByProductId(prodtCategory.getProductId());
				List<WxProductPic> productPics = picDao
						.findPicByProductId(prodtCategory.getProductId());
				_mallProductForm.setDefaultMallProductPic(productDefaultPic);
				_mallProductForm.setMallProduct(product);
				_mallProductForm.setMallProductPrice(productPrice);
				_mallProductForm.setMallProductPic(productPics);
				_mallProductForm.setMallProductCategory(prodtCategory);
				mallProductForms.add(_mallProductForm);
			}
		}
		Page<MallProductForm> prodtFormPage = new PageImpl<MallProductForm>(
				mallProductForms, new PageRequest(pageNo - 1, 5, null),
				pagePrdtCategory.getTotalElements());
		return prodtFormPage;
	}

	@Override
	@Transactional
	public Long saveProduct(WxProduct product, List<Long> subcategoryIds,
			MultipartFile productDefaultPic) throws IOException {
		if (product.getId() == null) {
			product = productDao.save(product);
			for (Long subcategoryId : subcategoryIds) {
				WxPrdtCategory productCategory = new WxPrdtCategory();
				productCategory.setProductId(product.getId());
				productCategory.setSubCategoryId(subcategoryId);
				prdtCategoryDao.save(productCategory);
			}
			File slideTmpFolder = new File(rootFolder, "/mallimg/images/"
					+ product.getMallId());
			String suffix = productDefaultPic.getOriginalFilename().substring(
					productDefaultPic.getOriginalFilename().lastIndexOf("."));
			DateFormat format = new SimpleDateFormat("yyyyMMdd hh:mm:ss");
			String imageUrl = format.format(new Date()) + UUID.randomUUID()
					+ suffix;
			FileUtils.copyInputStreamToFile(productDefaultPic.getInputStream(),
					new File(slideTmpFolder, imageUrl));
			WxProductPic pic = new WxProductPic();
			pic.setFlag("0");
			pic.setPicName(productDefaultPic.getName());
			pic.setPicUrl(File.separator + product.getMallId() + File.separator
					+ imageUrl);
			pic.setProductId(product.getId());
			picDao.save(pic);
		}
		return product.getId();
	}

	@Override
	@Transactional
	public void saveProductPrice(WxProductPrice productPrice) {
		priceDao.deletePrice(productPrice.getProductId());
		priceDao.save(productPrice);
	}

	@Override
	public void updateProductPrice(long productId, BigDecimal price) {
		priceDao.updatePrice(productId, price);
	}

	@Override
	public void saveProductPic(MultipartFile zipPicFile, long productId)
			throws IOException {
		final int buffer = 2048;
		WxProduct product = productDao.findOne(productId);
		InputStream is = zipPicFile.getInputStream();
		ZipInputStream zis = new ZipInputStream(is);
		ZipEntry entry = null;
		File slideTmpFolder = new File(rootFolder, "/mallimg/images/"
				+ product.getMallId());
		List<WxProductPic> productPics = new ArrayList<WxProductPic>();
		int count = -1;
		int index = -1;
		BufferedOutputStream bos = null;
		boolean flag = false;
		while ((entry = zis.getNextEntry()) != null) {
			byte data[] = new byte[buffer];
			String temp = entry.getName();
			flag = isPics(temp);
			if (!flag || temp.indexOf("/") != -1)
				continue;
			index = temp.lastIndexOf("/");
			if (index > -1)
				temp = temp.substring(index + 1);
			String suffix = temp.substring(temp.lastIndexOf("."));
			DateFormat format = new SimpleDateFormat("yyyyMMdd hh:mm:ss");
			String imageUrl = format.format(new Date()) + UUID.randomUUID()
					+ suffix;
			File f = new File(slideTmpFolder, imageUrl);
			f.createNewFile();
			FileOutputStream fos = new FileOutputStream(f);
			bos = new BufferedOutputStream(fos, buffer);
			while ((count = zis.read(data, 0, buffer)) != -1) {
				bos.write(data, 0, count);
			}
			bos.flush();
			bos.close();
			WxProductPic pic = new WxProductPic();
			pic.setFlag("1");
			pic.setPicName(f.getName());
			pic.setPicUrl(File.separator + product.getMallId() + File.separator
					+ f.getName());
			pic.setProductId(productId);
			productPics.add(pic);
		}
		deletePic(productId);
		zis.close();
		picDao.save(productPics);
	}

	public static boolean isPics(String filename) {
		boolean flag = false;
		if (filename.toUpperCase().endsWith(".JPG")
				|| filename.toUpperCase().endsWith(".GIF")
				|| filename.toUpperCase().endsWith(".BMP")
				|| filename.toUpperCase().endsWith(".PNG"))
			flag = true;
		return flag;
	}

	@Override
	@Transactional
	public void setProductPicDefault(long productPicId) {
		WxProductPic pic = picDao.findOne(productPicId);
		picDao.resetPic(pic.getProductId());
		picDao.setDaulftPic(productPicId);

	}

	@Transactional
	private void deletePic(long productId) {
		List<WxProductPic> pics = picDao
				.findPicByProductIdWherenotDefault(productId);
		File slideTmpFolder = new File(rootFolder + "/mallimg/images");
		for (WxProductPic pic : pics) {
			new File(slideTmpFolder, pic.getPicUrl()).delete();
		}
		picDao.deletePic(productId);

	}

	@Override
	@Transactional
	public Long editProduct(WxProduct product, List<Long> subcategoryIds,
			MultipartFile productDefaultPic) throws IOException {
		WxProduct productEntity = productDao.findOne(product.getId());
		productEntity.setDescrpiton(product.getDescrpiton());
		productEntity.setEffectiveDate(product.getEffectiveDate());
		productEntity.setExpiryDate(product.getExpiryDate());
		productEntity.setProductName(product.getProductName());
		productEntity.setStock(product.getStock());
		productEntity.setProductPrice(product.getProductPrice());
		prdtCategoryDao.deleteByProductId(product.getId());
		for (Long subcategoryId : subcategoryIds) {
			WxPrdtCategory productCategory = new WxPrdtCategory();
			productCategory.setProductId(product.getId());
			productCategory.setSubCategoryId(subcategoryId);
			prdtCategoryDao.save(productCategory);
		}
		WxProductPic productDefaultPicEntity = picDao
				.findDefaultPicByProductId(product.getId());
		new File(rootFolder, "/mallimg/images"
				+ productDefaultPicEntity.getPicUrl()).delete();
		File slideTmpFolder = new File(rootFolder, "/mallimg/images/"
				+ product.getMallId());
		String suffix = productDefaultPic.getOriginalFilename().substring(
				productDefaultPic.getOriginalFilename().lastIndexOf("."));
		DateFormat format = new SimpleDateFormat("yyyyMMdd hh:mm:ss");
		String imageUrl = format.format(new Date()) + UUID.randomUUID()
				+ suffix;
		FileUtils.copyInputStreamToFile(productDefaultPic.getInputStream(),
				new File(slideTmpFolder, imageUrl));
		productDefaultPicEntity.setPicUrl(File.separator + product.getMallId()
				+ File.separator + imageUrl);

		return product.getId();
	}

	public List<WxProductPrice> findProductPrice(Long productId) {
		return priceDao.findAllPriceByProductId(productId);
	}

	@Override
	public List<WxProductPic> findExtentionPic(Long productId) {
		return picDao.findExtentionPicByProductId(productId);
	}
}
