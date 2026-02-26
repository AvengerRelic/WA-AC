# WhatsApp Clone (Web & Android)

A full-stack, real-time messaging application styled to match the true WhatsApp experience. This clone utilizes a React Native (Expo) frontend for seamless web and mobile support, powered by a Node.js + Socket.IO backend.

## Features

- **Real-Time Messaging**: Instant chatting powered by WebSockets (Socket.IO).
- **Authentication**: Secure JWT-based login and registration (requires a 10-digit phone number).
- **Contacts & Chat List**: Filter chats by unread, favorites, or groups. 
- **Groups**: Create custom groups and chat with multiple people at once.
- **Media Attachments**: Open device camera or gallery to instantly send pictures in chats.
- **WhatsApp UI/UX**: Authentic WhatsApp styling, including message bubbles, typing indicators (`...is typing`), delivery ticks, and the classic `+` FAB.

---

## Tech Stack

### Frontend (`android-client`)
- **Framework**: React Native (via Expo)
- **Routing**: React Navigation
- **Networking**: Axios (HTTP APIs), Socket.IO-Client (WebSockets)
- **Media**: Expo Image Picker

### Backend (`backend`)
- **Runtime**: Node.js & Express
- **Real-Time Engine**: Socket.IO
- **Database**: PostgreSQL (Neon Serverless Db) via Prisma ORM
- **Security**: JWT Authentication & Bcrypt Password Hashing

---

## Getting Started Locally

To run this clone on your local machine, you will need two terminal windows running simultaneously.

### 1. Start the Backend API Server
Navigate to the `backend` folder, install the dependencies, and start the development server.

```bash
cd backend
npm install
npm run dev
# Server will start on http://localhost:5000
```

> **Note**: Ensure you have a `.env` file configured in the `backend` root with your Neon Postgres `DATABASE_URL` and a secret `JWT_SECRET`.

### 2. Start the Frontend Expo App
Open a new terminal, navigate to the `android-client` folder, install dependencies, and launch the Expo bundler for the web interface.

```bash
cd android-client
npm install
npx expo start --web
```
This will automatically open the application in your browser at `http://localhost:8081`. 

### Mobile Testing
If you wish to test on a physical phone, download the **Expo Go** app on your iOS/Android device. Stop the web process above, and simply run `npx expo start`. Scan the resulting QR terminal code with your phone!

---

## Deployment (Vercel)
The root includes a `vercel.json` configured to build the Expo Web Client. 
If you are deploying for the first time:
1. Run `vercel` globally via CLI from the `android-client` directory.
2. Authenticate through the browser popup.
3. Link the automatic Vercel build to the `main` GitHub branch.
