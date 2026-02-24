import { Router } from 'express';
import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import { PrismaClient } from '@prisma/client';

const router = Router();
const prisma = new PrismaClient();
const JWT_SECRET = process.env.JWT_SECRET || 'fallback_secret';

router.post('/register', async (req, res) => {
    try {
        const { name, phone, password } = req.body;

        if (!name || !phone || !password) {
            return res.status(400).json({ error: 'All fields are required' });
        }

        const existingUser = await prisma.user.findUnique({ where: { phone } });
        if (existingUser) {
            return res.status(400).json({ error: 'Phone number already registered' });
        }

        const hashedPassword = await bcrypt.hash(password, 10);

        const user = await prisma.user.create({
            data: {
                name,
                phone,
                password: hashedPassword,
            },
        });

        const token = jwt.sign({ userId: user.id }, JWT_SECRET, { expiresIn: '7d' });

        res.status(201).json({ user: { id: user.id, name: user.name, phone: user.phone }, token });
    } catch (error) {
        console.error('Registration error:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

router.post('/login', async (req, res) => {
    try {
        const { phone, password } = req.body;

        if (!phone || !password) {
            return res.status(400).json({ error: 'Phone and password are required' });
        }

        const user = await prisma.user.findUnique({ where: { phone } });
        if (!user) {
            return res.status(400).json({ error: 'Invalid credentials' });
        }

        const isMatch = await bcrypt.compare(password, user.password);
        if (!isMatch) {
            return res.status(400).json({ error: 'Invalid credentials' });
        }

        const token = jwt.sign({ userId: user.id }, JWT_SECRET, { expiresIn: '7d' });

        res.json({ user: { id: user.id, name: user.name, phone: user.phone }, token });
    } catch (error) {
        console.error('Login error:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

router.get('/users', async (req, res) => {
    try {
        const users = await prisma.user.findMany({
            select: { id: true, name: true, phone: true }
        });
        res.json(users);
    } catch (error) {
        res.status(500).json({ error: 'Failed to fetch users' });
    }
});

router.get('/chats', async (req, res) => {
    try {
        const authHeader = req.headers.authorization;
        if (!authHeader) return res.status(401).json({ error: 'Unauthorized' });
        const token = authHeader.split(' ')[1];
        const decoded: any = jwt.verify(token, JWT_SECRET);

        const chats = await prisma.chat.findMany({
            where: {
                participants: {
                    some: { id: decoded.userId }
                }
            },
            include: {
                participants: {
                    select: { id: true, name: true, phone: true }
                },
                messages: {
                    orderBy: { createdAt: 'desc' },
                    take: 1
                }
            },
            orderBy: { updatedAt: 'desc' }
        });

        const formattedChats = chats.map(chat => {
            const otherUser = chat.participants.find(p => p.id !== decoded.userId) || chat.participants[0];
            const lastMessage = chat.messages.length > 0 ? chat.messages[0].content : '';
            return {
                id: chat.id,
                isGroup: chat.isGroup,
                name: chat.name,
                receiver: chat.isGroup ? { id: chat.id, name: chat.name, isGroup: true } : otherUser,
                lastMessage,
                updatedAt: chat.updatedAt
            };
        });

        res.json(formattedChats);
    } catch (error) {
        res.status(500).json({ error: 'Failed to fetch chats' });
    }
});

router.post('/groups', async (req, res) => {
    try {
        const authHeader = req.headers.authorization;
        if (!authHeader) return res.status(401).json({ error: 'Unauthorized' });
        const token = authHeader.split(' ')[1];
        const decoded: any = jwt.verify(token, JWT_SECRET);

        const { name, participantIds } = req.body; // array of user IDs

        if (!name || !participantIds || participantIds.length === 0) {
            return res.status(400).json({ error: 'Group name and participants are required' });
        }

        // Add the creator (current user) to the participants
        const allParticipants = [...participantIds, decoded.userId].map(id => ({ id }));

        const group = await prisma.chat.create({
            data: {
                isGroup: true,
                name: name,
                participants: {
                    connect: allParticipants
                }
            },
            include: {
                participants: { select: { id: true, name: true } }
            }
        });

        // Also want to notify via socket if possible, but HTTP response is enough for the creator
        res.status(201).json(group);
    } catch (err) {
        console.error('Error creating group:', err);
        res.status(500).json({ error: 'Internal server error' });
    }
});

router.put('/profile', async (req, res) => {
    try {
        const authHeader = req.headers.authorization;
        if (!authHeader) return res.status(401).json({ error: 'Unauthorized' });
        const token = authHeader.split(' ')[1];
        const decoded: any = jwt.verify(token, JWT_SECRET);

        const { name, status, avatarUrl } = req.body;

        const updatedUser = await prisma.user.update({
            where: { id: decoded.userId },
            data: {
                name,
                status,
                avatarUrl
            }
        });

        res.json({ id: updatedUser.id, name: updatedUser.name, status: updatedUser.status, avatarUrl: updatedUser.avatarUrl });
    } catch (error) {
        console.error('Error updating profile:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

export default router;
