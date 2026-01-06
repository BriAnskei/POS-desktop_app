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




-- FTS5
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


