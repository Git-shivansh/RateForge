// Burst traffic: fires a constant 50 requests/second for 10 seconds
// regardless of how long each request takes. Simulates a sudden spike,
// e.g. a client retrying aggressively or a flash-sale moment.
//
// Run: k6 run k6/burst-load.js

import http from 'k6/http';

export const options = {
    scenarios: {
        burst: {
            executor: 'constant-arrival-rate',
            rate: 50,
            timeUnit: '1s',
            duration: '10s',
            preAllocatedVUs: 50,
            maxVUs: 100,
        },
    },
};

export default function () {
    http.get('http://localhost:8080/api/ping', {
        headers: { 'X-User-Id': 'user-1' },
    });
}
