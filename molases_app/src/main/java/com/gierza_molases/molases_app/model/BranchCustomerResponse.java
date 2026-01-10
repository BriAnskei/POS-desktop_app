package com.gierza_molases.molases_app.model;

public class BranchCustomerResponse {
    private Branch branch;
    private Customer customer;
    
    // Constructors
    public BranchCustomerResponse() {}
    
    public BranchCustomerResponse(Branch branch, Customer customer) {
        this.branch = branch;
        this.customer = customer;
    }
    
    // Getters and setters
    public Branch getBranch() { return branch; }
    public void setBranch(Branch branch) { this.branch = branch; }
    
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
}