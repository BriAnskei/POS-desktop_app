package com.gierza_molases.molases_app.context;

import java.sql.Connection;

import com.gierza_molases.molases_app.UiController.BranchesController;
import com.gierza_molases.molases_app.UiController.CustomersController;
import com.gierza_molases.molases_app.UiController.ProductsController;
import com.gierza_molases.molases_app.dao.BranchDao;
import com.gierza_molases.molases_app.dao.CustomerDao;
import com.gierza_molases.molases_app.dao.ProductAssociationDao;
import com.gierza_molases.molases_app.dao.ProductDao;
import com.gierza_molases.molases_app.service.BranchService;
import com.gierza_molases.molases_app.service.CustomerService;
import com.gierza_molases.molases_app.service.ProductAssociationService;
import com.gierza_molases.molases_app.service.ProductService;
import com.gierza_molases.molases_app.util.Database;
import com.gierza_molases.molases_app.util.DatabaseInitializer;

public class AppContext {
	private static Connection conn;

	// Services
	public static CustomerService customerService;
	public static BranchService branchService;
	public static ProductService productService;
	public static ProductAssociationService productAssociationService;

	// Controllers
	public static CustomersController customersController;
	public static BranchesController branchesController;
	public static ProductsController productsController;

	public static void init() {
		conn = Database.init();
		DatabaseInitializer.init();

		// DAO instance
		CustomerDao customerDao = new CustomerDao(conn);
		BranchDao branchDao = new BranchDao(conn);
		ProductDao productDao = new ProductDao(conn);
		ProductAssociationDao productAssociationDao = new ProductAssociationDao(conn);

		// service instance
		customerService = new CustomerService(customerDao, branchDao);
		branchService = new BranchService(branchDao, customerDao);
		productService = new ProductService(productDao, productAssociationDao);
		productAssociationService = new ProductAssociationService(productAssociationDao);

		// Initialize controller with a new state instance
		customersController = new CustomersController(new CustomerState(), customerService);
		branchesController = new BranchesController(new BranchState(), branchService);
		productsController = new ProductsController(new ProductState(), productService, productAssociationService);
	}

	public static Connection getConnection() {
		return conn;
	}
}