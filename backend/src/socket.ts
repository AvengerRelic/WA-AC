import { Server, Socket } from 'socket.io';
import { PrismaClient } from '@prisma/client';
import jwt from 'jsonwebtoken';

const prisma = new PrismaClient();
const JWT_SECRET = process.env.JWT_SECRET || 'fallback_secret';

export const setupSocketServer = (io: Server) => {
    io.use((socket, next) => {
        const token = socket.handshake.auth.token;
        if (!token) {
            return next(new Error('Authentication error'));
        }
        jwt.verify(token, JWT_SECRET, (err: any, decoded: any) => {
            if (err) return next(new Error('Authentication error'));
            socket.data.userId = decoded.userId;
            next();
        });
    });

    io.on('connection', (socket: Socket) => {
        console.log(`User connected: ${socket.data.userId} (socket id: ${socket.id})`);

        // Users join a personal room with their userId to receive direct messages
        socket.join(socket.data.userId);

        socket.on('join_chat', async (chatId: string) => {
            socket.join(chatId);
            console.log(`User ${socket.data.userId} joined chat ${chatId}`);
        });

        socket.on('send_message', async (data: { chatId: string; content?: string; mediaUrl?: string; receiverId?: string }) => {
            try {
                let chat;
                let finalChatId = data.chatId;

                // Create direct chat if it doesn't exist yet and receiverId is provided
                if (!data.chatId && data.receiverId) {
                    chat = await prisma.chat.findFirst({
                        where: {
                            isGroup: false,
                            AND: [
                                { participants: { some: { id: socket.data.userId } } },
                                { participants: { some: { id: data.receiverId } } }
                            ]
                        }
                    });

                    if (!chat) {
                        chat = await prisma.chat.create({
                            data: {
                                isGroup: false,
                                participants: {
                                    connect: [{ id: socket.data.userId }, { id: data.receiverId }]
                                }
                            }
                        });
                    }
                    finalChatId = chat.id;
                }

                if (!finalChatId) return;

                const messageData: any = {
                    senderId: socket.data.userId,
                    chatId: finalChatId,
                };

                if (data.content) messageData.content = data.content;
                if (data.mediaUrl) messageData.mediaUrl = data.mediaUrl;

                const message = await prisma.message.create({
                    data: messageData,
                    include: {
                        sender: { select: { id: true, name: true, phone: true } }
                    }
                });

                // Broadcast to everyone in the chat room (or to the specific user if direct socket routing is preferred)
                // Here we send it to the room which might just be the chat id, but we also send it to the receiver's personal room
                if (data.receiverId) {
                    io.to(data.receiverId).emit('receive_message', message);
                    io.to(socket.data.userId).emit('receive_message', message); // also send back to sender for confirmation
                } else {
                    io.to(finalChatId).emit('receive_message', message);
                }

            } catch (err) {
                console.error('Error sending message via socket:', err);
            }
        });

        socket.on('typing', (data: { receiverId: string }) => {
            io.to(data.receiverId).emit('user_typing', { senderId: socket.data.userId });
        });

        socket.on('stop_typing', (data: { receiverId: string }) => {
            io.to(data.receiverId).emit('user_stopped_typing', { senderId: socket.data.userId });
        });

        socket.on('disconnect', () => {
            console.log('User disconnected:', socket.id);
        });
    });
};
