
PRAGMA foreign_keys = ON;



CREATE TABLE IF NOT EXISTS user (
	user_name TEXT,
	password TEXT
);


-- Customer table
CREATE TABLE IF NOT EXISTS customer (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name TEXT,
    mid_name TEXT,
    last_name TEXT,
    company_name TEXT,
	display_name TEXT NOT NULL,
    type TEXT CHECK(type IN ('individual', 'company')) NOT NULL,
    contact_number TEXT,
    address TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    -- Conditional constraint
    CHECK (
        (type = 'individual' AND first_name IS NOT NULL AND last_name IS NOT NULL)
        OR
        (type = 'company' AND company_name IS NOT NULL)
    )
);
 -- customer indexis
CREATE INDEX IF NOT EXISTS idx_customer_created_at
ON customer(created_at DESC);

 



-- Branches table
CREATE TABLE IF NOT EXISTS branches (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id INTEGER NOT NULL,
    address TEXT NOT NULL,
    note TEXT,
    created_at TEXT DEFAULT (datetime('now')),
    FOREIGN KEY (customer_id)
        REFERENCES customer(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

 -- Branches indexis
 -- For pagination
CREATE INDEX IF NOT EXISTS idx_branches_id
ON branches(id);

-- For join
CREATE INDEX IF NOT EXISTS idx_branches_customer_id
ON branches(customer_id);




-- FTS5 (Customer, Branch)
CREATE VIRTUAL TABLE IF NOT EXISTS customer_fts
USING fts5(
    display_name,
    content='customer',
    content_rowid='id',
    prefix='2 3 4'
);




-- triggers
-- After INSERT
CREATE TRIGGER IF NOT EXISTS customer_ai
AFTER INSERT ON customer
BEGIN
  INSERT INTO customer_fts(rowid, display_name)
  VALUES (new.id, new.display_name);
END;

-- After DELETE
CREATE TRIGGER IF NOT EXISTS customer_ad
AFTER DELETE ON customer
BEGIN
  INSERT INTO customer_fts(customer_fts, rowid)
  VALUES ('delete', old.id);
END;

-- After UPDATE
CREATE TRIGGER IF NOT EXISTS customer_au
AFTER UPDATE ON customer
BEGIN
  INSERT INTO customer_fts(customer_fts, rowid)
  VALUES ('delete', old.id);

  INSERT INTO customer_fts(rowid, display_name)
  VALUES (new.id, new.display_name);
END;





-- Product table
CREATE TABLE IF NOT EXISTS product (
    id INTEGER PRIMARY KEY AUTOINCREMENT,     


    product_name TEXT NOT NULL,
    selling_price REAL NOT NULL CHECK (selling_price >= 0),
    capital REAL NOT NULL CHECK (capital >= 0),

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
 
);


CREATE INDEX IF NOT EXISTS idx_product_name ON product(product_name);
CREATE INDEX IF NOT EXISTS idx_product_created_at ON product(created_at);





-- Procduct association
CREATE TABLE IF NOT EXISTS product_association (
    id INTEGER PRIMARY KEY AUTOINCREMENT,

    customer_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    
    UNIQUE (customer_id, product_id), -- customer cannot be asscociated on the same product more than once


    FOREIGN KEY (customer_id)
        REFERENCES customer(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    FOREIGN KEY (product_id)
        REFERENCES product(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);  

CREATE INDEX IF NOT EXISTS idx_product_assoc_product ON product_association(product_id);



-- Delivery
CREATE TABLE IF NOT EXISTS delivery (
    id INTEGER PRIMARY KEY AUTOINCREMENT,

    schedule_date DATETIME, 	
    name TEXT NOT NULL,
    expenses TEXT,
    status TEXT,  --scheduled / delivered
    
    total_customers REAL NOT NULL,
    total_branches REAL NOT NULL,
    
    
    total_gross REAL NOT NULL,
    total_capital REAL NOT NULL,
    gross_profit REAL NOT NULL,
    total_expenses REAL NOT NUll,
    net_profit REAL NOT NULL,

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX  IF NOT EXISTS  idx_deliveries_created_at
ON delivery(created_at);

CREATE INDEX IF NOT EXISTS idx_delivery_name
ON delivery(name);

CREATE INDEX IF NOT EXISTS idx_delivery_schedule_date
ON delivery(schedule_date);








CREATE TABLE IF NOT EXISTS customer_delivery (
    id INTEGER PRIMARY KEY AUTOINCREMENT,   -- UniqueID (PK)
    
    customer_id INTEGER NOT NULL,           -- FK
    delivery_id INTEGER NOT NULL,           -- FK
    
     status TEXT, 							-- cancelled / delivered
    
    UNIQUE (customer_id, delivery_id), -- most be unique 


    FOREIGN KEY (customer_id)
        REFERENCES customer(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    FOREIGN KEY (delivery_id)
        REFERENCES delivery(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_customer_delivery_delivery
ON customer_delivery (delivery_id);




CREATE TABLE IF NOT EXISTS branch_delivery (
    id INTEGER PRIMARY KEY AUTOINCREMENT,   

    customer_delivery_id INTEGER NOT NULL,  
    branch_id INTEGER NOT NULL,          

    status TEXT,

    FOREIGN KEY (customer_delivery_id)
        REFERENCES customer_delivery(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    FOREIGN KEY (branch_id)
        REFERENCES branches (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE

);


CREATE INDEX  IF NOT EXISTS  idx_branch_delivery_customer_delivery
    ON branch_delivery(customer_delivery_id);

CREATE INDEX  IF NOT EXISTS  idx_branch_delivery_branch
    ON branch_delivery(branch_id);








CREATE TABLE IF NOT EXISTS product_delivery (
    id INTEGER PRIMARY KEY AUTOINCREMENT,   

    branch_delivery_id INTEGER NOT NULL,  
    product_id INTEGER NOT NULL,          
   
    quantity INTEGER NOT NULL CHECK (quantity >= 0),
 

    FOREIGN KEY (branch_delivery_id)
        REFERENCES branch_delivery(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,


    FOREIGN KEY (product_id)
        REFERENCES product(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE INDEX  IF NOT EXISTS  idx_product_delivery_branch
    ON product_delivery(branch_delivery_id);

CREATE INDEX  IF NOT EXISTS  idx_product_delivery
    ON product_delivery(product_id);



CREATE TABLE IF NOT EXISTS customer_payments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,

    customer_id INTEGER NOT NULL,
    customer_delivery_id INTEGER,

    payment_type TEXT NOT NULL, -- Paid Cash, Pad Cheque, Partial, Loan(with promise to pay)
    
    status TEXT,   -- complete, pending
    notes TEXT,

    total REAL NOT NULL,
    total_payment REAL NOT NULL,  -- over all payment
    
    promise_to_pay DATE,  -- only be filled on load payment type
  	created_at DATETIME DEFAULT CURRENT_TIMESTAMP,


    FOREIGN KEY (customer_id) REFERENCES customer(id)    
    	ON UPDATE CASCADE
        ON DELETE CASCADE,
      
        
    FOREIGN KEY (customer_delivery_id) REFERENCES customer_delivery(id)
         ON UPDATE CASCADE
        ON DELETE CASCADE
);


CREATE INDEX IF NOT EXISTS idx_customer_payments_created_at
ON customer_payments (created_at);




CREATE TABLE IF NOT EXISTS payment_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_payment_id INTEGER NOT NULL,
    amount REAL NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (customer_payment_id)
        REFERENCES customer_payments (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);
    

