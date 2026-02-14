package com.gierza_molases.molases_app.context;

import java.sql.Connection;

import com.gierza_molases.molases_app.UiController.BranchesController;
import com.gierza_molases.molases_app.UiController.CustomerPaymentController;
import com.gierza_molases.molases_app.UiController.CustomerPaymentViewController;
import com.gierza_molases.molases_app.UiController.CustomersController;
import com.gierza_molases.molases_app.UiController.DashboardController;
import com.gierza_molases.molases_app.UiController.DeliveryController;
import com.gierza_molases.molases_app.UiController.DeliveryDetailsController;
import com.gierza_molases.molases_app.UiController.LoginController;
import com.gierza_molases.molases_app.UiController.MaintenanceController;
import com.gierza_molases.molases_app.UiController.NewDeliveryController;
import com.gierza_molases.molases_app.UiController.ProductsController;
import com.gierza_molases.molases_app.dao.AuthDao;
import com.gierza_molases.molases_app.dao.BranchDao;
import com.gierza_molases.molases_app.dao.BranchDeliveryDao;
import com.gierza_molases.molases_app.dao.CustomerDao;
import com.gierza_molases.molases_app.dao.CustomerDeliveryDao;
import com.gierza_molases.molases_app.dao.CustomerPaymentDao;
import com.gierza_molases.molases_app.dao.DashboardDao;
import com.gierza_molases.molases_app.dao.DeliveryDao;
import com.gierza_molases.molases_app.dao.MaintenanceDAO;
import com.gierza_molases.molases_app.dao.PaymentHistoryDao;
import com.gierza_molases.molases_app.dao.ProductAssociationDao;
import com.gierza_molases.molases_app.dao.ProductDao;
import com.gierza_molases.molases_app.dao.ProductDeliveryDao;
import com.gierza_molases.molases_app.service.AuthService;
import com.gierza_molases.molases_app.service.BranchDeliveryService;
import com.gierza_molases.molases_app.service.BranchService;
import com.gierza_molases.molases_app.service.CustomerDeliveryService;
import com.gierza_molases.molases_app.service.CustomerPaymentsService;
import com.gierza_molases.molases_app.service.CustomerService;
import com.gierza_molases.molases_app.service.DashboardService;
import com.gierza_molases.molases_app.service.DeliveryService;
import com.gierza_molases.molases_app.service.MaintenanceService;
import com.gierza_molases.molases_app.service.ProductAssociationService;
import com.gierza_molases.molases_app.service.ProductService;
import com.gierza_molases.molases_app.util.Database;
import com.gierza_molases.molases_app.util.DatabaseInitializer;

public class AppContext {
	private static Connection conn;

	// Services
	public static DashboardService dashBoardService;
	public static CustomerService customerService;
	public static BranchService branchService;
	public static ProductService productService;
	public static ProductAssociationService productAssociationService;
	private static DeliveryService deliveryService;
	private static CustomerDeliveryService customerDeliveryService;
	private static BranchDeliveryService branchDeliveryService;
	private static CustomerPaymentsService customerPaymentsService;

	private static MaintenanceService maintenanceService;

	private static AuthService authService;

	// Controllers
	public static CustomersController customersController;
	public static BranchesController branchesController;
	public static ProductsController productsController;
	public static DeliveryController deliveryController;
	public static NewDeliveryController newDeliveryController;
	public static DeliveryDetailsController deliveryDetialsController;
	public static CustomerPaymentController customerPaymentController;
	public static CustomerPaymentViewController customerPaymentViewController;
	public static DashboardController dashboardController;
	public static MaintenanceController maintenanceController;

	public static LoginController loginController;

	public static void init() {
		// DB connection
		conn = Database.init();
		DatabaseInitializer.init();

		// DAO instance
		DashboardDao dashBoardDao = new DashboardDao(conn);

		CustomerDao customerDao = new CustomerDao(conn);
		BranchDao branchDao = new BranchDao(conn);
		ProductDao productDao = new ProductDao(conn);
		ProductAssociationDao productAssociationDao = new ProductAssociationDao(conn);

		// Delivery
		DeliveryDao deliveryDao = new DeliveryDao(conn);
		CustomerDeliveryDao customerDeliveryDao = new CustomerDeliveryDao(conn);
		BranchDeliveryDao branchDeliveryDao = new BranchDeliveryDao(conn);
		ProductDeliveryDao productDeliveryDao = new ProductDeliveryDao(conn);

		// Payments
		CustomerPaymentDao customerPaymentsDao = new CustomerPaymentDao(conn);
		PaymentHistoryDao paymentHistoryDao = new PaymentHistoryDao(conn);

		// Maintenance
		MaintenanceDAO maintenanceDAO = new MaintenanceDAO(conn);

		AuthDao authDao = new AuthDao(conn);

		// service instance
		dashBoardService = new DashboardService(dashBoardDao);

		customerService = new CustomerService(customerDao, branchDao);
		branchService = new BranchService(branchDao, customerDao, branchDeliveryDao);
		productService = new ProductService(productDao, productAssociationDao);
		productAssociationService = new ProductAssociationService(productAssociationDao);
		deliveryService = new DeliveryService(deliveryDao, customerDeliveryDao, branchDeliveryDao, productDeliveryDao,
				customerDao, branchDao, productDao, customerPaymentsDao, paymentHistoryDao);
		customerDeliveryService = new CustomerDeliveryService(customerDeliveryDao, branchDeliveryDao);
		branchDeliveryService = new BranchDeliveryService(branchDeliveryDao);
		customerPaymentsService = new CustomerPaymentsService(customerPaymentsDao, paymentHistoryDao);

		maintenanceService = new MaintenanceService(maintenanceDAO);
		authService = new AuthService(authDao);

		// Initialize controller with a new state instance
		customersController = new CustomersController(new CustomerState(), customerService);
		branchesController = new BranchesController(new BranchState(), branchService);
		productsController = new ProductsController(new ProductState(), productService, productAssociationService);

		deliveryController = new DeliveryController(new DeliveryState(), deliveryService);
		newDeliveryController = new NewDeliveryController(new NewDeliveryState(), customersController,
				branchesController, productsController, deliveryService);
		deliveryDetialsController = new DeliveryDetailsController(new DeliveryDetailsState(), deliveryService);

		customerPaymentController = new CustomerPaymentController(new CustomerPaymentState(), customerPaymentsService);
		customerPaymentViewController = new CustomerPaymentViewController(new CustomerPaymentViewState(),
				customerPaymentsService);

		dashboardController = new DashboardController(dashBoardService, new DashboardState());
		maintenanceController = new MaintenanceController(maintenanceService);

		loginController = new LoginController(authService);

	}

	public static Connection getConnection() {
		return conn;
	}
}