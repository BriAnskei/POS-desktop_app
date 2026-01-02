PRAGMA foreign_keys = ON;

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

 -- csutomer indexis
CREATE INDEX IF NOT EXISTS idx_customer_display_name
ON customer(display_name);

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
