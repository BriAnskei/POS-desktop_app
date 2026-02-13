package com.gierza_molases.molases_app.service;

import java.sql.SQLException;
import java.util.function.IntConsumer;

import com.gierza_molases.molases_app.dao.MaintenanceDAO;
import com.gierza_molases.molases_app.model.response.DeletionPreview;

public class MaintenanceService {
	private final MaintenanceDAO maintenanceDAO;

	public MaintenanceService(MaintenanceDAO maintenanceDAO) {
		this.maintenanceDAO = maintenanceDAO;
	}

	public int previewDeletion(int yearsOld) throws SQLException {
		return maintenanceDAO.previewDeletion(yearsOld);
	}

	public int deleteOldCompletedDeliveries(int yearsOld, IntConsumer progressCallback) throws SQLException {

		return maintenanceDAO.deleteOldCompletedDeliveries(yearsOld, progressCallback);
	}

	public DeletionPreview getDetailedPreview(int yearsOld) throws SQLException {
		return maintenanceDAO.getDetailedPreview(yearsOld);
	}

}
