package com.gierza_molases.molases_app.service;

import java.util.List;

import com.gierza_molases.molases_app.dao.ProductAssociationDao;
import com.gierza_molases.molases_app.model.Customer;

public class ProductAssociationService {

	private final ProductAssociationDao productAssociationDao;

	public ProductAssociationService(ProductAssociationDao productAssociationDao) {
		this.productAssociationDao = productAssociationDao;
	}

	public void insertAll(int productId, List<Integer> customerIds) {
		productAssociationDao.insertAll(productId, customerIds);
	}

	public List<Customer> fetchByProductId(int productId) {
		System.out.println("fetching associated by product: " + productId);
		return productAssociationDao.fetchAssociationByProductId(productId);
	}

	public void removeAssociation(int productId, int customerId) {
		productAssociationDao.removeAssociation(productId, customerId);
	}

}
