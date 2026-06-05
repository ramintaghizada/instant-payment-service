# Drop and recreate the database
docker exec -it payment-postgres psql -U payment_user -d postgres << 'EOF'
-- Terminate all connections
SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'payment_db';

-- Drop and recreate database
DROP DATABASE IF EXISTS payment_db;
CREATE DATABASE payment_db OWNER payment_user;
EOF

echo "✅ Database recreated!"