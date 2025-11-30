import express, { Request, Response, NextFunction } from 'express';
import axios from 'axios';
import rateLimit from 'express-rate-limit';
import helmet from 'helmet';
import cors from 'cors';

const app = express();
const PORT = process.env.GATEWAY_PORT || 3000;
const BRAND_VOICE_SERVICE_URL = process.env.BRAND_VOICE_SERVICE_URL || 'http://localhost:8080';

// Middleware
app.use(helmet());
app.use(cors());
app.use(express.json({ limit: '10mb' }));

// Rate limiting
const limiter = rateLimit({
    windowMs: 60 * 1000, // 1 minute
    max: 100, // limit each IP to 100 requests per minute
    message: 'Too many requests from this IP, please try again later.'
});

app.use('/api/', limiter);

// Request logging middleware
app.use((req: Request, res: Response, next: NextFunction) => {
    const start = Date.now();
    console.log(`[${new Date().toISOString()}] ${req.method} ${req.path}`);

    res.on('finish', () => {
        const duration = Date.now() - start;
        console.log(`[${new Date().toISOString()}] ${req.method} ${req.path} - ${res.statusCode} - ${duration}ms`);
    });

    next();
});

// Health check
app.get('/health', (req: Request, res: Response) => {
    res.json({
        status: 'healthy',
        service: 'api-gateway',
        timestamp: new Date().toISOString()
    });
});

// Proxy requests to Brand Voice Service
app.post('/api/v1/brand-voice/profiles', async (req: Request, res: Response) => {
    try {
        const response = await axios.post(
            `${BRAND_VOICE_SERVICE_URL}/api/v1/brand-voice/profiles`,
            req.body,
            {
                headers: {
                    'Content-Type': 'application/json',
                    'X-Request-ID': generateRequestId()
                },
                timeout: 10000
            }
        );

        res.status(response.status).json(response.data);
    } catch (error) {
        handleProxyError(error, res);
    }
});

app.get('/api/v1/brand-voice/profiles/:customerId', async (req: Request, res: Response) => {
    try {
        const { customerId } = req.params;

        const response = await axios.get(
            `${BRAND_VOICE_SERVICE_URL}/api/v1/brand-voice/profiles/${customerId}`,
            {
                headers: {
                    'X-Request-ID': generateRequestId()
                },
                timeout: 5000
            }
        );

        res.status(response.status).json(response.data);
    } catch (error) {
        handleProxyError(error, res);
    }
});

app.post('/api/v1/brand-voice/validate', async (req: Request, res: Response) => {
    try {
        // Add validation
        if (!req.body.customerId || !req.body.content) {
            return res.status(400).json({
                error: 'Bad Request',
                message: 'customerId and content are required'
            });
        }

        const response = await axios.post(
            `${BRAND_VOICE_SERVICE_URL}/api/v1/brand-voice/validate`,
            req.body,
            {
                headers: {
                    'Content-Type': 'application/json',
                    'X-Request-ID': generateRequestId()
                },
                timeout: 10000
            }
        );

        res.status(response.status).json(response.data);
    } catch (error) {
        handleProxyError(error, res);
    }
});

app.delete('/api/v1/brand-voice/profiles/:customerId', async (req: Request, res: Response) => {
    try {
        const { customerId } = req.params;

        const response = await axios.delete(
            `${BRAND_VOICE_SERVICE_URL}/api/v1/brand-voice/profiles/${customerId}`,
            {
                headers: {
                    'X-Request-ID': generateRequestId()
                },
                timeout: 5000
            }
        );

        res.status(response.status).send();
    } catch (error) {
        handleProxyError(error, res);
    }
});

// Error handling middleware
app.use((err: Error, req: Request, res: Response, next: NextFunction) => {
    console.error('Unhandled error:', err);
    res.status(500).json({
        error: 'Internal Server Error',
        message: 'An unexpected error occurred'
    });
});

// 404 handler
app.use((req: Request, res: Response) => {
    res.status(404).json({
        error: 'Not Found',
        message: `Route ${req.method} ${req.path} not found`
    });
});

function generateRequestId(): string {
    return `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
}

function handleProxyError(error: any, res: Response): void {
    if (axios.isAxiosError(error)) {
        if (error.response) {
            // Backend returned error
            res.status(error.response.status).json(error.response.data);
        } else if (error.code === 'ECONNREFUSED') {
            res.status(503).json({
                error: 'Service Unavailable',
                message: 'Brand Voice Service is not available'
            });
        } else if (error.code === 'ETIMEDOUT') {
            res.status(504).json({
                error: 'Gateway Timeout',
                message: 'Request to Brand Voice Service timed out'
            });
        } else {
            res.status(500).json({
                error: 'Internal Server Error',
                message: 'Failed to communicate with Brand Voice Service'
            });
        }
    } else {
        res.status(500).json({
            error: 'Internal Server Error',
            message: 'An unexpected error occurred'
        });
    }
}

app.listen(PORT, () => {
    console.log(`API Gateway listening on port ${PORT}`);
    console.log(`Proxying to Brand Voice Service at ${BRAND_VOICE_SERVICE_URL}`);
});

export default app;