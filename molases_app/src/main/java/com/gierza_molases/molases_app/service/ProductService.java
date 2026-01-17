package com.gierza_molases.molases_app.service;

import java.util.List;

import com.gierza_molases.molases_app.dao.ProductAssociationDao;
import com.gierza_molases.molases_app.dao.ProductDao;
import com.gierza_molases.molases_app.model.Product;
import com.gierza_molases.molases_app.util.TransactionHelper;

public class ProductService {

	private final ProductDao productDao;
	private final ProductAssociationDao productAssociationDao;

	public ProductService(ProductDao productDao, ProductAssociationDao productAssociationDao) {
		this.productDao = productDao;
		this.productAssociationDao = productAssociationDao;
	}

	public void addProduct(Product newProduct, List<Integer> selectedCustomerIds) {

		newProduct.validate();

		TransactionHelper.executeInTransaction(conn -> {

			int productId = productDao.insert(newProduct, conn);

			// check if there are associated customers
			if (selectedCustomerIds.size() > 0) {

				productAssociationDao.insertAll(productId, selectedCustomerIds, conn);
			}
		});

	}

	public List<Product> getAllProducts(String search, String sortOrder) {
		boolean newestFirst = sortOrder == "DESC";

		return productDao.findAll(search, newestFirst);
	}

	public List<Product> getallProductsForSelection(int customerId, String search) {
		System.out.println("Fetching product + " + customerId + search);
		return productDao.getProductsAndAssocitedByCustomerId(customerId, search);
	}

	public void updateProduct(Product product) {
		product.validate();

		productDao.update(product);
	}

	public void deleteProduct(int productId) {
		productDao.deleteById(productId);
	}

}
