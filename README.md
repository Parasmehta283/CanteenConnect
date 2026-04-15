# CanteenConnect - College Canteen Ordering System

A fully functional Android application built with Kotlin and Jetpack Compose.

## Setup Instructions

1. Create a new Android project in Android Studio
2. Copy all source files into the project
3. Add Firebase to your project:
   - Go to Firebase Console → Add Project
   - Add Android app with your package name
   - Download `google-services.json` → place in `app/` folder
   - Enable Firestore, Authentication, and Realtime Database

## Project Structure

```
app/
├── src/main/java/com/canteenconnect/
│   ├── data/
│   │   ├── model/          # Data classes
│   │   ├── repository/     # Firebase repositories
│   │   └── firebase/       # Firebase service
│   ├── ui/
│   │   ├── auth/           # Login screen
│   │   ├── student/        # Student screens
│   │   ├── staff/          # Staff screens
│   │   ├── admin/          # Admin screens
│   │   └── theme/          # App theme
│   ├── viewmodel/          # ViewModels
│   └── navigation/         # Navigation graph
└── src/main/res/
```

## Default Test Accounts

| Role    | Email               | Password   |
|---------|---------------------|------------|
| Student | student@test.com    | pass123    |
| Staff   | staff@test.com      | pass123    |
| Admin   | admin@test.com      | pass123    |

> Create these accounts in Firebase Auth + add role in Firestore `users` collection.

## Firestore Structure

```
users/{uid}
  - name: String
  - email: String
  - role: "student" | "staff" | "admin"

menuItems/{itemId}
  - name: String
  - price: Double
  - available: Boolean
  - category: String

orders/{orderId}
  - studentId: String
  - studentName: String
  - tokenNumber: Int
  - status: "PLACED" | "PREPARING" | "READY" | "COMPLETED"
  - totalAmount: Double
  - createdAt: Timestamp
  - items: List<OrderItem>

tokens/daily
  - date: String (YYYY-MM-DD)
  - counter: Int
```
