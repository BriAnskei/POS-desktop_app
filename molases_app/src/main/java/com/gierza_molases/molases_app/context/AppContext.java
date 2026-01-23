package com.gierza_molases.molases_app.context;

import java.sql.Connection;

import com.gierza_molases.molases_app.UiController.BranchesController;
import com.gierza_molases.molases_app.UiController.CustomersController;
import com.gierza_molases.molases_app.UiController.DeliveryController;
import com.gierza_molases.molases_app.UiController.DeliveryDetailsController;
import com.gierza_molases.molases_app.UiController.NewDeliveryController;
import com.gierza_molases.molases_app.UiController.ProductsController;
import com.gierza_molases.molases_app.dao.BranchDao;
import com.gierza_molases.molases_app.dao.BranchDeliveryDao;
import com.gierza_molases.molases_app.dao.CustomerDao;
import com.gierza_molases.molases_app.dao.CustomerDeliveryDao;
import com.gierza_molases.molases_app.dao.DeliveryDao;
import com.gierza_molases.molases_app.dao.ProductAssociationDao;
import com.gierza_molases.molases_app.dao.ProductDao;
import com.gierza_molases.molases_app.service.BranchService;
import com.gierza_molases.molases_app.service.CustomerService;
import com.gierza_molases.molases_app.service.DeliveryService;
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
	private static DeliveryService deliveryService;

	// Controllers
	public static CustomersController customersController;
	public static BranchesController branchesController;
	public static ProductsController productsController;

	public static DeliveryController deliveryController;
	public static NewDeliveryController newDeliveryController;
	public static DeliveryDetailsController deliveryDetialsController;

	public static void init() {
		// DB connection
		conn = Database.init();
		DatabaseInitializer.init();

		// DAO instance
		CustomerDao customerDao = new CustomerDao(conn);
		BranchDao branchDao = new BranchDao(conn);
		ProductDao productDao = new ProductDao(conn);
		ProductAssociationDao productAssociationDao = new ProductAssociationDao(conn);

		// Delivery
		DeliveryDao deliveryDao = new DeliveryDao(conn);
		CustomerDeliveryDao customerDeliveryDao = new CustomerDeliveryDao(conn);
		BranchDeliveryDao branchDeliveryDao = new BranchDeliveryDao(conn);

		// service instance
		customerService = new CustomerService(customerDao, branchDao);
		branchService = new BranchService(branchDao, customerDao, branchDeliveryDao);
		productService = new ProductService(productDao, productAssociationDao);
		productAssociationService = new ProductAssociationService(productAssociationDao);
		deliveryService = new DeliveryService(deliveryDao, customerDeliveryDao, branchDeliveryDao, customerDao,
				branchDao, productDao);

		// Initialize controller with a new state instance
		customersController = new CustomersController(new CustomerState(), customerService);
		branchesController = new BranchesController(new BranchState(), branchService);
		productsController = new ProductsController(new ProductState(), productService, productAssociationService);

		deliveryController = new DeliveryController(new DeliveryState(), deliveryService);
		newDeliveryController = new NewDeliveryController(new NewDeliveryState(), customersController,
				branchesController, productsController, deliveryService);
		deliveryDetialsController = new DeliveryDetailsController(new DeliveryDetailsState(), deliveryService);

	}

	public static Connection getConnection() {
		return conn;
	}
}