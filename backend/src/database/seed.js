import bcrypt from 'bcryptjs';
import { query } from './db.js';

const seedDatabase = async () => {
    try {
        console.log('🌱 Starting database seeding...');

        // Create admin user
        const adminPassword = await bcrypt.hash('Admin@123', 10);
        
        const adminResult = await query(
            `INSERT INTO users (username, email, password, first_name, last_name, role, is_active)
             VALUES ($1, $2, $3, $4, $5, $6, $7)
             ON CONFLICT (email) DO NOTHING
             RETURNING id, username, email, role`,
            ['bms.admin', 'admin@bms.gov.ph', adminPassword, 'System', 'Administrator', 'admin', true]
        );

        if (adminResult.rows.length > 0) {
            console.log('✅ Admin user created:', adminResult.rows[0]);
        } else {
            console.log('ℹ️  Admin user already exists');
        }

        // Create sample officers
        const officers = [
            { username: 'officer.santos', email: 'santos@bms.gov.ph', first_name: 'Juan', last_name: 'Santos', badge: 'BDG001' },
            { username: 'officer.cruz', email: 'cruz@bms.gov.ph', first_name: 'Maria', last_name: 'Cruz', badge: 'BDG002' },
            { username: 'officer.reyes', email: 'reyes@bms.gov.ph', first_name: 'Pedro', last_name: 'Reyes', badge: 'BDG003' }
        ];

        for (const officer of officers) {
            const hashedPassword = await bcrypt.hash('Officer@123', 10);
            const result = await query(
                `INSERT INTO users (username, email, password, first_name, last_name, badge_number, role, is_active)
                 VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
                 ON CONFLICT (email) DO NOTHING
                 RETURNING id, username, email, role`,
                [officer.username, officer.email, hashedPassword, officer.first_name, officer.last_name, officer.badge, 'officer', true]
            );

            if (result.rows.length > 0) {
                console.log('✅ Officer created:', result.rows[0]);
                
                // Initialize officer performance
                await query(
                    'INSERT INTO officer_performance (officer_id) VALUES ($1) ON CONFLICT DO NOTHING',
                    [result.rows[0].id]
                );
            }
        }

        console.log('✅ Database seeding completed successfully!');
        process.exit(0);
    } catch (error) {
        console.error('❌ Seeding failed:', error);
        process.exit(1);
    }
};

seedDatabase();
