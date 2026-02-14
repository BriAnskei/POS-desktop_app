package com.gierza_molases.molases_app.UiController;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import javax.swing.SwingWorker;

import com.gierza_molases.molases_app.model.response.DeletionPreview;
import com.gierza_molases.molases_app.service.MaintenanceService;

public class MaintenanceController {

	private final MaintenanceService maintenanceService;

	public MaintenanceController(MaintenanceService maintenanceService) {
		this.maintenanceService = maintenanceService;
	}

	/**
	 * Validate password for maintenance operations
	 * 
	 * @param password The password to validate
	 * @return true if password is valid, false otherwise
	 */
	public boolean validatePassword(String password) {
		return maintenanceService.verifyAdminPassword(password);
	}

	/**
	 * Preview how many deliveries would be deleted
	 * 
	 * @param yearsOld  Number of years to look back
	 * @param onSuccess Callback with the count of deliveries
	 * @param onError   Callback with error message
	 */
	public void previewDeletion(int yearsOld, Consumer<Integer> onSuccess, Consumer<String> onError) {
		new SwingWorker<Integer, Void>() {
			private Exception error;

			@Override
			protected Integer doInBackground() {
				try {
					return maintenanceService.previewDeletion(yearsOld);
				} catch (Exception e) {
					error = e;
					return 0;
				}
			}

			@Override
			protected void done() {
				if (error != null) {
					error.printStackTrace();
					if (onError != null) {
						onError.accept("Failed to preview deletion: " + error.getMessage());
					}
				} else {
					try {
						Integer count = get();
						if (onSuccess != null) {
							onSuccess.accept(count);
						}
					} catch (Exception e) {
						e.printStackTrace();
						if (onError != null) {
							onError.accept("Error processing preview results: " + e.getMessage());
						}
					}
				}
			}
		}.execute();
	}

	/**
	 * Get detailed preview of what will be deleted
	 * 
	 * @param yearsOld  Number of years to look back
	 * @param onSuccess Callback with DeletionPreview object
	 * @param onError   Callback with error message
	 */
	public void getDetailedPreview(int yearsOld, Consumer<DeletionPreview> onSuccess, Consumer<String> onError) {
		new SwingWorker<DeletionPreview, Void>() {
			private Exception error;

			@Override
			protected DeletionPreview doInBackground() {
				try {
					return maintenanceService.getDetailedPreview(yearsOld);
				} catch (Exception e) {
					error = e;
					return null;
				}
			}

			@Override
			protected void done() {
				if (error != null) {
					error.printStackTrace();
					if (onError != null) {
						onError.accept("Failed to get detailed preview: " + error.getMessage());
					}
				} else {
					try {
						DeletionPreview preview = get();
						if (onSuccess != null) {
							onSuccess.accept(preview);
						}
					} catch (Exception e) {
						e.printStackTrace();
						if (onError != null) {
							onError.accept("Error processing detailed preview: " + e.getMessage());
						}
					}
				}
			}
		}.execute();
	}

	/**
	 * Delete old completed deliveries and all related records
	 * 
	 * @param yearsOld  Number of years to look back
	 * @param onSuccess Callback with the count of deleted deliveries
	 * @param onError   Callback with error message
	 */
	public void deleteOldCompletedDeliveries(int yearsOld, Consumer<Integer> onSuccess, Consumer<String> onError,
			IntConsumer onProgress) {
		new SwingWorker<Integer, Integer>() {
			private Exception error;

			@Override
			protected Integer doInBackground() {
				try {
					return maintenanceService.deleteOldCompletedDeliveries(yearsOld, progress -> publish(progress));
				} catch (Exception e) {
					error = e;
					return 0;
				}
			}

			@Override
			protected void process(List<Integer> chunks) {
// Update progress on EDT
				if (onProgress != null && !chunks.isEmpty()) {
					int latestProgress = chunks.get(chunks.size() - 1);
					onProgress.accept(latestProgress);
				}
			}

			@Override
			protected void done() {
				if (error != null) {
					error.printStackTrace();
					if (onError != null) {
						String errorMessage = "Database error occurred during deletion";
						if (error.getMessage() != null && !error.getMessage().isEmpty()) {
							errorMessage += ": " + error.getMessage();
						}
						onError.accept(errorMessage);
					}
				} else {
					try {
						Integer deletedCount = get();
						if (onSuccess != null) {
							onSuccess.accept(deletedCount);
						}
					} catch (Exception e) {
						e.printStackTrace();
						if (onError != null) {
							onError.accept("Error processing deletion results: " + e.getMessage());
						}
					}
				}
			}
		}.execute();
	}
}