import fs from 'fs';
import path from 'path';
import { query } from './db.js';

const runMigrations = async () => {
    try {
        console.log('🔄 Starting database migrations...');

        // Read schema file
        const schemaPath = path.join(process.cwd(), 'src/database/schema.sql');
        const schema = fs.readFileSync(schemaPath, 'utf-8');

        // Split schema into individual statements
        const statements = schema.split(';').filter(stmt => stmt.trim());

        // Execute each statement
        for (const statement of statements) {
            if (statement.trim()) {
                try {
                    await query(statement);
                    console.log('✅ Executed:', statement.substring(0, 50) + '...');
                } catch (error) {
                    // Ignore "already exists" errors
                    if (!error.message.includes('already exists')) {
                        console.error('❌ Error executing statement:', error.message);
                    }
                }
            }
        }

        console.log('✅ Database migrations completed successfully!');
        process.exit(0);
    } catch (error) {
        console.error('❌ Migration failed:', error);
        process.exit(1);
    }
};

runMigrations();
