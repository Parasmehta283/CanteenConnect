/**
 * Firebase Seed Script (Node.js)
 * Run: node firebase_seed.js
 *
 * Prerequisites:
 *   npm install firebase-admin
 *   Download service account key from Firebase Console
 */

const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json'); // Download from Firebase Console

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();
const auth = admin.auth();

async function seed() {
  console.log('🌱 Seeding CanteenConnect...\n');

  // ── Create users ───────────────────────────────────────────────────────────
  const users = [
    { email: 'student@test.com', password: 'pass123', name: 'Rahul Kumar', role: 'student' },
    { email: 'staff@test.com',   password: 'pass123', name: 'Priya Sharma', role: 'staff' },
    { email: 'admin@test.com',   password: 'pass123', name: 'Dr. Mehta',    role: 'admin' },
  ];

  for (const user of users) {
    try {
      const created = await auth.createUser({ email: user.email, password: user.password, displayName: user.name });
      await db.collection('users').doc(created.uid).set({
        name: user.name,
        email: user.email,
        role: user.role
      });
      console.log(`✅ Created ${user.role}: ${user.email}`);
    } catch (e) {
      console.log(`⚠️  ${user.email}: ${e.message}`);
    }
  }

  // ── Menu items ────────────────────────────────────────────────────────────
  const menu = [
    { name: 'Veg Thali',       price: 60,  available: true,  category: 'Main' },
    { name: 'Chicken Biryani', price: 90,  available: true,  category: 'Main' },
    { name: 'Dal Tadka',       price: 45,  available: true,  category: 'Main' },
    { name: 'Paneer Butter',   price: 80,  available: true,  category: 'Main' },
    { name: 'Samosa (2 pcs)',  price: 20,  available: true,  category: 'Snacks' },
    { name: 'Vada Pav',        price: 15,  available: true,  category: 'Snacks' },
    { name: 'Bread Omelette',  price: 30,  available: true,  category: 'Snacks' },
    { name: 'Masala Chai',     price: 10,  available: true,  category: 'Beverages' },
    { name: 'Cold Coffee',     price: 35,  available: true,  category: 'Beverages' },
    { name: 'Fresh Lime Soda', price: 25,  available: true,  category: 'Beverages' },
    { name: 'Gulab Jamun',     price: 20,  available: true,  category: 'Desserts' },
    { name: 'Ice Cream Cup',   price: 30,  available: false, category: 'Desserts' },
  ];

  const batch = db.batch();
  for (const item of menu) {
    const ref = db.collection('menuItems').doc();
    batch.set(ref, { ...item, id: ref.id });
  }
  await batch.commit();
  console.log(`\n✅ Added ${menu.length} menu items`);

  // ── Token counter ──────────────────────────────────────────────────────────
  await db.collection('tokens').doc('daily').set({
    date: '',
    counter: 0
  });
  console.log('✅ Token counter initialized');

  console.log('\n🎉 Seeding complete! You can now log in with:');
  console.log('   Student: student@test.com / pass123');
  console.log('   Staff:   staff@test.com / pass123');
  console.log('   Admin:   admin@test.com / pass123');

  process.exit(0);
}

seed().catch(e => { console.error(e); process.exit(1); });
